package com.gymquest.app.feature.martialarts

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
fun MartialArtsHomeScreen() {
    QuestScreen {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                QuestSectionHeader(
                    title = "Dojo",
                    subtitle = "Modulo marcial integrado al mismo sistema visual, listo para un preset futuro.",
                )
            }
            item {
                MissionCard(
                    title = "Practica tecnica",
                    description = "Aqui viviran estilos, tecnicas, recordatorios y misiones secundarias de recompensa reducida.",
                    reward = "Comparte XP general",
                )
            }
            item {
                EmptyAdventureState(
                    title = "Contenido marcial pendiente",
                    description = "La pantalla ya usa paneles, iconografia y lenguaje comun para no quedar separada del gimnasio.",
                )
            }
        }
    }
}
