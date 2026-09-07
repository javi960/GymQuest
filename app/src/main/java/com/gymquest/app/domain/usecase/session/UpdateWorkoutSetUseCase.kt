package com.gymquest.app.domain.usecase.session

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.repository.WorkoutRepository

class UpdateWorkoutSetUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(workoutSet: WorkoutSet): AppResult<Unit> =
        workoutRepository.updateWorkoutSet(workoutSet)
}
