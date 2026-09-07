package com.gymquest.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.component.CharacterHeader
import com.gymquest.app.core.ui.component.MissionCard
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader

@Composable
fun HomeScreen(
    onOpenSession: () -> Unit,
    onOpenCatalog: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenMartialArts: () -> Unit,
    onOpenSettings: () -> Unit
) {
    QuestScreen {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CharacterHeader(
                    name = "GymQuest",
                    level = 1,
                    currentXp = 0,
                    targetXp = 100,
                    subtitle = "Aventura local de fuerza, tecnica y constancia",
                )
            }
            item {
                MissionCard(
                    title = "Mision principal",
                    description = "Inicia o continua una sesion y registra peso, reps y descanso sin distraerte.",
                    reward = "+ XP al completar series reales",
                    action = {
                        QuestActionButton(
                            action = QuestAction.Start,
                            label = "Entrenar ahora",
                            onClick = onOpenSession,
                        )
                    },
                )
            }
            item {
                QuestSectionHeader(
                    title = "Mapa rapido",
                    subtitle = "Todo funciona localmente: catalogo, historial, progreso y ajustes.",
                )
            }
            item {
                QuestButton(text = "Catalogo de ejercicios", action = QuestAction.Catalog, onClick = onOpenCatalog)
            }
            item {
                QuestButton(text = "Historial de sesiones", action = QuestAction.History, onClick = onOpenHistory)
            }
            item {
                QuestButton(text = "Progreso del personaje", action = QuestAction.Progress, onClick = onOpenProgress)
            }
            item {
                QuestButton(text = "Artes marciales", action = QuestAction.MartialArts, onClick = onOpenMartialArts)
            }
            item {
                QuestButton(text = "Ajustes", action = QuestAction.Settings, onClick = onOpenSettings)
            }
        }
    }
}
