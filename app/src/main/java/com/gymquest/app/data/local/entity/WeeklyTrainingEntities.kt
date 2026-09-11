package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "weekly_training_plans")
data class WeeklyTrainingPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(
    tableName = "weekly_training_days",
    foreignKeys = [ForeignKey(WeeklyTrainingPlanEntity::class, ["id"], ["planId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["planId", "dayKey"], unique = true), Index(value = ["planId", "sortOrder"], unique = true)],
)
data class WeeklyTrainingDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val dayKey: String,
    val label: String? = null,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(
    tableName = "training_sessions",
    foreignKeys = [
        ForeignKey(WeeklyTrainingPlanEntity::class, ["id"], ["planId"]),
        ForeignKey(WeeklyTrainingDayEntity::class, ["id"], ["plannedDayId"]),
    ],
    indices = [Index(value = ["startedAt"]), Index(value = ["planId"]), Index(value = ["plannedDayId"])],
)
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long? = null,
    val plannedDayId: Long? = null,
    val planNameSnapshot: String = "Sesión libre",
    val plannedDaySnapshot: String = "Sin planificación",
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(tableName = "weekly_training_exercises", foreignKeys = [ForeignKey(WeeklyTrainingDayEntity::class, ["id"], ["dayId"], onDelete = ForeignKey.CASCADE), ForeignKey(ExerciseVariantEntity::class, ["id"], ["exerciseVariantId"])], indices = [Index(value = ["dayId", "sortOrder"], unique = true), Index(value = ["exerciseVariantId"])])
data class WeeklyTrainingExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long, val exerciseVariantId: Long, val sortOrder: Int,
    val targetSets: Int, val targetReps: Int? = null, val targetWeight: Double? = null, val restSeconds: Int? = null,
    val createdAt: Instant, val updatedAt: Instant,
)

@Entity(tableName = "training_session_exercises", foreignKeys = [ForeignKey(TrainingSessionEntity::class, ["id"], ["sessionId"], onDelete = ForeignKey.CASCADE), ForeignKey(ExerciseVariantEntity::class, ["id"], ["exerciseVariantId"])], indices = [Index(value = ["sessionId", "sortOrder"], unique = true), Index(value = ["exerciseVariantId"])])
data class TrainingSessionExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long, val exerciseVariantId: Long, val sortOrder: Int,
    val plannedSets: Int, val plannedReps: Int? = null, val plannedWeight: Double? = null, val plannedRestSeconds: Int? = null,
    val createdAt: Instant, val updatedAt: Instant,
)

@Entity(tableName = "training_session_sets", foreignKeys = [ForeignKey(TrainingSessionExerciseEntity::class, ["id"], ["sessionExerciseId"], onDelete = ForeignKey.CASCADE)], indices = [Index(value = ["sessionExerciseId", "setNumber"], unique = true)])
data class TrainingSessionSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionExerciseId: Long, val setNumber: Int, val weight: Double, val reps: Int,
    val setType: String = "Efectiva", val completedAt: Instant, val createdAt: Instant, val updatedAt: Instant,
)
