package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "achievements", indices = [Index(value = ["code"], unique = true)])
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val description: String,
    val category: String? = null,
    val xpReward: Long = 0,
    val isBuiltIn: Boolean = true,
)
