package com.gymquest.app.domain.usecase.session

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.repository.WorkoutRepository
import java.time.Instant

class CancelWorkoutSessionUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(session: WorkoutSession, cancelledAt: Instant): AppResult<Unit> =
        workoutRepository.updateSession(
            session.copy(
                endedAt = cancelledAt,
                durationSeconds = session.startedAt.until(cancelledAt, java.time.temporal.ChronoUnit.SECONDS),
                status = SessionStatus.CANCELLED,
                updatedAt = cancelledAt,
            ),
        )
}
