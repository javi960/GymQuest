package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant

data class ExerciseVariant(
    val id: Long = 0,
    val exerciseBaseId: Long,
    val name: String,
    val equipmentType: EquipmentType,
    val weightComparisonType: WeightComparisonType,
    val notes: String? = null,
    val isBuiltIn: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)