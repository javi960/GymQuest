package com.gymquest.app.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.theme.GymQuestTheme

@Composable private fun QuestComponentsPreviewContent() = QuestScreen {
    Column(Modifier.padding(16.dp)) {
        QuestSectionHeader("Registro de serie extraordinariamente largo", "Comprueba texto traducible, números grandes y unidades visibles en una pantalla compacta.")
        QuestNumericField("1234,75", {}, "Peso", "kg", errorMessage = "El peso máximo permitido depende del equipo seleccionado.")
        QuestNumericField("999", {}, "Repeticiones", "reps", integerOnly = true)
        QuestDenseDataRow(listOf(QuestDenseMetric("Peso", "1.234,75 kg"), QuestDenseMetric("Reps", "999"), QuestDenseMetric("Volumen", "1.233.515,25 kg")))
        QuestSearchField("sentadilla con pausa y barra de seguridad", {}, label = "Buscar contenido técnico")
        QuestFilterChip("Grupo muscular actual", selected = true, onClick = {})
    }
}
@Preview(name = "Componentes compacto", widthDp = 320) @Composable fun QuestComponentsCompactPreview() = GymQuestTheme { QuestComponentsPreviewContent() }
@Preview(name = "Componentes fuente 200%", widthDp = 320, fontScale = 2f) @Composable fun QuestComponentsLargeFontPreview() = GymQuestTheme { QuestComponentsPreviewContent() }
@Preview(name = "Estado sin conexión") @Composable fun QuestOfflinePreview() = GymQuestTheme { QuestDataStateContent<String>(QuestDataState.Offline, ready = { Text(it) }) }
@Preview(name = "Estado listo") @Composable fun QuestReadyPreview() = GymQuestTheme { QuestDataStateContent(QuestDataState.Ready("Contenido listo"), ready = { Text(it) }) }
