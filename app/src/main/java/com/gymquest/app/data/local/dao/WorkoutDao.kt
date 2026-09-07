package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity
import com.gymquest.app.data.local.entity.WorkoutSetEntity
import com.gymquest.app.data.local.relation.WorkoutExerciseWithSets
import com.gymquest.app.data.local.relation.WorkoutSessionWithExercises
import com.gymquest.app.domain.model.enums.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertSession(entity: WorkoutSessionEntity): Long

    @Insert
    suspend fun insertWorkoutExercise(entity: WorkoutExerciseEntity): Long

    @Insert
    suspend fun insertWorkoutSet(entity: WorkoutSetEntity): Long

    @Update
    suspend fun updateSession(entity: WorkoutSessionEntity): Int

    @Update
    suspend fun updateWorkoutExercise(entity: WorkoutExerciseEntity): Int

    @Update
    suspend fun updateWorkoutSet(entity: WorkoutSetEntity): Int

    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' AND endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionEntity?

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE status = 'ACTIVE' AND endedAt IS NULL")
    suspend fun countActiveSessions(): Int

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_exercises WHERE id = :workoutExerciseId LIMIT 1")
    suspend fun getWorkoutExercise(workoutExerciseId: Long): WorkoutExerciseEntity?

    @Query("SELECT * FROM workout_sets WHERE id = :setId LIMIT 1")
    suspend fun getWorkoutSet(setId: Long): WorkoutSetEntity?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionWithExercises(sessionId: Long): WorkoutSessionWithExercises?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    fun observeSessionWithExercises(sessionId: Long): Flow<WorkoutSessionWithExercises?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' AND endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSessionWithExercises(): WorkoutSessionWithExercises?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' AND endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveSessionWithExercises(): Flow<WorkoutSessionWithExercises?>

    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC LIMIT :limit")
    fun observeSessions(limit: Int): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_exercises WHERE workoutSessionId = :sessionId ORDER BY orderIndex")
    suspend fun getWorkoutExercisesForSession(sessionId: Long): List<WorkoutExerciseEntity>

    @Query("SELECT * FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber")
    suspend fun getWorkoutSetsForExercise(workoutExerciseId: Long): List<WorkoutSetEntity>

    @Transaction
    @Query(
        """
        SELECT workout_exercises.*
        FROM workout_exercises
        INNER JOIN workout_sessions ON workout_sessions.id = workout_exercises.workoutSessionId
        WHERE workout_exercises.exerciseVariantId = :variantId
            AND workout_sessions.status = :sessionStatus
        ORDER BY workout_sessions.startedAt DESC, workout_exercises.orderIndex DESC
        LIMIT 1
        """,
    )
    suspend fun getLastExerciseWithSetsForVariant(
        variantId: Long,
        sessionStatus: SessionStatus = SessionStatus.FINISHED,
    ): WorkoutExerciseWithSets?

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteWorkoutSet(setId: Long): Int
}
