package com.gymquest.app.feature.catalog

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestFilterChip
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestTextField
import com.gymquest.app.domain.model.enums.MediaType
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType

@Composable
fun AddMuscleGroupScreen(onNavigateBack: () -> Unit) {
    val viewModel = rememberCatalogViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    LaunchedEffect(state.lastCreation) {
        if (state.lastCreation == CatalogCreation.MuscleGroup) onNavigateBack()
    }
    CatalogFormLayout("Nuevo grupo muscular") {
        QuestPanel {
            QuestTextField(name, { name = it }, "Nombre del grupo", singleLine = true)
            state.feedback?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            QuestActionButton(QuestAction.Add, label = "Crear grupo", onClick = { viewModel.addMuscleGroup(name) })
            QuestButton("Cancelar", onNavigateBack, action = QuestAction.Back)
        }
    }
}

@Composable
fun AddExerciseScreen(onNavigateBack: () -> Unit) {
    val viewModel = rememberCatalogViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var secondaryMuscles by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var techniqueTips by remember { mutableStateOf("") }
    var commonMistakes by remember { mutableStateOf("") }
    var guide by remember { mutableStateOf<ExerciseMediaDraft?>(null) }
    var pickerError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val mediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val mimeType = uri?.let { context.contentResolver.getType(it)?.lowercase() }
        val mediaType = when {
            mimeType == "image/gif" -> MediaType.GIF
            mimeType?.startsWith("video/") == true -> MediaType.VIDEO
            else -> null
        }
        if (uri == null) Unit else if (mediaType == null) {
            pickerError = "Elige solo un GIF o un vídeo local."
        } else {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                guide = ExerciseMediaDraft(uri.toString(), mimeType!!, context.contentResolver.displayName(uri) ?: "Guía local", mediaType)
                pickerError = null
            } catch (_: SecurityException) {
                pickerError = "No se pudo conservar el acceso al archivo seleccionado."
            }
        }
    }
    LaunchedEffect(state.lastCreation) {
        if (state.lastCreation == CatalogCreation.ExerciseBase) onNavigateBack()
    }
    CatalogFormLayout("Nuevo ejercicio") {
        QuestPanel {
            Text("Grupo muscular", style = MaterialTheme.typography.titleSmall)
            if (state.muscleGroups.isEmpty()) {
                Text("Primero crea un grupo muscular desde el botón +.")
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.muscleGroups.forEach { group ->
                        QuestFilterChip(group.name, state.selectedMuscleGroupId == group.id, onClick = { viewModel.selectMuscleGroup(group.id) })
                    }
                }
            }
            QuestTextField(name, { name = it }, "Ejercicio base", singleLine = true)
            QuestTextField(description, { description = it }, "Descripción opcional")
            QuestTextField(secondaryMuscles, { secondaryMuscles = it }, "Músculos secundarios (separados por comas)")
            QuestTextField(instructions, { instructions = it }, "Pasos de ejecución (uno por línea)")
            QuestTextField(techniqueTips, { techniqueTips = it }, "Consejos de técnica")
            QuestTextField(commonMistakes, { commonMistakes = it }, "Errores comunes")
            guide?.let {
                Text("Guía seleccionada: ${it.title}", style = MaterialTheme.typography.bodySmall)
                QuestButton("Quitar guía", { guide = null }, action = QuestAction.Delete)
            } ?: QuestButton("Añadir guía visual (GIF o vídeo)", { mediaLauncher.launch(arrayOf("image/gif", "video/*")) }, action = QuestAction.Add)
            pickerError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.feedback?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            QuestActionButton(
                QuestAction.Add,
                label = "Crear ejercicio",
                enabled = state.selectedMuscleGroupId != null,
                onClick = {
                    viewModel.addExerciseBase(
                        name = name,
                        description = description,
                        secondaryMuscles = secondaryMuscles,
                        instructions = instructions,
                        techniqueTips = techniqueTips,
                        commonMistakes = commonMistakes,
                        mediaDraft = guide,
                    )
                },
            )
            QuestButton("Cancelar", onNavigateBack, action = QuestAction.Back)
        }
    }
}

@Composable
fun AddExerciseVariantScreen(exerciseBaseId: Long, onNavigateBack: () -> Unit) {
    val viewModel = rememberCatalogViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf(EquipmentType.OTHER) }
    var weightType by remember { mutableStateOf(WeightComparisonType.TOTAL_WEIGHT) }

    LaunchedEffect(state.lastCreation) {
        if (state.lastCreation == CatalogCreation.Variant(exerciseBaseId)) onNavigateBack()
    }

    CatalogFormLayout("Nueva variante") {
        QuestPanel {
            QuestTextField(name, { name = it }, "Nombre de variante", singleLine = true)
            QuestTextField(notes, { notes = it }, "Notas de variante")
            VariantConfigurationSelector(
                equipment = equipment,
                weight = weightType,
                onEquipment = { equipment = it },
                onWeight = { weightType = it },
            )
            state.feedback?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            QuestActionButton(
                QuestAction.Add,
                label = "Crear variante",
                onClick = {
                    viewModel.addExerciseVariant(
                        exerciseBaseId = exerciseBaseId,
                        name = name,
                        equipmentType = equipment,
                        weightComparisonType = weightType,
                        notes = notes,
                    )
                },
            )
            QuestButton("Cancelar", onNavigateBack, action = QuestAction.Back)
        }
    }
}

@Composable
private fun CatalogFormLayout(title: String, content: @Composable () -> Unit) {
    QuestScreen {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { QuestSectionHeader(title, "Completa los datos y guarda para volver al catálogo.") }
            item { content() }
        }
    }
}

private fun android.content.ContentResolver.displayName(uri: Uri): String? =
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        cursor.takeIf { it.moveToFirst() }?.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
    }
