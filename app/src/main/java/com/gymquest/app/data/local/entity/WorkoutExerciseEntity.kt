package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutSessionId"],
        ),
        ForeignKey(
            entity = ExerciseVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseVariantId"],
        ),
        ForeignKey(entity = GymMachineEntity::class, parentColumns = ["id"], childColumns = ["gymMachineId"]),
    ],
    indices = [
        Index(value = ["workoutSessionId", "orderIndex"], unique = true),
        Index(value = ["exerciseVariantId"]),
        Index(value = ["gymMachineId"]),
    ],
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutSessionId: Long,
    val exerciseVariantId: Long,
    val gymMachineId: Long? = null,
    val orderIndex: Int,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
