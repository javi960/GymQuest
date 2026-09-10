package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.domain.model.ExerciseMastery
import com.gymquest.app.domain.model.MasteryRankRules
import com.gymquest.app.domain.model.PersonalRecords
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SetType
import java.time.Instant

class UpdateExerciseMasteryUseCase(
    private val detectPersonalRecords: DetectPersonalRecordsUseCase = DetectPersonalRecordsUseCase(),
) {
    operator fun invoke(exerciseVariantId: Long, sets: List<WorkoutSet>, updatedAt: Instant): ExerciseMastery {
        val validSets = sets.filter { it.reps > 0 && it.setType != SetType.WARM_UP }
            .sortedWith(compareBy(WorkoutSet::createdAt, WorkoutSet::id))
        val records = validSets.fold<WorkoutSet, PersonalRecords?>(null) { current, set ->
            detectPersonalRecords(current, set).records
        }
        val totalSets = validSets.size
        val level = MasteryRankRules.levelFor(totalSets)
        return ExerciseMastery(
            exerciseVariantId = exerciseVariantId,
            level = level,
            masteryRank = MasteryRankRules.rankFor(level),
            accumulatedVolume = validSets.sumOf { it.volume },
            totalSets = totalSets,
            totalReps = validSets.sumOf { it.reps },
            personalRecordWeight = records?.weight,
            personalRecordReps = records?.reps,
            personalRecordVolume = records?.volume,
            discoveredAt = validSets.minOfOrNull { it.createdAt },
            updatedAt = updatedAt,
        )
    }
}
