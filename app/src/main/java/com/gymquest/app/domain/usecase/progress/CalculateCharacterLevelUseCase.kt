package com.gymquest.app.domain.usecase.progress

import kotlin.math.sqrt

class CalculateCharacterLevelUseCase {
    operator fun invoke(totalXp: Long): Int {
        require(totalXp >= 0) { "La XP total no puede ser negativa." }
        val level = sqrt(totalXp / XP_MULTIPLIER.toDouble()).toLong() + 1L
        return level.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    private companion object {
        const val XP_MULTIPLIER = 100L
    }
}
