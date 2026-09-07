package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "martial_styles",
    foreignKeys = [ForeignKey(entity = MartialArtEntity::class, parentColumns = ["id"], childColumns = ["martialArtId"])],
    indices = [Index(value = ["martialArtId"])],
)
data class MartialStyleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val martialArtId: Long,
    val name: String,
    val description: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
