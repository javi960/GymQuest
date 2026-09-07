package com.gymquest.app.domain.usecase.session

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.repository.WorkoutRepository

class DeleteWorkoutSetUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(setId: Long): AppResult<Unit> =
        workoutRepository.deleteWorkoutSet(setId)
}
