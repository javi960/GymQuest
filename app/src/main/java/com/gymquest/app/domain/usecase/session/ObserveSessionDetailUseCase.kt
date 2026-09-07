package com.gymquest.app.domain.usecase.session

import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionDetailUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    operator fun invoke(sessionId: Long): Flow<WorkoutSessionDetail?> =
        workoutRepository.observeSessionDetail(sessionId)
}
