package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "martial_technical_contents",
    foreignKeys = [ForeignKey(entity = MartialStyleEntity::class, parentColumns = ["id"], childColumns = ["martialStyleId"])],
    indices = [Index(value = ["martialStyleId"]), Index(value = ["martialStyleId", "progressStatus"])],
)
data class MartialTechnicalContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val martialStyleId: Long,
    val name: String,
    val contentType: String,
    val description: String? = null,
    val movementCount: Int? = null,
    val timeCount: Int? = null,
    val progressStatus: String = "not_started",
    val discoveredAt: Instant? = null,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
