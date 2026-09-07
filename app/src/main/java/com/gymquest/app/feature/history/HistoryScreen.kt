package com.gymquest.app.feature.history

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.StatBadge
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail

@Composable
fun HistoryScreen() {
    val application = LocalContext.current.applicationContext as GymQuestApp
    val container = application.appContainer
    val viewModel: HistoryViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HistoryViewModel(
                    observeSessionHistory = container.observeSessionHistoryUseCase,
                    observeSessionDetail = container.observeSessionDetailUseCase,
                )
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryContent(
        state = state,
        onSelectSession = viewModel::selectSession,
    )
}

@Composable
private fun HistoryContent(
    state: HistoryUiState,
    onSelectSession: (Long) -> Unit,
) {
    QuestScreen {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                QuestPanel {
                    QuestSectionHeader(
                        title = "Historial",
                        subtitle = "Registro cronologico de aventuras completadas y canceladas.",
                    )
                    StatBadge(label = "sesiones", value = state.sessions.size.toString())
                }
            }
            state.selectedSessionDetail?.let { detail ->
                item {
                    SessionDetailCard(detail = detail)
                }
            }
            if (state.sessions.isEmpty()) {
                item {
                    EmptyAdventureState(
                        title = "Sin sesiones guardadas",
                        description = "Completa una sesion para verla aqui con duracion, volumen y series.",
                    )
                }
            } else {
                items(state.sessions, key = { it.id }) { session ->
                    SessionHistoryCard(
                        session = session,
                        selected = state.selectedSessionId == session.id,
                        onSelectSession = onSelectSession,
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(
    session: WorkoutSession,
    selected: Boolean,
    onSelectSession: (Long) -> Unit,
) {
    QuestPanel {
        Text(session.startedAt.toString(), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBadge(label = "estado", value = session.status.name)
            StatBadge(label = "duracion", value = formatDuration(session.durationSeconds))
        }
        QuestButton(
            text = if (selected) "Detalle abierto" else "Ver detalle",
            action = QuestAction.OpenDetail,
            onClick = { onSelectSession(session.id) },
        )
    }
}

@Composable
private fun SessionDetailCard(detail: WorkoutSessionDetail) {
    val setCount = detail.exercises.sumOf { it.sets.size }
    val totalVolume = detail.exercises.sumOf { exercise -> exercise.sets.sumOf { it.volume } }
    QuestPanel {
        QuestSectionHeader(
            title = "Detalle de sesion",
            subtitle = "Resumen escaneable antes de abrir vistas mas profundas.",
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBadge(label = "ejercicios", value = detail.exercises.size.toString())
            StatBadge(label = "series", value = setCount.toString())
            StatBadge(label = "volumen", value = "$totalVolume kg")
            StatBadge(label = "duracion", value = formatDuration(detail.session.durationSeconds))
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes}m ${remainingSeconds}s"
}
