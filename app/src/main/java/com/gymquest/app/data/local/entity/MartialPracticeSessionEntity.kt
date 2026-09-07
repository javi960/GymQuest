package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "martial_practice_sessions",
    foreignKeys = [ForeignKey(entity = MartialStyleEntity::class, parentColumns = ["id"], childColumns = ["martialStyleId"])],
    indices = [Index(value = ["martialStyleId"]), Index(value = ["startedAt"])],
)
data class MartialPracticeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val martialStyleId: Long,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val durationSeconds: Long = 0,
    val instructor: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
