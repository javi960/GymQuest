package com.gymquest.app.feature.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.component.CharacterHeader
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.MissionCard
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.StatBadge

@Composable
fun ProgressScreen() {
    QuestScreen {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CharacterHeader(
                    name = "Aspirante de hierro",
                    level = 1,
                    currentXp = 0,
                    targetXp = 100,
                    subtitle = "Escenario visual listo para conectar el repositorio de progreso.",
                )
            }
            item {
                QuestSectionHeader(
                    title = "Resumen de progreso",
                    subtitle = "Preparado para fuerza, volumen, records y dominio sin graficos confusos.",
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBadge(label = "volumen", value = "0 kg")
                    StatBadge(label = "series", value = "0")
                    StatBadge(label = "records", value = "0")
                }
            }
            item {
                MissionCard(
                    title = "Primer hito",
                    description = "Completa una sesion para activar calculos de XP, nivel y resumen historico.",
                    reward = "Desbloquea estadisticas reales",
                )
            }
            item {
                EmptyAdventureState(
                    title = "Aun no hay progresion calculada",
                    description = "El siguiente paso tecnico sera conectar CharacterStats, ExerciseMastery y logros.",
                )
            }
        }
    }
}
