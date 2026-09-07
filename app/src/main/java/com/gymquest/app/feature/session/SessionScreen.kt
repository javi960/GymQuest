package com.gymquest.app.feature.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.StatBadge
import com.gymquest.app.core.ui.component.StatBadgeRow

@Composable
fun SessionScreen() {
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
    )
}

@Composable
private fun SessionContent(
    state: SessionUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (SessionAction) -> Unit,
) {
    val variantNames = state.variants.associate { it.id to it.name }
    QuestScreen {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                                StatBadge(label = "estado", value = "activa")
                            }
                        }
                    }
                }
                item {
                    if (state.activeSession == null) {
                        EmptyAdventureState(
                            title = "No hay sesion activa",
                            description = "Pulsa iniciar y la pantalla cambiara al modo de registro rapido.",
                            action = {
                                QuestActionButton(
                                    action = QuestAction.Start,
                                    label = "Iniciar sesion",
                                    onClick = { onAction(SessionAction.StartSession) },
                                )
                            },
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuestActionButton(
                                action = QuestAction.Finish,
                                label = "Completar",
                                onClick = { onAction(SessionAction.CompleteSession) },
                            )
                            QuestActionButton(
                                action = QuestAction.Cancel,
                                label = "Cancelar",
                                onClick = { onAction(SessionAction.CancelSession) },
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
                                        FilterChip(
                                            selected = state.selectedVariantId == variant.id,
                                            onClick = { onAction(SessionAction.SelectVariant(variant.id)) },
                                            label = { Text(variant.name) },
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
                        )
                    }
                }
            }
        }
    }
}
