package com.gymquest.app.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Historial", style = MaterialTheme.typography.headlineSmall)
            Text("${state.sessions.size} sesiones guardadas")
        }
        state.selectedSessionDetail?.let { detail ->
            item {
                SessionDetailCard(detail = detail)
            }
        }
        if (state.sessions.isEmpty()) {
            item {
                Text("Completa una sesion para verla aqui.")
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

@Composable
private fun SessionHistoryCard(
    session: WorkoutSession,
    selected: Boolean,
    onSelectSession: (Long) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(session.startedAt.toString(), style = MaterialTheme.typography.titleMedium)
            Text("${session.status} · ${formatDuration(session.durationSeconds)}")
            Button(onClick = { onSelectSession(session.id) }) {
                Text(if (selected) "Detalle abierto" else "Ver detalle")
            }
        }
    }
}

@Composable
private fun SessionDetailCard(detail: WorkoutSessionDetail) {
    val setCount = detail.exercises.sumOf { it.sets.size }
    val totalVolume = detail.exercises.sumOf { exercise -> exercise.sets.sumOf { it.volume } }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Detalle de sesion", style = MaterialTheme.typography.titleMedium)
            Text("Ejercicios: ${detail.exercises.size}")
            Text("Series: $setCount")
            Text("Volumen: $totalVolume kg")
            Text("Duracion: ${formatDuration(detail.session.durationSeconds)}")
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes}m ${remainingSeconds}s"
}
