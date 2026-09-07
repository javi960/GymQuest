package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.VariantLastPerformance
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeActiveSession(): Flow<WorkoutSessionDetail?>

    fun observeSessionDetail(sessionId: Long): Flow<WorkoutSessionDetail?>

    fun observeSessionHistory(limit: Int = DEFAULT_HISTORY_LIMIT): Flow<List<WorkoutSession>>

    suspend fun findSessionDetailById(sessionId: Long): AppResult<WorkoutSessionDetail?>

    suspend fun findVariantLastPerformance(variantId: Long): AppResult<VariantLastPerformance?>

    suspend fun startSession(session: WorkoutSession): AppResult<Long>

    suspend fun updateSession(session: WorkoutSession): AppResult<Unit>

    suspend fun addExerciseToSession(workoutExercise: WorkoutExercise): AppResult<Long>

    suspend fun saveWorkoutSet(workoutSet: WorkoutSet): AppResult<Long>

    suspend fun updateWorkoutSet(workoutSet: WorkoutSet): AppResult<Unit>

    suspend fun deleteWorkoutSet(setId: Long): AppResult<Unit>

    companion object {
        const val DEFAULT_HISTORY_LIMIT = 50
    }
}
