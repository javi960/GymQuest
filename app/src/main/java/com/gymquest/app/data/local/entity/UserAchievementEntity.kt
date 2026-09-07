package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "user_achievements",
    foreignKeys = [ForeignKey(entity = AchievementEntity::class, parentColumns = ["id"], childColumns = ["achievementId"])],
    indices = [Index(value = ["achievementId"], unique = true)],
)
data class UserAchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val achievementId: Long,
    val unlockedAt: Instant,
    val relatedEntityType: String? = null,
    val relatedEntityId: Long? = null,
)
