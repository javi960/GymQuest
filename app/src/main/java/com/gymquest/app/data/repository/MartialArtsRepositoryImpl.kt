package com.gymquest.app.data.repository

import androidx.room.withTransaction
import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.data.local.entity.*
import com.gymquest.app.data.mapper.MediaFileMapper
import com.gymquest.app.data.seed.ShitoRyuCatalog
import com.gymquest.app.domain.model.*
import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.repository.MartialArtsRepository
import java.time.Instant

class MartialArtsRepositoryImpl(private val database: GymQuestDatabase) : MartialArtsRepository {
    private val dao = database.martialArtsDao()
    override fun observePracticeCount() = dao.observePracticeCount()
    override suspend fun activeArts() = guarded { dao.getActiveMartialArts().map(::toArt) }
    override suspend fun activeStyles(artId: Long) = guarded {
        require(dao.getMartialArt(artId)?.isArchived == false) { "El arte marcial no existe o está archivado." }
        dao.getActiveStyles(artId).map(::toStyle)
    }
    override suspend fun activeTechniques(styleId: Long) = guarded { dao.getActiveTechniques(styleId).map(::toTechnique) }
    override suspend fun activeStances(styleId: Long) = guarded { dao.getActiveStances(styleId).map(::toStance) }
    override suspend fun contentDetail(contentId: Long) = guarded {
        val content = requireNotNull(dao.getTechnicalContent(contentId)) { "El contenido no existe." }
        MartialContentDetail(
            content = toContent(content),
            techniques = dao.getTechniquesForContent(contentId).map(::toTechnique),
            steps = dao.getContentSteps(contentId).map(::toStep),
            stances = dao.getActiveStances(content.martialStyleId).map(::toStance),
        )
    }
    override suspend fun updateArt(art: MartialArt) = guarded { dao.updateMartialArt(MartialArtEntity(art.id, art.name.trim(), art.description, art.isArchived, art.createdAt, art.updatedAt)) }
    override suspend fun updateStyle(style: MartialStyle) = guarded { dao.updateMartialStyle(MartialStyleEntity(style.id, style.martialArtId, style.name.trim(), style.description, style.isArchived, style.createdAt, style.updatedAt)) }
    override suspend fun archiveArt(artId: Long, now: Instant) = guarded { require(dao.archiveMartialArt(artId, now) == 1) { "La disciplina no existe." } }
    override suspend fun archiveStyle(styleId: Long, now: Instant) = guarded { require(dao.archiveMartialStyle(styleId, now) == 1) { "El estilo no existe." } }
    override suspend fun addContentStep(step: MartialContentStep) = guarded { database.withTransaction { validateStepReferences(step); dao.insertContentStep(step.toEntity()) } }
    override suspend fun updateContentStep(step: MartialContentStep) = guarded { database.withTransaction { require(dao.getContentStep(step.id) != null) { "El paso no existe." }; validateStepReferences(step); dao.updateContentStep(step.toEntity()) } }
    override suspend fun deleteContentStep(stepId: Long) = guarded { database.withTransaction { require(dao.getContentStep(stepId) != null) { "El paso no existe." }; database.mediaFileDao().archiveActiveForOwner(MediaOwnerType.MARTIAL_CONTENT_STEP.name.lowercase(), stepId); require(dao.deleteContentStep(stepId) == 1) { "El paso no existe." } } }
    override suspend fun replaceContentSteps(contentId: Long, steps: List<MartialContentStep>) = guarded { database.withTransaction { require(dao.getTechnicalContent(contentId) != null) { "El contenido no existe." }; val sortedSteps = steps.sortedBy { it.orderIndex }; require(sortedSteps.map { it.orderIndex } == sortedSteps.indices.toList()) { "Los movimientos deben estar numerados de forma continua desde 0." }; sortedSteps.forEach { step -> require(step.technicalContentId == contentId) { "Todos los movimientos deben pertenecer al mismo contenido." }; validateStepReferences(step) }; val updatedAt = sortedSteps.fold(Instant.now()) { latest, step -> maxOf(latest, step.createdAt, step.updatedAt) }; dao.replaceContentSteps(contentId, sortedSteps.map { it.copy(updatedAt = updatedAt).toEntity() }) } }
    override suspend fun replaceContentStepMedia(stepId: Long, mediaFile: MediaFile) = guarded { database.withTransaction { requireNotNull(dao.getContentStep(stepId)) { "El paso no existe." }; val stored = mediaFile.copy(id = 0, ownerType = MediaOwnerType.MARTIAL_CONTENT_STEP, ownerId = stepId, isArchived = false); val mediaId = database.mediaFileDao().replaceForOwner(MediaFileMapper.toEntity(stored)); require(dao.updateContentStepMedia(stepId, mediaId, Instant.now()) == 1) { "No se pudo asociar el archivo al paso." }; mediaId } }
    override suspend fun removeContentStepMedia(stepId: Long) = guarded { database.withTransaction { requireNotNull(dao.getContentStep(stepId)) { "El paso no existe." }; database.mediaFileDao().archiveActiveForOwner(MediaOwnerType.MARTIAL_CONTENT_STEP.name.lowercase(), stepId); require(dao.updateContentStepMedia(stepId, null, Instant.now()) == 1) { "No se pudo retirar el archivo del paso." } } }
    override suspend fun createArt(art: MartialArt) = guarded { require(art.name.isNotBlank()); dao.insertMartialArt(MartialArtEntity(art.id, art.name.trim(), art.description, art.isArchived, art.createdAt, art.updatedAt)) }
    override suspend fun createStyle(style: MartialStyle) = guarded {
        require(style.name.isNotBlank())
        require(dao.getMartialArt(style.martialArtId)?.isArchived == false) { "El arte marcial no existe o está archivado." }
        database.withTransaction {
            val styleId = dao.insertMartialStyle(MartialStyleEntity(style.id, style.martialArtId, style.name.trim(), style.description, style.isArchived, style.createdAt, style.updatedAt))
            ensureShitoRyuCatalog(styleId, style.name, style.createdAt, style.updatedAt)
            styleId
        }
    }
    override suspend fun ensureShitoRyuCatalog(styleId: Long) = guarded {
        database.withTransaction {
            val style = requireNotNull(dao.getMartialStyle(styleId)) { "El estilo no existe." }
            ensureShitoRyuCatalog(styleId, style.name, style.createdAt, style.updatedAt)
        }
    }
    override suspend fun setCurrentRank(rank: MartialRank) = guarded { require(rank.name.isNotBlank()); database.withTransaction { requireNotNull(dao.getMartialStyle(rank.martialStyleId)) { "El estilo no existe." }; if (rank.isCurrent) dao.clearCurrentRanks(rank.martialStyleId, rank.updatedAt); dao.insertMartialRank(MartialRankEntity(rank.id, rank.martialStyleId, rank.name.trim(), rank.rankOrder, rank.achievedAt, rank.notes, rank.isCurrent, rank.createdAt, rank.updatedAt)) } }
    override suspend fun createContent(content: MartialContent) = guarded { require(content.name.isNotBlank() && content.contentType.isNotBlank()); require(dao.getMartialStyle(content.martialStyleId)?.isArchived == false) { "El estilo no existe o está archivado." }; dao.insertTechnicalContent(MartialTechnicalContentEntity(content.id, content.martialStyleId, content.name.trim(), content.contentType.trim(), content.description, null, null, content.progressStatus, content.discoveredAt, content.notes, content.isArchived, content.createdAt, content.updatedAt)) }
    override suspend fun createTechnique(technique: MartialTechnique) = guarded { require(technique.name.isNotBlank()); require(dao.getMartialStyle(technique.martialStyleId)?.isArchived == false) { "El estilo no existe o está archivado." }; dao.insertTechnique(MartialTechniqueEntity(technique.id, technique.martialStyleId, technique.name.trim(), technique.translation, technique.family, technique.description, technique.notes, technique.isArchived, technique.createdAt, technique.updatedAt)) }
    override suspend fun createStance(stance: MartialStance) = guarded { require(stance.name.isNotBlank()); require(dao.getMartialStyle(stance.martialStyleId)?.isArchived == false) { "El estilo no existe o está archivado." }; dao.insertStance(MartialStanceEntity(stance.id, stance.martialStyleId, stance.name.trim(), stance.translation, stance.description, stance.notes, stance.isArchived, stance.createdAt, stance.updatedAt)) }
    override suspend fun updateContent(content: MartialContent) = guarded { require(dao.getTechnicalContent(content.id)?.isArchived == false) { "El contenido no existe o está archivado." }; dao.updateTechnicalContent(MartialTechnicalContentEntity(content.id, content.martialStyleId, content.name.trim(), content.contentType.trim(), content.description, null, null, content.progressStatus, content.discoveredAt, content.notes, false, content.createdAt, content.updatedAt)) }
    override suspend fun updateTechnique(technique: MartialTechnique) = guarded { require(dao.getTechnique(technique.id)?.isArchived == false) { "La técnica no existe o está archivada." }; dao.updateTechnique(MartialTechniqueEntity(technique.id, technique.martialStyleId, technique.name.trim(), technique.translation, technique.family, technique.description, technique.notes, false, technique.createdAt, technique.updatedAt)) }
    override suspend fun updateStance(stance: MartialStance) = guarded { require(dao.getStance(stance.id)?.isArchived == false) { "La posición no existe o está archivada." }; dao.updateStance(MartialStanceEntity(stance.id, stance.martialStyleId, stance.name.trim(), stance.translation, stance.description, stance.notes, false, stance.createdAt, stance.updatedAt)) }
    override suspend fun linkTechnique(contentId: Long, techniqueId: Long) = guarded { database.withTransaction { val content = requireNotNull(dao.getTechnicalContent(contentId)) { "El contenido no existe." }; val technique = requireNotNull(dao.getTechnique(techniqueId)) { "La técnica no existe." }; require(content.martialStyleId == technique.martialStyleId) { "El contenido y la técnica deben pertenecer al mismo estilo." }; dao.insertContentTechniqueCrossRef(MartialContentTechniqueCrossRef(contentId, techniqueId, dao.getTechniquesForContent(contentId).size)) } }
    override suspend fun registerPractice(practice: MartialPractice) = guarded { require(practice.durationSeconds > 0 && practice.items.isNotEmpty()); database.withTransaction { require(dao.getMartialStyle(practice.martialStyleId)?.isArchived == false) { "El estilo no existe o está archivado." }; val sessionId = dao.insertPracticeSession(MartialPracticeSessionEntity(martialStyleId = practice.martialStyleId, startedAt = practice.startedAt, endedAt = practice.startedAt.plusSeconds(practice.durationSeconds), durationSeconds = practice.durationSeconds, instructor = practice.instructor, location = practice.location, notes = practice.notes, createdAt = practice.startedAt, updatedAt = practice.startedAt)); practice.items.forEach { item -> validateItemStyle(practice.martialStyleId, item); dao.insertPracticeItem(MartialPracticeItemEntity(practiceSessionId = sessionId, contentId = item.contentId, techniqueId = item.techniqueId, repetitions = item.repetitions, minutes = item.minutes, confidence = item.confidence, difficulty = item.difficulty, progressStatusAfter = item.progressStatusAfter, notes = item.notes, createdAt = practice.startedAt, updatedAt = practice.startedAt)); item.contentId?.let { dao.updateContentProgress(it, item.progressStatusAfter ?: "practiced", practice.startedAt, practice.startedAt) } }; sessionId } }
    override suspend fun observeProgress(styleId: Long) = guarded { val style = requireNotNull(dao.getMartialStyle(styleId)) { "El estilo no existe." }; MartialProgress(toStyle(style), dao.getActiveTechnicalContents(styleId).map(::toContent), dao.getLastPracticeAt(styleId)) }
    override suspend fun updateContentProgress(contentId: Long, status: String, now: Instant) = guarded { require(status.isNotBlank()); require(dao.updateContentProgress(contentId, status, now, now) == 1) { "El contenido no existe." } }
    override suspend fun archiveContent(contentId: Long, now: Instant) = guarded { require(dao.archiveTechnicalContent(contentId, now) == 1) { "El contenido no existe." } }
    override suspend fun archiveTechnique(techniqueId: Long, now: Instant) = guarded { require(dao.archiveTechnique(techniqueId, now) == 1) { "La técnica no existe." } }
    override suspend fun archiveStance(stanceId: Long, now: Instant) = guarded { require(dao.archiveStance(stanceId, now) == 1) { "La posición no existe." } }
    override suspend fun deleteArt(artId: Long) = guarded { require(dao.deleteArtCascade(artId) == 1) { "La disciplina no existe." } }
    override suspend fun deleteStyle(styleId: Long) = guarded { require(dao.deleteStyleCascade(styleId) == 1) { "El estilo no existe." } }
    override suspend fun deleteContent(contentId: Long) = guarded { require(dao.deleteContentCascade(contentId) == 1) { "El contenido no existe." } }
    override suspend fun deleteTechnique(techniqueId: Long) = guarded { require(dao.deleteTechniqueCascade(techniqueId) == 1) { "La técnica no existe." } }
    override suspend fun deleteStance(stanceId: Long) = guarded { require(dao.deleteStanceCascade(stanceId) == 1) { "La posición no existe." } }
    override suspend fun currentBelt() = guarded {
        dao.getBeltSettings()
            ?.belt
            ?.let { MartialBelt.valueOf(it) }
            ?: MartialBelt.WHITE
    }
    override suspend fun setCurrentBelt(belt: MartialBelt, now: Instant) = guarded {
        dao.upsertBeltSettings(MartialBeltSettingsEntity(belt = belt.name, updatedAt = now))
    }
    private suspend fun validateItemStyle(styleId: Long, item: MartialPracticeItem) { item.contentId?.let { require(dao.getTechnicalContent(it)?.martialStyleId == styleId) { "El contenido no pertenece al estilo de práctica." } }; item.techniqueId?.let { require(dao.getTechnique(it)?.martialStyleId == styleId) { "La técnica no pertenece al estilo de práctica." } } }
    private suspend fun ensureShitoRyuCatalog(styleId: Long, styleName: String, createdAt: Instant, updatedAt: Instant) {
        if (!ShitoRyuCatalog.matchesStyle(styleName)) return
        val existingTechniqueNames = dao.getTechniquesForStyle(styleId).map { ShitoRyuCatalog.normalizedEntryName(it.name) }.toSet()
        ShitoRyuCatalog.techniques
            .filterNot { ShitoRyuCatalog.normalizedEntryName(it.name) in existingTechniqueNames }
            .forEach { entry -> dao.insertTechnique(MartialTechniqueEntity(martialStyleId = styleId, name = entry.name, translation = entry.translation, category = entry.family, description = entry.description, createdAt = createdAt, updatedAt = updatedAt)) }
        val existingStanceNames = dao.getStancesForStyle(styleId).map { ShitoRyuCatalog.normalizedEntryName(it.name) }.toSet()
        ShitoRyuCatalog.stances
            .filterNot { ShitoRyuCatalog.normalizedEntryName(it.name) in existingStanceNames }
            .forEach { entry -> dao.insertStance(MartialStanceEntity(martialStyleId = styleId, name = entry.name, translation = entry.translation, description = entry.description, createdAt = createdAt, updatedAt = updatedAt)) }
    }
    private suspend fun validateStepReferences(step: MartialContentStep) {
        val content = requireNotNull(dao.getTechnicalContent(step.technicalContentId)) { "El contenido no existe." }
        require(!content.isArchived) { "El contenido está archivado." }
        step.techniqueId?.let { techniqueId ->
            val technique = requireNotNull(dao.getTechnique(techniqueId)) { "La técnica no existe." }
            require(!technique.isArchived && technique.martialStyleId == content.martialStyleId) { "La técnica debe estar activa y pertenecer al estilo del contenido." }
        }
        step.stanceId?.let { stanceId ->
            val stance = requireNotNull(dao.getStance(stanceId)) { "La posición no existe." }
            require(!stance.isArchived && stance.martialStyleId == content.martialStyleId) { "La posición debe estar activa y pertenecer al estilo del contenido." }
        }
        step.mediaFileId?.let { mediaFileId ->
            require(step.id > 0) { "Primero guarda el movimiento antes de adjuntar multimedia." }
            val media = requireNotNull(database.mediaFileDao().getById(mediaFileId)) { "El archivo multimedia no existe." }
            require(!media.isArchived && media.ownerType == MediaOwnerType.MARTIAL_CONTENT_STEP.name.lowercase() && media.ownerId == step.id) { "El archivo multimedia debe pertenecer al movimiento." }
        }
    }
    private fun MartialContentStep.toEntity() = MartialContentStepEntity(id, technicalContentId, orderIndex, stanceId, techniqueId, direction, side, movementType, displacement, turnDegrees, angleDegrees, description, hasKiai, hasPause, mediaFileId, createdAt, updatedAt)
    private fun toStyle(e: MartialStyleEntity) = MartialStyle(e.id, e.martialArtId, e.name, e.description, e.isArchived, e.createdAt, e.updatedAt)
    private fun toArt(e: MartialArtEntity) = MartialArt(e.id, e.name, e.description, e.isArchived, e.createdAt, e.updatedAt)
    private fun toContent(e: MartialTechnicalContentEntity) = MartialContent(e.id, e.martialStyleId, e.name, e.contentType, e.description, e.progressStatus, e.discoveredAt, e.notes, e.isArchived, e.createdAt, e.updatedAt)
    private fun toTechnique(e: MartialTechniqueEntity) = MartialTechnique(e.id, e.martialStyleId, e.name, e.translation, e.category, e.description, e.notes, e.isArchived, e.createdAt, e.updatedAt)
    private fun toStance(e: MartialStanceEntity) = MartialStance(e.id, e.martialStyleId, e.name, e.translation, e.description, e.notes, e.isArchived, e.createdAt, e.updatedAt)
    private fun toStep(e: MartialContentStepEntity) = MartialContentStep(e.id, e.technicalContentId, e.orderIndex, e.stanceId, e.techniqueId, e.direction, e.side, e.movementType, e.displacement, e.turnDegrees, e.angleDegrees, e.description, e.hasKiai, e.hasPause, e.mediaFileId, e.createdAt, e.updatedAt)
    private suspend fun <T> guarded(block: suspend () -> T): AppResult<T> = try { AppResult.Success(block()) } catch (e: NoSuchElementException) { AppResult.Failure(AppError.NotFound(e.message ?: "No encontrado.")) } catch (e: IllegalArgumentException) { AppResult.Failure(AppError.Validation(e.message ?: "Datos marciales no válidos.")) } catch (e: Exception) { AppResult.Failure(AppError.Storage("No se pudieron guardar los datos marciales.", e)) }
}
