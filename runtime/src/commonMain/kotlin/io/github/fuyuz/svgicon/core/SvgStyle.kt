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
 *
 * Note: MITER_CLIP and ARCS are SVG2 values. Compose only supports Miter, Round, and Bevel,
 * so using MITER_CLIP or ARCS will throw an error at render time.
 */
enum class LineJoin {
    MITER,
    MITER_CLIP,  // SVG2: Not supported by Compose
    ROUND,
    BEVEL,
    ARCS         // SVG2: Not supported by Compose
}

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
 * SVG visibility values.
 * Controls whether an element is rendered.
 */
enum class Visibility {
    VISIBLE,   // Default: element is rendered
    HIDDEN,    // Element is not rendered but still affects layout
    COLLAPSE   // Same as hidden for most elements
}

/**
 * SVG display values.
 * Controls how an element participates in layout and rendering.
 */
enum class Display {
    INLINE,    // Default: element is rendered inline
    BLOCK,     // Element is rendered as a block
    NONE       // Element is not rendered and does not affect layout
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
 * - Color.Unspecified = "currentColor" (resolved to CSS color property, then tint)
 * - null = inherit from parent
 * - Any other Color = that specific color
 *
 * To explicitly set "none" (no fill/stroke), use a fully transparent color.
 *
 * @param color CSS color property value (used to resolve currentColor)
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
 * @param markerStart Marker at the start of a path/line (e.g., "url(#arrow)")
 * @param markerMid Marker at middle vertices of a path
 * @param markerEnd Marker at the end of a path/line
 * @param visibility Controls whether the element is rendered (visible, hidden, collapse)
 * @param display Controls how the element participates in layout (inline, block, none)
 */
data class SvgStyle(
    val color: Color? = null,
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
    val maskId: String? = null,
    val markerStart: String? = null,
    val markerMid: String? = null,
    val markerEnd: String? = null,
    val visibility: Visibility? = null,
    val display: Display? = null
) {
    companion object {
        val Empty = SvgStyle()
    }
}
