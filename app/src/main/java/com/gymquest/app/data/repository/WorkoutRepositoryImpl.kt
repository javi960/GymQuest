package com.gymquest.app.data.repository

import androidx.room.withTransaction
import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.validation.TextValidators
import com.gymquest.app.core.validation.ValidationResult
import com.gymquest.app.core.validation.WorkoutValidators
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.data.mapper.WorkoutMapper
import com.gymquest.app.domain.model.VariantLastPerformance
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl(
    private val database: GymQuestDatabase,
) : WorkoutRepository {
    private val workoutDao = database.workoutDao()

    override fun observeActiveSession(): Flow<WorkoutSessionDetail?> =
        workoutDao.observeActiveSessionWithExercises().map { relation ->
            relation?.let(WorkoutMapper::toSessionDetail)
        }

    override fun observeSessionDetail(sessionId: Long): Flow<WorkoutSessionDetail?> =
        workoutDao.observeSessionWithExercises(sessionId).map { relation ->
            relation?.let(WorkoutMapper::toSessionDetail)
        }

    override fun observeSessionHistory(limit: Int): Flow<List<WorkoutSession>> =
        workoutDao.observeSessions(limit.coerceAtLeast(1)).map { entities ->
            entities.map(WorkoutMapper::toDomain)
        }

    override suspend fun findSessionDetailById(sessionId: Long): AppResult<WorkoutSessionDetail?> =
        validateId(sessionId, "sessionId") {
            workoutDao.getSessionWithExercises(sessionId)?.let(WorkoutMapper::toSessionDetail)
        }

    override suspend fun findVariantLastPerformance(variantId: Long): AppResult<VariantLastPerformance?> =
        validateId(variantId, "variantId") {
            val exerciseRelation = workoutDao.getLastExerciseWithSetsForVariant(
                variantId = variantId,
                sessionStatus = SessionStatus.FINISHED,
            ) ?: return@validateId null
            val exercise = WorkoutMapper.toDomain(exerciseRelation.workoutExercise)
            val session = workoutDao.getSession(exercise.workoutSessionId)
                ?: throw NoSuchElementException("La sesion del ultimo rendimiento no existe.")
            VariantLastPerformance(
                session = WorkoutMapper.toDomain(session),
                exercise = exercise,
                sets = exerciseRelation.sets.sortedBy { it.setNumber }.map(WorkoutMapper::toDomain),
            )
        }

    override suspend fun startSession(session: WorkoutSession): AppResult<Long> =
        validateSession(session, requireExistingId = false) {
            require(session.status == SessionStatus.ACTIVE) { "La nueva sesion debe empezar activa." }
            require(session.endedAt == null) { "La nueva sesion activa no puede tener fecha de fin." }
            database.withTransaction {
                require(workoutDao.countActiveSessions() == 0) { "Ya hay una sesion activa." }
                workoutDao.insertSession(WorkoutMapper.toEntity(session))
            }
        }

    override suspend fun updateSession(session: WorkoutSession): AppResult<Unit> =
        validateSession(session, requireExistingId = true) {
            if (workoutDao.updateSession(WorkoutMapper.toEntity(session)) == 0) {
                throw NoSuchElementException("La sesion no existe.")
            }
            Unit
        }

    override suspend fun addExerciseToSession(workoutExercise: WorkoutExercise): AppResult<Long> =
        validateWorkoutExercise(workoutExercise, requireExistingId = false) {
            val session = workoutDao.getSession(workoutExercise.workoutSessionId)
                ?: throw NoSuchElementException("La sesion no existe.")
            require(session.status == SessionStatus.ACTIVE && session.endedAt == null) {
                "Solo se pueden anadir ejercicios a una sesion activa."
            }
            workoutDao.insertWorkoutExercise(WorkoutMapper.toEntity(workoutExercise))
        }

    override suspend fun saveWorkoutSet(workoutSet: WorkoutSet): AppResult<Long> =
        validateWorkoutSet(workoutSet, requireExistingId = false) {
            val workoutExercise = workoutDao.getWorkoutExercise(workoutSet.workoutExerciseId)
                ?: throw NoSuchElementException("El ejercicio de sesion no existe.")
            val session = workoutDao.getSession(workoutExercise.workoutSessionId)
                ?: throw NoSuchElementException("La sesion no existe.")
            require(session.status == SessionStatus.ACTIVE && session.endedAt == null) {
                "Solo se pueden guardar series en una sesion activa."
            }
            workoutDao.insertWorkoutSet(WorkoutMapper.toEntity(workoutSet))
        }

    override suspend fun updateWorkoutSet(workoutSet: WorkoutSet): AppResult<Unit> =
        validateWorkoutSet(workoutSet, requireExistingId = true) {
            if (workoutDao.updateWorkoutSet(WorkoutMapper.toEntity(workoutSet)) == 0) {
                throw NoSuchElementException("La serie no existe.")
            }
            Unit
        }

    override suspend fun deleteWorkoutSet(setId: Long): AppResult<Unit> =
        validateId(setId, "setId") {
            if (workoutDao.deleteWorkoutSet(setId) == 0) {
                throw NoSuchElementException("La serie no existe.")
            }
            Unit
        }

    private suspend fun <T> validateSession(
        session: WorkoutSession,
        requireExistingId: Boolean,
        operation: suspend () -> T,
    ): AppResult<T> {
        if (requireExistingId && session.id <= 0) {
            return AppResult.Failure(AppError.Validation("La sesion debe existir para actualizarse."))
        }
        return validate(
            WorkoutValidators.validateSession(session),
            TextValidators.optional(session.notes, "notes"),
            operation,
        )
    }

    private suspend fun <T> validateWorkoutExercise(
        workoutExercise: WorkoutExercise,
        requireExistingId: Boolean,
        operation: suspend () -> T,
    ): AppResult<T> {
        val checks = listOf(
            (workoutExercise.workoutSessionId > 0) to "El ejercicio debe pertenecer a una sesion.",
            (workoutExercise.exerciseVariantId > 0) to "El ejercicio debe apuntar a una variante.",
            (workoutExercise.orderIndex >= 0) to "El orden del ejercicio no puede ser negativo.",
            (!requireExistingId || workoutExercise.id > 0) to "El ejercicio de sesion debe existir para actualizarse.",
            (!workoutExercise.updatedAt.isBefore(workoutExercise.createdAt)) to "La fecha de actualizacion no puede ser anterior a la creacion.",
        )
        val failedCheck = checks.firstOrNull { !it.first }
        if (failedCheck != null) return AppResult.Failure(AppError.Validation(failedCheck.second))
        return validate(TextValidators.optional(workoutExercise.notes, "notes"), operation)
    }

    private suspend fun <T> validateWorkoutSet(
        workoutSet: WorkoutSet,
        requireExistingId: Boolean,
        operation: suspend () -> T,
    ): AppResult<T> {
        if (requireExistingId && workoutSet.id <= 0) {
            return AppResult.Failure(AppError.Validation("La serie debe existir para actualizarse."))
        }
        return validate(
            WorkoutValidators.validateSet(workoutSet),
            TextValidators.optional(workoutSet.notes, "notes"),
            operation,
        )
    }

    private suspend fun <T> validate(
        validation: ValidationResult,
        notesValidation: ValidationResult,
        operation: suspend () -> T,
    ): AppResult<T> {
        if (validation is ValidationResult.Invalid) {
            return AppResult.Failure(AppError.Validation(validation.errors.first().message))
        }
        return validate(notesValidation, operation)
    }

    private suspend fun <T> validate(
        validation: ValidationResult,
        operation: suspend () -> T,
    ): AppResult<T> {
        if (validation is ValidationResult.Invalid) {
            return AppResult.Failure(AppError.Validation(validation.errors.first().message))
        }
        return runStorageOperation(operation)
    }

    private suspend fun <T> validateId(
        id: Long,
        field: String,
        operation: suspend () -> T,
    ): AppResult<T> {
        if (id <= 0) return AppResult.Failure(AppError.Validation("El identificador $field debe ser positivo."))
        return runStorageOperation(operation)
    }

    private suspend fun <T> runStorageOperation(operation: suspend () -> T): AppResult<T> =
        try {
            AppResult.Success(operation())
        } catch (error: NoSuchElementException) {
            AppResult.Failure(AppError.NotFound(error.message ?: "No se encontro el dato solicitado."))
        } catch (error: IllegalArgumentException) {
            AppResult.Failure(AppError.Validation(error.message ?: "Los datos de la sesion no son validos."))
        } catch (error: Exception) {
            AppResult.Failure(AppError.Storage("No se pudieron guardar los datos de entrenamiento.", error))
        }
}
