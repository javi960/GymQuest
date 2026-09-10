package com.gymquest.app.data.local.projection

import androidx.room.Embedded
import com.gymquest.app.data.local.entity.ExerciseMasteryEntity

data class ExerciseMasteryWithVariantName(
    @Embedded
    val mastery: ExerciseMasteryEntity,
    val variantName: String,
)
