package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "martial_content_techniques",
    primaryKeys = ["contentId", "techniqueId"],
    foreignKeys = [
        ForeignKey(entity = MartialTechnicalContentEntity::class, parentColumns = ["id"], childColumns = ["contentId"]),
        ForeignKey(entity = MartialTechniqueEntity::class, parentColumns = ["id"], childColumns = ["techniqueId"]),
    ],
    indices = [Index(value = ["techniqueId"]), Index(value = ["contentId", "orderIndex"], unique = true)],
)
data class MartialContentTechniqueCrossRef(
    val contentId: Long,
    val techniqueId: Long,
    val orderIndex: Int,
    val notes: String? = null,
)
