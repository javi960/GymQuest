package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow

class ObserveProgressSummaryUseCase(private val repository: ProgressRepository) {
    operator fun invoke(): Flow<ProgressSummary> = repository.observeProgressSummary()
}
