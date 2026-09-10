package com.gymquest.app.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import com.gymquest.app.core.ui.theme.QuestTheme

/** A numeric input with an explicit unit and a localized inline validation message. */
@Composable
fun QuestNumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    unit: String,
    modifier: Modifier = Modifier,
    integerOnly: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
) {
    val allowedInput = if (integerOnly) {
        { text: String -> text.filter(Char::isDigit) }
    } else {
        { text: String -> text.filter { it.isDigit() || it == ',' || it == '.' } }
    }
    val keyboardType = if (integerOnly) KeyboardType.Number else KeyboardType.Decimal
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(allowedInput(it)) },
        label = { Text(label) },
        suffix = { Text(unit) },
        supportingText = errorMessage?.let { message -> { Text(message) } },
        isError = errorMessage != null,
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(QuestTheme.tokens.radii.field),
        modifier = modifier.semantics {
            contentDescription = "$label, unidad $unit"
            stateDescription = errorMessage ?: "$label en $unit"
        },
    )
}

@Composable
fun QuestSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Buscar ejercicios",
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(QuestTheme.tokens.radii.field),
        modifier = modifier.fillMaxWidth(),
    )
}

/** A shared text field for short names, optional descriptions, and notes. */
@Composable
fun QuestTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        enabled = enabled,
        shape = RoundedCornerShape(QuestTheme.tokens.radii.field),
        modifier = modifier.fillMaxWidth(),
    )
}

/** A shared, read-only selector for a closed list of choices. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> QuestSingleChoiceMenu(
    selected: T,
    options: List<T>,
    label: String,
    optionLabel: (T) -> String,
    onSelectedChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = enabled && it },
    ) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(QuestTheme.tokens.radii.field),
            modifier = modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelectedChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun QuestFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        enabled = enabled,
        modifier = modifier.semantics {
            stateDescription = if (selected) "Seleccionado" else "No seleccionado"
        },
    )
}

data class QuestDenseMetric(
    val label: String,
    val value: String,
)

/** Keeps comparable training values on one compact, accessible row. */
@Composable
fun QuestDenseDataRow(
    metrics: List<QuestDenseMetric>,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val rowDescription = metrics.joinToString(separator = ", ") { "${it.label}: ${it.value}" }
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = actions == null) { contentDescription = rowDescription },
        horizontalArrangement = Arrangement.spacedBy(QuestTheme.tokens.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(QuestTheme.tokens.spacing.xs),
    ) {
        metrics.forEach { metric ->
            Text(
                text = "${metric.label}: ${metric.value}",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = QuestTheme.tokens.colors.textSecondary,
            )
        }
        if (actions != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(QuestTheme.tokens.spacing.sm),
                content = actions,
            )
        }
    }
}
