package io.github.fuyuz.svgicon.core

import androidx.compose.ui.graphics.Color

/**
 * SVG fill-rule values.
 */
enum class FillRule { NONZERO, EVENODD }

/**
 * SVG stroke-linecap values.
 */
enum class LineCap { BUTT, ROUND, SQUARE }

/**
 * SVG stroke-linejoin values.
 */
enum class LineJoin { MITER, ROUND, BEVEL }

/**
 * Paint order determines the order in which fill and stroke are painted.
 * Default SVG behavior is FILL_STROKE (fill first, then stroke on top).
 */
enum class PaintOrder {
    FILL_STROKE,   // Default: fill first, stroke on top
    STROKE_FILL    // Stroke first, fill on top
}

/**
 * Vector effect for special rendering behaviors.
 */
enum class VectorEffect {
    NONE,              // Default behavior
    NON_SCALING_STROKE // Stroke width is not affected by transformations
}

/**
 * Coordinate units for clipPath.
 */
enum class ClipPathUnits {
    USER_SPACE_ON_USE,
    OBJECT_BOUNDING_BOX
}

/**
 * Coordinate units for mask.
 */
enum class MaskUnits {
    USER_SPACE_ON_USE,
    OBJECT_BOUNDING_BOX
}

/**
 * SVG transform.
 */
sealed interface SvgTransform {
    data class Translate(val x: Float, val y: Float = 0f) : SvgTransform
    data class Scale(val sx: Float, val sy: Float = sx) : SvgTransform
    data class Rotate(val angle: Float, val cx: Float = 0f, val cy: Float = 0f) : SvgTransform
    data class SkewX(val angle: Float) : SvgTransform
    data class SkewY(val angle: Float) : SvgTransform
    data class Matrix(val a: Float, val b: Float, val c: Float, val d: Float, val e: Float, val f: Float) : SvgTransform
    data class Combined(val transforms: List<SvgTransform>) : SvgTransform
}

/**
 * SVG style attributes for presentation.
 * These correspond to SVG presentation attributes.
 *
 * Color handling:
 * - Color.Unspecified = "currentColor" (uses tint from SvgIcon composable)
 * - null = inherit from parent
 * - Any other Color = that specific color
 *
 * To explicitly set "none" (no fill/stroke), use a fully transparent color.
 *
 * @param fill Fill color (null = inherit, Unspecified = currentColor)
 * @param fillOpacity Fill opacity (0.0 - 1.0)
 * @param fillRule Fill rule (nonzero or evenodd)
 * @param stroke Stroke color (null = inherit, Unspecified = currentColor)
 * @param strokeWidth Stroke width
 * @param strokeOpacity Stroke opacity (0.0 - 1.0)
 * @param strokeLinecap Line cap style (butt, round, square)
 * @param strokeLinejoin Line join style (miter, round, bevel)
 * @param strokeDasharray Dash pattern
 * @param strokeDashoffset Dash offset
 * @param strokeMiterlimit Miter limit
 * @param opacity Overall opacity (0.0 - 1.0)
 * @param transform Transform
 * @param paintOrder Order of fill/stroke painting
 * @param vectorEffect Special rendering effects (e.g., non-scaling-stroke)
 * @param clipPathId Reference to a clip path by ID
 * @param maskId Reference to a mask by ID
 */
data class SvgStyle(
    val fill: Color? = null,
    val fillOpacity: Float? = null,
    val fillRule: FillRule? = null,
    val stroke: Color? = null,
    val strokeWidth: Float? = null,
    val strokeOpacity: Float? = null,
    val strokeLinecap: LineCap? = null,
    val strokeLinejoin: LineJoin? = null,
    val strokeDasharray: List<Float>? = null,
    val strokeDashoffset: Float? = null,
    val strokeMiterlimit: Float? = null,
    val opacity: Float? = null,
    val transform: SvgTransform? = null,
    val paintOrder: PaintOrder? = null,
    val vectorEffect: VectorEffect? = null,
    val clipPathId: String? = null,
    val maskId: String? = null
) {
    companion object {
        val Empty = SvgStyle()
    }
}
