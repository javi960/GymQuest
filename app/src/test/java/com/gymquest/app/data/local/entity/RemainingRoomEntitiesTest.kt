package com.gymquest.app.data.local.entity

import com.gymquest.app.domain.model.enums.MasteryRank
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class RemainingRoomEntitiesTest {
    private val timestamp = Instant.parse("2026-09-04T10:15:30Z")

    @Test
    fun `gym progress entities preserve their documented MVP defaults`() {
        val profile = UserProfileEntity(createdAt = timestamp, updatedAt = timestamp)
        val stats = CharacterStatsEntity(userProfileId = profile.id, updatedAt = timestamp)
        val mastery = ExerciseMasteryEntity(
            exerciseVariantId = 7,
            updatedAt = timestamp,
        )
        val achievement = AchievementEntity(code = "first_set", name = "First set", description = "Complete a set")
        val media = MediaFileEntity(
            ownerType = "exercise_variant",
            ownerId = 7,
            mediaType = "image",
            localUri = "content://media/7",
            createdAt = timestamp,
        )

        assertEquals("kg", profile.preferredWeightUnit)
        assertEquals(1, stats.level)
        assertEquals(0, stats.totalXp)
        assertEquals(MasteryRank.NOVICE, mastery.masteryRank)
        assertTrue(achievement.isBuiltIn)
        assertFalse(media.isArchived)
    }

    @Test
    fun `martial reminder data permits a single content or technique target`() {
        val settings = MartialReminderSettingsEntity(createdAt = timestamp, updatedAt = timestamp)
        val contentItem = MartialPracticeItemEntity(
            practiceSessionId = 1,
            contentId = 3,
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        val techniqueMission = MartialSecondaryMissionEntity(
            techniqueId = 5,
            scheduledFor = timestamp,
            createdAt = timestamp,
            updatedAt = timestamp,
        )

        assertFalse(settings.enabled)
        assertEquals(1, settings.missionsPerDay)
        assertEquals(3L, contentItem.contentId)
        assertEquals(5L, techniqueMission.techniqueId)
    }

    @Test
    fun `martial practice items reject missing or ambiguous targets`() {
        assertThrows(IllegalArgumentException::class.java) {
            MartialPracticeItemEntity(
                practiceSessionId = 1,
                createdAt = timestamp,
                updatedAt = timestamp,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            MartialSecondaryMissionEntity(
                technicalContentId = 3,
                techniqueId = 5,
                scheduledFor = timestamp,
                createdAt = timestamp,
                updatedAt = timestamp,
            )
        }
    }
}
