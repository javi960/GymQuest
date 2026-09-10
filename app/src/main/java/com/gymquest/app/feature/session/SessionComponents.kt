package com.gymquest.app.feature.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestDenseDataRow
import com.gymquest.app.core.ui.component.QuestDenseMetric
import com.gymquest.app.core.ui.component.QuestNumericField
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestSingleChoiceMenu
import com.gymquest.app.core.ui.component.QuestSymbol
import com.gymquest.app.core.ui.component.StatBadge
import com.gymquest.app.core.ui.component.StatBadgeRow
import com.gymquest.app.core.ui.theme.QuestTheme
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
    setSaveState: SetSaveState,
    manualRestTimer: ManualRestTimerState,
    manualRestError: String?,
    onStartRest: () -> Unit,
    onPauseRest: () -> Unit,
    onResumeRest: () -> Unit,
    onAdvanceRest: () -> Unit,
    onRestTargetChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    QuestPanel(modifier = modifier.fillMaxWidth()) {
        Text(exerciseName, style = MaterialTheme.typography.titleMedium)
        StatBadgeRow {
            StatBadge(label = "series", value = exerciseDetail.sets.size.toString())
            StatBadge(label = "volumen", value = "${exerciseDetail.sets.sumOf { it.volume }} kg")
        }
        SetHistoryList(
            sets = exerciseDetail.sets,
            onUpdateSet = onUpdateSet,
            onDeleteSet = onDeleteSet,
        )
        RestTimerBar(
            lastSet = exerciseDetail.sets.lastOrNull(),
            timer = manualRestTimer,
            errorMessage = manualRestError,
            onStart = onStartRest,
            onPause = onPauseRest,
            onResume = onResumeRest,
            onTick = onAdvanceRest,
            onTargetChange = onRestTargetChange,
        )
        SetInputRow(onSaveSet = onSaveSet, saveState = setSaveState)
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
    var weightError by remember(set.id) { mutableStateOf<String?>(null) }
    var repsError by remember(set.id) { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            imageVector = QuestSymbol.SavedSet.icon,
            contentDescription = null,
            tint = QuestTheme.tokens.colors.positive,
        )
        QuestDenseDataRow(
            metrics = listOf(
                QuestDenseMetric("Serie", set.setNumber.toString()),
                QuestDenseMetric("Guardada", "${set.weightValue} kg x ${set.reps} reps"),
                QuestDenseMetric("Descanso", "${set.restBeforeSeconds ?: 0} s"),
            ),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestNumericField(
                value = weightText,
                onValueChange = { weightText = it; weightError = null },
                label = "Peso",
                unit = "kg",
                errorMessage = weightError,
                modifier = Modifier.weight(1f),
            )
            QuestNumericField(
                value = repsText,
                onValueChange = { repsText = it; repsError = null },
                label = "Reps",
                unit = "reps",
                integerOnly = true,
                errorMessage = repsError,
                modifier = Modifier.weight(1f),
            )
        }
        SetTypeDropdown(selectedType = setType, onSelectedTypeChange = { setType = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestActionButton(
                action = QuestAction.Save,
                label = "Actualizar",
                onClick = {
                    val errors = validateSetInputs(weightText, repsText)
                    weightError = errors.weight
                    repsError = errors.reps
                    if (errors.isValid) onUpdateSet(set, weightText, repsText, setType)
                },
            )
            QuestActionButton(
                action = QuestAction.Delete,
                label = "Eliminar",
                onClick = { onDeleteSet(set.id) },
            )
        }
    }
}

