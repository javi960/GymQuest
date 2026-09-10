package com.gymquest.app.domain.model

import java.time.Instant

/** A target set only; completed weights and repetitions belong to WorkoutSet in a session. */
data class RoutinePlannedSet(
    val id: Long = 0,
    val routineExerciseId: Long,
    val setNumber: Int,
    val targetWeight: Double? = null,
    val targetReps: Int? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
