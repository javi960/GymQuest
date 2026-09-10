package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymquest.app.domain.model.enums.Weekday
import java.time.Instant

@Entity(tableName = "workout_routines")
data class WorkoutRoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(
    tableName = "routine_days",
    foreignKeys = [ForeignKey(WorkoutRoutineEntity::class, ["id"], ["routineId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["routineId", "weekday"], unique = true)],
)
data class RoutineDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    val weekday: Weekday,
    val label: String? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(RoutineDayEntity::class, ["id"], ["routineDayId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(ExerciseVariantEntity::class, ["id"], ["exerciseVariantId"]),
        ForeignKey(GymMachineEntity::class, ["id"], ["gymMachineId"]),
    ],
    indices = [Index(value = ["routineDayId", "orderIndex"], unique = true), Index(value = ["exerciseVariantId"]), Index(value = ["gymMachineId"])],
)
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineDayId: Long,
    val exerciseVariantId: Long,
    val gymMachineId: Long? = null,
    val orderIndex: Int,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Entity(
    tableName = "routine_planned_sets",
    foreignKeys = [ForeignKey(RoutineExerciseEntity::class, ["id"], ["routineExerciseId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["routineExerciseId", "setNumber"], unique = true)],
)
data class RoutinePlannedSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineExerciseId: Long,
    val setNumber: Int,
    val targetWeight: Double? = null,
    val targetReps: Int? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
