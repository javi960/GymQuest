package com.gymquest.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.gymquest.app.data.local.entity.MartialStyleEntity
import com.gymquest.app.data.local.entity.MartialTechnicalContentEntity

data class MartialStyleWithContent(
    @Embedded val martialStyle: MartialStyleEntity,
    @Relation(parentColumn = "id", entityColumn = "martialStyleId")
    val technicalContents: List<MartialTechnicalContentEntity>,
)
