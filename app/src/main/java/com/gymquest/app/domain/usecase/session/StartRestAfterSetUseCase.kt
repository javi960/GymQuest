package com.gymquest.app.domain.usecase.session

import com.gymquest.app.domain.model.WorkoutSet
import java.time.Instant

class StartRestAfterSetUseCase {
    operator fun invoke(set: WorkoutSet, savedAt: Instant): WorkoutSet =
        set.copy(
            startedAt = set.startedAt ?: savedAt,
            endedAt = set.endedAt ?: savedAt,
            updatedAt = savedAt,
        )
}