@Composable
fun RestTimerBar(
    lastSet: WorkoutSet?,
    timer: ManualRestTimerState,
    errorMessage: String?,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onTick: () -> Unit,
    onTargetChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val restStartedAt = lastSet?.endedAt ?: return
    var targetText by remember(lastSet.workoutExerciseId, timer.targetSeconds) {
        mutableStateOf(timer.targetSeconds.toString())
    }
    LaunchedEffect(timer.isRunning, lastSet.id) {
        while (timer.isRunning) {
            delay(1_000)
            onTick()
        }
    }
    val actualRestSeconds = restStartedAt.until(Instant.now(), ChronoUnit.SECONDS).coerceAtLeast(0)
    val timerState = if (timer.isRunning) "en marcha" else "en pausa"
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Descanso registrado: $actualRestSeconds segundos. " +
                    "Temporizador manual $timerState: ${timer.elapsedSeconds} de ${timer.targetSeconds} segundos."
                progressBarRangeInfo = ProgressBarRangeInfo(timer.progress, 0f..1f)
            },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Descanso registrado: ${actualRestSeconds}s", style = MaterialTheme.typography.bodySmall)
        QuestNumericField(
            value = targetText,
            onValueChange = { value ->
                targetText = value
                onTargetChange(value)
            },
            label = "Objetivo de descanso",
            unit = "s",
            integerOnly = true,
            errorMessage = errorMessage,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Temporizador manual: ${timer.elapsedSeconds}s de ${timer.targetSeconds}s",
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestActionButton(
                action = QuestAction.Start,
                label = "Reiniciar descanso",
                onClick = onStart,
            )
            QuestActionButton(
                action = if (timer.isRunning) QuestAction.Pause else QuestAction.Resume,
                label = if (timer.isRunning) "Pausar descanso" else "Reanudar descanso",
                onClick = if (timer.isRunning) onPause else onResume,
            )
        }
        LinearProgressIndicator(
            progress = { timer.progress },
            color = QuestTheme.tokens.colors.xpGold,
            trackColor = QuestTheme.tokens.colors.panelBorder,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun SetInputRow(
    onSaveSet: (String, String, SetType) -> Unit,
    saveState: SetSaveState = SetSaveState.Idle,
    modifier: Modifier = Modifier,
) {
    var weightText by remember { mutableStateOf("") }
    var repsText by remember { mutableStateOf("") }
    var setType by remember { mutableStateOf(SetType.WORK) }
    var weightError by remember { mutableStateOf<String?>(null) }
    var repsError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(saveState) {
        if (saveState is SetSaveState.Saved) {
            weightText = ""
            repsText = ""
        }
    }
    val isSaving = saveState is SetSaveState.Saving
    val saveError = (saveState as? SetSaveState.Failed)?.message
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestNumericField(
                value = weightText,
                onValueChange = { weightText = it; weightError = null },
                label = "Peso",
                unit = "kg",
                errorMessage = weightError,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
            )
            QuestNumericField(
                value = repsText,
                onValueChange = { repsText = it; repsError = null },
                label = "Reps",
                unit = "reps",
                integerOnly = true,
                errorMessage = repsError,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
            )
        }
        SetTypeDropdown(selectedType = setType, onSelectedTypeChange = { setType = it }, enabled = !isSaving)
        saveError?.let { Text(it, color = QuestTheme.tokens.colors.error, style = MaterialTheme.typography.bodySmall) }
        QuestActionButton(
            action = QuestAction.Save,
            label = if (isSaving) "Guardando…" else "Guardar serie",
            enabled = !isSaving,
            onClick = {
                val errors = validateSetInputs(weightText, repsText)
                weightError = errors.weight
                repsError = errors.reps
                if (errors.isValid) {
                    onSaveSet(weightText, repsText, setType)
                }
            },
        )
    }
}

private data class SetInputErrors(
    val weight: String? = null,
    val reps: String? = null,
) {
    val isValid: Boolean get() = weight == null && reps == null
}

private fun validateSetInputs(weightInput: String, repsInput: String): SetInputErrors {
    val weight = weightInput.replace(',', '.').toDoubleOrNull()
    val reps = repsInput.toIntOrNull()
    return SetInputErrors(
        weight = if (weight == null || weight < 0) "Introduce un peso válido de 0 kg o más." else null,
        reps = if (reps == null || reps <= 0) "Introduce al menos 1 repetición." else null,
    )
}

@Composable
private fun SetTypeDropdown(
    selectedType: SetType,
    onSelectedTypeChange: (SetType) -> Unit,
    enabled: Boolean = true,
) {
    QuestSingleChoiceMenu(
        selected = selectedType,
        options = SetType.entries,
        label = "Tipo",
        optionLabel = SetType::label,
        onSelectedChange = onSelectedTypeChange,
        enabled = enabled,
    )
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
