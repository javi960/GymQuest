package com.gymquest.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.gymquest.app.data.local.entity.MartialContentTechniqueCrossRef
import com.gymquest.app.data.local.entity.MartialTechnicalContentEntity
import com.gymquest.app.data.local.entity.MartialTechniqueEntity
import com.gymquest.app.data.local.entity.MartialContentStepEntity

data class MartialContentWithTechniques(
    @Embedded val technicalContent: MartialTechnicalContentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MartialContentTechniqueCrossRef::class,
            parentColumn = "contentId",
            entityColumn = "techniqueId",
        ),
    )
    val techniques: List<MartialTechniqueEntity>,
    @Relation(parentColumn = "id", entityColumn = "technicalContentId")
    val steps: List<MartialContentStepEntity>,
)
