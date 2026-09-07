package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "gyms")
data class GymEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
