package com.gymquest.app.feature.home

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.domain.model.VariantLastPerformance
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.repository.ProgressRepository
import com.gymquest.app.domain.repository.WorkoutRepository
import com.gymquest.app.domain.usecase.progress.ObserveProgressSummaryUseCase
import com.gymquest.app.domain.usecase.session.ObserveActiveSessionUseCase
import androidx.lifecycle.ViewModelStore
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val now = Instant.parse("2026-09-07T10:00:00Z")

    @Test
    fun `home reflects progress and changes primary action for an active session`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
        val progress = MutableStateFlow(summary(totalWorkouts = 2))
        val activeSession = MutableStateFlow<WorkoutSessionDetail?>(null)
        val viewModel = HomeViewModel(
            ObserveProgressSummaryUseCase(FakeProgressRepository(progress)),
            ObserveActiveSessionUseCase(FakeWorkoutRepository(activeSession)),
        )

        advanceUntilIdle()
        val initial = viewModel.uiState.value
        assertEquals(2, initial.summary?.characterStats?.totalWorkouts)
        assertFalse(initial.hasActiveSession)

        activeSession.value = WorkoutSessionDetail(session(), emptyList())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasActiveSession)
        ViewModelStore().apply {
            put("home", viewModel)
            clear()
        }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `home exposes a recoverable message when loading fails`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val viewModel = HomeViewModel(
                ObserveProgressSummaryUseCase(FakeProgressRepository(flow { error("boom") })),
                ObserveActiveSessionUseCase(FakeWorkoutRepository(MutableStateFlow(null))),
            )

            advanceUntilIdle()

            assertEquals("No se pudo actualizar el inicio.", viewModel.uiState.value.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun summary(totalWorkouts: Int) = ProgressSummary(
        CharacterStats(totalWorkouts = totalWorkouts, updatedAt = now),
        totalSets = totalWorkouts * 3,
    )

    private fun session() = WorkoutSession(id = 4, startedAt = now, createdAt = now, updatedAt = now)

    private class FakeProgressRepository(private val summaries: Flow<ProgressSummary>) : ProgressRepository {
        override fun observeCharacterStats(): Flow<CharacterStats> = throw UnsupportedOperationException()
        override fun observeVariantMastery(variantId: Long) = throw UnsupportedOperationException()
        override fun observeProgressSummary(): Flow<ProgressSummary> = summaries
        override suspend fun saveCharacterStats(stats: CharacterStats): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun saveMastery(mastery: com.gymquest.app.domain.model.ExerciseMastery): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun replaceDerivedProgress(stats: CharacterStats, mastery: List<com.gymquest.app.domain.model.ExerciseMastery>): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeWorkoutRepository(private val activeSession: Flow<WorkoutSessionDetail?>) : WorkoutRepository {
        override fun observeActiveSession(): Flow<WorkoutSessionDetail?> = activeSession
        override fun observeSessionDetail(sessionId: Long): Flow<WorkoutSessionDetail?> = throw UnsupportedOperationException()
        override fun observeSessionHistory(limit: Int): Flow<List<WorkoutSession>> = throw UnsupportedOperationException()
        override suspend fun findSessionDetailById(sessionId: Long): AppResult<WorkoutSessionDetail?> = AppResult.Success(null)
        override suspend fun findVariantLastPerformance(variantId: Long): AppResult<VariantLastPerformance?> = AppResult.Success(null)
        override suspend fun startSession(session: WorkoutSession): AppResult<Long> = AppResult.Success(0)
        override suspend fun updateSession(session: WorkoutSession): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun addExerciseToSession(workoutExercise: WorkoutExercise): AppResult<Long> = AppResult.Success(0)
        override suspend fun saveWorkoutSet(workoutSet: WorkoutSet): AppResult<Long> = AppResult.Success(0)
        override suspend fun updateWorkoutSet(workoutSet: WorkoutSet): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deleteWorkoutSet(setId: Long): AppResult<Unit> = AppResult.Success(Unit)
    }
}
