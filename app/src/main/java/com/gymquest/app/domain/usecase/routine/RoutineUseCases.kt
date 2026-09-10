package com.gymquest.app.domain.usecase.routine

import com.gymquest.app.domain.model.WorkoutRoutineDetail
import com.gymquest.app.domain.repository.RoutineRepository
import java.time.Instant

class ObserveRoutinesUseCase(private val repository: RoutineRepository) { operator fun invoke() = repository.observeRoutines() }
class ObserveRoutineUseCase(private val repository: RoutineRepository) { operator fun invoke(routineId: Long) = repository.observeRoutine(routineId) }
class SaveRoutineUseCase(private val repository: RoutineRepository) { suspend operator fun invoke(routine: WorkoutRoutineDetail) = repository.saveRoutine(routine) }
class ArchiveRoutineUseCase(private val repository: RoutineRepository) { suspend operator fun invoke(routineId: Long, now: Instant) = repository.archiveRoutine(routineId, now) }
class StartSessionFromRoutineDayUseCase(private val repository: RoutineRepository) { suspend operator fun invoke(routineDayId: Long, startedAt: Instant) = repository.startSessionFromRoutineDay(routineDayId, startedAt) }
