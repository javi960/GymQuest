package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.repository.ExerciseRepository
import java.time.Instant

class ArchiveExerciseVariantUseCase(
    private val exerciseRepository: ExerciseRepository,
) {
    suspend operator fun invoke(variantId: Long, updatedAt: Instant): AppResult<Unit> =
        exerciseRepository.archiveExerciseVariant(variantId, updatedAt)
}
