package com.gymquest.app.feature.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestFilterChip
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSearchField
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestTextField
import com.gymquest.app.domain.model.RoutineExerciseDetail
import com.gymquest.app.domain.model.WorkoutRoutineDetail
import com.gymquest.app.domain.model.enums.Weekday
import com.gymquest.app.domain.model.enums.EquipmentType

/** Connect this overload to any [RoutineStateHolder], including a repository-backed ViewModel. */
@Composable
fun RoutineScreen(
    stateHolder: RoutineStateHolder,
    onUseRoutineDay: (WorkoutRoutineDetail, Weekday) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by stateHolder.uiState.collectAsState()
    RoutineScreen(
        state = state,
        onAction = stateHolder::onAction,
        onUseRoutineDay = onUseRoutineDay,
        modifier = modifier,
    )
}

/** Stateless routine editor and weekly-plan browser for navigation hosts and tests. */
@Composable
fun RoutineScreen(
    state: RoutineUiState,
    onAction: (RoutineAction) -> Unit,
    onUseRoutineDay: (WorkoutRoutineDetail, Weekday) -> Unit,
    modifier: Modifier = Modifier,
) {
    QuestScreen(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                QuestPanel {
                    QuestSectionHeader(
                        title = if (state.draft == null) "Rutinas semanales" else "Crear rutina semanal",
                        subtitle = if (state.draft == null) "Planifica cada día y úsalo al iniciar una sesión." else "Añade ejercicios y sus series previstas por día.",
                    )
                }
            }
            state.draft?.let { draft ->
                item {
                    RoutineDraftEditor(
                        draft = draft,
                        availableExercises = state.availableExercises,
                        onAction = onAction,
                    )
                }
            } ?: run {
                if (state.routines.isEmpty()) {
                    item {
                        EmptyAdventureState(
                            title = "Aún no hay rutinas",
                            description = "Crea una rutina como “Fuerza semanal” y distribuye piernas, espalda o pecho por días.",
                            action = {
                                QuestActionButton(
                                    action = QuestAction.Add,
                                    label = "Crear rutina",
                                    onClick = { onAction(RoutineAction.StartCreating) },
                                )
                            },
                        )
                    }
                } else {
                    item {
                        QuestActionButton(
                            action = QuestAction.Add,
                            label = "Crear rutina",
                            onClick = { onAction(RoutineAction.StartCreating) },
                        )
                    }
                    items(state.routines, key = { it.routine.id }) { routine ->
                        RoutineCard(
                            routine = routine,
                            isSelected = routine.routine.id.toString() == state.selectedRoutineId,
                            onSelect = { onAction(RoutineAction.SelectRoutine(routine.routine.id.toString())) },
                        )
                    }
                    state.selectedRoutine?.let { routine ->
                        item {
                            SelectedRoutineDay(
                                routine = routine,
                                selectedDay = state.selectedDay,
                                exerciseNames = state.availableExercises.associate { it.id to it.name },
                                onDaySelected = { onAction(RoutineAction.SelectRoutineDay(it)) },
                                onUse = { onUseRoutineDay(routine, state.selectedDay) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineDraftEditor(
    draft: RoutineDraft,
    availableExercises: List<RoutineExerciseOption>,
    onAction: (RoutineAction) -> Unit,
) {
    var exerciseQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf<String?>(null) }
    var selectedEquipmentType by remember { mutableStateOf<EquipmentType?>(null) }
    val matchingExercises = availableExercises.filter { exercise ->
        exercise.name.contains(exerciseQuery.trim(), ignoreCase = true) &&
            (selectedMuscleGroup == null || exercise.muscleGroupName == selectedMuscleGroup) &&
            (selectedEquipmentType == null || exercise.equipmentType == selectedEquipmentType)
    }
    QuestPanel {
        QuestTextField(
            value = draft.name,
            onValueChange = { onAction(RoutineAction.ChangeDraftName(it)) },
            label = "Nombre de la rutina",
            singleLine = true,
        )
        DayPicker(selectedDay = draft.selectedDay, onSelected = { onAction(RoutineAction.SelectDraftDay(it)) })
        Text("Ejercicios para ${draft.selectedDay.displayName}", style = MaterialTheme.typography.titleMedium)
        val plannedExercises = draft.days[draft.selectedDay].orEmpty()
        if (plannedExercises.isEmpty()) {
            Text("Selecciona ejercicios para preparar este día.", style = MaterialTheme.typography.bodyMedium)
        } else {
            plannedExercises.forEach { exercise ->
                PlannedExerciseRow(exercise = exercise, onAction = onAction)
            }
        }
        if (availableExercises.isEmpty()) {
            Text("No hay ejercicios disponibles todavía.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Text("Añadir ejercicio", style = MaterialTheme.typography.labelLarge)
            QuestSearchField(
                value = exerciseQuery,
                onValueChange = { exerciseQuery = it },
                label = "Buscar ejercicio para ${draft.selectedDay.displayName}",
            )
            Text("Explorar por parte del cuerpo", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                QuestFilterChip("Todos", selectedMuscleGroup == null, onClick = { selectedMuscleGroup = null })
                availableExercises.map { it.muscleGroupName }.distinct().sorted().forEach { group ->
                    val count = availableExercises.count { it.muscleGroupName == group }
                    QuestFilterChip("$group ($count)", selectedMuscleGroup == group, onClick = { selectedMuscleGroup = group })
                }
            }
            Text("Filtrar por equipo", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                QuestFilterChip("Todos", selectedEquipmentType == null, onClick = { selectedEquipmentType = null })
                EquipmentType.entries.forEach { equipment ->
                    val count = availableExercises.count { it.equipmentType == equipment }
                    QuestFilterChip("${equipment.routineLabel()} ($count)", selectedEquipmentType == equipment, onClick = { selectedEquipmentType = equipment })
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                matchingExercises.forEach { exercise ->
                    QuestFilterChip(
                        label = exercise.name,
                        selected = plannedExercises.any { it.exercise.id == exercise.id },
                        onClick = { onAction(RoutineAction.AddExercise(exercise)) },
                    )
                }
            }
            if (matchingExercises.isEmpty()) {
                Text("No hay ejercicios que coincidan con la búsqueda.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestActionButton(
                action = QuestAction.Save,
                label = "Guardar rutina",
                enabled = draft.name.isNotBlank(),
                onClick = { onAction(RoutineAction.SaveDraft) },
            )
            QuestButton(text = "Cancelar", onClick = { onAction(RoutineAction.CancelEditing) })
        }
    }
}

@Composable
private fun PlannedExerciseRow(exercise: RoutineDraftExercise, onAction: (RoutineAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(exercise.exercise.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        IconButton(
            onClick = { onAction(RoutineAction.ChangePlannedSets(exercise.exercise.id, exercise.plannedSets - 1)) },
            modifier = Modifier.semantics { contentDescription = "Reducir series de ${exercise.exercise.name}" },
        ) { Icon(Icons.Filled.Remove, contentDescription = null) }
        Text("${exercise.plannedSets} series", style = MaterialTheme.typography.labelLarge)
        IconButton(
            onClick = { onAction(RoutineAction.ChangePlannedSets(exercise.exercise.id, exercise.plannedSets + 1)) },
            modifier = Modifier.semantics { contentDescription = "Aumentar series de ${exercise.exercise.name}" },
        ) { Icon(Icons.Filled.Add, contentDescription = null) }
        QuestButton(text = "Quitar", onClick = { onAction(RoutineAction.RemoveExercise(exercise.exercise.id)) })
    }
}

@Composable
private fun RoutineCard(routine: WorkoutRoutineDetail, isSelected: Boolean, onSelect: () -> Unit) {
    QuestPanel {
        Text(routine.routine.name, style = MaterialTheme.typography.titleMedium)
        Text(
            "${routine.days.count { it.exercises.isNotEmpty() }} días planificados · ${routine.days.sumOf { it.exercises.size }} ejercicios",
            style = MaterialTheme.typography.bodyMedium,
        )
        QuestButton(text = if (isSelected) "Rutina seleccionada" else "Ver rutina", onClick = onSelect)
    }
}

@Composable
private fun SelectedRoutineDay(
    routine: WorkoutRoutineDetail,
    selectedDay: Weekday,
    exerciseNames: Map<String, String>,
    onDaySelected: (Weekday) -> Unit,
    onUse: () -> Unit,
) {
    QuestPanel {
        QuestSectionHeader(title = routine.routine.name, subtitle = "Elige el día que quieres usar en tu próxima sesión.")
        DayPicker(selectedDay = selectedDay, onSelected = onDaySelected)
        val exercises = routine.days.firstOrNull { it.day.weekday == selectedDay }?.exercises.orEmpty()
        if (exercises.isEmpty()) {
            Text("No hay ejercicios programados para ${selectedDay.displayName}.")
        } else {
            exercises.forEach { exercise ->
                Text(exercise.displayName(exerciseNames) + " · ${exercise.plannedSets.size} series", style = MaterialTheme.typography.bodyLarge)
            }
            QuestActionButton(action = QuestAction.Start, label = "Usar rutina de ${selectedDay.displayName}", onClick = onUse)
        }
    }
}

@Composable
private fun DayPicker(selectedDay: Weekday, onSelected: (Weekday) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Weekday.entries.forEach { day ->
            QuestFilterChip(label = day.displayName, selected = day == selectedDay, onClick = { onSelected(day) })
        }
    }
}

private fun RoutineExerciseDetail.displayName(exerciseNames: Map<String, String>): String =
    exerciseNames[exercise.exerciseVariantId.toString()] ?: "Ejercicio"

private fun EquipmentType.routineLabel(): String = when (this) {
    EquipmentType.BARBELL -> "Barra"
    EquipmentType.DUMBBELL -> "Mancuernas"
    EquipmentType.MACHINE -> "Máquina"
    EquipmentType.CABLE -> "Polea"
    EquipmentType.BODYWEIGHT -> "Peso corporal"
    EquipmentType.MULTIPOWER -> "Multipower"
    EquipmentType.KETTLEBELL -> "Kettlebell"
    EquipmentType.ELASTIC_BAND -> "Banda elástica"
    EquipmentType.OTHER -> "Otro"
}
