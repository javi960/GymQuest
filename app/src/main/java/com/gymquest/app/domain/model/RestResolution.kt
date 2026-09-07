package com.gymquest.app.domain.model

data class RestResolution(
    val previousSet: WorkoutSet,
    val restBeforeSeconds: Long,
)
