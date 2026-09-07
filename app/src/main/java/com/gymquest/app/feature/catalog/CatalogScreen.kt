package com.gymquest.app.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.time.SystemClockProvider
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType

@Composable
fun CatalogScreen() {
    val application = LocalContext.current.applicationContext as GymQuestApp
    val container = application.appContainer
    val viewModel: CatalogViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                CatalogViewModel(
                    createMuscleGroup = container.createMuscleGroupUseCase,
                    observeMuscleGroups = container.observeMuscleGroupsUseCase,
                    createExerciseBase = container.createExerciseBaseUseCase,
                    createExerciseVariant = container.createExerciseVariantUseCase,
                    observeExerciseCatalog = container.observeExerciseCatalogUseCase,
                    clock = SystemClockProvider,
                )
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogContent(
        state = state,
        onSelectMuscleGroup = viewModel::selectMuscleGroup,
        onAddMuscleGroup = viewModel::addMuscleGroup,
        onAddExerciseBase = viewModel::addExerciseBase,
        onAddExerciseVariant = viewModel::addExerciseVariant,
        onClearFeedback = viewModel::clearFeedback,
    )
}

@Composable
private fun CatalogContent(
    state: CatalogUiState,
    onSelectMuscleGroup: (Long) -> Unit,
    onAddMuscleGroup: (String) -> Unit,
    onAddExerciseBase: (String, String?) -> Unit,
    onAddExerciseVariant: (Long, String, EquipmentType, WeightComparisonType, String?) -> Unit,
    onClearFeedback: () -> Unit,
) {
    var muscleGroupName by remember { mutableStateOf("") }
    var exerciseName by remember { mutableStateOf("") }
    var exerciseDescription by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Catálogo de ejercicios", style = MaterialTheme.typography.headlineSmall)
            Text("Crea tu catálogo local: no necesita conexión ni cuenta.")
        }
        item {
            OutlinedTextField(
                value = muscleGroupName,
                onValueChange = { muscleGroupName = it },
                label = { Text("Nuevo grupo muscular") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Button(
                onClick = {
                    onAddMuscleGroup(muscleGroupName)
                    muscleGroupName = ""
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Añadir grupo")
            }
        }
        item {
            Text("Grupo para el ejercicio", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.muscleGroups.forEach { group ->
                    FilterChip(
                        selected = state.selectedMuscleGroupId == group.id,
                        onClick = { onSelectMuscleGroup(group.id) },
                        label = { Text(group.name) },
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = exerciseName,
                onValueChange = { exerciseName = it },
                label = { Text("Ejercicio base") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = exerciseDescription,
                onValueChange = { exerciseDescription = it },
                label = { Text("Descripción opcional") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
            Button(
                onClick = {
                    onAddExerciseBase(exerciseName, exerciseDescription)
                    exerciseName = ""
                    exerciseDescription = ""
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Añadir ejercicio")
            }
        }
        state.feedback?.let { feedback ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(feedback)
                        Button(onClick = onClearFeedback, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Entendido")
                        }
                    }
                }
            }
        }
        item { HorizontalDivider() }
        item { Text("Ejercicios guardados", style = MaterialTheme.typography.titleMedium) }
        items(state.catalog, key = { it.exerciseBase.id }) { entry ->
            var variantName by remember(entry.exerciseBase.id) { mutableStateOf("") }
            var variantNotes by remember(entry.exerciseBase.id) { mutableStateOf("") }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(entry.exerciseBase.name, style = MaterialTheme.typography.titleMedium)
                    entry.exerciseBase.description?.let { Text(it) }
                    Text(
                        text = if (entry.variants.isEmpty()) "Aún no tiene variantes." else "${entry.variants.size} variantes",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    entry.variants.forEach { variant ->
                        Text(
                            text = "${variant.name} · ${variant.equipmentType.name}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    OutlinedTextField(
                        value = variantName,
                        onValueChange = { variantName = it },
                        label = { Text("Nueva variante") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = variantNotes,
                        onValueChange = { variantNotes = it },
                        label = { Text("Notas de variante") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                    Button(
                        onClick = {
                            onAddExerciseVariant(
                                entry.exerciseBase.id,
                                variantName,
                                EquipmentType.OTHER,
                                WeightComparisonType.TOTAL_WEIGHT,
                                variantNotes,
                            )
                            variantName = ""
                            variantNotes = ""
                        },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text("Añadir variante")
                    }
                }
            }
        }
    }
}
