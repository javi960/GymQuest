package com.gymquest.app.data.local.projection

/** Totales persistidos necesarios para calcular la experiencia del entrenamiento. */
data class TrainingExperienceSummary(
    val completedExercises: Long,
    val completedSessions: Long,
)
