package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.model.ExerciseMastery
import com.gymquest.app.domain.model.ProgressSummary
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    fun observeCharacterStats(): Flow<CharacterStats>
    fun observeVariantMastery(variantId: Long): Flow<ExerciseMastery?>
    fun observeProgressSummary(): Flow<ProgressSummary>
    suspend fun saveCharacterStats(stats: CharacterStats): AppResult<Unit>
    suspend fun saveMastery(mastery: ExerciseMastery): AppResult<Unit>
    suspend fun replaceDerivedProgress(stats: CharacterStats, mastery: List<ExerciseMastery>): AppResult<Unit>
}
