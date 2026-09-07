package com.gymquest.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.component.MissionCard
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader

@Composable
fun SettingsScreen() {
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
        }
    }
}
