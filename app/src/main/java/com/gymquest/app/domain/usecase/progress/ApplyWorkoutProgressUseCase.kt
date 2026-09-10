package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.core.result.AppResult
import java.time.Instant

class ApplyWorkoutProgressUseCase(
    private val recalculateProgress: RecalculateProgressUseCase,
) {
    suspend operator fun invoke(completedAt: Instant): AppResult<Unit> = recalculateProgress(completedAt)
}
