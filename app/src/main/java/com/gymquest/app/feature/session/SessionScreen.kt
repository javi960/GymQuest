package com.gymquest.app.feature.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.time.SystemClockProvider
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestConfirmationDialog
import com.gymquest.app.core.ui.component.QuestFilterChip
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.StatBadge
import com.gymquest.app.core.ui.component.StatBadgeRow

@Composable
fun SessionScreen(onOpenRoutines: () -> Unit = {}) {
    val application = LocalContext.current.applicationContext as GymQuestApp
    val container = application.appContainer
    val viewModel: SessionViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SessionViewModel(
                    observeActiveSession = container.observeActiveSessionUseCase,
                    observeActiveExerciseVariants = container.observeActiveExerciseVariantsUseCase,
                    startWorkoutSession = container.startWorkoutSessionUseCase,
                    cancelWorkoutSession = container.cancelWorkoutSessionUseCase,
                    completeWorkoutSession = container.completeWorkoutSessionUseCase,
                    addExerciseToSession = container.addExerciseToSessionUseCase,
                    saveWorkoutSet = container.saveWorkoutSetUseCase,
                    updateWorkoutSet = container.updateWorkoutSetUseCase,
                    deleteWorkoutSet = container.deleteWorkoutSetUseCase,
                    startRestAfterSet = container.startRestAfterSetUseCase,
                    resolveRestBeforeNextSet = container.resolveRestBeforeNextSetUseCase,
                    applyWorkoutProgress = container.applyWorkoutProgressUseCase,
                    clock = SystemClockProvider,
                )
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SessionEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
    SessionContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onRetry = viewModel::retry,
        onOpenRoutines = onOpenRoutines,
    )
}

