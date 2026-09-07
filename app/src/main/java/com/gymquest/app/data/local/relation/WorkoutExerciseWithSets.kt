package com.gymquest.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSetEntity

data class WorkoutExerciseWithSets(
    @Embedded val workoutExercise: WorkoutExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "workoutExerciseId")
    val sets: List<WorkoutSetEntity>,
)
