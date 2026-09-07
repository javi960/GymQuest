package com.gymquest.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity

data class WorkoutSessionWithExercises(
    @Embedded val workoutSession: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutSessionId",
        entity = WorkoutExerciseEntity::class,
    )
    val exercises: List<WorkoutExerciseWithSets>,
)
