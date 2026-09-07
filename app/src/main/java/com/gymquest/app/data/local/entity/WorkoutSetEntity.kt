package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymquest.app.domain.model.enums.SetType
import java.time.Instant

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
        ),
    ],
    indices = [
        Index(value = ["workoutExerciseId", "setNumber"], unique = true),
        Index(value = ["createdAt"]),
    ],
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val weightValue: Double,
    val reps: Int,
    val setType: SetType = SetType.WORK,
    val startedAt: Instant? = null,
    val endedAt: Instant? = null,
    val restBeforeSeconds: Long? = null,
    val restAfterSeconds: Long? = null,
    val notes: String? = null,
    val volume: Double,
    val xpAwarded: Long = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
)
