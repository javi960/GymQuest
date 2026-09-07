package com.gymquest.app.feature.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymquest.app.domain.model.WorkoutExerciseDetail
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SetType
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay

@Composable
fun WorkoutExerciseCard(
    exerciseDetail: WorkoutExerciseDetail,
    exerciseName: String,
    onSaveSet: (String, String, SetType) -> Unit,
    onUpdateSet: (WorkoutSet, String, String, SetType) -> Unit,
    onDeleteSet: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(exerciseName, style = MaterialTheme.typography.titleMedium)
            SetHistoryList(
                sets = exerciseDetail.sets,
                onUpdateSet = onUpdateSet,
                onDeleteSet = onDeleteSet,
            )
            RestTimerBar(lastSet = exerciseDetail.sets.lastOrNull())
            SetInputRow(onSaveSet = onSaveSet)
        }
    }
}

@Composable
fun SetHistoryList(
    sets: List<WorkoutSet>,
    onUpdateSet: (WorkoutSet, String, String, SetType) -> Unit,
    onDeleteSet: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (sets.isEmpty()) {
            Text("Sin series guardadas todavia.")
        } else {
            sets.forEach { set ->
                EditableSetRow(
                    set = set,
                    onUpdateSet = onUpdateSet,
                    onDeleteSet = onDeleteSet,
                )
            }
        }
    }
}

@Composable
private fun EditableSetRow(
    set: WorkoutSet,
    onUpdateSet: (WorkoutSet, String, String, SetType) -> Unit,
    onDeleteSet: (Long) -> Unit,
) {
    var weightText by remember(set.id, set.weightValue) { mutableStateOf(set.weightValue.toString()) }
    var repsText by remember(set.id, set.reps) { mutableStateOf(set.reps.toString()) }
    var setType by remember(set.id, set.setType) { mutableStateOf(set.setType) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("${set.setNumber}. ${set.setType.label()} · Descanso previo: ${set.restBeforeSeconds ?: 0}s")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Peso") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = repsText,
                onValueChange = { repsText = it },
                label = { Text("Reps") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }
        SetTypeDropdown(selectedType = setType, onSelectedTypeChange = { setType = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onUpdateSet(set, weightText, repsText, setType) }) {
                Text("Actualizar")
            }
            OutlinedButton(onClick = { onDeleteSet(set.id) }) {
                Text("Eliminar")
            }
        }
    }
}

@Composable
fun RestTimerBar(
    lastSet: WorkoutSet?,
    modifier: Modifier = Modifier,
) {
    val restStartedAt = lastSet?.endedAt ?: return
    var now by remember(restStartedAt) { mutableStateOf(Instant.now()) }
    LaunchedEffect(restStartedAt) {
        while (true) {
            now = Instant.now()
            delay(1_000)
        }
    }
    val restSeconds = restStartedAt.until(now, ChronoUnit.SECONDS).coerceAtLeast(0)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Descanso actual: ${restSeconds}s", style = MaterialTheme.typography.bodySmall)
        LinearProgressIndicator(
            progress = { ((restSeconds % DEFAULT_REST_WINDOW_SECONDS).toFloat() / DEFAULT_REST_WINDOW_SECONDS) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SetInputRow(
    onSaveSet: (String, String, SetType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var weightText by remember { mutableStateOf("") }
    var repsText by remember { mutableStateOf("") }
    var setType by remember { mutableStateOf(SetType.WORK) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Peso") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedTextField(
                value = repsText,
                onValueChange = { repsText = it },
                label = { Text("Reps") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        SetTypeDropdown(selectedType = setType, onSelectedTypeChange = { setType = it })
        Button(
            onClick = {
                onSaveSet(weightText, repsText, setType)
                weightText = ""
                repsText = ""
            },
        ) {
            Text("Guardar serie")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetTypeDropdown(
    selectedType: SetType,
    onSelectedTypeChange: (SetType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedType.label(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SetType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label()) },
                    onClick = {
                        onSelectedTypeChange(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun SetType.label(): String =
    when (this) {
        SetType.WARM_UP -> "Calentamiento"
        SetType.APPROACH -> "Aproximacion"
        SetType.WORK -> "Trabajo"
        SetType.TOP_SET -> "Top set"
        SetType.BACK_OFF -> "Back off"
        SetType.DROPSET -> "Dropset"
        SetType.REST_PAUSE -> "Rest pause"
    }

private const val DEFAULT_REST_WINDOW_SECONDS = 180
