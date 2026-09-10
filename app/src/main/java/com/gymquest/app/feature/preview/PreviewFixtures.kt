package com.gymquest.app.feature.preview

import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseMastery
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MasteryProgress
import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutExerciseDetail
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.MasteryRank
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.model.enums.SetType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant

object PreviewFixtures {
    val now: Instant = Instant.parse("2026-09-07T10:30:00Z")

    fun progressSummary(totalWorkouts: Int = 12): ProgressSummary {
        val mastery = if (totalWorkouts == 0) {
            emptyList()
        } else {
            listOf(
                ExerciseMastery(
                    exerciseVariantId = 1,
                    level = 3,
                    masteryRank = MasteryRank.INTERMEDIATE,
                    accumulatedVolume = 4_920.0,
                    totalSets = 24,
                    totalReps = 192,
                    personalRecordWeight = 82.5,
                    personalRecordReps = 8,
                    personalRecordVolume = 660.0,
                    discoveredAt = now.minusSeconds(86_400),
                    updatedAt = now,
                ),
                ExerciseMastery(
                    exerciseVariantId = 2,
                    level = 2,
                    masteryRank = MasteryRank.APPRENTICE,
                    accumulatedVolume = 4_320.0,
                    totalSets = 24,
                    totalReps = 240,
                    personalRecordWeight = 100.0,
                    personalRecordReps = 10,
                    personalRecordVolume = 1_000.0,
                    discoveredAt = now.minusSeconds(43_200),
                    updatedAt = now,
                ),
            )
        }
        return ProgressSummary(
        characterStats = CharacterStats(
            level = if (totalWorkouts == 0) 1 else 4,
            totalXp = if (totalWorkouts == 0) 0 else 720,
            totalWorkouts = totalWorkouts,
            discoveredVariants = if (totalWorkouts == 0) 0 else 5,
            updatedAt = now,
        ),
        totalVolume = if (totalWorkouts == 0) 0.0 else 9_240.0,
        totalSets = if (totalWorkouts == 0) 0 else 48,
        personalRecordCount = if (totalWorkouts == 0) 0 else 5,
        mastery = mastery,
        masteryProgress = mastery.mapIndexed { index, item ->
            MasteryProgress(
                variantName = if (index == 0) "Press banca con barra" else "Sentadilla con barra",
                mastery = item,
            )
        },
    )
    }

    fun catalog(): List<ExerciseCatalogEntry> {
        val base = ExerciseBase(
            id = 1,
            name = "Press banca",
            primaryMuscleGroupId = 1,
            description = "Empuje horizontal con barra.",
            createdAt = now,
            updatedAt = now,
        )
        return listOf(
            ExerciseCatalogEntry(
                exerciseBase = base,
                variants = listOf(
                    ExerciseVariant(1, base.id, "Barra", EquipmentType.BARBELL, WeightComparisonType.TOTAL_WEIGHT, createdAt = now, updatedAt = now),
                    ExerciseVariant(2, base.id, "Mancuernas", EquipmentType.DUMBBELL, WeightComparisonType.PER_DUMBBELL, createdAt = now, updatedAt = now),
                ),
            ),
        )
    }

    fun sessionDetail(status: SessionStatus = SessionStatus.ACTIVE): WorkoutSessionDetail {
        val session = WorkoutSession(
            id = 1,
            startedAt = now.minusSeconds(2_820),
            endedAt = if (status == SessionStatus.ACTIVE) null else now,
            durationSeconds = 2_820,
            status = status,
            createdAt = now.minusSeconds(2_820),
            updatedAt = now,
        )
        val exercise = WorkoutExercise(1, session.id, 1, orderIndex = 0, createdAt = now, updatedAt = now)
        return WorkoutSessionDetail(
            session = session,
            exercises = listOf(
                WorkoutExerciseDetail(
                    workoutExercise = exercise,
                    sets = listOf(
                        WorkoutSet(1, exercise.id, 1, 80.0, 8, SetType.WORK, restBeforeSeconds = 70, createdAt = now, updatedAt = now),
                        WorkoutSet(2, exercise.id, 2, 82.5, 8, SetType.WORK, restBeforeSeconds = 84, createdAt = now, updatedAt = now),
                    ),
                ),
            ),
        )
    }
}
