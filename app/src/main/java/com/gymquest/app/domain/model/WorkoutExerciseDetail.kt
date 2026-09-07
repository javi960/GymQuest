package com.gymquest.app.domain.model

data class WorkoutExerciseDetail(
    val workoutExercise: WorkoutExercise,
    val sets: List<WorkoutSet>,
)
