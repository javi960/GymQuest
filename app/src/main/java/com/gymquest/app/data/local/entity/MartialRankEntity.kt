package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "martial_ranks",
    foreignKeys = [ForeignKey(entity = MartialStyleEntity::class, parentColumns = ["id"], childColumns = ["martialStyleId"])],
    indices = [Index(value = ["martialStyleId"])],
)
data class MartialRankEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val martialStyleId: Long,
    val name: String,
    val rankOrder: Int = 0,
    val achievedAt: Instant? = null,
    val notes: String? = null,
    val isCurrent: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
