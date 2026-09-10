package com.gymquest.app.feature.routine

import androidx.lifecycle.ViewModel
import com.gymquest.app.domain.model.WorkoutRoutineDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory editor suitable for previews and initial wiring. A parent can observe
 * [onRoutineSaved] and persist the emitted plan using its own data layer.
 */
class RoutineEditorViewModel(
    routines: List<WorkoutRoutineDetail> = emptyList(),
    availableExercises: List<RoutineExerciseOption> = emptyList(),
    private val onRoutineSaved: (RoutineDraft) -> Unit = {},
) : ViewModel(), RoutineStateHolder {
    private val mutableUiState = MutableStateFlow(
        RoutineUiState(routines = routines, availableExercises = availableExercises),
    )
    override val uiState: StateFlow<RoutineUiState> = mutableUiState.asStateFlow()

    override fun onAction(action: RoutineAction) {
        val current = mutableUiState.value
        when (action) {
            RoutineAction.StartCreating -> update { copy(draft = RoutineDraft()) }
            RoutineAction.CancelEditing -> update { copy(draft = null) }
            is RoutineAction.ChangeDraftName -> updateDraft { copy(name = action.name) }
            is RoutineAction.SelectDraftDay -> updateDraft { copy(selectedDay = action.day) }
            is RoutineAction.AddExercise -> updateDraft {
                val exercises = days[selectedDay].orEmpty()
                if (exercises.any { it.exercise.id == action.exercise.id }) this else copy(
                    days = days + (selectedDay to exercises + RoutineDraftExercise(action.exercise)),
                )
            }
            is RoutineAction.ChangePlannedSets -> updateDraft {
                val updatedExercises = days[selectedDay].orEmpty().map { exercise ->
                    if (exercise.exercise.id == action.exerciseId) exercise.copy(plannedSets = action.plannedSets.coerceIn(1, 20)) else exercise
                }
                copy(days = days + (selectedDay to updatedExercises))
            }
            is RoutineAction.RemoveExercise -> updateDraft {
                copy(days = days + (selectedDay to days[selectedDay].orEmpty().filterNot { it.exercise.id == action.exerciseId }))
            }
            RoutineAction.SaveDraft -> current.draft?.takeIf { it.name.trim().isNotEmpty() }?.let { draft ->
                onRoutineSaved(draft.copy(name = draft.name.trim(), days = draft.days.filterValues { it.isNotEmpty() }))
                update { copy(draft = null) }
            }
            is RoutineAction.SelectRoutine -> update { copy(selectedRoutineId = action.routineId) }
            is RoutineAction.SelectRoutineDay -> update { copy(selectedDay = action.day) }
        }
    }

    private fun update(transform: RoutineUiState.() -> RoutineUiState) {
        mutableUiState.value = mutableUiState.value.transform()
    }

    private fun updateDraft(transform: RoutineDraft.() -> RoutineDraft) {
        update { draft?.let { copy(draft = it.transform()) } ?: this }
    }
}
