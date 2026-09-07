package com.gymquest.app.domain.model

import java.time.Instant

data class Gym(
    val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
