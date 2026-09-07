package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveExerciseVariantsUseCase(
    private val exerciseRepository: ExerciseRepository,
) {
    operator fun invoke(): Flow<List<ExerciseVariant>> =
        exerciseRepository.observeActiveExerciseVariants()
}
