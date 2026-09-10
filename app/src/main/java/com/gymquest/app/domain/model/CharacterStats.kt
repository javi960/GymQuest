package com.gymquest.app.domain.model

import java.time.Instant

data class CharacterStats(
    val userProfileId: Long = 1,
    val level: Int = 1,
    val totalXp: Long = 0,
    val currentStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val totalTrainingSeconds: Long = 0,
    val discoveredVariants: Int = 0,
    val strengthXp: Long = 0,
    val enduranceXp: Long = 0,
    val consistencyXp: Long = 0,
    val techniqueXp: Long = 0,
    val disciplineXp: Long = 0,
    val updatedAt: Instant,
) {
    val category: Int
        get() = CharacterCategoryRules.categoryFor(level)

    val categoryDefinition: CharacterCategoryRules.Definition
        get() = CharacterCategoryRules.definitionFor(level)

    init {
        require(userProfileId > 0) { "El perfil debe existir." }
        require(level >= 1) { "El nivel debe ser al menos uno." }
        require(totalXp >= 0 && currentStreak >= 0 && totalWorkouts >= 0) { "Las estadisticas no pueden ser negativas." }
        require(totalTrainingSeconds >= 0 && discoveredVariants >= 0) { "Las estadisticas no pueden ser negativas." }
        require(listOf(strengthXp, enduranceXp, consistencyXp, techniqueXp, disciplineXp).all { it >= 0 }) {
            "La XP por categoria no puede ser negativa."
        }
    }
}
