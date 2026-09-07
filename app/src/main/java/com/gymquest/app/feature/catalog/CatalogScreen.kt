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
import androidx.compose.material3.FilterChip
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
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.StatBadge
import com.gymquest.app.core.ui.component.StatBadgeRow
import com.gymquest.app.domain.model.ExerciseCatalogEntry
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

    QuestScreen {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                QuestPanel {
                    QuestSectionHeader(
                        title = "Catalogo de ejercicios",
                        subtitle = "Crea tu coleccion local: grupos, ejercicios base y variantes listas para entrenar.",
                    )
                    StatBadgeRow {
                        StatBadge(label = "grupos", value = state.muscleGroups.size.toString())
                        StatBadge(label = "ejercicios", value = state.catalog.size.toString())
                    }
                }
            }
            item {
                QuestPanel {
                    Text("Nuevo grupo muscular", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = muscleGroupName,
                        onValueChange = { muscleGroupName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    QuestActionButton(
                        action = QuestAction.Add,
                        label = "Anadir grupo",
                        onClick = {
                            onAddMuscleGroup(muscleGroupName)
                            muscleGroupName = ""
                        },
                    )
                }
            }
            item {
                QuestPanel {
                    Text("Grupo para el ejercicio", style = MaterialTheme.typography.titleMedium)
                    if (state.muscleGroups.isEmpty()) {
                        Text("Crea al menos un grupo para clasificar tus ejercicios.")
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.muscleGroups.forEach { group ->
                                FilterChip(
                                    selected = state.selectedMuscleGroupId == group.id,
                                    onClick = { onSelectMuscleGroup(group.id) },
                                    label = { Text(group.name) },
                                )
                            }
                        }
                    }
                }
            }
            item {
                QuestPanel {
                    Text("Nuevo ejercicio base", style = MaterialTheme.typography.titleMedium)
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
                        label = { Text("Descripcion opcional") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    QuestActionButton(
                        action = QuestAction.Add,
                        label = "Anadir ejercicio",
                        onClick = {
                            onAddExerciseBase(exerciseName, exerciseDescription)
                            exerciseName = ""
                            exerciseDescription = ""
                        },
                    )
                }
            }
            state.feedback?.let { feedback ->
                item {
                    QuestPanel {
                        Text(feedback)
                        QuestButton(text = "Entendido", action = QuestAction.Finish, onClick = onClearFeedback)
                    }
                }
            }
            item {
                QuestSectionHeader(
                    title = "Ejercicios guardados",
                    subtitle = "Variantes disponibles para anadir a la sesion activa.",
                )
            }
            if (state.catalog.isEmpty()) {
                item {
                    EmptyAdventureState(
                        title = "Catalogo vacio",
                        description = "Crea un grupo muscular y un ejercicio base para empezar a registrar sesiones reales.",
                    )
                }
            } else {
                items(state.catalog, key = { it.exerciseBase.id }) { entry ->
                    CatalogEntryCard(
                        entry = entry,
                        onAddExerciseVariant = onAddExerciseVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogEntryCard(
    entry: ExerciseCatalogEntry,
    onAddExerciseVariant: (Long, String, EquipmentType, WeightComparisonType, String?) -> Unit,
) {
    var variantName by remember(entry.exerciseBase.id) { mutableStateOf("") }
    var variantNotes by remember(entry.exerciseBase.id) { mutableStateOf("") }
    QuestPanel {
        Text(entry.exerciseBase.name, style = MaterialTheme.typography.titleMedium)
        entry.exerciseBase.description?.let { Text(it) }
        StatBadge(label = "variantes", value = entry.variants.size.toString())
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            entry.variants.forEach { variant ->
                Text(
                    text = "${variant.name} - ${variant.equipmentType.name}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        OutlinedTextField(
            value = variantName,
            onValueChange = { variantName = it },
            label = { Text("Nueva variante") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = variantNotes,
            onValueChange = { variantNotes = it },
            label = { Text("Notas de variante") },
            modifier = Modifier.fillMaxWidth(),
        )
        QuestActionButton(
            action = QuestAction.Add,
            label = "Anadir variante",
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
        )
    }
}
