package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymquest.app.domain.model.enums.MasteryRank
import java.time.Instant

@Entity(
    tableName = "exercise_mastery",
    foreignKeys = [
        ForeignKey(entity = ExerciseVariantEntity::class, parentColumns = ["id"], childColumns = ["exerciseVariantId"]),
    ],
    indices = [Index(value = ["exerciseVariantId"], unique = true)],
)
data class ExerciseMasteryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseVariantId: Long,
    val level: Int = 1,
    val masteryRank: MasteryRank = MasteryRank.NOVICE,
    val accumulatedVolume: Double = 0.0,
    val totalSets: Int = 0,
    val totalReps: Int = 0,
    val personalRecordWeight: Double? = null,
    val personalRecordReps: Int? = null,
    val personalRecordVolume: Double? = null,
    val discoveredAt: Instant? = null,
    val updatedAt: Instant,
)
