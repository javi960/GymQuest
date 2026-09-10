package com.gymquest.app.feature.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.time.SystemClockProvider
import com.gymquest.app.data.local.entity.MartialReminderSettingsEntity
import kotlinx.coroutines.launch
import com.gymquest.app.core.ui.component.MissionCard
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestTextField
import com.gymquest.app.core.ui.component.QuestFilterChip

@Composable
fun SettingsScreen(onOpenBackup: () -> Unit) {
    val app = LocalContext.current.applicationContext as GymQuestApp
    val container = app.appContainer
    var remindersEnabled by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var missionsPerDay by remember { mutableStateOf(1) }
    var privacyMode by remember { mutableStateOf("generic") }
    var startTime by remember { mutableStateOf("18:00") }
    var endTime by remember { mutableStateOf("20:00") }
    var editingWindowId by remember { mutableStateOf<Long?>(null) }
    var windows by remember { mutableStateOf(emptyList<com.gymquest.app.data.local.entity.MartialReminderWindowEntity>()) }
    val scope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        feedback = if (granted) "Permiso concedido. Los recordatorios se planificarán en tus ventanas." else "Permiso no concedido: puedes practicar manualmente y activarlo más tarde."
        scope.launch { container.martialReminderCoordinator.replan() }
    }
    LaunchedEffect(Unit) {
        when (val result = container.martialReminderRepository.settings()) {
            is com.gymquest.app.core.result.AppResult.Success -> {
                remindersEnabled = result.value?.enabled == true
                missionsPerDay = result.value?.missionsPerDay ?: 1
                privacyMode = result.value?.privacyMode ?: "generic"
                val savedWindows = container.martialReminderRepository.windows()
                if (savedWindows is com.gymquest.app.core.result.AppResult.Success) windows = savedWindows.value
            }
            is com.gymquest.app.core.result.AppResult.Failure -> feedback = result.error.message
        }
    }
    QuestScreen {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                QuestSectionHeader(
                    title = "Ajustes",
                    subtitle = "Preferencias preparadas para tema, unidades, accesibilidad y vibracion.",
                )
            }
            item {
                MissionCard(
                    title = "Frecuencia diaria",
                    description = if (missionsPerDay == 0) "Pausada: se conserva la configuración." else "$missionsPerDay misión(es) secundaria(s) al día.",
                    reward = "Límite: 0–5",
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..5).forEach { value -> QuestFilterChip(label = value.toString(), selected = missionsPerDay == value, onClick = {
                        val now = SystemClockProvider.now()
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            val current = container.martialReminderRepository.settings()
                            val previous = (current as? com.gymquest.app.core.result.AppResult.Success)?.value
                            val result = container.martialReminderRepository.saveSettings(MartialReminderSettingsEntity(enabled = remindersEnabled, missionsPerDay = value, privacyMode = previous?.privacyMode ?: "generic", maxDailyXp = previous?.maxDailyXp ?: 20, createdAt = previous?.createdAt ?: now, updatedAt = now))
                            if (result is com.gymquest.app.core.result.AppResult.Success) { missionsPerDay = value; container.martialReminderCoordinator.replan() } else if (result is com.gymquest.app.core.result.AppResult.Failure) feedback = result.error.message
                        }
                    }) }
                }
            }
            item {
                MissionCard(
                    title = "Privacidad de la notificación",
                    description = if (privacyMode == "detailed") "Muestra el contenido de la misión en la notificación." else "Muestra un recordatorio genérico sin revelar el contenido.",
                    reward = if (privacyMode == "detailed") "Detallada" else "Discreta",
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("generic" to "Discreta", "detailed" to "Detallada").forEach { (value, label) ->
                        QuestFilterChip(label = label, selected = privacyMode == value, onClick = {
                            val now = SystemClockProvider.now()
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                val current = container.martialReminderRepository.settings()
                                val previous = (current as? com.gymquest.app.core.result.AppResult.Success)?.value
                                val saved = container.martialReminderRepository.saveSettings(MartialReminderSettingsEntity(
                                    enabled = remindersEnabled, missionsPerDay = missionsPerDay, privacyMode = value,
                                    maxDailyXp = previous?.maxDailyXp ?: 20, createdAt = previous?.createdAt ?: now, updatedAt = now,
                                ))
                                if (saved is com.gymquest.app.core.result.AppResult.Success) { privacyMode = value; container.martialReminderCoordinator.replan() } else if (saved is com.gymquest.app.core.result.AppResult.Failure) feedback = saved.error.message
                            }
                        })
                    }
                }
            }
            item {
                MissionCard(title = "Ventanas horarias", description = "Las misiones solo se programan dentro de estas franjas locales.", reward = "${windows.size} configurada(s)")
                windows.forEach { window ->
                    QuestButton(text = "Editar ${window.startLocalTime}–${window.endLocalTime}", action = QuestAction.Edit, onClick = {
                        editingWindowId = window.id
                        startTime = window.startLocalTime
                        endTime = window.endLocalTime
                    })
                    QuestButton(text = "Eliminar ${window.startLocalTime}–${window.endLocalTime}", action = QuestAction.Delete, onClick = {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            container.martialReminderRepository.removeWindow(window.id)
                            windows = container.martialReminderRepository.windows().let { (it as? com.gymquest.app.core.result.AppResult.Success)?.value.orEmpty() }
                            container.martialReminderCoordinator.replan()
                        }
                    })
                }
                QuestTextField(value = startTime, onValueChange = { startTime = it }, label = "Inicio (HH:mm)", singleLine = true)
                QuestTextField(value = endTime, onValueChange = { endTime = it }, label = "Fin (HH:mm)", singleLine = true)
                QuestButton(text = if (editingWindowId == null) "Añadir ventana" else "Guardar ventana", action = if (editingWindowId == null) QuestAction.Add else QuestAction.Edit, onClick = {
                    val now = SystemClockProvider.now()
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        val editing = windows.firstOrNull { it.id == editingWindowId }
                        val result = if (editing == null) {
                            container.martialReminderRepository.addWindow(com.gymquest.app.data.local.entity.MartialReminderWindowEntity(startLocalTime = startTime, endLocalTime = endTime, sortOrder = windows.size, createdAt = now, updatedAt = now))
                        } else {
                            container.martialReminderRepository.updateWindow(editing.copy(startLocalTime = startTime, endLocalTime = endTime, updatedAt = now))
                        }
                        if (result is com.gymquest.app.core.result.AppResult.Success) { windows = container.martialReminderRepository.windows().let { (it as? com.gymquest.app.core.result.AppResult.Success)?.value.orEmpty() }; container.martialReminderCoordinator.replan() } else if (result is com.gymquest.app.core.result.AppResult.Failure) feedback = result.error.message
                        editingWindowId = null
                    }
                })
            }
            item {
                MissionCard(
                    title = "Tema activo",
                    description = "ClassicQuestTheme: fantasia JRPG luminosa, datos legibles y recursos 100% locales.",
                    reward = "Preset base instalado",
                )
            }
            item {
                MissionCard(
                    title = "Accesibilidad",
                    description = "Los botones iconicos comparten descripciones accesibles y area tactil minima.",
                    reward = "Lista para pruebas con TalkBack",
                )
            }
            item {
                MissionCard(
                    title = "Recordatorios marciales",
                    description = if (remindersEnabled) "Activos: recibirás misiones solo dentro de tus ventanas horarias." else "Desactivados. No se solicitará permiso ni se programarán alarmas.",
                    reward = if (remindersEnabled) "Hasta 5 misiones al día" else "Privacidad local",
                )
            }
            item {
                QuestButton(
                    text = if (remindersEnabled) "Desactivar recordatorios" else "Activar recordatorios",
                    action = if (remindersEnabled) QuestAction.Delete else QuestAction.Add,
                    onClick = {
                        val now = SystemClockProvider.now()
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            if (!remindersEnabled) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            val current = container.martialReminderRepository.settings()
                            val previous = (current as? com.gymquest.app.core.result.AppResult.Success)?.value
                            val saved = container.martialReminderRepository.saveSettings(MartialReminderSettingsEntity(
                                enabled = !remindersEnabled,
                                missionsPerDay = if (!remindersEnabled) (previous?.missionsPerDay?.takeIf { it > 0 } ?: 1) else 0,
                                privacyMode = previous?.privacyMode ?: "generic",
                                maxDailyXp = previous?.maxDailyXp ?: 20,
                                createdAt = previous?.createdAt ?: now,
                                updatedAt = now,
                            ))
                            if (saved is com.gymquest.app.core.result.AppResult.Success) {
                                remindersEnabled = !remindersEnabled
                                container.martialReminderCoordinator.replan()
                            } else if (saved is com.gymquest.app.core.result.AppResult.Failure) feedback = saved.error.message
                        }
                    },
                )
            }
            feedback?.let { message -> item { MissionCard(title = "Recordatorios", description = message, reward = "") } }
            item {
                QuestButton(
                    text = "Copias de seguridad",
                    action = QuestAction.Export,
                    onClick = onOpenBackup,
                )
            }
        }
    }
}
