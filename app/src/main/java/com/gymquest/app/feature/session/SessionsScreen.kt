package com.gymquest.app.feature.session

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
import com.gymquest.app.core.ui.component.QuestFilterChip
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.data.local.entity.WeeklyTrainingDayEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/** Punto de entrada para iniciar una sesión libre o el día elegido de una rutina. */
@Composable
fun SessionsScreen(onOpenSession: (Long) -> Unit) {
    val app = LocalContext.current.applicationContext as GymQuestApp
    val repository = app.appContainer.weeklyTrainingRepository
    val plans by repository.observePlans().collectAsStateWithLifecycle(emptyList())
    val finishedSessions by repository.observeFinishedSessions().collectAsStateWithLifecycle(emptyList())
    val activeSession by repository.observeLatestActiveSession().collectAsStateWithLifecycle(null)
    var selectedPlanId by remember { mutableStateOf<Long?>(null) }
    val selectedPlan = plans.firstOrNull { it.id == selectedPlanId } ?: plans.firstOrNull()
    val days by (selectedPlan?.let { repository.observeDays(it.id) } ?: flowOf(emptyList<WeeklyTrainingDayEntity>())).collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()

    QuestScreen {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { QuestSectionHeader("Sesiones", "Elige el día de cualquier rutina o empieza un entrenamiento libre.") }
            activeSession?.let { session ->
                item { QuestPanel {
                    Text("Sesión en curso", style = MaterialTheme.typography.titleMedium)
                    Text(session.planNameSnapshot)
                    Text("Iniciada: ${session.startedAt.toString().replace('T', ' ').take(16)}")
                    QuestActionButton(
                        QuestAction.Resume,
                        onClick = { onOpenSession(session.id) },
                        label = "Reanudar sesión",
                    )
                } }
            }
            item { QuestPanel {
                Text("Entrenamiento libre", style = MaterialTheme.typography.titleMedium)
                Text("Elige ejercicios y registra cada serie según la completes.")
                QuestActionButton(QuestAction.Start, onClick = { scope.launch {
                    onOpenSession(repository.startFreeSession(SystemClockProvider.now()))
                } }, label = "Iniciar entrenamiento libre")
            } }
            item { QuestSectionHeader("Seguir una rutina", "Puedes elegir cualquier día de la semana, aunque hoy sea otro día.") }
            if (plans.isEmpty()) {
                item { QuestPanel { Text("Aún no hay rutinas. Crea una desde la pestaña Rutinas.") } }
            } else {
                item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    plans.forEach { plan -> QuestFilterChip(plan.name, selectedPlan?.id == plan.id, onClick = { selectedPlanId = plan.id }) }
                } }
                selectedPlan?.let { plan ->
                    items(days, key = { it.id }) { day -> QuestPanel {
                        Text(day.label ?: day.dayKey, style = MaterialTheme.typography.titleMedium)
                        Text("Día planificado: ${day.dayKey}")
                        QuestButton("Iniciar este día", onClick = { scope.launch {
                            onOpenSession(repository.startSession(plan, day, SystemClockProvider.now()))
                        } }, action = QuestAction.Start)
                    } }
                }
            }
            if (finishedSessions.isNotEmpty()) {
                item { QuestSectionHeader("Historial", "La fecha real y el día planificado se guardan por separado.") }
                items(finishedSessions, key = { "history-${it.id}" }) { session -> QuestPanel {
                    Text(session.planNameSnapshot, style = MaterialTheme.typography.titleMedium)
                    Text("Día planificado: ${session.plannedDaySnapshot}")
                    Text("Realizada: ${session.startedAt.toString().replace('T', ' ').take(16)}")
                } }
            }
        }
    }
}
