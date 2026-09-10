package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.Weekday
import java.time.Instant

data class RoutineDay(
    val id: Long = 0,
    val routineId: Long,
    val weekday: Weekday,
    /** Optional training focus shown with the day, e.g. "Piernas". */
    val label: String? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
