package com.gymquest.app.domain.usecase.martial

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.*
import com.gymquest.app.domain.repository.MartialArtsRepository
import com.gymquest.app.domain.repository.MartialMiniGameRepository
import com.gymquest.app.domain.repository.MediaRepository
import com.gymquest.app.domain.model.enums.MediaOwnerType
import java.time.Instant

class GetActiveMartialArtsUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke() = repository.activeArts() }
class ObserveMartialPracticeCountUseCase(private val repository: MartialArtsRepository) { operator fun invoke() = repository.observePracticeCount() }
class GetActiveMartialStylesUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(artId: Long) = repository.activeStyles(artId) }
class GetActiveMartialTechniquesUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(styleId: Long) = repository.activeTechniques(styleId) }
class GetActiveMartialStancesUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(styleId: Long) = repository.activeStances(styleId) }
class GetMartialContentDetailUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(contentId: Long) = repository.contentDetail(contentId) }
class UpdateMartialArtUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialArt) = repository.updateArt(value) }
class UpdateMartialStyleUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialStyle) = repository.updateStyle(value) }
class ArchiveMartialArtUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(id: Long, now: Instant) = repository.archiveArt(id, now) }
class ArchiveMartialStyleUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(id: Long, now: Instant) = repository.archiveStyle(id, now) }
class AddMartialContentStepUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialContentStep) = repository.addContentStep(value) }
class UpdateMartialContentStepUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialContentStep) = repository.updateContentStep(value) }
class DeleteMartialContentStepUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(id: Long) = repository.deleteContentStep(id) }
class ReplaceMartialContentStepsUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(contentId: Long, steps: List<MartialContentStep>) = repository.replaceContentSteps(contentId, steps) }
class ReplaceMartialContentStepMediaUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(stepId: Long, mediaFile: MediaFile) = repository.replaceContentStepMedia(stepId, mediaFile) }
class RemoveMartialContentStepMediaUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(stepId: Long) = repository.removeContentStepMedia(stepId) }
class ObserveMartialContentStepMediaUseCase(private val repository: MediaRepository) {
    operator fun invoke() = repository.observeActiveForOwnerType(MediaOwnerType.MARTIAL_CONTENT_STEP)
}

/**
 * Stores one local visual guide for a kata, form, drill, or other technical content.
 * The owner is assigned here so callers cannot accidentally attach it to another kind
 * of martial record.
 */
class ReplaceMartialTechnicalContentMediaUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(contentId: Long, mediaFile: MediaFile): AppResult<Long> =
        repository.replaceForOwner(
            mediaFile.copy(
                id = 0,
                ownerType = MediaOwnerType.MARTIAL_TECHNICAL_CONTENT,
                ownerId = contentId,
                isArchived = false,
            ),
        )
}

class RemoveMartialTechnicalContentMediaUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(contentId: Long): AppResult<Unit> =
        repository.removeForOwner(MediaOwnerType.MARTIAL_TECHNICAL_CONTENT, contentId)
}

class ObserveMartialTechnicalContentMediaUseCase(private val repository: MediaRepository) {
    operator fun invoke() = repository.observeActiveForOwnerType(MediaOwnerType.MARTIAL_TECHNICAL_CONTENT)
}

/** Stores one local GIF or video guide for a reusable martial technique. */
class ReplaceMartialTechniqueMediaUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(techniqueId: Long, mediaFile: MediaFile): AppResult<Long> =
        repository.replaceForOwner(
            mediaFile.copy(
                id = 0,
                ownerType = MediaOwnerType.MARTIAL_TECHNIQUE,
                ownerId = techniqueId,
                isArchived = false,
            ),
        )
}

class RemoveMartialTechniqueMediaUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(techniqueId: Long): AppResult<Unit> =
        repository.removeForOwner(MediaOwnerType.MARTIAL_TECHNIQUE, techniqueId)
}

