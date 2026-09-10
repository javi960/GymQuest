package com.gymquest.app.domain.reminder

import com.gymquest.app.domain.model.MartialContent
import com.gymquest.app.domain.model.MartialReminderWindow
import java.time.Duration
import java.time.ZonedDateTime

data class ReminderPlan(val triggerAt: ZonedDateTime, val window: Duration)

object MartialReminderPlanner {
    private val minimumWindow = Duration.ofMinutes(10)

    fun nextPlan(now: ZonedDateTime, windows: List<MartialReminderWindow>): ReminderPlan? {
        val enabled = windows.filter { it.enabled }.sortedBy { it.startLocalTime }
        if (enabled.isEmpty()) return null
        for (offset in 0L..1L) {
            val date = now.toLocalDate().plusDays(offset)
            for (window in enabled) {
                val start = ZonedDateTime.of(date, window.startLocalTime, now.zone)
                val end = ZonedDateTime.of(date, window.endLocalTime, now.zone)
                if (end.isAfter(now)) {
                    val trigger = if (start.isAfter(now)) start else now
                    return ReminderPlan(trigger, Duration.between(trigger, end).coerceAtLeast(minimumWindow))
                }
            }
        }
        return null
    }
}

object MartialMissionXp {
    const val BASE_XP = 5L
    const val NEEDS_REVIEW_BONUS = 2L
    fun award(progressStatus: String, awardedToday: Long, maxDailyXp: Long): Long =
        (BASE_XP + if (progressStatus == "needs_review") NEEDS_REVIEW_BONUS else 0L)
            .coerceAtMost((maxDailyXp - awardedToday).coerceAtLeast(0L))
}

object MartialMissionSelector {
    fun select(contents: List<MartialContent>): MartialContent? = contents
        .asSequence()
        .filter { !it.isArchived && it.discoveredAt != null }
        .sortedWith(compareBy<MartialContent> { priority(it.progressStatus) }.thenBy { it.id })
        .firstOrNull()

    private fun priority(status: String): Int = when (status) {
        "needs_review" -> 0
        "learning" -> 1
        "improving" -> 2
        else -> 3
    }
}
