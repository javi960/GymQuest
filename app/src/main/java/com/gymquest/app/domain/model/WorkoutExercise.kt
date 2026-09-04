package com.gymquest.app.domain.model

import java.time.Instant

data class WorkoutExercise(
    val id: Long = 0,
    val workoutSessionId: Long,
    val exerciseVariantId: Long,
    val gymMachineId: Long? = null,
    val orderIndex: Int,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)