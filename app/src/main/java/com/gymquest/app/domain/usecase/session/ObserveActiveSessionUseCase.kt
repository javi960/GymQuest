package com.gymquest.app.domain.usecase.session

import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveSessionUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    operator fun invoke(): Flow<WorkoutSessionDetail?> =
        workoutRepository.observeActiveSession()
}
