package com.gymquest.app.feature.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.time.SystemClockProvider
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestTextField
import com.gymquest.app.core.ui.component.QuestFilterChip
import com.gymquest.app.domain.model.RecordedWorkingSet
import com.gymquest.app.domain.model.WeightRecommendationCalculator
import com.gymquest.app.data.local.entity.TrainingSessionSetEntity
import androidx.compose.foundation.layout.FlowRow
import kotlinx.coroutines.launch

@Composable
fun ActiveTrainingSessionScreen(sessionId: Long, onOpenExerciseCatalog: () -> Unit, onFinished: () -> Unit) {
    val container = (LocalContext.current.applicationContext as GymQuestApp).appContainer
    val repository = container.weeklyTrainingRepository
    val variants by container.observeActiveExerciseVariantsUseCase().collectAsStateWithLifecycle(emptyList())
    val exercises by repository.observeSessionExercises(sessionId).collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    QuestScreen { LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { QuestSectionHeader("Sesión activa", "Cada ejercicio con una serie registrada aporta +15 XP; finalizar la sesión aporta +40 XP.") }
        item { QuestPanel {
            Text("Añadir ejercicio", style = MaterialTheme.typography.titleMedium)
            Text("Abre el catálogo para consultar la ficha antes de añadirlo a la sesión.")
            QuestActionButton(QuestAction.Add, onClick = onOpenExerciseCatalog, label = "Buscar en el catálogo")
        } }
        items(exercises, key = { it.id }) { exercise ->
            val sets by repository.observeSets(exercise.id).collectAsStateWithLifecycle(emptyList())
            var weight by rememberSaveable(exercise.id) { mutableStateOf(exercise.plannedWeight?.toString().orEmpty()) }
            var reps by rememberSaveable(exercise.id) { mutableStateOf(exercise.plannedReps?.toString().orEmpty()) }
            var setType by rememberSaveable(exercise.id) { mutableStateOf("Efectiva") }
            var editingSet by remember(exercise.id) { mutableStateOf<TrainingSessionSetEntity?>(null) }
            var lastWorkingSet by remember(exercise.id) { mutableStateOf<RecordedWorkingSet?>(null) }
            var usesComparableHistory by remember(exercise.id) { mutableStateOf(false) }
            LaunchedEffect(exercise.exerciseVariantId) {
                val exact = repository.latestEffectiveSet(exercise.exerciseVariantId)
                lastWorkingSet = exact ?: repository.latestComparableSet(exercise.exerciseVariantId)
                usesComparableHistory = exact == null && lastWorkingSet != null
            }
            QuestPanel {
                val name = variants.firstOrNull { it.id == exercise.exerciseVariantId }?.name ?: "Ejercicio"
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text("Objetivo: ${exercise.plannedSets} series · ${exercise.plannedReps ?: "—"} repeticiones")
                lastWorkingSet?.let { previous ->
                    Text(if (usesComparableHistory) "Referencia comparable: ${previous.weight} kg × ${previous.reps}" else "Última vez: ${previous.weight} kg × ${previous.reps}", style = MaterialTheme.typography.bodySmall)
                    listOf(5, 8, 12).forEach { target ->
                        val recommendation = WeightRecommendationCalculator.recommend(previous, target, SystemClockProvider.now()) ?: return@forEach
                        Text("$target reps: ${recommendation.suggestedWeight} kg" + if (recommendation.returnToTraining) " · -${recommendation.reductionPercent}% por vuelta" else "", style = MaterialTheme.typography.bodySmall)
                    }
                }
                QuestTextField(weight, { weight = it }, "Peso real", singleLine = true)
                QuestTextField(reps, { reps = it }, "Repeticiones reales", singleLine = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Calentamiento", "Efectiva", "Drop set", "Al fallo").forEach { type -> QuestFilterChip(type, type == setType, onClick = { setType = type }) } }
                QuestActionButton(QuestAction.Save, onClick = {
                    val parsedWeight = weight.replace(',', '.').toDoubleOrNull(); val parsedReps = reps.toIntOrNull()
                    if (parsedWeight != null && parsedReps != null && parsedReps > 0) scope.launch {
                        editingSet?.let { repository.updateSet(it, parsedWeight, parsedReps, setType, SystemClockProvider.now()) } ?: repository.addSet(exercise.id, sets.size + 1, parsedWeight, parsedReps, setType, SystemClockProvider.now())
                        editingSet = null
                    }
                }, label = if (editingSet == null) "Guardar serie" else "Actualizar serie")
                sets.forEach { set ->
                    Text("Serie ${set.setNumber}: ${set.weight} kg × ${set.reps} · ${set.setType}")
                    QuestButton("Editar serie ${set.setNumber}", onClick = { editingSet = set; weight = set.weight.toString(); reps = set.reps.toString(); setType = set.setType }, action = QuestAction.Edit)
                    QuestButton("Eliminar serie ${set.setNumber}", onClick = { scope.launch { repository.deleteSet(set.id) } }, action = QuestAction.Delete)
                }
            }
        }
        item { QuestActionButton(QuestAction.Finish, onClick = { scope.launch { repository.finishSession(sessionId, SystemClockProvider.now()); onFinished() } }, label = "Finalizar sesión · +40 XP") }
    } }
}
