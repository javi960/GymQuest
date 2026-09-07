package com.gymquest.app.feature.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.time.SystemClockProvider

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
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Sesión de entrenamiento", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = if (state.activeSession == null) {
                        "Inicia una sesion y registra tus primeras series."
                    } else {
                        "Sesion activa con ${state.activeSession.exercises.size} ejercicios."
                    },
                )
            }
            item {
                if (state.activeSession == null) {
                    Button(onClick = { onAction(SessionAction.StartSession) }) {
                        Text("Iniciar sesión")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onAction(SessionAction.CompleteSession) }) {
                            Text("Completar")
                        }
                        OutlinedButton(onClick = { onAction(SessionAction.CancelSession) }) {
                            Text("Cancelar")
                        }
                    }
                }
            }
            if (state.activeSession != null) {
                item {
                    Text("Añadir ejercicio", style = MaterialTheme.typography.titleMedium)
                    if (state.variants.isEmpty()) {
                        Text("Crea primero una variante desde el catálogo.")
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
                        Button(
                            onClick = { onAction(SessionAction.AddSelectedVariant) },
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            Text("Añadir a sesión")
                        }
                    }
                }
                item { HorizontalDivider() }
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
