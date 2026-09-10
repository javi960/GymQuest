package com.gymquest.app.data.repository

import androidx.room.withTransaction
import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.data.mapper.RoutineMapper
import com.gymquest.app.data.mapper.WorkoutMapper
import com.gymquest.app.data.local.relation.RoutineExerciseWithPlannedSets
import com.gymquest.app.domain.model.RoutineDayDetail
import com.gymquest.app.domain.model.RoutineExerciseDetail
import com.gymquest.app.domain.model.WorkoutRoutineDetail
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class RoutineRepositoryImpl(private val database: GymQuestDatabase) : RoutineRepository {
    private val routineDao = database.routineDao()
    private val workoutDao = database.workoutDao()
    private val exerciseDao = database.exerciseDao()
    private val machineDao = database.machineDao()

    override fun observeRoutines(): Flow<List<WorkoutRoutineDetail>> =
        routineDao.observeActiveRoutines().map { it.map(RoutineMapper::toDetail) }

    override fun observeRoutine(routineId: Long): Flow<WorkoutRoutineDetail?> =
        routineDao.observeRoutine(routineId).map { it?.let(RoutineMapper::toDetail) }

    override suspend fun saveRoutine(routine: WorkoutRoutineDetail): AppResult<Long> = guarded {
        validateRoutine(routine)
        database.withTransaction {
            val routineId = if (routine.routine.id == 0L) {
                routineDao.insertRoutine(RoutineMapper.toEntity(routine.routine))
            } else {
                requireNotNull(routineDao.getRoutine(routine.routine.id)) { "La rutina no existe." }
                require(routineDao.updateRoutine(RoutineMapper.toEntity(routine.routine)) == 1) { "No se pudo actualizar la rutina." }
                routineDao.deleteDaysForRoutine(routine.routine.id)
                routine.routine.id
            }
            routine.days.forEach { day -> insertDayTree(routineId, day) }
            routineId
        }
    }

    override suspend fun archiveRoutine(routineId: Long, now: Instant): AppResult<Unit> = guarded {
        require(routineId > 0) { "La rutina debe existir." }
        require(routineDao.archiveRoutine(routineId, now) == 1) { "La rutina no existe o ya está archivada." }
    }

    override suspend fun startSessionFromRoutineDay(routineDayId: Long, startedAt: Instant): AppResult<Long> = guarded {
        require(routineDayId > 0) { "El día de rutina debe existir." }
        database.withTransaction {
            require(workoutDao.countActiveSessions() == 0) { "Ya hay una sesión activa." }
            val day = requireNotNull(routineDao.getDay(routineDayId)) { "El día de rutina no existe." }
            val routine = requireNotNull(routineDao.getRoutine(day.routineId)) { "La rutina no existe." }
            require(!routine.isArchived) { "No se puede iniciar una rutina archivada." }
            val detail = requireNotNull(routineDao.getRoutineWithDays(day.routineId)) { "La rutina no existe." }
            val selectedDay = requireNotNull(detail.days.firstOrNull { it.day.id == routineDayId }) { "El día de rutina no existe." }
            for (planned in selectedDay.exercises.sortedBy { it.exercise.orderIndex }) {
                validateExerciseIsUsable(planned)
            }
            val sessionId = workoutDao.insertSession(
                WorkoutMapper.toEntity(WorkoutSession(startedAt = startedAt, createdAt = startedAt, updatedAt = startedAt)),
            )
            selectedDay.exercises.sortedBy { it.exercise.orderIndex }.forEach { planned ->
                val exercise = planned.exercise
                workoutDao.insertWorkoutExercise(
                    WorkoutMapper.toEntity(
                        WorkoutExercise(
                            workoutSessionId = sessionId,
                            exerciseVariantId = exercise.exerciseVariantId,
                            gymMachineId = exercise.gymMachineId,
                            orderIndex = exercise.orderIndex,
                            notes = exercise.notes,
                            createdAt = startedAt,
                            updatedAt = startedAt,
                        ),
                    ),
                )
            }
            sessionId
        }
    }

    private suspend fun insertDayTree(routineId: Long, detail: RoutineDayDetail) {
        val day = detail.day
        val dayId = routineDao.insertDay(RoutineMapper.toEntity(day.copy(id = 0, routineId = routineId)))
        detail.exercises.forEach { insertExerciseTree(dayId, it) }
    }

    private suspend fun insertExerciseTree(dayId: Long, detail: RoutineExerciseDetail) {
        val exercise = detail.exercise
        validateExerciseReference(exercise.exerciseVariantId, exercise.gymMachineId)
        val exerciseId = routineDao.insertExercise(RoutineMapper.toEntity(exercise.copy(id = 0, routineDayId = dayId)))
        detail.plannedSets.forEach { plannedSet ->
            routineDao.insertPlannedSet(RoutineMapper.toEntity(plannedSet.copy(id = 0, routineExerciseId = exerciseId)))
        }
    }

    private suspend fun validateExerciseIsUsable(detail: RoutineExerciseWithPlannedSets) =
        validateExerciseReference(detail.exercise.exerciseVariantId, detail.exercise.gymMachineId)

    private suspend fun validateExerciseReference(variantId: Long, machineId: Long?) {
        val variant = requireNotNull(exerciseDao.getExerciseVariant(variantId)) { "La variante de ejercicio no existe." }
        require(!variant.isArchived) { "La variante de ejercicio está archivada." }
        val base = requireNotNull(exerciseDao.getExerciseBase(variant.exerciseBaseId)) { "El ejercicio base no existe." }
        require(!base.isArchived) { "El ejercicio base está archivado." }
        machineId?.let {
            val machine = requireNotNull(machineDao.getMachineById(it)) { "La máquina no existe." }
            require(!machine.isArchived) { "La máquina está archivada." }
        }
    }

    private fun validateRoutine(detail: WorkoutRoutineDetail) {
        require(detail.routine.name.isNotBlank()) { "La rutina debe tener nombre." }
        require(!detail.routine.updatedAt.isBefore(detail.routine.createdAt)) { "La fecha de actualización no es válida." }
        val weekdays = detail.days.map { it.day.weekday }
        require(weekdays.distinct().size == weekdays.size) { "No puede haber dos días para la misma jornada." }
        detail.days.forEach { day ->
            require(!day.day.updatedAt.isBefore(day.day.createdAt)) { "La fecha del día no es válida." }
            val orders = day.exercises.map { it.exercise.orderIndex }
            require(orders.all { it >= 0 } && orders.distinct().size == orders.size) { "El orden de ejercicios no es válido." }
            day.exercises.forEach { exercise ->
                require(exercise.exercise.exerciseVariantId > 0) { "Cada ejercicio debe tener una variante." }
                val setNumbers = exercise.plannedSets.map { it.setNumber }
                require(setNumbers.all { it > 0 } && setNumbers.distinct().size == setNumbers.size) { "Las series planificadas no son válidas." }
                exercise.plannedSets.forEach { set ->
                    require(set.targetWeight == null || set.targetWeight >= 0) { "El peso objetivo no puede ser negativo." }
                    require(set.targetReps == null || set.targetReps > 0) { "Las repeticiones objetivo deben ser positivas." }
                }
            }
        }
    }

    private suspend fun <T> guarded(operation: suspend () -> T): AppResult<T> = try {
        AppResult.Success(operation())
    } catch (error: NoSuchElementException) {
        AppResult.Failure(AppError.NotFound(error.message ?: "No se encontró el dato solicitado."))
    } catch (error: IllegalArgumentException) {
        AppResult.Failure(AppError.Validation(error.message ?: "Los datos de la rutina no son válidos."))
    } catch (error: Exception) {
        AppResult.Failure(AppError.Storage("No se pudieron guardar los datos de la rutina.", error))
    }
}
