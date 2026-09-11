package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.gymquest.app.data.local.entity.TrainingSessionEntity
import com.gymquest.app.data.local.entity.WeeklyTrainingDayEntity
import com.gymquest.app.data.local.entity.WeeklyTrainingPlanEntity
import com.gymquest.app.data.local.entity.WeeklyTrainingExerciseEntity
import com.gymquest.app.data.local.entity.TrainingSessionExerciseEntity
import com.gymquest.app.data.local.entity.TrainingSessionSetEntity
import com.gymquest.app.domain.model.RecordedWorkingSet
import com.gymquest.app.data.local.projection.TrainingExperienceSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyTrainingDao {
    @Query("SELECT * FROM weekly_training_plans WHERE isArchived = 0 ORDER BY name")
    fun observePlans(): Flow<List<WeeklyTrainingPlanEntity>>
    @Query("SELECT * FROM training_sessions WHERE endedAt IS NOT NULL ORDER BY endedAt DESC") fun observeFinishedSessions(): Flow<List<TrainingSessionEntity>>
    @Query("SELECT * FROM training_sessions WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeLatestActiveSession(): Flow<TrainingSessionEntity?>
    @Query("SELECT (SELECT COUNT(DISTINCT sessionExercises.id) FROM training_session_exercises AS sessionExercises INNER JOIN training_session_sets AS sets ON sets.sessionExerciseId = sessionExercises.id) AS completedExercises, (SELECT COUNT(*) FROM training_sessions WHERE endedAt IS NOT NULL) AS completedSessions")
    fun observeExperienceSummary(): Flow<TrainingExperienceSummary>

    @Query("SELECT * FROM weekly_training_days WHERE planId = :planId ORDER BY sortOrder")
    fun observeDays(planId: Long): Flow<List<WeeklyTrainingDayEntity>>
    @Query("SELECT * FROM weekly_training_exercises WHERE dayId = :dayId ORDER BY sortOrder") fun observeExercises(dayId: Long): Flow<List<WeeklyTrainingExerciseEntity>>

    @Insert suspend fun insertPlan(entity: WeeklyTrainingPlanEntity): Long
    @Update suspend fun updatePlan(entity: WeeklyTrainingPlanEntity): Int
    @Query("UPDATE weekly_training_plans SET isArchived = 1, updatedAt = :updatedAt WHERE id = :planId")
    suspend fun archivePlan(planId: Long, updatedAt: java.time.Instant): Int
    @Insert suspend fun insertDay(entity: WeeklyTrainingDayEntity): Long
    @Insert suspend fun insertSession(entity: TrainingSessionEntity): Long
    @Query("SELECT COUNT(*) FROM training_session_exercises WHERE sessionId = :sessionId") suspend fun countSessionExercises(sessionId: Long): Int
    @Insert suspend fun insertSessionExercise(entity: TrainingSessionExerciseEntity): Long
    @Insert suspend fun insertExercise(entity: WeeklyTrainingExerciseEntity): Long
    @Query("SELECT * FROM weekly_training_exercises WHERE dayId = :dayId ORDER BY sortOrder") suspend fun getExercises(dayId: Long): List<WeeklyTrainingExerciseEntity>
    @Insert suspend fun insertSessionExercises(entities: List<TrainingSessionExerciseEntity>)
    @Query("SELECT * FROM training_session_exercises WHERE sessionId = :sessionId ORDER BY sortOrder") fun observeSessionExercises(sessionId: Long): Flow<List<TrainingSessionExerciseEntity>>
    @Query("SELECT * FROM training_session_sets WHERE sessionExerciseId = :exerciseId ORDER BY setNumber") fun observeSets(exerciseId: Long): Flow<List<TrainingSessionSetEntity>>
    @Insert suspend fun insertSet(entity: TrainingSessionSetEntity): Long
    @Update suspend fun updateSet(entity: TrainingSessionSetEntity): Int
    @Query("DELETE FROM training_session_sets WHERE id = :setId") suspend fun deleteSet(setId: Long): Int
    @Query("UPDATE training_sessions SET endedAt = :endedAt, updatedAt = :endedAt WHERE id = :sessionId AND endedAt IS NULL") suspend fun finishSession(sessionId: Long, endedAt: java.time.Instant): Int
    @Query("SELECT sets.weight AS weight, sets.reps AS reps, sets.completedAt AS completedAt FROM training_session_sets AS sets INNER JOIN training_session_exercises AS exercises ON exercises.id = sets.sessionExerciseId WHERE exercises.exerciseVariantId = :variantId AND sets.setType = 'Efectiva' ORDER BY sets.completedAt DESC LIMIT 1")
    suspend fun getLatestEffectiveSetForVariant(variantId: Long): RecordedWorkingSet?
    @Query("SELECT sets.weight AS weight, sets.reps AS reps, sets.completedAt AS completedAt FROM training_session_sets AS sets INNER JOIN training_session_exercises AS loggedExercise ON loggedExercise.id = sets.sessionExerciseId INNER JOIN exercise_variants AS loggedVariant ON loggedVariant.id = loggedExercise.exerciseVariantId INNER JOIN exercise_variants AS targetVariant ON targetVariant.id = :variantId WHERE loggedVariant.exerciseBaseId = targetVariant.exerciseBaseId AND loggedVariant.weightComparisonType = targetVariant.weightComparisonType AND loggedVariant.id != targetVariant.id AND sets.setType = 'Efectiva' ORDER BY sets.completedAt DESC LIMIT 1")
    suspend fun getLatestComparableSetForVariant(variantId: Long): RecordedWorkingSet?
    @Transaction suspend fun startSessionFromDay(session: TrainingSessionEntity): Long {
        val sessionId = insertSession(session)
        insertSessionExercises(getExercises(requireNotNull(session.plannedDayId)).map { planned -> TrainingSessionExerciseEntity(sessionId = sessionId, exerciseVariantId = planned.exerciseVariantId, sortOrder = planned.sortOrder, plannedSets = planned.targetSets, plannedReps = planned.targetReps, plannedWeight = planned.targetWeight, plannedRestSeconds = planned.restSeconds, createdAt = session.createdAt, updatedAt = session.updatedAt) })
        return sessionId
    }
}
