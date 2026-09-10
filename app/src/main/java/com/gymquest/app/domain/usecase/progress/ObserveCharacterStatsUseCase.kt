package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow

class ObserveCharacterStatsUseCase(private val repository: ProgressRepository) {
    operator fun invoke(): Flow<CharacterStats> = repository.observeCharacterStats()
}
