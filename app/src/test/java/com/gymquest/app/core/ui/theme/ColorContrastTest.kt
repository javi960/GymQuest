package com.gymquest.app.core.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorContrastTest {
    @Test fun `all text and interactive action pairs meet WCAG AA`() {
        listOf(false, true).forEach { dark ->
            val colors = classicQuestTokens(dark).tokens.colors
            val pairs = listOf(
                "primary text" to (colors.textPrimary to colors.background),
                "secondary text" to (colors.textSecondary to colors.panel),
                "primary action" to (Color.White to colors.blueStructure),
                "positive action" to (Color.White to colors.positive),
                "destructive action" to (Color.White to colors.error),
            )
            pairs.forEach { (name, pair) -> assertTrue("$name (${if (dark) "dark" else "light"}) must meet 4.5:1", ColorContrast.ratio(pair.first, pair.second) >= 4.5) }
        }
    }
}
