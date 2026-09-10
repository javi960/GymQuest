package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.domain.model.VariantLastPerformance
import com.gymquest.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ObserveVariantHistoryUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    operator fun invoke(variantId: Long): Flow<List<VariantLastPerformance>> =
        if (variantId > 0) workoutRepository.observeVariantHistory(variantId) else flowOf(emptyList())
}
