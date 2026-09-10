package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.MasteryRank

object MasteryRankRules {
    fun levelFor(totalSets: Int): Int {
        require(totalSets >= 0) { "Las series no pueden ser negativas." }
        return totalSets / SETS_PER_LEVEL + 1
    }

    fun rankFor(level: Int): MasteryRank {
        require(level >= 1) { "El nivel de dominio debe ser positivo." }
        return when {
            level >= 20 -> MasteryRank.MASTER
            level >= 10 -> MasteryRank.EXPERT
            level >= 5 -> MasteryRank.ADVANCED
            level >= 3 -> MasteryRank.INTERMEDIATE
            level >= 2 -> MasteryRank.APPRENTICE
            else -> MasteryRank.NOVICE
        }
    }

    private const val SETS_PER_LEVEL = 10
}
