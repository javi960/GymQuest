package com.gymquest.app.domain.model

import java.time.Instant

data class ExerciseBase(
    val id: Long = 0,
    val name: String,
    val primaryMuscleGroupId: Long,
    val description: String? = null,
    /** Comma-separated display labels. The primary muscle remains a normalized relation. */
    val secondaryMuscles: String? = null,
    /** One instruction per line, so the UI can render a practical ordered guide. */
    val instructions: String? = null,
    val techniqueTips: String? = null,
    val commonMistakes: String? = null,
    val builtInGifUrl: String? = null,
    val isBuiltIn: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)
