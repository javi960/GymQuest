package com.gymquest.app.domain.usecase.session

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.repository.WorkoutRepository

class StartWorkoutSessionUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(session: WorkoutSession): AppResult<Long> =
        workoutRepository.startSession(session)
}