class ObserveMartialTechniqueMediaUseCase(private val repository: MediaRepository) {
    operator fun invoke() = repository.observeActiveForOwnerType(MediaOwnerType.MARTIAL_TECHNIQUE)
}

/** Stores one local GIF or video guide for a reusable martial stance. */
class ReplaceMartialStanceMediaUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(stanceId: Long, mediaFile: MediaFile): AppResult<Long> =
        repository.replaceForOwner(
            mediaFile.copy(
                id = 0,
                ownerType = MediaOwnerType.MARTIAL_STANCE,
                ownerId = stanceId,
                isArchived = false,
            ),
        )
}

class RemoveMartialStanceMediaUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(stanceId: Long): AppResult<Unit> =
        repository.removeForOwner(MediaOwnerType.MARTIAL_STANCE, stanceId)
}

class ObserveMartialStanceMediaUseCase(private val repository: MediaRepository) {
    operator fun invoke() = repository.observeActiveForOwnerType(MediaOwnerType.MARTIAL_STANCE)
}
class CreateMartialArtUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialArt) = repository.createArt(value) }
class CreateMartialStyleUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialStyle) = repository.createStyle(value) }
class EnsureShitoRyuCatalogUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(styleId: Long) = repository.ensureShitoRyuCatalog(styleId) }
class SetCurrentMartialRankUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialRank) = repository.setCurrentRank(value) }
class CreateMartialContentUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialContent) = repository.createContent(value) }
class CreateMartialTechniqueUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialTechnique) = repository.createTechnique(value) }
class CreateMartialStanceUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialStance) = repository.createStance(value) }
class UpdateMartialContentUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialContent) = repository.updateContent(value) }
class UpdateMartialTechniqueUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialTechnique) = repository.updateTechnique(value) }
class UpdateMartialStanceUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialStance) = repository.updateStance(value) }
class LinkMartialTechniqueUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(contentId: Long, techniqueId: Long) = repository.linkTechnique(contentId, techniqueId) }
class RegisterMartialPracticeUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(value: MartialPractice) = repository.registerPractice(value) }
class ObserveMartialProgressUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(styleId: Long) = repository.observeProgress(styleId) }
class UpdateMartialContentProgressUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(contentId: Long, status: String, now: Instant) = repository.updateContentProgress(contentId, status, now) }
class ArchiveMartialContentUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(contentId: Long, now: Instant) = repository.archiveContent(contentId, now) }
class ArchiveMartialTechniqueUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(techniqueId: Long, now: Instant) = repository.archiveTechnique(techniqueId, now) }
class ArchiveMartialStanceUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(stanceId: Long, now: Instant) = repository.archiveStance(stanceId, now) }
class DeleteMartialArtUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(artId: Long) = repository.deleteArt(artId) }
class DeleteMartialStyleUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(styleId: Long) = repository.deleteStyle(styleId) }
class DeleteMartialContentUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(contentId: Long) = repository.deleteContent(contentId) }
class DeleteMartialTechniqueUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(techniqueId: Long) = repository.deleteTechnique(techniqueId) }
class DeleteMartialStanceUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(stanceId: Long) = repository.deleteStance(stanceId) }
class GetCurrentMartialBeltUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke() = repository.currentBelt() }
class SetCurrentMartialBeltUseCase(private val repository: MartialArtsRepository) { suspend operator fun invoke(belt: MartialBelt, now: Instant) = repository.setCurrentBelt(belt, now) }
class CreateMartialQuestionUseCase(private val repository: MartialMiniGameRepository) { suspend operator fun invoke(categoryId: Long, question: MartialQuestion, now: Instant): AppResult<Long> = repository.createQuestion(categoryId, question, now) }
class AnswerMartialQuestionUseCase(private val repository: MartialMiniGameRepository) { suspend operator fun invoke(questionId: Long, optionId: Long) = repository.answer(questionId, optionId) }
