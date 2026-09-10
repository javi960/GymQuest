package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.model.ExerciseMastery
import com.gymquest.app.domain.model.PersonalRecords
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SetType
import com.gymquest.app.domain.repository.ProgressRepository
import com.gymquest.app.domain.repository.WorkoutRepository
import java.time.Instant

class RecalculateProgressUseCase(
    private val workoutRepository: WorkoutRepository,
    private val progressRepository: ProgressRepository,
    private val calculateSetXp: CalculateSetXpUseCase = CalculateSetXpUseCase(),
    private val calculateCharacterLevel: CalculateCharacterLevelUseCase = CalculateCharacterLevelUseCase(),
    private val detectPersonalRecords: DetectPersonalRecordsUseCase = DetectPersonalRecordsUseCase(),
    private val updateExerciseMastery: UpdateExerciseMasteryUseCase = UpdateExerciseMasteryUseCase(),
) {
    suspend operator fun invoke(now: Instant): AppResult<Unit> {
        val sessions = when (val result = workoutRepository.findCompletedSessionDetails()) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        val setsByVariant = sessions.flatMap { detail -> detail.exercises.flatMap { exercise ->
            exercise.sets.filter { it.reps > 0 && it.setType != SetType.WARM_UP }.map {
                exercise.workoutExercise.exerciseVariantId to it
            }
        } }.groupBy({ it.first }, { it.second })
        val derivedUpdatedAt = sessions
            .flatMap { detail -> listOf(detail.session.updatedAt) + detail.exercises.flatMap { exercise -> exercise.sets.map(WorkoutSet::updatedAt) } }
            .maxOrNull() ?: Instant.EPOCH
        val mastery = setsByVariant.map { (variantId, sets) -> updateExerciseMastery(variantId, sets, derivedUpdatedAt) }
        val totalXp = setsByVariant.values.sumOf(::xpForVariant) + sessions.size * SESSION_COMPLETION_XP
        val stats = CharacterStats(
            level = calculateCharacterLevel(totalXp), totalXp = totalXp,
            totalWorkouts = sessions.size, totalTrainingSeconds = sessions.sumOf { it.session.durationSeconds },
            discoveredVariants = mastery.size, strengthXp = totalXp, updatedAt = derivedUpdatedAt,
        )
        return progressRepository.replaceDerivedProgress(stats, mastery)
    }

    private fun xpForVariant(sets: List<WorkoutSet>): Long {
        var records: PersonalRecords? = null
        return sets.sortedWith(compareBy(WorkoutSet::createdAt, WorkoutSet::id)).sumOf { set ->
            val detection = detectPersonalRecords(records, set)
            val xp = calculateSetXp(
                set = set,
                isNewDiscovery = records == null,
                isNewWeightRecord = detection.isWeightRecord,
                isNewVolumeRecord = detection.isVolumeRecord,
            )
            records = detection.records
            xp
        }
    }

    private companion object {
        const val SESSION_COMPLETION_XP = 30L
    }
}
