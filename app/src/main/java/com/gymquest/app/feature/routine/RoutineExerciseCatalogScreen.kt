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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.gymquest.app.core.ui.component.QuestSearchField
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestTextField
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.feature.catalog.CatalogBuiltInGifPreview
import com.gymquest.app.feature.catalog.CatalogLocalGuidePreview
import com.gymquest.app.feature.catalog.catalogLabel
import com.gymquest.app.feature.catalog.filterCatalog
import kotlinx.coroutines.launch

/** Selección de catálogo para programar un ejercicio, con sus objetivos antes de confirmarlo. */
@Composable
fun RoutineExerciseCatalogScreen(dayId: Long, onClose: () -> Unit) {
    val app = LocalContext.current.applicationContext as GymQuestApp
    val container = app.appContainer
    val repository = container.weeklyTrainingRepository
    val catalog by container.observeExerciseCatalogUseCase().collectAsStateWithLifecycle(emptyList())
    val muscleGroups by container.observeMuscleGroupsUseCase().collectAsStateWithLifecycle(emptyList())
    val media by container.observeExerciseBaseMediaUseCase().collectAsStateWithLifecycle(emptyList())
    val planned by repository.observeExercises(dayId).collectAsStateWithLifecycle(emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    var groupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var equipment by rememberSaveable { mutableStateOf<EquipmentType?>(null) }
    var selectedBaseId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedVariantId by rememberSaveable { mutableStateOf<Long?>(null) }
    var setsText by rememberSaveable { mutableStateOf("3") }
    var repsText by rememberSaveable { mutableStateOf("10") }
    var weightText by rememberSaveable { mutableStateOf("") }
    var restText by rememberSaveable { mutableStateOf("90") }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val visible = catalog.filterCatalog(query, groupId, equipment)
    val selected = catalog.firstOrNull { it.exerciseBase.id == selectedBaseId }
    val selectedMedia = selected?.let { entry -> media.filter { it.ownerId == entry.exerciseBase.id }.maxByOrNull { it.createdAt } }

    QuestScreen {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { QuestSectionHeader("Catálogo de ejercicios", "Consulta la ficha y define las series antes de añadir el ejercicio a la rutina.") }
            if (selected == null) {
                item { QuestPanel {
                    QuestSearchField(query, { query = it })
                    Text("Explorar por parte del cuerpo", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuestFilterChip("Todos", groupId == null, onClick = { groupId = null })
                        muscleGroups.forEach { group ->
                            val count = catalog.count { it.exerciseBase.primaryMuscleGroupId == group.id }
                            if (count > 0) QuestFilterChip("${group.name} ($count)", groupId == group.id, onClick = { groupId = group.id })
                        }
                    }
                    Text("Filtrar por equipo", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuestFilterChip("Todos", equipment == null, onClick = { equipment = null })
                        EquipmentType.entries.forEach { type ->
                            val count = catalog.count { entry -> entry.variants.any { it.equipmentType == type } }
                            if (count > 0) QuestFilterChip("${type.catalogLabel()} ($count)", equipment == type, onClick = { equipment = type })
                        }
                    }
                } }
                items(visible, key = { it.exerciseBase.id }) { entry -> QuestPanel {
                    Text(entry.exerciseBase.name, style = MaterialTheme.typography.titleMedium)
                    Text(entry.exerciseBase.description ?: "Sin descripción", style = MaterialTheme.typography.bodySmall)
                    Text(entry.variants.joinToString { it.equipmentType.catalogLabel() }, style = MaterialTheme.typography.bodySmall)
                    QuestButton("Ver ficha", onClick = {
                        selectedBaseId = entry.exerciseBase.id
                        selectedVariantId = entry.variants.firstOrNull()?.id
                        feedback = null
                    }, action = QuestAction.OpenDetail)
                } }
                if (visible.isEmpty()) item { QuestPanel { Text("No hay ejercicios con esos filtros.") } }
            } else {
                item { QuestPanel {
                    Text(selected.exerciseBase.name, style = MaterialTheme.typography.titleLarge)
                    selected.exerciseBase.description?.let { Text(it) }
                    if (selectedMedia != null || selected.exerciseBase.builtInGifUrl != null) {
                        Text("Guía visual", style = MaterialTheme.typography.titleMedium)
                        if (selectedMedia != null) CatalogLocalGuidePreview(selectedMedia)
                        else CatalogBuiltInGifPreview(requireNotNull(selected.exerciseBase.builtInGifUrl))
                    }
                    selected.exerciseBase.instructions?.let { Text("Cómo hacerlo:\n$it") }
                    Text("Elige una variante", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selected.variants.forEach { variant -> QuestFilterChip(
                            "${variant.name} · ${variant.equipmentType.catalogLabel()}",
                            selectedVariantId == variant.id,
                            onClick = { selectedVariantId = variant.id },
                        ) }
                    }
                    QuestTextField(setsText, { setsText = it }, "Series", singleLine = true)
                    QuestTextField(repsText, { repsText = it }, "Repeticiones objetivo", singleLine = true)
                    QuestTextField(weightText, { weightText = it }, "Peso objetivo (opcional)", singleLine = true)
                    QuestTextField(restText, { restText = it }, "Descanso en segundos", singleLine = true)
                    QuestActionButton(QuestAction.Add, onClick = {
                        val variantId = selectedVariantId
                        val sets = setsText.toIntOrNull()
                        if (variantId == null || sets == null || sets < 1) {
                            feedback = "Elige una variante e indica al menos una serie."
                        } else scope.launch {
                            runCatching {
                                repository.addExercise(dayId, variantId, planned.size, sets, repsText.toIntOrNull(), weightText.replace(',', '.').toDoubleOrNull(), restText.toIntOrNull(), SystemClockProvider.now())
                            }.onSuccess { onClose() }.onFailure { feedback = "No se pudo añadir el ejercicio. Vuelve a intentarlo." }
                        }
                    }, label = "Aceptar y añadir a la rutina", enabled = selectedVariantId != null)
                    feedback?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    QuestButton("Cancelar", onClick = { selectedBaseId = null; selectedVariantId = null }, action = QuestAction.Cancel)
                } }
            }
        }
    }
}
