package com.gymquest.app.domain.model

import java.time.Instant

data class WorkoutRoutine(
    val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
