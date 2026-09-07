package com.gymquest.app.domain.usecase.session

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.repository.WorkoutRepository

class SaveWorkoutSetUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(workoutSet: WorkoutSet): AppResult<Long> =
        workoutRepository.saveWorkoutSet(workoutSet)
}
