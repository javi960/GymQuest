package com.gymquest.app.domain.model

data class ExerciseCatalogEntry(
    val exerciseBase: ExerciseBase,
    val variants: List<ExerciseVariant>,
)
