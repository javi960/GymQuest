package com.gymquest.app.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun QuestConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmAction: QuestAction = QuestAction.Finish,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            QuestActionButton(
                action = confirmAction,
                label = confirmLabel,
                onClick = onConfirm,
            )
        },
        dismissButton = {
            QuestButton(
                text = "Volver",
                action = QuestAction.Back,
                onClick = onDismiss,
            )
        },
    )
}
