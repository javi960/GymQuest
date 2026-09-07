package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.repository.ExerciseRepository

class CreateExerciseVariantUseCase(
    private val exerciseRepository: ExerciseRepository,
) {
    suspend operator fun invoke(exerciseVariant: ExerciseVariant): AppResult<Long> =
        exerciseRepository.createExerciseVariant(exerciseVariant)
}
