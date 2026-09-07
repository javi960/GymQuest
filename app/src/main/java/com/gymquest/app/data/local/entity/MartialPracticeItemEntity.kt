package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "martial_practice_items",
    foreignKeys = [
        ForeignKey(entity = MartialPracticeSessionEntity::class, parentColumns = ["id"], childColumns = ["practiceSessionId"]),
        ForeignKey(entity = MartialTechnicalContentEntity::class, parentColumns = ["id"], childColumns = ["contentId"]),
        ForeignKey(entity = MartialTechniqueEntity::class, parentColumns = ["id"], childColumns = ["techniqueId"]),
    ],
    indices = [Index(value = ["practiceSessionId"]), Index(value = ["contentId"]), Index(value = ["techniqueId"])],
)
data class MartialPracticeItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val practiceSessionId: Long,
    val contentId: Long? = null,
    val techniqueId: Long? = null,
    val practiceType: String = "practice",
    val repetitions: Int? = null,
    val minutes: Int? = null,
    val confidence: Int? = null,
    val difficulty: Int? = null,
    val progressStatusAfter: String? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require((contentId == null) != (techniqueId == null)) {
            "A martial practice item must target exactly one content or technique."
        }
    }
}
