package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.model.ExerciseMastery
import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.domain.model.VariantLastPerformance
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutExerciseDetail
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.repository.ProgressRepository
import com.gymquest.app.domain.repository.WorkoutRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RecalculateProgressUseCaseTest {
    private val now = Instant.parse("2026-09-07T10:00:00Z")

    @Test
    fun `recalculation is idempotent and never writes workout sets`() = runBlocking {
        val workouts = FakeWorkoutRepository(sessionDetail())
        val progress = RecordingProgressRepository()
        val useCase = RecalculateProgressUseCase(workouts, progress)

        useCase(now.plusSeconds(100))
        val firstStats = progress.stats
        val firstMastery = progress.mastery
        useCase(now)

        assertEquals(firstStats, progress.stats)
        assertEquals(firstMastery, progress.mastery)
        assertEquals(1, progress.stats?.totalWorkouts)
        assertEquals(146L, progress.stats?.totalXp)
        assertEquals(1, progress.mastery.size)
    }

    private fun sessionDetail(): WorkoutSessionDetail {
        val session = WorkoutSession(
            id = 1, startedAt = now, endedAt = now.plusSeconds(60), durationSeconds = 60,
            status = SessionStatus.FINISHED, createdAt = now, updatedAt = now,
        )
        val exercise = WorkoutExercise(id = 2, workoutSessionId = 1, exerciseVariantId = 3, orderIndex = 0, createdAt = now, updatedAt = now)
        val sets = listOf(
            WorkoutSet(id = 4, workoutExerciseId = 2, setNumber = 1, weightValue = 50.0, reps = 10, createdAt = now, updatedAt = now),
            WorkoutSet(id = 5, workoutExerciseId = 2, setNumber = 2, weightValue = 60.0, reps = 10, createdAt = now.plusSeconds(1), updatedAt = now.plusSeconds(1)),
        )
        return WorkoutSessionDetail(session, listOf(WorkoutExerciseDetail(exercise, sets)))
    }

    private class RecordingProgressRepository : ProgressRepository {
        var stats: CharacterStats? = null
        var mastery: List<ExerciseMastery> = emptyList()
        override fun observeCharacterStats(): Flow<CharacterStats> = flowOf(CharacterStats(updatedAt = Instant.EPOCH))
        override fun observeVariantMastery(variantId: Long): Flow<ExerciseMastery?> = flowOf(null)
        override fun observeProgressSummary(): Flow<ProgressSummary> = flowOf(ProgressSummary(CharacterStats(updatedAt = Instant.EPOCH)))
        override suspend fun saveCharacterStats(stats: CharacterStats): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun saveMastery(mastery: ExerciseMastery): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun replaceDerivedProgress(stats: CharacterStats, mastery: List<ExerciseMastery>): AppResult<Unit> {
            this.stats = stats
            this.mastery = mastery
            return AppResult.Success(Unit)
        }
    }

    private class FakeWorkoutRepository(private val details: List<WorkoutSessionDetail>) : WorkoutRepository {
        constructor(detail: WorkoutSessionDetail) : this(listOf(detail))
        override fun observeActiveSession(): Flow<WorkoutSessionDetail?> = flowOf(null)
        override fun observeSessionDetail(sessionId: Long): Flow<WorkoutSessionDetail?> = flowOf(null)
        override fun observeSessionHistory(limit: Int): Flow<List<WorkoutSession>> = flowOf(emptyList())
        override suspend fun findSessionDetailById(sessionId: Long): AppResult<WorkoutSessionDetail?> = AppResult.Success(null)
        override suspend fun findVariantLastPerformance(variantId: Long): AppResult<VariantLastPerformance?> = AppResult.Success(null)
        override suspend fun findCompletedSessionDetails(): AppResult<List<WorkoutSessionDetail>> = AppResult.Success(details)
        override suspend fun startSession(session: WorkoutSession): AppResult<Long> = AppResult.Success(0)
        override suspend fun updateSession(session: WorkoutSession): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun addExerciseToSession(workoutExercise: WorkoutExercise): AppResult<Long> = AppResult.Success(0)
        override suspend fun saveWorkoutSet(workoutSet: WorkoutSet): AppResult<Long> = AppResult.Success(0)
        override suspend fun updateWorkoutSet(workoutSet: WorkoutSet): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deleteWorkoutSet(setId: Long): AppResult<Unit> = AppResult.Success(Unit)
    }
}
