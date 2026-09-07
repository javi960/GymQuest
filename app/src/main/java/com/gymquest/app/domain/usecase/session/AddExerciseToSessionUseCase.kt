package com.gymquest.app.domain.usecase.session

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.repository.WorkoutRepository

class AddExerciseToSessionUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(workoutExercise: WorkoutExercise): AppResult<Long> =
        workoutRepository.addExerciseToSession(workoutExercise)
}
