package com.gymquest.app.feature.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.time.SystemClockProvider
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestConfirmationDialog
import com.gymquest.app.core.ui.component.QuestFilterChip
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestTextField
import com.gymquest.app.data.local.entity.WeeklyTrainingDayEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private val weekDays = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

/** Gestión de rutinas. El inicio de entrenamientos vive en la pantalla Sesiones. */
@Composable
fun WeeklyPlansScreen(onConfigureExercises: (Long) -> Unit) {
    val app = LocalContext.current.applicationContext as GymQuestApp
    val repository = app.appContainer.weeklyTrainingRepository
    val plans by repository.observePlans().collectAsStateWithLifecycle(emptyList())
    var selectedPlanId by remember { mutableStateOf<Long?>(null) }
    val selectedPlan = plans.firstOrNull { it.id == selectedPlanId } ?: plans.firstOrNull()
    val days by (selectedPlan?.let { repository.observeDays(it.id) } ?: flowOf(emptyList<WeeklyTrainingDayEntity>())).collectAsStateWithLifecycle(emptyList())
    var newPlanName by remember { mutableStateOf("") }
    var editablePlanName by remember(selectedPlan?.id) { mutableStateOf(selectedPlan?.name.orEmpty()) }
    var editablePlanNotes by remember(selectedPlan?.id) { mutableStateOf(selectedPlan?.notes.orEmpty()) }
    var editingPlan by remember { mutableStateOf(false) }
    var deletePlanRequested by remember { mutableStateOf(false) }
    var dayLabel by remember { mutableStateOf("") }
    var selectedDayKey by remember { mutableStateOf("Lunes") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    QuestScreen {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { QuestSectionHeader("Rutinas", "Crea y organiza tus semanas tipo. Para entrenar, usa la pestaña Sesiones.") }
            item { QuestPanel {
                QuestTextField(newPlanName, { newPlanName = it }, "Nombre de la rutina", singleLine = true)
                QuestActionButton(QuestAction.Add, onClick = {
                    if (newPlanName.isBlank()) message = "Escribe un nombre para la rutina." else scope.launch {
                        val id = repository.addPlan(newPlanName, null, SystemClockProvider.now())
                        selectedPlanId = id
                        newPlanName = ""
                        message = "Rutina creada."
                    }
                }, label = "Crear rutina")
            } }
            if (plans.isNotEmpty()) item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    plans.forEach { plan -> QuestFilterChip(plan.name, plan.id == selectedPlan?.id, onClick = {
                        selectedPlanId = plan.id
                        editingPlan = false
                    }) }
                }
            }
            selectedPlan?.let { plan ->
                item { QuestPanel {
                    Text(plan.name, style = MaterialTheme.typography.titleMedium)
                    plan.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    if (editingPlan) {
                        QuestTextField(editablePlanName, { editablePlanName = it }, "Nombre", singleLine = true)
                        QuestTextField(editablePlanNotes, { editablePlanNotes = it }, "Notas (opcional)")
                        QuestActionButton(QuestAction.Save, onClick = {
                            if (editablePlanName.isBlank()) message = "La rutina necesita un nombre." else scope.launch {
                                repository.updatePlan(plan, editablePlanName, editablePlanNotes, SystemClockProvider.now())
                                editingPlan = false
                                message = "Rutina actualizada."
                            }
                        }, label = "Guardar cambios")
                        QuestButton("Cancelar edición", onClick = {
                            editablePlanName = plan.name
                            editablePlanNotes = plan.notes.orEmpty()
                            editingPlan = false
                        }, action = QuestAction.Cancel)
                    } else {
                        QuestButton("Editar rutina", onClick = { editingPlan = true }, action = QuestAction.Edit)
                        QuestButton("Eliminar rutina", onClick = { deletePlanRequested = true }, action = QuestAction.Delete)
                    }
                } }
                item { QuestPanel {
                    Text("Días de la rutina", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        weekDays.forEach { day -> QuestFilterChip(day, selectedDayKey == day, onClick = { selectedDayKey = day }) }
                    }
                    QuestTextField(dayLabel, { dayLabel = it }, "Nombre opcional, por ejemplo: Tirón")
                    QuestActionButton(QuestAction.Add, onClick = { scope.launch {
                        repository.addDay(plan.id, selectedDayKey, dayLabel, days.size, SystemClockProvider.now())
                        dayLabel = ""
                        message = "Día añadido."
                    } }, label = "Añadir día")
                } }
                items(days, key = { it.id }) { day -> QuestPanel {
                    Text(day.label ?: day.dayKey, style = MaterialTheme.typography.titleMedium)
                    Text("Día planificado: ${day.dayKey}")
                    QuestButton("Configurar ejercicios", onClick = { onConfigureExercises(day.id) }, action = QuestAction.Edit)
                } }
            }
            message?.let { item { Text(it, color = MaterialTheme.colorScheme.primary) } }
        }
    }
    if (deletePlanRequested && selectedPlan != null) {
        QuestConfirmationDialog(
            title = "Eliminar rutina",
            message = "La rutina dejará de aparecer para entrenar, pero se conservarán sus sesiones, series e historial de progreso.",
            confirmLabel = "Eliminar rutina",
            confirmAction = QuestAction.Delete,
            onConfirm = {
                scope.launch {
                    repository.archivePlan(selectedPlan.id, SystemClockProvider.now())
                    selectedPlanId = null
                    editingPlan = false
                    message = "Rutina eliminada de la lista. Su historial se conserva."
                }
                deletePlanRequested = false
            },
            onDismiss = { deletePlanRequested = false },
        )
    }
}
