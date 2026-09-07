package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "martial_arts")
data class MartialArtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
