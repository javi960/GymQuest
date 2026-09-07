package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.repository.ExerciseRepository

class CreateExerciseBaseUseCase(
    private val exerciseRepository: ExerciseRepository,
) {
    suspend operator fun invoke(exerciseBase: ExerciseBase): AppResult<Long> =
        exerciseRepository.createExerciseBase(exerciseBase)
}
