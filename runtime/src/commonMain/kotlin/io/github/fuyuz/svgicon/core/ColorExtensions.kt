package io.github.fuyuz.svgicon.core

import androidx.compose.ui.graphics.Color

/**
 * Color extension functions for SVG DSL.
 */

/**
 * Returns a copy of this color with the specified alpha value.
 *
 * Example:
 * ```kotlin
 * Color.Blue.withAlpha(0.5f)
 * ```
 */
fun Color.withAlpha(alpha: Float): Color = copy(alpha = alpha)

/**
 * Returns a copy of this color with alpha multiplied by the given factor.
 *
 * Example:
 * ```kotlin
 * Color.Blue.alpha(0.5f)  // 50% of current alpha
 * ```
 */
fun Color.alpha(multiplier: Float): Color = copy(alpha = this.alpha * multiplier)

/**
 * Parses a hex color string to Color.
 * Supports formats: "#RGB", "#RRGGBB", "#AARRGGBB", "RGB", "RRGGBB", "AARRGGBB"
 *
 * Example:
 * ```kotlin
 * "#3B82F6".toSvgColor()
 * "FF0000".toSvgColor()
 * ```
 */
fun String.toSvgColor(): Color {
    val hex = removePrefix("#")
    return when (hex.length) {
        3 -> {
            // #RGB -> #RRGGBB
            val r = hex[0].toString().repeat(2).toInt(16)
            val g = hex[1].toString().repeat(2).toInt(16)
            val b = hex[2].toString().repeat(2).toInt(16)
            Color(r, g, b)
        }
        4 -> {
            // #ARGB -> #AARRGGBB
            val a = hex[0].toString().repeat(2).toInt(16)
            val r = hex[1].toString().repeat(2).toInt(16)
            val g = hex[2].toString().repeat(2).toInt(16)
            val b = hex[3].toString().repeat(2).toInt(16)
            Color(r, g, b, a)
        }
        6 -> {
            // #RRGGBB
            val r = hex.substring(0, 2).toInt(16)
            val g = hex.substring(2, 4).toInt(16)
            val b = hex.substring(4, 6).toInt(16)
            Color(r, g, b)
        }
        8 -> {
            // #AARRGGBB
            val a = hex.substring(0, 2).toInt(16)
            val r = hex.substring(2, 4).toInt(16)
            val g = hex.substring(4, 6).toInt(16)
            val b = hex.substring(6, 8).toInt(16)
            Color(r, g, b, a)
        }
        else -> throw IllegalArgumentException("Invalid hex color: $this")
    }
}

/**
 * Common SVG colors.
 */
object SvgColors {
    /** Transparent color (no fill/stroke) */
    val Transparent = Color.Transparent

    /** Uses the tint color from SvgIcon composable */
    val CurrentColor = Color.Unspecified

    // Common UI colors
    val Primary = Color(0xFF3B82F6)
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF0EA5E9)

    // Grays
    val Gray50 = Color(0xFFF9FAFB)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray200 = Color(0xFFE5E7EB)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    val Gray600 = Color(0xFF4B5563)
    val Gray700 = Color(0xFF374151)
    val Gray800 = Color(0xFF1F2937)
    val Gray900 = Color(0xFF111827)
}
