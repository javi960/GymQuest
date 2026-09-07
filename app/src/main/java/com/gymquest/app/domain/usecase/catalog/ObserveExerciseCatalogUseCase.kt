package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow

class ObserveExerciseCatalogUseCase(
    private val exerciseRepository: ExerciseRepository,
) {
    operator fun invoke(): Flow<List<ExerciseCatalogEntry>> =
        exerciseRepository.observeActiveExerciseCatalog()
}
