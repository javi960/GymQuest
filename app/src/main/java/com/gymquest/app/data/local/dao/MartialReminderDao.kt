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

    @Insert
    suspend fun insertSecondaryMission(entity: MartialSecondaryMissionEntity): Long

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
}
