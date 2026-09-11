package com.gymquest.app.data.repository

import com.gymquest.app.data.local.dao.WeeklyTrainingDao
import com.gymquest.app.data.local.entity.TrainingSessionEntity
import com.gymquest.app.data.local.entity.WeeklyTrainingDayEntity
import com.gymquest.app.data.local.entity.WeeklyTrainingPlanEntity
import com.gymquest.app.data.local.entity.WeeklyTrainingExerciseEntity
import com.gymquest.app.data.local.entity.TrainingSessionSetEntity
import com.gymquest.app.data.local.entity.TrainingSessionExerciseEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

class WeeklyTrainingRepository(private val dao: WeeklyTrainingDao) {
    fun observePlans(): Flow<List<WeeklyTrainingPlanEntity>> = dao.observePlans()
    fun observeFinishedSessions() = dao.observeFinishedSessions()
    fun observeLatestActiveSession() = dao.observeLatestActiveSession()
    fun observeExperienceSummary() = dao.observeExperienceSummary()
    fun observeDays(planId: Long): Flow<List<WeeklyTrainingDayEntity>> = dao.observeDays(planId)
    fun observeExercises(dayId: Long): Flow<List<WeeklyTrainingExerciseEntity>> = dao.observeExercises(dayId)
    fun observeSessionExercises(sessionId: Long) = dao.observeSessionExercises(sessionId)
    fun observeSets(exerciseId: Long) = dao.observeSets(exerciseId)
    suspend fun latestEffectiveSet(variantId: Long) = dao.getLatestEffectiveSetForVariant(variantId)
    suspend fun latestComparableSet(variantId: Long) = dao.getLatestComparableSetForVariant(variantId)
    suspend fun addPlan(name: String, notes: String?, now: Instant) = dao.insertPlan(
        WeeklyTrainingPlanEntity(name = name.trim(), notes = notes?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now),
    )
    suspend fun updatePlan(plan: WeeklyTrainingPlanEntity, name: String, notes: String?, now: Instant) =
        dao.updatePlan(plan.copy(name = name.trim(), notes = notes?.trim()?.ifBlank { null }, updatedAt = now))
    suspend fun archivePlan(planId: Long, now: Instant) = dao.archivePlan(planId, now)
    suspend fun addDay(planId: Long, key: String, label: String?, order: Int, now: Instant) = dao.insertDay(
        WeeklyTrainingDayEntity(planId = planId, dayKey = key, label = label?.trim()?.ifBlank { null }, sortOrder = order, createdAt = now, updatedAt = now),
    )
    suspend fun startSession(plan: WeeklyTrainingPlanEntity, day: WeeklyTrainingDayEntity, now: Instant) = dao.startSessionFromDay(
        TrainingSessionEntity(planId = plan.id, plannedDayId = day.id, planNameSnapshot = plan.name, plannedDaySnapshot = day.label ?: day.dayKey, startedAt = now, createdAt = now, updatedAt = now),
    )
    suspend fun startFreeSession(now: Instant) = dao.insertSession(TrainingSessionEntity(startedAt = now, createdAt = now, updatedAt = now))
    suspend fun addExerciseToSession(sessionId: Long, variantId: Long, now: Instant) = dao.insertSessionExercise(TrainingSessionExerciseEntity(sessionId = sessionId, exerciseVariantId = variantId, sortOrder = dao.countSessionExercises(sessionId), plannedSets = 0, createdAt = now, updatedAt = now))
    suspend fun addExercise(dayId: Long, variantId: Long, order: Int, sets: Int, reps: Int?, weight: Double?, rest: Int?, now: Instant) = dao.insertExercise(
        WeeklyTrainingExerciseEntity(dayId = dayId, exerciseVariantId = variantId, sortOrder = order, targetSets = sets, targetReps = reps, targetWeight = weight, restSeconds = rest, createdAt = now, updatedAt = now),
    )
    suspend fun addSet(exerciseId: Long, number: Int, weight: Double, reps: Int, type: String, now: Instant) = dao.insertSet(TrainingSessionSetEntity(sessionExerciseId = exerciseId, setNumber = number, weight = weight, reps = reps, setType = type, completedAt = now, createdAt = now, updatedAt = now))
    suspend fun updateSet(set: TrainingSessionSetEntity, weight: Double, reps: Int, type: String, now: Instant) = dao.updateSet(set.copy(weight = weight, reps = reps, setType = type, updatedAt = now))
    suspend fun deleteSet(setId: Long) = dao.deleteSet(setId)
    suspend fun finishSession(sessionId: Long, now: Instant) = dao.finishSession(sessionId, now)
}
