package com.gymquest.app.domain.model

data class WorkoutSessionDetail(
    val session: WorkoutSession,
    val exercises: List<WorkoutExerciseDetail>,
)
