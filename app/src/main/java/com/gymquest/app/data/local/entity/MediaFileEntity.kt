package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "media_files", indices = [Index(value = ["ownerType", "ownerId"])])
data class MediaFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerType: String,
    val ownerId: Long,
    val mediaType: String,
    val localUri: String,
    val thumbnailUri: String? = null,
    val title: String? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val isArchived: Boolean = false,
)
