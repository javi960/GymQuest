package com.gymquest.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun GymQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    preset: GymQuestThemePreset = GymQuestThemePreset.ClassicQuest,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val theme = when (preset) {
        GymQuestThemePreset.ClassicQuest -> classicQuestTokens(darkTheme = darkTheme)
    }

    GymQuestThemeTokensProvider(tokens = theme.tokens) {
        MaterialTheme(
            colorScheme = theme.colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
