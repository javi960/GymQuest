package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "exercise_bases",
    foreignKeys = [
        ForeignKey(
            entity = MuscleGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["primaryMuscleGroupId"],
        ),
    ],
    indices = [
        Index(value = ["primaryMuscleGroupId"]),
        Index(value = ["primaryMuscleGroupId", "name"], unique = true),
    ],
)
data class ExerciseBaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val primaryMuscleGroupId: Long,
    val description: String? = null,
    val secondaryMuscles: String? = null,
    val instructions: String? = null,
    val techniqueTips: String? = null,
    val commonMistakes: String? = null,
    val builtInGifUrl: String? = null,
    val isBuiltIn: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
