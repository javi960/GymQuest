package com.gymquest.app.domain.model

import com.gymquest.app.data.local.projection.TrainingExperienceSummary

/** Reglas únicas y transparentes para la progresión de fuerza. */
object TrainingExperience {
    const val XP_PER_COMPLETED_EXERCISE = 15L
    const val XP_PER_COMPLETED_SESSION = 40L

    fun total(summary: TrainingExperienceSummary): Long =
        summary.completedExercises * XP_PER_COMPLETED_EXERCISE +
            summary.completedSessions * XP_PER_COMPLETED_SESSION
}
