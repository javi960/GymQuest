package com.gymquest.app.feature.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestFilterChip
import com.gymquest.app.core.ui.component.icon
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSearchField
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.StatBadge
import com.gymquest.app.core.ui.component.StatBadgeRow
import com.gymquest.app.domain.model.enums.EquipmentType

@Composable
fun CatalogScreen(
    onOpenExerciseDetail: (Long) -> Unit,
    onAddMuscleGroup: () -> Unit,
    onAddExercise: () -> Unit,
) {
    val viewModel = rememberCatalogViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogContent(state, onOpenExerciseDetail, onAddMuscleGroup, onAddExercise, viewModel::clearFeedback, viewModel::retry)
}

@Composable
internal fun CatalogContent(
    state: CatalogUiState,
    onOpenExerciseDetail: (Long) -> Unit,
    onAddMuscleGroup: () -> Unit,
    onAddExercise: () -> Unit,
    onClearFeedback: () -> Unit,
    onRetry: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilterGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedEquipmentType by rememberSaveable { mutableStateOf<EquipmentType?>(null) }
    var speedDialExpanded by rememberSaveable { mutableStateOf(false) }
    val visibleCatalog = state.catalog.filterCatalog(query, selectedFilterGroupId, selectedEquipmentType)
    val visibleCatalogByGroup = visibleCatalog.groupBy { it.exerciseBase.primaryMuscleGroupId }

    QuestScreen {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    QuestSectionHeader(
                        title = "Catálogo de ejercicios",
                        subtitle = "Encuentra un ejercicio o abre su ficha para ver y editar todos sus datos.",
                    )
                }
                item {
                    QuestPanel {
                        QuestSearchField(value = query, onValueChange = { query = it })
                        Text("Explorar por parte del cuerpo", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuestFilterChip("Todos", selectedFilterGroupId == null, onClick = { selectedFilterGroupId = null })
                            state.muscleGroups.forEach { group ->
                                val count = state.catalog.count { it.exerciseBase.primaryMuscleGroupId == group.id }
                                QuestFilterChip("${group.name} ($count)", selectedFilterGroupId == group.id, onClick = { selectedFilterGroupId = group.id })
                            }
                        }
                        Text("Filtrar por equipo", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuestFilterChip("Todos", selectedEquipmentType == null, onClick = { selectedEquipmentType = null })
                            EquipmentType.entries.forEach { equipment ->
                                val count = state.catalog.count { entry -> entry.variants.any { it.equipmentType == equipment } }
                                if (count > 0) {
                                    QuestFilterChip(
                                        "${equipment.catalogLabel()} ($count)",
                                        selectedEquipmentType == equipment,
                                        onClick = { selectedEquipmentType = equipment },
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    StatBadgeRow {
                        StatBadge(label = "grupos", value = state.muscleGroups.size.toString())
                        StatBadge(label = "ejercicios", value = state.catalog.size.toString())
                    }
                }
                if (state.isLoading) {
                    item { EmptyAdventureState("Cargando catálogo", "Preparando grupos musculares y ejercicios disponibles.") }
                } else if (state.errorMessage != null) {
                    item {
                        EmptyAdventureState(
                            "No se pudo cargar el catálogo",
                            state.errorMessage,
                            action = { QuestButton("Reintentar", onClick = onRetry) },
                        )
                    }
                } else {
                    state.feedback?.let { feedback ->
                        item {
                            QuestPanel {
                                Text(feedback)
                                QuestButton("Entendido", onClick = onClearFeedback, action = QuestAction.Finish)
                            }
                        }
                    }
                    item {
                        QuestSectionHeader(
                            "Ejercicios por parte del cuerpo",
                            "Filtra por equipo y toca una ficha para consultar variantes, guía visual y opciones de edición.",
                        )
                    }
                    when {
                        state.catalog.isEmpty() -> item {
                            EmptyAdventureState("Catálogo vacío", "Añade un grupo muscular y después crea tu primer ejercicio.")
                        }
                        visibleCatalog.isEmpty() -> item {
                            EmptyAdventureState("Sin resultados", "Prueba otra búsqueda o elimina el filtro de grupo.")
                        }
                        else -> state.muscleGroups.forEach { group ->
                            val entries = visibleCatalogByGroup[group.id].orEmpty()
                            if (entries.isNotEmpty()) {
                                item(key = "group-${group.id}") {
                                    QuestSectionHeader("${group.name} (${entries.size})")
                                }
                                items(entries, key = { it.exerciseBase.id }) { entry ->
                                    ExerciseSummaryCard(
                                        name = entry.exerciseBase.name,
                                        variants = entry.variants.size,
                                        equipment = entry.variants.map { it.equipmentType }.distinct(),
                                        hasVisualGuide = state.mediaByExerciseBaseId.containsKey(entry.exerciseBase.id),
                                    ) {
                                        onOpenExerciseDetail(entry.exerciseBase.id)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            CatalogSpeedDial(
                expanded = speedDialExpanded,
                onToggle = { speedDialExpanded = !speedDialExpanded },
                onAddMuscleGroup = { speedDialExpanded = false; onAddMuscleGroup() },
                onAddExercise = { speedDialExpanded = false; onAddExercise() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            )
        }
    }
}

@Composable
private fun ExerciseSummaryCard(
    name: String,
    variants: Int,
    equipment: List<EquipmentType>,
    hasVisualGuide: Boolean,
    onClick: () -> Unit,
) {
    QuestPanel(
        Modifier
            .semantics { contentDescription = "Abrir ficha del ejercicio $name" }
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium)
        Text(if (variants == 1) "1 variante configurada" else "$variants variantes configuradas", style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            equipment.forEach { type -> QuestFilterChip(type.catalogLabel(), selected = false, onClick = onClick) }
            if (hasVisualGuide) QuestFilterChip("Guía visual", selected = false, onClick = onClick)
        }
    }
}

@Composable
private fun CatalogSpeedDial(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddMuscleGroup: () -> Unit,
    onAddExercise: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (expanded) {
            ExtendedFloatingActionButton(
                text = { Text("Añadir ejercicio") },
                icon = { Icon(QuestAction.Add.icon(), contentDescription = null) },
                onClick = onAddExercise,
            )
            ExtendedFloatingActionButton(
                text = { Text("Añadir grupo muscular") },
                icon = { Icon(QuestAction.Add.icon(), contentDescription = null) },
                onClick = onAddMuscleGroup,
            )
        }
        FloatingActionButton(
            onClick = onToggle,
            modifier = Modifier.size(64.dp).semantics {
                contentDescription = if (expanded) "Cerrar acciones de creación" else "Abrir acciones de creación"
            },
        ) { Icon(QuestAction.Add.icon(), contentDescription = null) }
    }
}
