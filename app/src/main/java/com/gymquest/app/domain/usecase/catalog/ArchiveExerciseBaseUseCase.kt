package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.repository.ExerciseRepository
import java.time.Instant

class ArchiveExerciseBaseUseCase(
    private val exerciseRepository: ExerciseRepository,
) {
    suspend operator fun invoke(baseId: Long, updatedAt: Instant): AppResult<Unit> =
        exerciseRepository.archiveExerciseBase(baseId, updatedAt)
}
