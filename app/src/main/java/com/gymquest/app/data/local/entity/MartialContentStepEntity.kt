package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymquest.app.domain.model.enums.MartialDirection
import com.gymquest.app.domain.model.enums.MartialSide
import java.time.Instant

/** One ordered movement in a kata, form, drill, or other technical content. */
@Entity(
    tableName = "martial_content_steps",
    foreignKeys = [
        ForeignKey(entity = MartialTechnicalContentEntity::class, parentColumns = ["id"], childColumns = ["technicalContentId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = MartialStanceEntity::class, parentColumns = ["id"], childColumns = ["stanceId"]),
        ForeignKey(entity = MartialTechniqueEntity::class, parentColumns = ["id"], childColumns = ["techniqueId"]),
    ],
    indices = [
        Index(value = ["technicalContentId", "orderIndex"], unique = true),
        Index(value = ["stanceId"]),
        Index(value = ["techniqueId"]),
        Index(value = ["mediaFileId"]),
    ],
)
data class MartialContentStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val technicalContentId: Long,
    val orderIndex: Int,
    val stanceId: Long? = null,
    val techniqueId: Long? = null,
    val direction: MartialDirection,
    val side: MartialSide? = null,
    val movementType: String? = null,
    val displacement: String? = null,
    val turnDegrees: Int? = null,
    val angleDegrees: Int? = null,
    val description: String? = null,
    val hasKiai: Boolean = false,
    val hasPause: Boolean = false,
    /** Optional local media record. The media file is owned polymorphically by this step. */
    val mediaFileId: Long? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
