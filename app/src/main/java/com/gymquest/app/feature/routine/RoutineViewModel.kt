package com.gymquest.app.feature.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.time.ClockProvider
import com.gymquest.app.domain.model.RoutineDay
import com.gymquest.app.domain.model.RoutineDayDetail
import com.gymquest.app.domain.model.RoutineExercise
import com.gymquest.app.domain.model.RoutineExerciseDetail
import com.gymquest.app.domain.model.RoutinePlannedSet
import com.gymquest.app.domain.model.WorkoutRoutine
import com.gymquest.app.domain.model.WorkoutRoutineDetail
import com.gymquest.app.domain.usecase.catalog.ObserveExerciseCatalogUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveMuscleGroupsUseCase
import com.gymquest.app.domain.usecase.routine.ObserveRoutinesUseCase
import com.gymquest.app.domain.usecase.routine.SaveRoutineUseCase
import com.gymquest.app.domain.usecase.routine.StartSessionFromRoutineDayUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineViewModel(
    observeRoutines: ObserveRoutinesUseCase,
    observeExerciseCatalog: ObserveExerciseCatalogUseCase,
    observeMuscleGroups: ObserveMuscleGroupsUseCase,
    private val saveRoutine: SaveRoutineUseCase,
    private val startSessionFromRoutineDay: StartSessionFromRoutineDayUseCase,
    private val clock: ClockProvider,
) : ViewModel(), RoutineStateHolder {
    private val mutableUiState = MutableStateFlow(RoutineUiState())
    override val uiState: StateFlow<RoutineUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(observeRoutines(), observeExerciseCatalog(), observeMuscleGroups()) { routines, catalog, muscleGroups ->
                val muscleNames = muscleGroups.associate { it.id to it.name }
                routines to catalog.flatMap { entry ->
                    entry.variants.map { variant ->
                        RoutineExerciseOption(
                            id = variant.id.toString(),
                            name = variant.name,
                            muscleGroupName = muscleNames[entry.exerciseBase.primaryMuscleGroupId] ?: "Sin grupo",
                            equipmentType = variant.equipmentType,
                        )
                    }
                }
            }.collect { (routines, exercises) ->
                mutableUiState.update { state ->
                    state.copy(
                        routines = routines,
                        availableExercises = exercises,
                        selectedRoutineId = state.selectedRoutineId?.takeIf { id -> routines.any { it.routine.id.toString() == id } }
                            ?: routines.firstOrNull()?.routine?.id?.toString(),
                    )
                }
            }
        }
    }

    override fun onAction(action: RoutineAction) {
        when (action) {
            RoutineAction.StartCreating -> mutableUiState.update { it.copy(draft = RoutineDraft()) }
            RoutineAction.CancelEditing -> mutableUiState.update { it.copy(draft = null) }
            is RoutineAction.ChangeDraftName -> updateDraft { copy(name = action.name) }
            is RoutineAction.SelectDraftDay -> updateDraft { copy(selectedDay = action.day) }
            is RoutineAction.AddExercise -> updateDraft {
                val exercises = days[selectedDay].orEmpty()
                if (exercises.any { it.exercise.id == action.exercise.id }) this else copy(
                    days = days + (selectedDay to exercises + RoutineDraftExercise(action.exercise)),
                )
            }
            is RoutineAction.ChangePlannedSets -> updateDraft {
                copy(days = days + (selectedDay to days[selectedDay].orEmpty().map { exercise ->
                    if (exercise.exercise.id == action.exerciseId) exercise.copy(plannedSets = action.plannedSets.coerceIn(1, 20)) else exercise
                }))
            }
            is RoutineAction.RemoveExercise -> updateDraft {
                copy(days = days + (selectedDay to days[selectedDay].orEmpty().filterNot { it.exercise.id == action.exerciseId }))
            }
            RoutineAction.SaveDraft -> saveDraft()
            is RoutineAction.SelectRoutine -> mutableUiState.update { it.copy(selectedRoutineId = action.routineId) }
            is RoutineAction.SelectRoutineDay -> mutableUiState.update { it.copy(selectedDay = action.day) }
        }
    }

    fun useRoutineDay(routine: WorkoutRoutineDetail, weekday: com.gymquest.app.domain.model.enums.Weekday, onStarted: (Boolean) -> Unit) {
        val dayId = routine.days.firstOrNull { it.day.weekday == weekday }?.day?.id ?: return onStarted(false)
        viewModelScope.launch { onStarted(startSessionFromRoutineDay(dayId, clock.now()) is AppResult.Success) }
    }

    private fun saveDraft() {
        val draft = mutableUiState.value.draft?.takeIf { it.name.trim().isNotEmpty() } ?: return
        val now = clock.now()
        val detail = WorkoutRoutineDetail(
            routine = WorkoutRoutine(name = draft.name.trim(), createdAt = now, updatedAt = now),
            days = draft.days.filterValues { it.isNotEmpty() }.map { (weekday, exercises) ->
                RoutineDayDetail(
                    day = RoutineDay(routineId = 0, weekday = weekday, createdAt = now, updatedAt = now),
                    exercises = exercises.mapIndexed { index, item ->
                        RoutineExerciseDetail(
                            exercise = RoutineExercise(routineDayId = 0, exerciseVariantId = item.exercise.id.toLong(), orderIndex = index, createdAt = now, updatedAt = now),
                            plannedSets = (1..item.plannedSets).map { setNumber ->
                                RoutinePlannedSet(routineExerciseId = 0, setNumber = setNumber, createdAt = now, updatedAt = now)
                            },
                        )
                    },
                )
            },
        )
        viewModelScope.launch {
            if (saveRoutine(detail) is AppResult.Success) mutableUiState.update { it.copy(draft = null) }
        }
    }

    private fun updateDraft(transform: RoutineDraft.() -> RoutineDraft) {
        mutableUiState.update { state -> state.draft?.let { state.copy(draft = it.transform()) } ?: state }
    }
}
