package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.WorkoutRoutineDetail
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface RoutineRepository {
    fun observeRoutines(): Flow<List<WorkoutRoutineDetail>>
    fun observeRoutine(routineId: Long): Flow<WorkoutRoutineDetail?>
    suspend fun saveRoutine(routine: WorkoutRoutineDetail): AppResult<Long>
    suspend fun archiveRoutine(routineId: Long, now: Instant): AppResult<Unit>

    /** Starts an empty active session populated with the selected day's exercises. */
    suspend fun startSessionFromRoutineDay(routineDayId: Long, startedAt: Instant): AppResult<Long>
}
