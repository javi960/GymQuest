package com.gymquest.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity

data class ExerciseBaseWithVariants(
    @Embedded val exerciseBase: ExerciseBaseEntity,
    @Relation(parentColumn = "id", entityColumn = "exerciseBaseId")
    val variants: List<ExerciseVariantEntity>,
)
