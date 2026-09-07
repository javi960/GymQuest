package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "martial_reminder_settings")
data class MartialReminderSettingsEntity(
    @PrimaryKey val id: Long = DEFAULT_SETTINGS_ID,
    val enabled: Boolean = false,
    val missionsPerDay: Int = 1,
    val privacyMode: String = "generic",
    val maxDailyXp: Long = 20,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        const val DEFAULT_SETTINGS_ID = 1L
    }
}
