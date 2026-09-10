package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class ReplaceExerciseBaseMediaUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(mediaFile: MediaFile): AppResult<Long> = repository.replaceForOwner(mediaFile)
}

class RemoveExerciseBaseMediaUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(exerciseBaseId: Long): AppResult<Unit> =
        repository.removeForOwner(MediaOwnerType.EXERCISE_BASE, exerciseBaseId)
}

class ObserveExerciseBaseMediaUseCase(private val repository: MediaRepository) {
    operator fun invoke(): Flow<List<MediaFile>> = repository.observeActiveForOwnerType(MediaOwnerType.EXERCISE_BASE)
}
