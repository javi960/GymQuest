package com.gymquest.app.domain.model

data class MuscleGroup(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val sortOrder: Int,
    val isArchived: Boolean = false,
)
