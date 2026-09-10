package com.gymquest.app.feature.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.ui.component.MissionCard
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButtonState
import com.gymquest.app.core.ui.component.QuestConfirmationDialog
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestStatusMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BackupScreen() {
    val context = LocalContext.current
    val exportBackup = (context.applicationContext as GymQuestApp).appContainer.exportBackupJsonUseCase
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val documentLauncher = rememberLauncherForActivityResult(CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val stream = context.contentResolver.openOutputStream(uri)
        if (stream == null) {
            statusMessage = "No se pudo abrir la ubicación elegida."
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            isExporting = true
            val result: AppResult<Unit> = withContext(Dispatchers.IO) { stream.use { output -> exportBackup(output) } }
            isExporting = false
            statusMessage = when (result) {
                is AppResult.Success -> "Copia de seguridad exportada correctamente."
                is AppResult.Failure -> result.error.message
            }
        }
    }
    BackupContent(isExporting, statusMessage) { documentLauncher.launch("gymquest-backup.json") }
}

@Composable
internal fun BackupContent(
    isExporting: Boolean,
    statusMessage: String?,
    onRequestDocument: () -> Unit,
) {
    var showConfirmation by remember { mutableStateOf(false) }
    QuestScreen {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                QuestSectionHeader(
                    title = "Backup",
                    subtitle = "Exportación local manual. No se suben datos a ningún servicio.",
                )
            }
            item {
                MissionCard(
                    title = "Copia de seguridad JSON",
                    description = "Incluye tus datos locales de entrenamiento. No incluye multimedia ni registros de diagnóstico.",
                    reward = "Formato versionado para restauración futura",
                    action = {
                        QuestActionButton(
                            action = QuestAction.Export,
                            label = if (isExporting) "Exportando…" else "Exportar JSON",
                            state = if (isExporting) QuestButtonState.Loading else QuestButtonState.Normal,
                            onClick = { showConfirmation = true },
                        )
                    },
                )
            }
            statusMessage?.let { message -> item { QuestStatusMessage(message = message, isError = message.startsWith("No se pudo")) } }
        }
    }
    if (showConfirmation) {
        QuestConfirmationDialog(
            title = "Exportar copia de seguridad",
            message = "Este archivo contiene datos personales de entrenamiento.",
            confirmLabel = "Elegir ubicación y exportar",
            confirmAction = QuestAction.Export,
            onConfirm = { showConfirmation = false; onRequestDocument() },
            onDismiss = { showConfirmation = false },
        )
    }
}
