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
import com.gymquest.app.feature.catalog.catalogLabel
import com.gymquest.app.feature.catalog.filterCatalog
import com.gymquest.app.feature.catalog.CatalogBuiltInGifPreview
import com.gymquest.app.feature.catalog.CatalogLocalGuidePreview
import com.gymquest.app.domain.model.enums.EquipmentType
import kotlinx.coroutines.launch

/** Catálogo de selección para una sesión: la adición se confirma desde la ficha. */
@Composable
fun SessionExerciseCatalogScreen(sessionId: Long, onClose: () -> Unit) {
    val app = LocalContext.current.applicationContext as GymQuestApp
    val container = app.appContainer
    val catalog by container.observeExerciseCatalogUseCase().collectAsStateWithLifecycle(emptyList())
    val muscleGroups by container.observeMuscleGroupsUseCase().collectAsStateWithLifecycle(emptyList())
    val media by container.observeExerciseBaseMediaUseCase().collectAsStateWithLifecycle(emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    var groupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var equipment by rememberSaveable { mutableStateOf<EquipmentType?>(null) }
    var selectedBaseId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedVariantId by rememberSaveable { mutableStateOf<Long?>(null) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val visible = catalog.filterCatalog(query, groupId, equipment)
    val selected = catalog.firstOrNull { it.exerciseBase.id == selectedBaseId }
    val selectedMedia = selected?.let { entry -> media.filter { it.ownerId == entry.exerciseBase.id }.maxByOrNull { it.createdAt } }

    QuestScreen {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { QuestSectionHeader("Catálogo de ejercicios", "Toca una ficha para revisar sus datos y decidir si añadirla a la sesión.") }
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
                        if (selectedMedia != null) {
                            CatalogLocalGuidePreview(selectedMedia)
                        } else {
                            CatalogBuiltInGifPreview(requireNotNull(selected.exerciseBase.builtInGifUrl))
                        }
                    }
                    selected.exerciseBase.instructions?.let { Text("Cómo hacerlo:\n$it") }
                    selected.exerciseBase.techniqueTips?.let { Text("Consejos: $it") }
                    Text("Elige una variante", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selected.variants.forEach { variant ->
                            QuestFilterChip(
                                "${variant.name} · ${variant.equipmentType.catalogLabel()}",
                                selectedVariantId == variant.id,
                                onClick = { selectedVariantId = variant.id },
                            )
                        }
                    }
                    QuestActionButton(QuestAction.Add, onClick = {
                        val variantId = selectedVariantId
                        if (variantId == null) {
                            feedback = "Elige una variante antes de añadir el ejercicio."
                        } else scope.launch {
                            runCatching {
                                container.weeklyTrainingRepository.addExerciseToSession(sessionId, variantId, SystemClockProvider.now())
                            }.onSuccess {
                                onClose()
                            }.onFailure {
                                feedback = "No se pudo añadir el ejercicio. Vuelve a intentarlo."
                            }
                        }
                    }, label = "Aceptar y añadir a la sesión", enabled = selectedVariantId != null)
                    feedback?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    QuestButton("Cancelar", onClick = { selectedBaseId = null; selectedVariantId = null }, action = QuestAction.Cancel)
                } }
            }
        }
    }
}
