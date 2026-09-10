package com.gymquest.app.domain.model

/**
 * Read model for presenting a variant's real mastery alongside its catalog name.
 * It deliberately keeps catalogue data out of [ExerciseMastery], which is a
 * persisted, derived aggregate.
 */
data class MasteryProgress(
    val variantName: String,
    val mastery: ExerciseMastery,
) {
    init {
        require(variantName.isNotBlank()) { "El nombre de la variante no puede estar vacio." }
    }
}
