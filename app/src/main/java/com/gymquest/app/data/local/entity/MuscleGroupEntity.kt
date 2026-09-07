package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "muscle_groups",
    indices = [Index(value = ["name"], unique = true)],
)
data class MuscleGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val sortOrder: Int,
    val isArchived: Boolean = false,
)
