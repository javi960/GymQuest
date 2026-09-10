package com.gymquest.app.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.theme.GymQuestTheme
import com.gymquest.app.core.ui.theme.QuestTheme

@Composable private fun QuestButtonStatesPreview() = Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    QuestActionButton(QuestAction.Add, onClick = {})
    QuestActionButton(QuestAction.Edit, onClick = {}, state = QuestButtonState.Selected)
    QuestActionButton(QuestAction.Save, onClick = {}, state = QuestButtonState.Loading)
    QuestActionButton(QuestAction.Start, onClick = {}, state = QuestButtonState.Disabled)
    QuestActionButton(QuestAction.Delete, onClick = {})
    QuestIconButton(QuestAction.More, onClick = {})
}
@Preview(name = "Botones claro", widthDp = 320) @Composable fun QuestButtonsLightPreview() = GymQuestTheme(false) { QuestButtonStatesPreview() }
@Preview(name = "Botones oscuro", widthDp = 320) @Composable fun QuestButtonsDarkPreview() = GymQuestTheme(true) { QuestButtonStatesPreview() }
@Preview(name = "Botones pergamino") @Composable fun QuestButtonsParchmentPreview() = GymQuestTheme(false) { androidx.compose.material3.Surface(color = QuestTheme.tokens.colors.parchment) { QuestButtonStatesPreview() } }
@Preview(name = "Botones panel azul") @Composable fun QuestButtonsBluePanelPreview() = GymQuestTheme(false) { androidx.compose.material3.Surface(color = QuestTheme.tokens.colors.blueStructure) { QuestButtonStatesPreview() } }
