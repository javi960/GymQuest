package com.gymquest.app.core.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen

@Composable private fun ThemePreviewContent() = QuestScreen { QuestPanel { Column { Text("Classic Quest") ; QuestActionButton(QuestAction.Start, onClick = {}) ; QuestActionButton(QuestAction.Delete, onClick = {}) } } }
@Preview(name = "Classic claro", widthDp = 320) @Composable fun ClassicQuestLightPreview() = GymQuestTheme(darkTheme = false) { ThemePreviewContent() }
@Preview(name = "Classic oscuro", widthDp = 412) @Composable fun ClassicQuestDarkPreview() = GymQuestTheme(darkTheme = true) { ThemePreviewContent() }
@Preview(name = "Classic fuente 200%", fontScale = 2f, widthDp = 320) @Composable fun ClassicQuestLargeFontPreview() = GymQuestTheme(darkTheme = false) { ThemePreviewContent() }
