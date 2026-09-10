package com.gymquest.app.domain.model

import java.time.Instant

data class RoutineExercise(
    val id: Long = 0,
    val routineDayId: Long,
    val exerciseVariantId: Long,
    val gymMachineId: Long? = null,
    val orderIndex: Int,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
