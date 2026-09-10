package com.gymquest.app.domain.reminder

import java.time.Instant

interface ReminderScheduler {
    fun schedule(missionId: Long, earliestTriggerAt: Instant, windowLengthMillis: Long)
    fun cancel()
}
