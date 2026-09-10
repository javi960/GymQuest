package com.gymquest.app.domain.model

data class PersonalRecords(
    val weight: Double? = null,
    val reps: Int? = null,
    val volume: Double? = null,
) {
    init {
        require(listOfNotNull(weight, volume).all { it.isFinite() && it >= 0.0 }) { "Los records de carga no son validos." }
        require(reps == null || reps >= 0) { "El record de repeticiones no es valido." }
    }
}
