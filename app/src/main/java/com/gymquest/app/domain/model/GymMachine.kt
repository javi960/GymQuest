package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant

data class GymMachine(
    val id: Long = 0,
    val gymId: Long,
    val exerciseVariantId: Long? = null,
    val name: String,
    val brand: String? = null,
    val loadType: String? = null,
    val weightComparisonType: WeightComparisonType,
    val notes: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
