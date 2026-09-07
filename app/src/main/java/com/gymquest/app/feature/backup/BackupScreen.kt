package com.gymquest.app.feature.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.MissionCard
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader

@Composable
fun BackupScreen() {
    QuestScreen {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                QuestSectionHeader(
                    title = "Backup",
                    subtitle = "Exportacion local y restauracion explicita, sin metaforas que oculten consecuencias.",
                )
            }
            item {
                MissionCard(
                    title = "Cofre de datos",
                    description = "Espacio reservado para exportar, validar y restaurar JSON versionado.",
                    reward = "Prioridad: seguridad y claridad",
                )
            }
            item {
                EmptyAdventureState(
                    title = "Exportacion pendiente",
                    description = "Aun no hay repositorio de backup. La pantalla ya marca jerarquia, advertencias futuras y accion explicita.",
                )
            }
        }
    }
}
