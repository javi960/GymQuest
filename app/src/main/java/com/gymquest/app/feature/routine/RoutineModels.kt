package com.gymquest.app.feature.routine

import com.gymquest.app.domain.model.WorkoutRoutineDetail
import com.gymquest.app.domain.model.enums.Weekday
import com.gymquest.app.domain.model.enums.EquipmentType

data class RoutineExerciseOption(
    val id: String,
    val name: String,
    val muscleGroupName: String,
    val equipmentType: EquipmentType,
)

data class RoutineDraftExercise(
    val exercise: RoutineExerciseOption,
    val plannedSets: List<RoutineDraftPlannedSet> = RoutineDraftPlannedSet.defaults(),
)

data class RoutineDraftPlannedSet(
    val targetWeight: String = "",
    val targetReps: String = "",
) {
    companion object {
        fun defaults(count: Int = 3): List<RoutineDraftPlannedSet> =
            List(count.coerceIn(1, 20)) { RoutineDraftPlannedSet() }
    }
)

data class RoutineDraft(
    val name: String = "",
    val selectedDay: Weekday = Weekday.MONDAY,
    val days: Map<Weekday, List<RoutineDraftExercise>> = emptyMap(),
)

data class RoutineUiState(
    val routines: List<WorkoutRoutineDetail> = emptyList(),
    val availableExercises: List<RoutineExerciseOption> = emptyList(),
    val selectedRoutineId: String? = null,
    val selectedDay: Weekday = Weekday.MONDAY,
    val draft: RoutineDraft? = null,
) {
    val selectedRoutine: WorkoutRoutineDetail?
        get() = routines.firstOrNull { it.routine.id.toString() == selectedRoutineId }
}

sealed interface RoutineAction {
    data object StartCreating : RoutineAction
    data object CancelEditing : RoutineAction
    data class ChangeDraftName(val name: String) : RoutineAction
    data class SelectDraftDay(val day: Weekday) : RoutineAction
    data class AddExercise(val exercise: RoutineExerciseOption) : RoutineAction
    data class ChangePlannedSets(val exerciseId: String, val plannedSets: Int) : RoutineAction
    data class RemoveExercise(val exerciseId: String) : RoutineAction
    data object SaveDraft : RoutineAction
    data class SelectRoutine(val routineId: String) : RoutineAction
    data class SelectRoutineDay(val day: Weekday) : RoutineAction
}

/**
 * Minimal UI contract so the screen can be backed by Room, a remote service, or the
 * in-memory editor below without coupling this feature to a repository.
 */
interface RoutineStateHolder {
    val uiState: kotlinx.coroutines.flow.StateFlow<RoutineUiState>
    fun onAction(action: RoutineAction)
}