@Composable
internal fun SessionContent(
    state: SessionUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (SessionAction) -> Unit,
    onRetry: () -> Unit,
    onOpenRoutines: () -> Unit = {},
) {
    val variantNames = state.variants.associate { it.id to it.name }
    var confirmation by remember { mutableStateOf<SessionConfirmation?>(null) }
    val hasSavedSets = state.activeSession?.exercises?.any { it.sets.isNotEmpty() } == true
    QuestScreen {
        confirmation?.let { pending ->
            QuestConfirmationDialog(
                title = pending.title,
                message = pending.message,
                confirmLabel = pending.confirmLabel,
                confirmAction = pending.action,
                onConfirm = {
                    confirmation = null
                    onAction(pending.sessionAction)
                },
                onDismiss = { confirmation = null },
            )
        }
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .imePadding(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    QuestPanel {
                        QuestSectionHeader(
                            title = "Sesion de entrenamiento",
                            subtitle = if (state.activeSession == null) {
                                "Inicia una aventura y registra tus primeras series."
                            } else {
                                "Aventura activa con ${state.activeSession.exercises.size} ejercicios."
                            },
                        )
                        state.activeSession?.let { activeSession ->
                            StatBadgeRow {
                                StatBadge(label = "ejercicios", value = activeSession.exercises.size.toString())
                                StatBadge(label = "estado", value = state.operation.label())
                            }
                        }
                    }
                }
                if (state.isLoading) {
                    item {
                        EmptyAdventureState(
                            title = "Cargando sesion",
                            description = "Preparando la sesion activa y el catalogo disponible.",
                        )
                    }
                    return@LazyColumn
                }
                state.errorMessage?.let { message ->
                    item {
                        EmptyAdventureState(
                            title = "No se pudo cargar la sesion",
                            description = message,
                            action = { com.gymquest.app.core.ui.component.QuestButton(text = "Reintentar", onClick = onRetry) },
                        )
                    }
                    return@LazyColumn
                }
                item {
                    if (state.activeSession == null) {
                        when (state.outcome) {
                            SessionOutcome.Completed -> OutcomeState(
                                title = "Sesión completada",
                                description = "El progreso se actualizó con las series guardadas.",
                                onDismiss = { onAction(SessionAction.DismissOutcome) },
                            )
                            SessionOutcome.Cancelled -> OutcomeState(
                                title = "Sesión cancelada",
                                description = "La sesión no se ha contado para el progreso.",
                                onDismiss = { onAction(SessionAction.DismissOutcome) },
                            )
                            SessionOutcome.None -> QuestPanel {
                                QuestSectionHeader("No hay sesión activa", "Inicia una sesión manual o carga el día de una rutina semanal.")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    QuestActionButton(
                                        action = QuestAction.Start,
                                        label = "Iniciar sesion",
                                        onClick = { onAction(SessionAction.StartSession) },
                                    )
                                    com.gymquest.app.core.ui.component.QuestButton("Rutinas", onOpenRoutines, action = QuestAction.Catalog)
                                }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuestActionButton(
                                action = QuestAction.Finish,
                                label = state.operation.completeLabel(),
                                enabled = state.operation == SessionOperation.None,
                                onClick = {
                                    if (hasSavedSets) confirmation = SessionConfirmation.Complete
                                    else onAction(SessionAction.CompleteSession)
                                },
                            )
                            QuestActionButton(
                                action = QuestAction.Cancel,
                                label = "Cancelar",
                                enabled = state.operation == SessionOperation.None,
                                onClick = {
                                    if (hasSavedSets) confirmation = SessionConfirmation.Cancel
                                    else onAction(SessionAction.CancelSession)
                                },
                            )
                        }
                    }
                }
                if (state.activeSession != null) {
                    item {
                        QuestPanel {
                            Text("Anadir ejercicio", style = MaterialTheme.typography.titleMedium)
                            if (state.variants.isEmpty()) {
                                Text("Crea primero una variante desde el catalogo.")
                            } else {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    state.variants.forEach { variant ->
                                        QuestFilterChip(
                                            selected = state.selectedVariantId == variant.id,
                                            onClick = { onAction(SessionAction.SelectVariant(variant.id)) },
                                            label = variant.name,
                                        )
                                    }
                                }
                                QuestActionButton(
                                    action = QuestAction.Add,
                                    label = "Anadir a sesion",
                                    onClick = { onAction(SessionAction.AddSelectedVariant) },
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                    items(state.activeSession.exercises, key = { it.workoutExercise.id }) { exerciseDetail ->
                        val exerciseName = variantNames[exerciseDetail.workoutExercise.exerciseVariantId] ?: "Variante"
                        WorkoutExerciseCard(
                            exerciseDetail = exerciseDetail,
                            exerciseName = exerciseName,
                            onSaveSet = { weightInput, repsInput, setType ->
                                onAction(
                                    SessionAction.SaveSet(
                                        workoutExerciseId = exerciseDetail.workoutExercise.id,
                                        weightInput = weightInput,
                                        repsInput = repsInput,
                                        setType = setType,
                                    ),
                                )
                            },
                            onUpdateSet = { set, weightInput, repsInput, setType ->
                                onAction(SessionAction.UpdateSet(set, weightInput, repsInput, setType))
                            },
                            onDeleteSet = { setId ->
                                onAction(SessionAction.DeleteSet(setId))
                            },
                            setSaveState = state.setSaveStates[exerciseDetail.workoutExercise.id] ?: SetSaveState.Idle,
                            manualRestTimer = state.manualRestTimers[exerciseDetail.workoutExercise.id] ?: ManualRestTimerState(),
                            manualRestError = state.manualRestErrors[exerciseDetail.workoutExercise.id],
                            onStartRest = { onAction(SessionAction.StartRest(exerciseDetail.workoutExercise.id)) },
                            onPauseRest = { onAction(SessionAction.PauseRest(exerciseDetail.workoutExercise.id)) },
                            onResumeRest = { onAction(SessionAction.ResumeRest(exerciseDetail.workoutExercise.id)) },
                            onAdvanceRest = { onAction(SessionAction.AdvanceRest(exerciseDetail.workoutExercise.id)) },
                            onRestTargetChange = { input ->
                                onAction(SessionAction.ChangeRestTarget(exerciseDetail.workoutExercise.id, input))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutcomeState(title: String, description: String, onDismiss: () -> Unit) {
    EmptyAdventureState(
        title = title,
        description = description,
        action = { com.gymquest.app.core.ui.component.QuestButton("Entendido", onDismiss) },
    )
}

private fun SessionOperation.label(): String = when (this) {
    SessionOperation.None -> "activa"
    SessionOperation.Completing -> "completando"
    SessionOperation.Cancelling -> "cancelando"
}

private fun SessionOperation.completeLabel(): String = when (this) {
    SessionOperation.None -> "Completar"
    SessionOperation.Completing -> "Completando…"
    SessionOperation.Cancelling -> "Completar"
}

private enum class SessionConfirmation(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val action: QuestAction,
    val sessionAction: SessionAction,
) {
    Complete(
        title = "¿Completar sesión?",
        message = "Se calculará el progreso con las series guardadas. Esta acción cierra la sesión.",
        confirmLabel = "Completar sesión",
        action = QuestAction.Finish,
        sessionAction = SessionAction.CompleteSession,
    ),
    Cancel(
        title = "¿Cancelar sesión?",
        message = "La sesión quedará cancelada y no contará para el progreso.",
        confirmLabel = "Cancelar sesión",
        action = QuestAction.Discard,
        sessionAction = SessionAction.CancelSession,
    ),
}
