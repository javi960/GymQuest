package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant

@Entity(
    tableName = "exercise_variants",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseBaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseBaseId"],
        ),
    ],
    indices = [
        Index(value = ["exerciseBaseId"]),
        Index(value = ["exerciseBaseId", "name"], unique = true),
        Index(value = ["weightComparisonType"]),
    ],
)
data class ExerciseVariantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseBaseId: Long,
    val name: String,
    val equipmentType: EquipmentType,
    val weightComparisonType: WeightComparisonType,
    val notes: String? = null,
    val isBuiltIn: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
