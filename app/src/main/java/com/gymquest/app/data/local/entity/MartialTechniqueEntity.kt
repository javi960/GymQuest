package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymquest.app.domain.model.enums.MartialTechniqueFamily
import java.time.Instant

@Entity(
    tableName = "martial_techniques",
    foreignKeys = [ForeignKey(entity = MartialStyleEntity::class, parentColumns = ["id"], childColumns = ["martialStyleId"])],
    indices = [Index(value = ["martialStyleId"])],
)
data class MartialTechniqueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val martialStyleId: Long,
    val name: String,
    val translation: String? = null,
    val category: MartialTechniqueFamily? = null,
    val description: String? = null,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
