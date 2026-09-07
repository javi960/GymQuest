package com.gymquest.app.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class GymQuestResolvedTheme(
    val tokens: GymQuestThemeTokens,
    val colorScheme: ColorScheme,
)

fun classicQuestTokens(darkTheme: Boolean): GymQuestResolvedTheme {
    val colors = if (darkTheme) {
        GymQuestColors(
            background = QuestNight,
            surface = QuestNightPanel,
            panel = QuestNightParchment,
            panelBorder = QuestGold,
            textPrimary = Color(0xFFF9F0DE),
            textSecondary = Color(0xFFD8C9AD),
            blueStructure = Color(0xFF78B8F2),
            positive = Color(0xFF78D6A4),
            xpGold = Color(0xFFFFC957),
            parchment = QuestNightParchment,
            error = Color(0xFFFFB4AB),
            warning = Color(0xFFFFD08A),
            focus = Color(0xFFFFD45C),
            disabled = Color(0xFF8996A3),
        )
    } else {
        GymQuestColors(
            background = QuestCloud,
            surface = Color.White,
            panel = QuestParchment,
            panelBorder = QuestParchmentDeep,
            textPrimary = QuestInk,
            textSecondary = QuestInkSoft,
            blueStructure = QuestSky,
            positive = QuestLeaf,
            xpGold = QuestGold,
            parchment = QuestParchment,
            error = QuestError,
            warning = QuestWarning,
            focus = QuestGoldDark,
            disabled = QuestDisabled,
        )
    }

    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.blueStructure,
            onPrimary = QuestNight,
            secondary = colors.xpGold,
            onSecondary = QuestNight,
            tertiary = colors.positive,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            error = colors.error,
            onError = QuestNight,
        )
    } else {
        lightColorScheme(
            primary = colors.blueStructure,
            onPrimary = Color.White,
            secondary = colors.xpGold,
            onSecondary = QuestInk,
            tertiary = colors.positive,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            error = colors.error,
            onError = Color.White,
        )
    }

    return GymQuestResolvedTheme(
        tokens = GymQuestThemeTokens(colors = colors),
        colorScheme = scheme,
    )
}
