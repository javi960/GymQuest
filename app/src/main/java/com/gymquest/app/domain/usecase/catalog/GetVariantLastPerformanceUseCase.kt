package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.VariantLastPerformance
import com.gymquest.app.domain.repository.WorkoutRepository

class GetVariantLastPerformanceUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(variantId: Long): AppResult<VariantLastPerformance?> =
        workoutRepository.findVariantLastPerformance(variantId)
}
