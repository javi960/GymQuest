package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.MartialDirection
import java.time.Instant
import java.time.LocalTime
import org.junit.Test

class MartialDomainTest {
    private val now = Instant.parse("2026-09-08T10:00:00Z")

    @Test(expected = IllegalArgumentException::class)
    fun `a practice requires at least one technical target`() {
        MartialPractice(1, now, 60, emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a sequence step cannot have a negative order`() {
        MartialContentStep(technicalContentId = 1, orderIndex = -1, direction = MartialDirection.FRONT, createdAt = now, updatedAt = now)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a reminder window cannot end before it starts`() {
        MartialReminderWindow(LocalTime.of(18, 0), LocalTime.of(9, 0), sortOrder = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a mission targets exactly one kind of technical content`() {
        MartialSecondaryMission(technicalContentId = 1, techniqueId = 2, scheduledFor = now, createdAt = now, updatedAt = now)
    }
}
