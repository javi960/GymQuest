package com.gymquest.app.feature.history

import androidx.lifecycle.ViewModelStore
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.VariantLastPerformance
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.repository.WorkoutRepository
import com.gymquest.app.domain.usecase.session.ObserveSessionDetailUseCase
import com.gymquest.app.domain.usecase.session.ObserveSessionHistoryUseCase
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @Test
    fun `history selection exposes the matching session detail`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val session = WorkoutSession(id = 3, startedAt = Instant.EPOCH, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH)
            val detail = WorkoutSessionDetail(session, emptyList())
            val repository = FakeWorkoutRepository(flowOf(listOf(session)), MutableStateFlow(detail))
            val viewModel = HistoryViewModel(ObserveSessionHistoryUseCase(repository), ObserveSessionDetailUseCase(repository))

            advanceUntilIdle()
            viewModel.selectSession(session.id)
            advanceUntilIdle()
            assertEquals(session.id, viewModel.uiState.value.selectedSessionId)
            assertEquals(detail, viewModel.uiState.value.selectedSessionDetail)
            ViewModelStore().apply { put("history", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `history exposes a recoverable message when loading fails`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeWorkoutRepository(flow { error("boom") }, flowOf(null))
            val viewModel = HistoryViewModel(ObserveSessionHistoryUseCase(repository), ObserveSessionDetailUseCase(repository))

            advanceUntilIdle()

            assertEquals("No se pudo cargar el historial.", viewModel.uiState.value.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeWorkoutRepository(
        private val sessions: Flow<List<WorkoutSession>>,
        private val detail: Flow<WorkoutSessionDetail?>,
    ) : WorkoutRepository {
        override fun observeActiveSession(): Flow<WorkoutSessionDetail?> = flowOf(null)
        override fun observeSessionDetail(sessionId: Long): Flow<WorkoutSessionDetail?> = detail
        override fun observeSessionHistory(limit: Int): Flow<List<WorkoutSession>> = sessions
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
