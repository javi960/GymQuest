package com.gymquest.app.core.ui.theme

import androidx.compose.ui.graphics.Color

object ColorContrast {
    fun ratio(foreground: Color, background: Color): Double {
        fun channel(value: Float): Double = if (value <= 0.04045f) value / 12.92 else Math.pow(((value + 0.055f) / 1.055f).toDouble(), 2.4)
        fun luminance(color: Color): Double = 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
        val lighter = maxOf(luminance(foreground), luminance(background))
        val darker = minOf(luminance(foreground), luminance(background))
        return (lighter + 0.05) / (darker + 0.05)
    }
}
