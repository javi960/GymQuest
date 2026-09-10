package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymquest.app.data.local.entity.RoutineDayEntity
import com.gymquest.app.data.local.entity.RoutineExerciseEntity
import com.gymquest.app.data.local.entity.RoutinePlannedSetEntity
import com.gymquest.app.data.local.entity.WorkoutRoutineEntity
import com.gymquest.app.data.local.relation.WorkoutRoutineWithDays
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface RoutineDao {
    @Insert suspend fun insertRoutine(entity: WorkoutRoutineEntity): Long
    @Update suspend fun updateRoutine(entity: WorkoutRoutineEntity): Int
    @Insert suspend fun insertDay(entity: RoutineDayEntity): Long
    @Insert suspend fun insertExercise(entity: RoutineExerciseEntity): Long
    @Insert suspend fun insertPlannedSet(entity: RoutinePlannedSetEntity): Long

    @Query("SELECT * FROM workout_routines WHERE id = :routineId LIMIT 1")
    suspend fun getRoutine(routineId: Long): WorkoutRoutineEntity?

    @Query("SELECT * FROM routine_days WHERE id = :dayId LIMIT 1")
    suspend fun getDay(dayId: Long): RoutineDayEntity?

    @Transaction
    @Query("SELECT * FROM workout_routines WHERE isArchived = 0 ORDER BY updatedAt DESC, name COLLATE NOCASE")
    fun observeActiveRoutines(): Flow<List<WorkoutRoutineWithDays>>

    @Transaction
    @Query("SELECT * FROM workout_routines WHERE id = :routineId LIMIT 1")
    fun observeRoutine(routineId: Long): Flow<WorkoutRoutineWithDays?>

    @Transaction
    @Query("SELECT * FROM workout_routines WHERE id = :routineId LIMIT 1")
    suspend fun getRoutineWithDays(routineId: Long): WorkoutRoutineWithDays?

    @Query("DELETE FROM routine_days WHERE routineId = :routineId")
    suspend fun deleteDaysForRoutine(routineId: Long): Int

    @Query("UPDATE workout_routines SET isArchived = 1, updatedAt = :updatedAt WHERE id = :routineId AND isArchived = 0")
    suspend fun archiveRoutine(routineId: Long, updatedAt: Instant): Int
}
