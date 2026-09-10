package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.gymquest.app.data.local.entity.MartialReminderSettingsEntity
import com.gymquest.app.data.local.entity.MartialReminderWindowEntity
import com.gymquest.app.data.local.entity.MartialSecondaryMissionEntity
import java.time.Instant

@Dao
interface MartialReminderDao {
    @Upsert
    suspend fun upsertSettings(entity: MartialReminderSettingsEntity)

    @Insert
    suspend fun insertReminderWindow(entity: MartialReminderWindowEntity): Long

    @Upsert
    suspend fun upsertReminderWindow(entity: MartialReminderWindowEntity)

    @Query("DELETE FROM martial_reminder_windows WHERE id = :windowId")
    suspend fun deleteReminderWindow(windowId: Long): Int

    @Insert
    suspend fun insertSecondaryMission(entity: MartialSecondaryMissionEntity): Long

    @Query("SELECT COUNT(*) FROM martial_secondary_missions WHERE scheduledFor >= :start AND scheduledFor < :end")
    suspend fun countMissions(start: Instant, end: Instant): Int

    @Query("SELECT COALESCE(SUM(xpAwarded), 0) FROM martial_secondary_missions WHERE status = 'completed' AND completedAt >= :start AND completedAt < :end")
    suspend fun awardedXp(start: Instant, end: Instant): Long

    @Query("SELECT * FROM martial_technical_contents WHERE isArchived = 0 AND discoveredAt IS NOT NULL ORDER BY CASE progressStatus WHEN 'needs_review' THEN 0 WHEN 'learning' THEN 1 WHEN 'improving' THEN 2 ELSE 3 END, id LIMIT 1")
    suspend fun nextEligibleContent(): com.gymquest.app.data.local.entity.MartialTechnicalContentEntity?

    @Query("SELECT * FROM martial_reminder_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): MartialReminderSettingsEntity?

    @Query("SELECT * FROM martial_secondary_missions WHERE id = :missionId LIMIT 1")
    suspend fun getSecondaryMission(missionId: Long): MartialSecondaryMissionEntity?

    @Query("SELECT * FROM martial_reminder_windows WHERE settingsId = :settingsId ORDER BY sortOrder")
    suspend fun getReminderWindows(settingsId: Long = MartialReminderSettingsEntity.DEFAULT_SETTINGS_ID): List<MartialReminderWindowEntity>

    @Query("SELECT * FROM martial_secondary_missions WHERE status IN ('scheduled', 'postponed') AND scheduledFor <= :scheduledBefore ORDER BY scheduledFor")
    suspend fun getPendingMissions(scheduledBefore: Instant): List<MartialSecondaryMissionEntity>

    @Query("UPDATE martial_secondary_missions SET status = :newStatus, completedAt = :completedAt, xpAwarded = :xpAwarded, updatedAt = :updatedAt WHERE id = :missionId AND status IN ('scheduled', 'postponed')")
    suspend fun transitionPendingMission(
        missionId: Long,
        newStatus: String,
        completedAt: Instant?,
        xpAwarded: Long,
        updatedAt: Instant,
    ): Int

    @androidx.room.Transaction
    suspend fun completeMissionWithDailyCap(missionId: Long, start: Instant, end: Instant, maxDailyXp: Long, now: Instant): Long {
        val mission = getSecondaryMission(missionId) ?: return 0
        if (mission.status !in setOf("scheduled", "postponed")) return 0
        val xp = com.gymquest.app.domain.reminder.MartialMissionXp.award("learning", awardedXp(start, end), maxDailyXp)
        return if (transitionPendingMission(missionId, "completed", now, xp, now) == 1) xp else 0
    }
}
