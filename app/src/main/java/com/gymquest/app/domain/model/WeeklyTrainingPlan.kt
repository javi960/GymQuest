package com.gymquest.app.domain.model

import java.time.Instant

/** A reusable week template. Its days are planned labels, never calendar constraints. */
data class WeeklyTrainingPlan(
    val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class WeeklyTrainingDay(
    val id: Long = 0,
    val planId: Long,
    val dayKey: String,
    val label: String? = null,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/** The selected template day is retained even when it is trained on a different calendar day. */
data class TrainingSessionPlanSource(
    val planId: Long,
    val planName: String,
    val plannedDayId: Long,
    val plannedDayName: String,
)
