package com.gymquest.app.domain.model

data class ProgressSummary(
    val characterStats: CharacterStats,
    val totalVolume: Double = 0.0,
    val totalSets: Int = 0,
    val personalRecordCount: Int = 0,
    val mastery: List<ExerciseMastery> = emptyList(),
    val masteryProgress: List<MasteryProgress> = emptyList(),
) {
    init {
        require(totalVolume.isFinite() && totalVolume >= 0.0 && totalSets >= 0 && personalRecordCount >= 0) {
            "El resumen de progreso no es valido."
        }
        require(masteryProgress.map { it.mastery.exerciseVariantId }.distinct().size == masteryProgress.size) {
            "No puede haber progreso de dominio duplicado para una variante."
        }
    }
}
