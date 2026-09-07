package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.domain.repository.ExerciseRepository

class CreateMuscleGroupUseCase(
    private val exerciseRepository: ExerciseRepository,
) {
    suspend operator fun invoke(muscleGroup: MuscleGroup): AppResult<Long> =
        exerciseRepository.createMuscleGroup(muscleGroup)
}
