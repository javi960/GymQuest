package com.gymquest.app.domain.usecase.session

import com.gymquest.app.domain.model.RestResolution
import com.gymquest.app.domain.model.WorkoutSet
import java.time.Instant
import java.time.temporal.ChronoUnit

class ResolveRestBeforeNextSetUseCase {
    operator fun invoke(previousSet: WorkoutSet?, nextSetStartedAt: Instant): RestResolution? {
        val previousEndedAt = previousSet?.endedAt ?: return null
        val restSeconds = previousEndedAt.until(nextSetStartedAt, ChronoUnit.SECONDS).coerceAtLeast(0)
        return RestResolution(
            previousSet = previousSet.copy(
                restAfterSeconds = restSeconds,
                updatedAt = nextSetStartedAt,
            ),
            restBeforeSeconds = restSeconds,
        )
    }
}
