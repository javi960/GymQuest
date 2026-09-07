package com.gymquest.app.domain.model

data class VariantLastPerformance(
    val session: WorkoutSession,
    val exercise: WorkoutExercise,
    val sets: List<WorkoutSet>,
)
