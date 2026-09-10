package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.MasteryRank
import java.time.Instant

data class ExerciseMastery(
    val exerciseVariantId: Long,
    val level: Int = 1,
    val masteryRank: MasteryRank = MasteryRank.NOVICE,
    val accumulatedVolume: Double = 0.0,
    val totalSets: Int = 0,
    val totalReps: Int = 0,
    val personalRecordWeight: Double? = null,
    val personalRecordReps: Int? = null,
    val personalRecordVolume: Double? = null,
    val discoveredAt: Instant? = null,
    val updatedAt: Instant,
) {
    init {
        require(exerciseVariantId > 0 && level >= 1 && totalSets >= 0 && totalReps >= 0) { "El dominio de ejercicio no es valido." }
        require(accumulatedVolume.isFinite() && accumulatedVolume >= 0.0) { "El volumen acumulado no es valido." }
        require(listOfNotNull(personalRecordWeight, personalRecordVolume).all { it.isFinite() && it >= 0.0 }) {
            "Los records de carga no son validos."
        }
        require(personalRecordReps == null || personalRecordReps >= 0) { "El record de repeticiones no es valido." }
        require(masteryRank == MasteryRankRules.rankFor(level)) { "El rango no corresponde con el nivel de dominio." }
    }
}
