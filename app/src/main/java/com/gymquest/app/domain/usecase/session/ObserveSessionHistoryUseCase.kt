package com.gymquest.app.domain.usecase.session

import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionHistoryUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    operator fun invoke(limit: Int = WorkoutRepository.DEFAULT_HISTORY_LIMIT): Flow<List<WorkoutSession>> =
        workoutRepository.observeSessionHistory(limit)
}
