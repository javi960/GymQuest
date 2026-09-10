package com.gymquest.app.feature.progress

import androidx.lifecycle.ViewModelStore
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.model.ExerciseMastery
import com.gymquest.app.domain.model.MasteryProgress
import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.domain.repository.ProgressRepository
import com.gymquest.app.domain.usecase.progress.ObserveProgressSummaryUseCase
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {
    @Test
    fun `progress view model exposes the latest summary`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val summaries = MutableStateFlow(summary(totalSets = 3))
            val viewModel = ProgressViewModel(ObserveProgressSummaryUseCase(FakeProgressRepository(summaries)))

            advanceUntilIdle()
            assertEquals(3, viewModel.uiState.value.summary?.totalSets)
            summaries.value = summary(totalSets = 8)
            advanceUntilIdle()
            assertEquals(8, viewModel.uiState.value.summary?.totalSets)
            ViewModelStore().apply { put("progress", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `progress view model preserves named mastery for the progress screen`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val mastery = ExerciseMastery(exerciseVariantId = 7, totalSets = 20, updatedAt = Instant.EPOCH)
            val viewModel = ProgressViewModel(
                ObserveProgressSummaryUseCase(
                    FakeProgressRepository(
                        MutableStateFlow(
                            summary(totalSets = 20).copy(
                                mastery = listOf(mastery),
                                masteryProgress = listOf(MasteryProgress("Press banca con barra", mastery)),
                            ),
                        ),
                    ),
                ),
            )

            advanceUntilIdle()
            assertEquals("Press banca con barra", viewModel.uiState.value.summary?.masteryProgress?.single()?.variantName)
            ViewModelStore().apply { put("progress", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun summary(totalSets: Int) = ProgressSummary(
        characterStats = CharacterStats(updatedAt = Instant.EPOCH),
        totalSets = totalSets,
    )

    private class FakeProgressRepository(private val summaries: Flow<ProgressSummary>) : ProgressRepository {
        override fun observeCharacterStats(): Flow<CharacterStats> = flowOf(CharacterStats(updatedAt = Instant.EPOCH))
        override fun observeVariantMastery(variantId: Long): Flow<ExerciseMastery?> = flowOf(null)
        override fun observeProgressSummary(): Flow<ProgressSummary> = summaries
        override suspend fun saveCharacterStats(stats: CharacterStats): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun saveMastery(mastery: ExerciseMastery): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun replaceDerivedProgress(stats: CharacterStats, mastery: List<ExerciseMastery>): AppResult<Unit> = AppResult.Success(Unit)
    }
}
