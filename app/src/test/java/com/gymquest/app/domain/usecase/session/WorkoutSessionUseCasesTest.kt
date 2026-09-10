package com.gymquest.app.domain.usecase.session

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.VariantLastPerformance
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.repository.WorkoutRepository
import com.gymquest.app.domain.usecase.catalog.GetVariantLastPerformanceUseCase
import com.gymquest.app.domain.usecase.progress.ObserveVariantHistoryUseCase
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class WorkoutSessionUseCasesTest {
    private val startedAt = Instant.parse("2026-09-04T10:00:00Z")
    private val endedAt = Instant.parse("2026-09-04T11:15:00Z")

    @Test
    fun writeUseCases_delegateModelsAndResults() = runBlocking {
        val repository = RecordingWorkoutRepository()
        val session = workoutSession()
        val exercise = workoutExercise()
        val set = workoutSet()
        repository.startResult = AppResult.Success(11L)
        repository.addExerciseResult = AppResult.Success(12L)
        repository.saveSetResult = AppResult.Success(13L)
        repository.updateSetResult = AppResult.Success(Unit)
        repository.deleteSetResult = AppResult.Success(Unit)

        assertSame(repository.startResult, StartWorkoutSessionUseCase(repository)(session))
        assertSame(repository.addExerciseResult, AddExerciseToSessionUseCase(repository)(exercise))
        assertSame(repository.saveSetResult, SaveWorkoutSetUseCase(repository)(set))
        assertSame(repository.updateSetResult, UpdateWorkoutSetUseCase(repository)(set))
        assertSame(repository.deleteSetResult, DeleteWorkoutSetUseCase(repository)(set.id))
        assertEquals(session, repository.startedSession)
        assertEquals(exercise, repository.addedExercise)
        assertEquals(set, repository.savedSet)
        assertEquals(set, repository.updatedSet)
        assertEquals(set.id, repository.deletedSetId)
    }

    @Test
    fun finishUseCases_applyStatusEndTimeAndDuration() = runBlocking {
        val repository = RecordingWorkoutRepository()
        val session = workoutSession(id = 3)
        repository.updateSessionResult = AppResult.Success(Unit)

        CompleteWorkoutSessionUseCase(repository)(session, endedAt, perceivedEnergy = 4, notes = "Good")

        assertEquals(SessionStatus.FINISHED, repository.updatedSession?.status)
        assertEquals(endedAt, repository.updatedSession?.endedAt)
        assertEquals(4_500L, repository.updatedSession?.durationSeconds)
        assertEquals(4, repository.updatedSession?.perceivedEnergy)
        assertEquals("Good", repository.updatedSession?.notes)

        CancelWorkoutSessionUseCase(repository)(session, endedAt)

        assertEquals(SessionStatus.CANCELLED, repository.updatedSession?.status)
        assertEquals(4_500L, repository.updatedSession?.durationSeconds)
    }

    @Test
    fun observeAndLastPerformanceUseCases_exposeRepositoryData() = runBlocking {
        val detail = WorkoutSessionDetail(workoutSession(id = 1), emptyList())
        val performance = VariantLastPerformance(workoutSession(id = 1), workoutExercise(), listOf(workoutSet()))
        val repository = RecordingWorkoutRepository().apply {
            activeSession = flowOf(detail)
            sessionDetail = flowOf(detail)
            lastPerformanceResult = AppResult.Success(performance)
        }

        assertEquals(detail, ObserveActiveSessionUseCase(repository)().first())
        assertEquals(detail, ObserveSessionDetailUseCase(repository)(detail.session.id).first())
        assertSame(repository.lastPerformanceResult, GetVariantLastPerformanceUseCase(repository)(7))
        assertEquals(7L, repository.lastPerformanceVariantId)
    }

    @Test
    fun observeVariantHistoryUseCase_exposesOnlyTheRequestedVariantHistory() = runBlocking {
        val performance = VariantLastPerformance(workoutSession(id = 1), workoutExercise(), listOf(workoutSet()))
        val repository = RecordingWorkoutRepository().apply { variantHistory = flowOf(listOf(performance)) }

        assertEquals(listOf(performance), ObserveVariantHistoryUseCase(repository)(7).first())
        assertEquals(7L, repository.variantHistoryVariantId)
    }

    @Test
    fun restUseCases_startRestAndResolveElapsedTimeForNextSet() {
        val savedSet = StartRestAfterSetUseCase()(workoutSet(), startedAt)
        val nextSetStartedAt = startedAt.plusSeconds(95)

        val resolution = ResolveRestBeforeNextSetUseCase()(savedSet, nextSetStartedAt)

        assertEquals(startedAt, savedSet.endedAt)
        assertEquals(95L, resolution?.restBeforeSeconds)
        assertEquals(95L, resolution?.previousSet?.restAfterSeconds)
        assertEquals(nextSetStartedAt, resolution?.previousSet?.updatedAt)
    }

    private fun workoutSession(id: Long = 1) = WorkoutSession(
        id = id,
        startedAt = startedAt,
        createdAt = startedAt,
        updatedAt = startedAt,
    )

    private fun workoutExercise() = WorkoutExercise(
        id = 2,
        workoutSessionId = 1,
        exerciseVariantId = 7,
        orderIndex = 0,
        createdAt = startedAt,
        updatedAt = startedAt,
    )

    private fun workoutSet() = WorkoutSet(
        id = 4,
        workoutExerciseId = 2,
        setNumber = 1,
        weightValue = 80.0,
        reps = 8,
        createdAt = startedAt,
        updatedAt = startedAt,
    )

    private class RecordingWorkoutRepository : WorkoutRepository {
        var activeSession: Flow<WorkoutSessionDetail?> = flowOf(null)
        var sessionDetail: Flow<WorkoutSessionDetail?> = flowOf(null)
        var startResult: AppResult<Long> = AppResult.Success(0L)
        var updateSessionResult: AppResult<Unit> = AppResult.Success(Unit)
        var addExerciseResult: AppResult<Long> = AppResult.Success(0L)
        var saveSetResult: AppResult<Long> = AppResult.Success(0L)
        var updateSetResult: AppResult<Unit> = AppResult.Success(Unit)
        var deleteSetResult: AppResult<Unit> = AppResult.Success(Unit)
        var lastPerformanceResult: AppResult<VariantLastPerformance?> = AppResult.Success(null)
        var variantHistory: Flow<List<VariantLastPerformance>> = flowOf(emptyList())
        var startedSession: WorkoutSession? = null
        var updatedSession: WorkoutSession? = null
        var addedExercise: WorkoutExercise? = null
        var savedSet: WorkoutSet? = null
        var updatedSet: WorkoutSet? = null
        var deletedSetId: Long? = null
        var lastPerformanceVariantId: Long? = null
        var variantHistoryVariantId: Long? = null

        override fun observeActiveSession(): Flow<WorkoutSessionDetail?> = activeSession
        override fun observeSessionDetail(sessionId: Long): Flow<WorkoutSessionDetail?> = sessionDetail
        override fun observeSessionHistory(limit: Int): Flow<List<WorkoutSession>> = flowOf(emptyList())
        override suspend fun findSessionDetailById(sessionId: Long): AppResult<WorkoutSessionDetail?> = AppResult.Success(null)

        override suspend fun findVariantLastPerformance(variantId: Long): AppResult<VariantLastPerformance?> {
            lastPerformanceVariantId = variantId
            return lastPerformanceResult
        }

        override fun observeVariantHistory(variantId: Long): Flow<List<VariantLastPerformance>> {
            variantHistoryVariantId = variantId
            return variantHistory
        }

        override suspend fun startSession(session: WorkoutSession): AppResult<Long> {
            startedSession = session
            return startResult
        }

        override suspend fun updateSession(session: WorkoutSession): AppResult<Unit> {
            updatedSession = session
            return updateSessionResult
        }

        override suspend fun addExerciseToSession(workoutExercise: WorkoutExercise): AppResult<Long> {
            addedExercise = workoutExercise
            return addExerciseResult
        }

        override suspend fun saveWorkoutSet(workoutSet: WorkoutSet): AppResult<Long> {
            savedSet = workoutSet
            return saveSetResult
        }

        override suspend fun updateWorkoutSet(workoutSet: WorkoutSet): AppResult<Unit> {
            updatedSet = workoutSet
            return updateSetResult
        }

        override suspend fun deleteWorkoutSet(setId: Long): AppResult<Unit> {
            deletedSetId = setId
            return deleteSetResult
        }
    }
}
