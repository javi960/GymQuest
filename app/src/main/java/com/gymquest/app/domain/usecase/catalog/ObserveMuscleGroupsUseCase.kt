package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow

class ObserveMuscleGroupsUseCase(
    private val exerciseRepository: ExerciseRepository,
) {
    operator fun invoke(): Flow<List<MuscleGroup>> = exerciseRepository.observeMuscleGroups()
}
