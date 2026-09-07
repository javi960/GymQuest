package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "character_stats",
    foreignKeys = [
        ForeignKey(entity = UserProfileEntity::class, parentColumns = ["id"], childColumns = ["userProfileId"]),
    ],
    indices = [Index(value = ["userProfileId"], unique = true)],
)
data class CharacterStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userProfileId: Long,
    val level: Int = 1,
    val totalXp: Long = 0,
    val currentStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val totalTrainingSeconds: Long = 0,
    val discoveredVariants: Int = 0,
    val strengthXp: Long = 0,
    val enduranceXp: Long = 0,
    val consistencyXp: Long = 0,
    val techniqueXp: Long = 0,
    val disciplineXp: Long = 0,
    val updatedAt: Instant,
)
