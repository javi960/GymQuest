package com.gymquest.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.gymquest.app.data.local.entity.MartialPracticeItemEntity
import com.gymquest.app.data.local.entity.MartialPracticeSessionEntity

data class MartialPracticeSessionWithItems(
    @Embedded val practiceSession: MartialPracticeSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "practiceSessionId")
    val items: List<MartialPracticeItemEntity>,
)
