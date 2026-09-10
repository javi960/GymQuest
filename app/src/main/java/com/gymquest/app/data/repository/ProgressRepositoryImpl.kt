package com.gymquest.app.data.repository

import androidx.room.withTransaction
import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.data.local.entity.UserProfileEntity
import com.gymquest.app.data.mapper.ProgressMapper
import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.model.ExerciseMastery
import com.gymquest.app.domain.model.MasteryProgress
import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.domain.repository.ProgressRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ProgressRepositoryImpl(
    private val database: GymQuestDatabase,
) : ProgressRepository {
    private val dao = database.progressDao()

    override fun observeCharacterStats(): Flow<CharacterStats> = dao.observeCharacterStats().map { entity ->
        entity?.let(ProgressMapper::toDomain) ?: emptyCharacterStats()
    }

    override fun observeVariantMastery(variantId: Long): Flow<ExerciseMastery?> =
        dao.observeExerciseMastery(variantId).map { it?.let(ProgressMapper::toDomain) }

    override fun observeProgressSummary(): Flow<ProgressSummary> = combine(
        observeCharacterStats(),
        dao.observeExerciseMasteriesWithVariantNames().map { rows ->
            rows.map { MasteryProgress(variantName = it.variantName, mastery = ProgressMapper.toDomain(it.mastery)) }
        },
    ) { stats, masteryProgress ->
        val mastery = masteryProgress.map(MasteryProgress::mastery)
        ProgressSummary(
            characterStats = stats,
            totalVolume = mastery.sumOf { it.accumulatedVolume },
            totalSets = mastery.sumOf { it.totalSets },
            personalRecordCount = mastery.count { it.personalRecordWeight != null || it.personalRecordVolume != null },
            mastery = mastery,
            masteryProgress = masteryProgress,
        )
    }

    override suspend fun saveCharacterStats(stats: CharacterStats): AppResult<Unit> = runStorageOperation {
        database.withTransaction {
            ensureProfile(stats.userProfileId, stats.updatedAt)
            dao.upsertCharacterStats(ProgressMapper.toEntity(stats))
        }
    }

    override suspend fun saveMastery(mastery: ExerciseMastery): AppResult<Unit> = runStorageOperation {
        dao.upsertExerciseMastery(ProgressMapper.toEntity(mastery))
    }

    override suspend fun replaceDerivedProgress(
        stats: CharacterStats,
        mastery: List<ExerciseMastery>,
    ): AppResult<Unit> = runStorageOperation {
        require(mastery.map { it.exerciseVariantId }.distinct().size == mastery.size) {
            "No puede haber dominio duplicado para una variante."
        }
        database.withTransaction {
            ensureProfile(stats.userProfileId, stats.updatedAt)
            dao.upsertCharacterStats(ProgressMapper.toEntity(stats))
            dao.deleteAllExerciseMasteries()
            mastery.forEach { dao.upsertExerciseMastery(ProgressMapper.toEntity(it)) }
        }
    }

    private suspend fun ensureProfile(profileId: Long, timestamp: Instant) {
        if (dao.getUserProfile(profileId) == null) {
            dao.upsertUserProfile(
                UserProfileEntity(id = profileId, createdAt = timestamp, updatedAt = timestamp),
            )
        }
    }

    private fun emptyCharacterStats() = CharacterStats(updatedAt = Instant.EPOCH)

    private suspend fun runStorageOperation(operation: suspend () -> Unit): AppResult<Unit> = try {
        operation()
        AppResult.Success(Unit)
    } catch (error: IllegalArgumentException) {
        AppResult.Failure(AppError.Validation(error.message ?: "El progreso no es valido."))
    } catch (error: Exception) {
        AppResult.Failure(AppError.Storage("No se pudo guardar el progreso.", error))
    }
}
