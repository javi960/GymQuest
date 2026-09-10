package com.gymquest.app.domain.reminder

import com.gymquest.app.domain.model.MartialContent
import com.gymquest.app.domain.model.MartialReminderWindow
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MartialReminderPlannerTest {
    private val zone = ZoneId.of("Europe/Madrid")
    @Test fun `selects the next available configured window`() {
        val now = ZonedDateTime.of(2026, 9, 8, 11, 0, 0, 0, zone)
        val plan = MartialReminderPlanner.nextPlan(now, listOf(MartialReminderWindow(LocalTime.of(10, 0), LocalTime.of(12, 30), sortOrder = 0), MartialReminderWindow(LocalTime.of(17, 0), LocalTime.of(20, 0), sortOrder = 1)))
        assertEquals(11, plan?.triggerAt?.hour)
    }
    @Test fun `uses tomorrow when all windows have passed`() {
        val now = ZonedDateTime.of(2026, 9, 8, 21, 0, 0, 0, zone)
        val plan = MartialReminderPlanner.nextPlan(now, listOf(MartialReminderWindow(LocalTime.of(17, 0), LocalTime.of(20, 0), sortOrder = 0)))
        assertEquals(9, plan?.triggerAt?.dayOfMonth)
        assertEquals(17, plan?.triggerAt?.hour)
    }
    @Test fun `does not plan without enabled windows`() {
        val now = ZonedDateTime.now(zone)
        assertNull(MartialReminderPlanner.nextPlan(now, listOf(MartialReminderWindow(LocalTime.of(17, 0), LocalTime.of(20, 0), enabled = false, sortOrder = 0))))
    }
    @Test fun `xp never exceeds the daily maximum`() {
        assertEquals(1, MartialMissionXp.award("needs_review", awardedToday = 19, maxDailyXp = 20))
        assertEquals(0, MartialMissionXp.award("learning", awardedToday = 20, maxDailyXp = 20))
    }
    @Test fun `selector prioritizes known content needing review`() {
        val now = Instant.parse("2026-09-08T10:00:00Z")
        val selected = MartialMissionSelector.select(listOf(
            MartialContent(id = 1, martialStyleId = 1, name = "Dominado", contentType = "forma", progressStatus = "mastered", discoveredAt = now, createdAt = now, updatedAt = now),
            MartialContent(id = 2, martialStyleId = 1, name = "Repasar", contentType = "tecnica", progressStatus = "needs_review", discoveredAt = now, createdAt = now, updatedAt = now),
        ))
        assertEquals(2L, selected?.id)
    }
    @Test fun `daily frequency never schedules more missions than configured`() {
        assertEquals(true, MartialReminderRules.canSchedule(missionsToday = 1, missionsPerDay = 2))
        assertEquals(false, MartialReminderRules.canSchedule(missionsToday = 2, missionsPerDay = 2))
        assertEquals(false, MartialReminderRules.canSchedule(missionsToday = 0, missionsPerDay = 0))
    }
    @Test fun `rejects overlapping reminder windows`() {
        try {
            MartialReminderRules.validateWindow("19:00", "21:00", listOf("18:00" to "20:00"))
            throw AssertionError("Expected overlap validation to fail")
        } catch (error: IllegalArgumentException) {
            assertEquals("La ventana se solapa con otra ya configurada.", error.message)
        }
    }
}
