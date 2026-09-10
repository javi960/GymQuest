package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "martial_belt_settings")
data class MartialBeltSettingsEntity(
    @PrimaryKey val id: Long = DEFAULT_ID,
    val belt: String = "WHITE",
    val updatedAt: Instant,
) {
    companion object { const val DEFAULT_ID = 1L }
}
