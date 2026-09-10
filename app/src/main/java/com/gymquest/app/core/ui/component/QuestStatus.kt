package com.gymquest.app.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.theme.QuestTheme

enum class QuestFeedbackEvent { SetSaved, RestStarted, SessionCompleted, LevelUp, PersonalRecord, ExerciseDiscovered }

fun QuestFeedbackEvent.message(): String = when (this) {
    QuestFeedbackEvent.SetSaved -> "Serie guardada"
    QuestFeedbackEvent.RestStarted -> "Descanso iniciado"
    QuestFeedbackEvent.SessionCompleted -> "Sesión completada"
    QuestFeedbackEvent.LevelUp -> "Nivel alcanzado"
    QuestFeedbackEvent.PersonalRecord -> "Nuevo récord personal"
    QuestFeedbackEvent.ExerciseDiscovered -> "Ejercicio descubierto"
}

/** Persistent success/error feedback: icon + text, never color alone. */
@Composable
fun QuestStatusMessage(
    message: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val tokens = QuestTheme.tokens
    QuestPanel(modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Icon(
            imageVector = if (isError) Icons.Filled.ErrorOutline else Icons.Filled.CheckCircle,
            contentDescription = if (isError) "Error" else "Correcto",
            tint = if (isError) tokens.colors.error else tokens.colors.positive,
        )
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

/** A shape-preserving loading placeholder; do not replace it with an indeterminate full-screen spinner. */
@Composable
fun QuestSkeleton(
    lines: Int = 3,
    modifier: Modifier = Modifier,
) {
    val tokens = QuestTheme.tokens
    QuestPanel(modifier) {
        repeat(lines.coerceIn(1, 6)) { index ->
            androidx.compose.foundation.layout.Box(
                Modifier
                    .fillMaxWidth(if (index == lines - 1) 0.6f else 1f)
                    .height(16.dp)
                    .background(tokens.colors.disabled.copy(alpha = 0.25f)),
            )
        }
    }
}
