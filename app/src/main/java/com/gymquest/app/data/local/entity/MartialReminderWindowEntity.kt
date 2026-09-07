package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "martial_reminder_windows",
    foreignKeys = [ForeignKey(entity = MartialReminderSettingsEntity::class, parentColumns = ["id"], childColumns = ["settingsId"])],
    indices = [Index(value = ["settingsId"]), Index(value = ["settingsId", "sortOrder"], unique = true)],
)
data class MartialReminderWindowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val settingsId: Long = MartialReminderSettingsEntity.DEFAULT_SETTINGS_ID,
    val startLocalTime: String,
    val endLocalTime: String,
    val enabled: Boolean = true,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
