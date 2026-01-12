package io.github.fuyuz.svgicon.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Utility functions for parsing and applying SVG styles.
 */

internal fun applyStyle(parent: DrawContext, style: SvgStyle): DrawContext {
    val strokeColor = when (val c = style.stroke) {
        null -> parent.strokeColor
        Color.Unspecified -> parent.strokeColor
        Color.Transparent -> parent.strokeColor  // "none" for stroke means don't draw, handled by hasStroke
        else -> c
    }
    val hasStroke = style.stroke != Color.Transparent && (style.stroke != null || parent.hasStroke)

    val fillColor = when (val c = style.fill) {
        null -> parent.fillColor?.takeIf { it != Color.Transparent }  // inherit, but skip if parent is transparent
        Color.Unspecified -> parent.strokeColor
        Color.Transparent -> null  // "none" means don't fill
        else -> c
    }?.takeIf { it != Color.Transparent }  // ensure we don't return Transparent as fillColor

    val strokeWidth = style.strokeWidth ?: parent.stroke.width
    val strokeCap = style.strokeLinecap?.toCompose() ?: parent.stroke.cap
    val strokeJoin = style.strokeLinejoin?.toCompose() ?: parent.stroke.join
    val miterLimit = style.strokeMiterlimit ?: parent.stroke.miter
    val pathEffect = buildPathEffect(style, parent.stroke.pathEffect)

    val stroke = Stroke(
        width = strokeWidth,
        cap = strokeCap,
        join = strokeJoin,
        miter = miterLimit,
        pathEffect = pathEffect
    )

    val opacity = (style.opacity ?: 1f) * parent.opacity
    val strokeOpacity = style.strokeOpacity ?: 1f
    val fillOpacity = style.fillOpacity ?: 1f

    val fillRule = style.fillRule?.toCompose() ?: parent.fillRule
    val paintOrder = style.paintOrder ?: parent.paintOrder
    val vectorEffect = style.vectorEffect ?: parent.vectorEffect
    val clipPathId = style.clipPathId ?: parent.clipPathId
    val maskId = style.maskId ?: parent.maskId

    // Safely apply alpha to colors, handling special colors that can't be copied
    val finalStrokeColor = if (strokeColor.isSpecified) {
        strokeColor.copy(alpha = strokeColor.alpha * strokeOpacity * opacity)
    } else {
        strokeColor
    }
    val finalFillColor = fillColor?.let { color ->
        if (color.isSpecified) {
            color.copy(alpha = color.alpha * fillOpacity * opacity)
        } else {
            color
        }
    }

    return DrawContext(
        strokeColor = finalStrokeColor,
        fillColor = finalFillColor,
        stroke = stroke,
        opacity = opacity,
        fillRule = fillRule,
        hasStroke = hasStroke,
        paintOrder = paintOrder,
        vectorEffect = vectorEffect,
        scaleFactor = parent.scaleFactor,
        clipPathId = clipPathId,
        maskId = maskId
    )
}

internal fun buildPathEffect(style: SvgStyle, parent: PathEffect?): PathEffect? {
    val dashArray = style.strokeDasharray
    return if (dashArray != null) {
        if (dashArray.isEmpty()) {
            null
        } else {
            val intervals = normalizeDashArray(dashArray)
            PathEffect.dashPathEffect(intervals, style.strokeDashoffset ?: 0f)
        }
    } else {
        parent
    }
}

/**
 * Normalizes a dash array according to SVG spec.
 * If the array has an odd number of values, it is repeated to make it even.
 */
internal fun normalizeDashArray(dashArray: List<Float>): FloatArray {
    return if (dashArray.size % 2 == 0) {
        dashArray.toFloatArray()
    } else {
        FloatArray(dashArray.size * 2) { i -> dashArray[i % dashArray.size] }
    }
}

internal fun parseColor(colorStr: String, default: Color): Color {
    return when {
        colorStr == "none" || colorStr == "transparent" -> Color.Transparent
        colorStr == "currentColor" -> Color.Unspecified
        colorStr.startsWith("#") -> parseHexColor(colorStr)
        colorStr.startsWith("rgb") -> parseRgbColor(colorStr)
        else -> default // Named colors can be added here or in a separate registry
    }
}

internal fun parseHexColor(hex: String): Color {
    val h = hex.removePrefix("#")
    return when (h.length) {
        3 -> {
            val r = h.substring(0, 1).repeat(2).toInt(16)
            val g = h.substring(1, 2).repeat(2).toInt(16)
            val b = h.substring(2, 3).repeat(2).toInt(16)
            Color(r, g, b)
        }
        6 -> {
            val r = h.substring(0, 2).toInt(16)
            val g = h.substring(2, 4).toInt(16)
            val b = h.substring(4, 6).toInt(16)
            Color(r, g, b)
        }
        8 -> {
            val r = h.substring(0, 2).toInt(16)
            val g = h.substring(2, 4).toInt(16)
            val b = h.substring(4, 6).toInt(16)
            val a = h.substring(6, 8).toInt(16)
            Color(r, g, b, a)
        }
        else -> Color.Unspecified
    }
}

internal fun parseRgbColor(rgb: String): Color {
    val parts = rgb.removePrefix("rgb(").removePrefix("rgba(").removeSuffix(")").split(",")
    if (parts.size < 3) return Color.Unspecified
    val r = parts[0].trim().toIntOrNull() ?: 0
    val g = parts[1].trim().toIntOrNull() ?: 0
    val b = parts[2].trim().toIntOrNull() ?: 0
    val a = if (parts.size > 3) parts[3].trim().toFloatOrNull() ?: 1f else 1f
    return Color(r, g, b).copy(alpha = a)
}

internal fun LineCap.toCompose(): StrokeCap = when (this) {
    LineCap.BUTT -> StrokeCap.Butt
    LineCap.ROUND -> StrokeCap.Round
    LineCap.SQUARE -> StrokeCap.Square
}

internal fun LineJoin.toCompose(): StrokeJoin = when (this) {
    LineJoin.MITER -> StrokeJoin.Miter
    LineJoin.ROUND -> StrokeJoin.Round
    LineJoin.BEVEL -> StrokeJoin.Bevel
}

internal fun FillRule.toCompose(): PathFillType = when (this) {
    FillRule.NONZERO -> PathFillType.NonZero
    FillRule.EVENODD -> PathFillType.EvenOdd
}
