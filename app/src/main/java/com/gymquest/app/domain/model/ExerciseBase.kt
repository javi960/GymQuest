package com.gymquest.app.domain.model

import java.time.Instant

data class ExerciseBase(
    val id: Long = 0,
    val name: String,
    val primaryMuscleGroupId: Long,
    val description: String? = null,
    val isBuiltIn: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)