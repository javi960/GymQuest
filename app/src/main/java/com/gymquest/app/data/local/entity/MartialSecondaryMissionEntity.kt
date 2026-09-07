package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "martial_secondary_missions",
    foreignKeys = [
        ForeignKey(entity = MartialTechnicalContentEntity::class, parentColumns = ["id"], childColumns = ["technicalContentId"]),
        ForeignKey(entity = MartialTechniqueEntity::class, parentColumns = ["id"], childColumns = ["techniqueId"]),
        ForeignKey(entity = MartialPracticeSessionEntity::class, parentColumns = ["id"], childColumns = ["createdPracticeSessionId"]),
    ],
    indices = [
        Index(value = ["technicalContentId"]),
        Index(value = ["techniqueId"]),
        Index(value = ["createdPracticeSessionId"]),
        Index(value = ["status", "scheduledFor"]),
    ],
)
data class MartialSecondaryMissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val technicalContentId: Long? = null,
    val techniqueId: Long? = null,
    val scheduledFor: Instant,
    val status: String = "scheduled",
    val completedAt: Instant? = null,
    val xpAwarded: Long = 0,
    val createdPracticeSessionId: Long? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require((technicalContentId == null) != (techniqueId == null)) {
            "A martial secondary mission must target exactly one content or technique."
        }
    }
}
