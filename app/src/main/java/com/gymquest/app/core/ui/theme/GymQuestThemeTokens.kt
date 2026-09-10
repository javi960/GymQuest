package com.gymquest.app.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class GymQuestColors(
    val background: Color,
    val surface: Color,
    val panel: Color,
    val panelBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val blueStructure: Color,
    val positive: Color,
    val xpGold: Color,
    val parchment: Color,
    val error: Color,
    val warning: Color,
    val focus: Color,
    val disabled: Color,
)

@Immutable
data class GymQuestSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
)

@Immutable
data class GymQuestRadii(
    // Compact game HUD geometry: rectangular enough to feel like a panel,
    // still rounded enough for comfortable touch targets.
    val panel: Dp = 14.dp,
    val button: Dp = 10.dp,
    val field: Dp = 8.dp,
    val badge: Dp = 10.dp,
)

@Immutable
data class GymQuestSizes(
    val icon: Dp = 22.dp,
    val touchTarget: Dp = 48.dp,
)

@Immutable
data class GymQuestMotion(
    val feedbackMillis: Int = 100,
    val stateChangeMillis: Int = 180,
    val celebrationMillis: Int = 300,
    val hapticFeedbackEnabled: Boolean = true,
) {
    fun respecting(reducedMotion: Boolean): GymQuestMotion =
        if (reducedMotion) copy(feedbackMillis = 0, stateChangeMillis = 0, celebrationMillis = 0, hapticFeedbackEnabled = false) else this
}

@Immutable
data class GymQuestThemeTokens(
    val colors: GymQuestColors,
    val spacing: GymQuestSpacing = GymQuestSpacing(),
    val radii: GymQuestRadii = GymQuestRadii(),
    val sizes: GymQuestSizes = GymQuestSizes(),
    val motion: GymQuestMotion = GymQuestMotion(),
)

private val LocalGymQuestThemeTokens = compositionLocalOf {
    classicQuestTokens(darkTheme = false).tokens
}

@Composable
fun GymQuestThemeTokensProvider(
    tokens: GymQuestThemeTokens,
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalGymQuestThemeTokens provides tokens,
        content = content,
    )
}

object QuestTheme {
    val tokens: GymQuestThemeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalGymQuestThemeTokens.current
}
