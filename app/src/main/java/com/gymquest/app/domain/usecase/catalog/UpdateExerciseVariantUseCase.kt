package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.repository.ExerciseRepository

class UpdateExerciseVariantUseCase(
    private val exerciseRepository: ExerciseRepository,
) {
    suspend operator fun invoke(exerciseVariant: ExerciseVariant): AppResult<Unit> =
        exerciseRepository.updateExerciseVariant(exerciseVariant)
}
