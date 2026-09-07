package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.SetType
import java.time.Instant

data class WorkoutSet(
    val id: Long = 0,
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
    val xpAwarded: Long = 0,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val volume: Double
        get() = weightValue * reps
}
