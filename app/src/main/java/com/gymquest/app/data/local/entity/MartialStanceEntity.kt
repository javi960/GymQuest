package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/** A reusable leg stance belonging to a martial style. */
@Entity(
    tableName = "martial_stances",
    foreignKeys = [ForeignKey(entity = MartialStyleEntity::class, parentColumns = ["id"], childColumns = ["martialStyleId"])],
    indices = [Index(value = ["martialStyleId"])],
)
data class MartialStanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val martialStyleId: Long,
    val name: String,
    val translation: String? = null,
    val description: String? = null,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
