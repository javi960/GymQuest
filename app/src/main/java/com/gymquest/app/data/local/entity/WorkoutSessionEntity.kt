package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymquest.app.domain.model.enums.SessionStatus
import java.time.Instant

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(entity = GymEntity::class, parentColumns = ["id"], childColumns = ["gymId"]),
    ],
    indices = [Index(value = ["gymId"]), Index(value = ["startedAt"]), Index(value = ["status"])],
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gymId: Long? = null,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val durationSeconds: Long = 0,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val notes: String? = null,
    val perceivedEnergy: Int? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
