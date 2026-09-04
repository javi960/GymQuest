package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.SessionStatus
import java.time.Instant

data class WorkoutSession(
    val id: Long = 0,
    val gymId: Long? = null,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val durationSeconds: Long = 0,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val notes: String? = null,
    val perceivedEnergy: Int? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)