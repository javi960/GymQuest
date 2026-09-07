package com.gymquest.app.domain.usecase.session

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.repository.WorkoutRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

class CompleteWorkoutSessionUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(
        session: WorkoutSession,
        completedAt: Instant,
        perceivedEnergy: Int? = session.perceivedEnergy,
        notes: String? = session.notes,
    ): AppResult<Unit> =
        workoutRepository.updateSession(
            session.copy(
                endedAt = completedAt,
                durationSeconds = session.startedAt.until(completedAt, ChronoUnit.SECONDS),
                status = SessionStatus.FINISHED,
                perceivedEnergy = perceivedEnergy,
                notes = notes,
                updatedAt = completedAt,
            ),
        )
}
