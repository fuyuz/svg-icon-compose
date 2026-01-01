package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * SVG DSL for representing icons.
 * Mirrors SVG structure directly in Kotlin.
 */

// ============================================
// Type-safe ViewBox
// ============================================

/**
 * Alignment for preserveAspectRatio.
 * Specifies how to align the viewBox within the viewport.
 */
enum class AspectRatioAlign {
    /** Do not force uniform scaling */
    NONE,
    /** Align min-X of viewBox with min-X of viewport, min-Y with min-Y */
    X_MIN_Y_MIN,
    /** Align mid-X of viewBox with mid-X of viewport, min-Y with min-Y */
    X_MID_Y_MIN,
    /** Align max-X of viewBox with max-X of viewport, min-Y with min-Y */
    X_MAX_Y_MIN,
    /** Align min-X of viewBox with min-X of viewport, mid-Y with mid-Y */
    X_MIN_Y_MID,
    /** Align mid-X of viewBox with mid-X of viewport, mid-Y with mid-Y (default) */
    X_MID_Y_MID,
    /** Align max-X of viewBox with max-X of viewport, mid-Y with mid-Y */
    X_MAX_Y_MID,
    /** Align min-X of viewBox with min-X of viewport, max-Y with max-Y */
    X_MIN_Y_MAX,
    /** Align mid-X of viewBox with mid-X of viewport, max-Y with max-Y */
    X_MID_Y_MAX,
    /** Align max-X of viewBox with max-X of viewport, max-Y with max-Y */
    X_MAX_Y_MAX;

    companion object {
        fun parse(value: String): AspectRatioAlign = when (value.lowercase()) {
            "none" -> NONE
            "xminymin" -> X_MIN_Y_MIN
            "xmidymin" -> X_MID_Y_MIN
            "xmaxymin" -> X_MAX_Y_MIN
            "xminymid" -> X_MIN_Y_MID
            "xmidymid" -> X_MID_Y_MID
            "xmaxymid" -> X_MAX_Y_MID
            "xminymax" -> X_MIN_Y_MAX
            "xmidymax" -> X_MID_Y_MAX
            "xmaxymax" -> X_MAX_Y_MAX
            else -> X_MID_Y_MID
        }
    }
}

/**
 * Meet or slice option for preserveAspectRatio.
 */
enum class MeetOrSlice {
    /** Scale to fit entirely within viewport (default) */
    MEET,
    /** Scale to cover entire viewport, may be clipped */
    SLICE;

    companion object {
        fun parse(value: String): MeetOrSlice = when (value.lowercase()) {
            "slice" -> SLICE
            else -> MEET
        }
    }
}

/**
 * SVG preserveAspectRatio attribute.
 * Controls how viewBox is scaled and positioned within the viewport.
 *
 * @param align How to align the viewBox within the viewport
 * @param meetOrSlice Whether to fit (meet) or cover (slice) the viewport
 */
data class PreserveAspectRatio(
    val align: AspectRatioAlign = AspectRatioAlign.X_MID_Y_MID,
    val meetOrSlice: MeetOrSlice = MeetOrSlice.MEET
) {
    companion object {
        /** Default: xMidYMid meet */
        val Default = PreserveAspectRatio()

        /** No uniform scaling */
        val None = PreserveAspectRatio(AspectRatioAlign.NONE)

        /** Parse from SVG preserveAspectRatio string */
        fun parse(value: String): PreserveAspectRatio {
            val parts = value.trim().split("\\s+".toRegex())
            val align = if (parts.isNotEmpty()) AspectRatioAlign.parse(parts[0]) else AspectRatioAlign.X_MID_Y_MID
            val meetOrSlice = if (parts.size > 1) MeetOrSlice.parse(parts[1]) else MeetOrSlice.MEET
            return PreserveAspectRatio(align, meetOrSlice)
        }
    }
}

/**
 * Type-safe representation of SVG viewBox.
 *
 * @param minX Minimum X coordinate
 * @param minY Minimum Y coordinate
 * @param width Width of the viewBox
 * @param height Height of the viewBox
 */
data class ViewBox(
    val minX: Float = 0f,
    val minY: Float = 0f,
    val width: Float = 24f,
    val height: Float = 24f
) {
    /**
     * Converts to SVG viewBox string format.
     */
    fun toSvgString(): String = "$minX $minY $width $height"

    companion object {
        /** Default 24x24 viewBox */
        val Default = ViewBox(0f, 0f, 24f, 24f)

        /** 16x16 viewBox */
        val Size16 = ViewBox(0f, 0f, 16f, 16f)

        /** 32x32 viewBox */
        val Size32 = ViewBox(0f, 0f, 32f, 32f)

        /** 48x48 viewBox */
        val Size48 = ViewBox(0f, 0f, 48f, 48f)

        /** Create a square viewBox */
        fun square(size: Float) = ViewBox(0f, 0f, size, size)

        /** Parse from SVG viewBox string */
        fun parse(viewBox: String): ViewBox {
            val parts = viewBox.split(" ", ",").mapNotNull { it.trim().toFloatOrNull() }
            return when (parts.size) {
                4 -> ViewBox(parts[0], parts[1], parts[2], parts[3])
                2 -> ViewBox(0f, 0f, parts[0], parts[1])
                else -> Default
            }
        }
    }
}


// ============================================
// SVG Elements
// ============================================

/**
 * Base interface for all SVG elements.
 */
sealed interface SvgElement

/**
 * SVG root element.
 * Represents the <svg> tag with its attributes.
 *
 * Color handling:
 * - Color.Unspecified = "currentColor" (uses tint from SvgIcon composable)
 * - null = "none" (no color / transparent)
 * - Any other Color = that specific color
 *
 * Size determination:
 * - If width/height are specified, they define the viewport size
 * - If only viewBox is specified, viewBox dimensions are used
 * - preserveAspectRatio controls how viewBox maps to viewport
 *
 * @param width Viewport width (null = use viewBox width)
 * @param height Viewport height (null = use viewBox height)
 * @param viewBox ViewBox defining the coordinate system (default: 24x24)
 * @param preserveAspectRatio How to scale/align viewBox within viewport
 * @param fill Default fill color (default: null = no fill)
 * @param stroke Default stroke color (default: Unspecified = uses tint color)
 * @param strokeWidth Default stroke width (default: 2)
 * @param strokeLinecap Default stroke linecap (default: Round)
 * @param strokeLinejoin Default stroke linejoin (default: Round)
 * @param children Child SVG elements
 */
data class Svg(
    val width: Float? = null,
    val height: Float? = null,
    val viewBox: ViewBox? = null,
    val preserveAspectRatio: PreserveAspectRatio = PreserveAspectRatio.Default,
    val fill: Color? = null,
    val stroke: Color? = Color.Unspecified,
    val strokeWidth: Float = 2f,
    val strokeLinecap: LineCap = LineCap.ROUND,
    val strokeLinejoin: LineJoin = LineJoin.ROUND,
    val children: List<SvgElement> = emptyList()
) {
    /**
     * Effective width for rendering.
     * Priority: explicit width > viewBox width > default (24)
     */
    val effectiveWidth: Float get() = width ?: viewBox?.width ?: 24f

    /**
     * Effective height for rendering.
     * Priority: explicit height > viewBox height > default (24)
     */
    val effectiveHeight: Float get() = height ?: viewBox?.height ?: 24f

    /**
     * Effective viewBox for rendering.
     * If viewBox is not specified, creates one from width/height or defaults.
     */
    val effectiveViewBox: ViewBox get() = viewBox ?: ViewBox(0f, 0f, effectiveWidth, effectiveHeight)
}

// ============================================
// CSS Stylesheet Support
// ============================================

/**
 * CSS selector for matching elements.
 * Supports basic selectors: .class, #id, tag, *
 */
sealed interface CssSelector {
    /** Class selector: .my-class */
    data class Class(val className: String) : CssSelector

    /** ID selector: #my-id */
    data class Id(val id: String) : CssSelector

    /** Tag selector: path, circle, etc. */
    data class Tag(val tagName: String) : CssSelector

    /** Universal selector: * */
    data object Universal : CssSelector

    /**
     * CSS specificity for this selector.
     * Higher values take precedence.
     * Order: Universal(0) < Tag(1) < Class(2) < Id(3)
     */
    val specificity: Int
        get() = when (this) {
            is Universal -> 0
            is Tag -> 1
            is Class -> 2
            is Id -> 3
        }
}

/**
 * A single CSS rule consisting of a selector and style declarations.
 */
data class CssRule(
    val selector: CssSelector,
    val declarations: Map<String, String>
)

/**
 * Parsed CSS stylesheet containing rules and keyframes.
 */
data class CssStylesheet(
    val rules: List<CssRule> = emptyList(),
    val keyframes: List<CssKeyframes> = emptyList()
)

// ============================================
// CSS Animation Support
// ============================================

/**
 * CSS timing function for animation easing.
 * Maps to CSS animation-timing-function values.
 */
sealed interface CssTimingFunction {
    /** Linear interpolation */
    data object Linear : CssTimingFunction

    /** CSS ease: cubic-bezier(0.25, 0.1, 0.25, 1) */
    data object Ease : CssTimingFunction

    /** CSS ease-in: cubic-bezier(0.42, 0, 1, 1) */
    data object EaseIn : CssTimingFunction

    /** CSS ease-out: cubic-bezier(0, 0, 0.58, 1) */
    data object EaseOut : CssTimingFunction

    /** CSS ease-in-out: cubic-bezier(0.42, 0, 0.58, 1) */
    data object EaseInOut : CssTimingFunction

    /** Custom cubic bezier timing function */
    data class CubicBezier(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : CssTimingFunction

    /** Step function for discrete animation */
    data class Steps(val count: Int, val position: StepPosition = StepPosition.END) : CssTimingFunction

    /**
     * Convert to KeySplines for SMIL animation.
     * Returns null for non-cubic-bezier functions.
     */
    fun toKeySplines(): KeySplines? = when (this) {
        is Linear -> null // Linear doesn't need keySplines
        is Ease -> KeySplines.EASE
        is EaseIn -> KeySplines.EASE_IN
        is EaseOut -> KeySplines.EASE_OUT
        is EaseInOut -> KeySplines.EASE_IN_OUT
        is CubicBezier -> KeySplines(x1, y1, x2, y2)
        is Steps -> null // Steps not supported as keySplines
    }

    /**
     * Convert to CalcMode for SMIL animation.
     */
    fun toCalcMode(): CalcMode = when (this) {
        is Linear -> CalcMode.LINEAR
        is Steps -> CalcMode.DISCRETE
        else -> CalcMode.SPLINE
    }

    companion object {
        /**
         * Parse CSS timing function string.
         */
        fun parse(value: String): CssTimingFunction {
            val trimmed = value.trim().lowercase()
            return when {
                trimmed == "linear" -> Linear
                trimmed == "ease" -> Ease
                trimmed == "ease-in" -> EaseIn
                trimmed == "ease-out" -> EaseOut
                trimmed == "ease-in-out" -> EaseInOut
                trimmed.startsWith("cubic-bezier(") -> {
                    val params = trimmed
                        .removePrefix("cubic-bezier(")
                        .removeSuffix(")")
                        .split(",")
                        .mapNotNull { it.trim().toFloatOrNull() }
                    if (params.size == 4) {
                        CubicBezier(params[0], params[1], params[2], params[3])
                    } else {
                        Ease // Default fallback
                    }
                }
                trimmed.startsWith("steps(") -> {
                    val params = trimmed
                        .removePrefix("steps(")
                        .removeSuffix(")")
                        .split(",")
                        .map { it.trim() }
                    val count = params.getOrNull(0)?.toIntOrNull() ?: 1
                    val position = when (params.getOrNull(1)?.lowercase()) {
                        "start", "jump-start" -> StepPosition.START
                        "both", "jump-both" -> StepPosition.BOTH
                        "none", "jump-none" -> StepPosition.NONE
                        else -> StepPosition.END
                    }
                    Steps(count, position)
                }
                else -> Ease // Default
            }
        }
    }
}

/**
 * Step position for steps() timing function.
 */
enum class StepPosition {
    START, END, BOTH, NONE
}

/**
 * A single keyframe in a CSS @keyframes animation.
 *
 * @param offset Position in the animation (0.0 = 0%, 1.0 = 100%)
 * @param properties CSS properties at this keyframe
 */
data class CssKeyframe(
    val offset: Float,
    val properties: Map<String, String>
)

/**
 * CSS @keyframes animation definition.
 *
 * @param name Animation name (e.g., "spin", "fadeIn")
 * @param keyframes List of keyframes
 */
data class CssKeyframes(
    val name: String,
    val keyframes: List<CssKeyframe>
)

/**
 * Parsed CSS animation property.
 *
 * @param name Reference to @keyframes name
 * @param duration Animation duration
 * @param timingFunction Easing function
 * @param delay Animation delay
 * @param iterationCount Number of iterations (Int.MAX_VALUE for infinite)
 * @param direction Animation direction
 * @param fillMode How styles are applied before/after animation
 */
data class CssAnimation(
    val name: String,
    val duration: kotlin.time.Duration = kotlin.time.Duration.ZERO,
    val timingFunction: CssTimingFunction = CssTimingFunction.Ease,
    val delay: kotlin.time.Duration = kotlin.time.Duration.ZERO,
    val iterationCount: Int = 1,
    val direction: AnimationDirection = AnimationDirection.NORMAL,
    val fillMode: AnimationFillMode = AnimationFillMode.NONE
)

/**
 * CSS animation-direction values.
 */
enum class AnimationDirection {
    NORMAL,
    REVERSE,
    ALTERNATE,
    ALTERNATE_REVERSE
}

/**
 * CSS animation-fill-mode values.
 */
enum class AnimationFillMode {
    NONE,
    FORWARDS,
    BACKWARDS,
    BOTH
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
)

enum class FillRule { NONZERO, EVENODD }
enum class LineCap { BUTT, ROUND, SQUARE }
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
 * SVG element with style attributes.
 */
data class SvgStyled(
    val element: SvgElement,
    val style: SvgStyle
) : SvgElement

/**
 * SVG path element with parsed commands.
 * @param commands List of path commands
 */
data class SvgPath(val commands: List<PathCommand>) : SvgElement {
    companion object {
        /**
         * Creates SvgPath from path data string.
         */
        operator fun invoke(d: String): SvgPath = SvgPath(parsePathCommands(d))
    }
}

/**
 * SVG path commands.
 */
sealed interface PathCommand {
    /** Move to absolute position */
    data class MoveTo(val x: Float, val y: Float) : PathCommand
    /** Move to relative position */
    data class MoveToRelative(val dx: Float, val dy: Float) : PathCommand
    /** Line to absolute position */
    data class LineTo(val x: Float, val y: Float) : PathCommand
    /** Line to relative position */
    data class LineToRelative(val dx: Float, val dy: Float) : PathCommand
    /** Horizontal line to absolute x */
    data class HorizontalLineTo(val x: Float) : PathCommand
    /** Horizontal line to relative dx */
    data class HorizontalLineToRelative(val dx: Float) : PathCommand
    /** Vertical line to absolute y */
    data class VerticalLineTo(val y: Float) : PathCommand
    /** Vertical line to relative dy */
    data class VerticalLineToRelative(val dy: Float) : PathCommand
    /** Cubic bezier curve (absolute) */
    data class CubicTo(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val x: Float, val y: Float) : PathCommand
    /** Cubic bezier curve (relative) */
    data class CubicToRelative(val dx1: Float, val dy1: Float, val dx2: Float, val dy2: Float, val dx: Float, val dy: Float) : PathCommand
    /** Smooth cubic bezier curve (absolute) */
    data class SmoothCubicTo(val x2: Float, val y2: Float, val x: Float, val y: Float) : PathCommand
    /** Smooth cubic bezier curve (relative) */
    data class SmoothCubicToRelative(val dx2: Float, val dy2: Float, val dx: Float, val dy: Float) : PathCommand
    /** Quadratic bezier curve (absolute) */
    data class QuadTo(val x1: Float, val y1: Float, val x: Float, val y: Float) : PathCommand
    /** Quadratic bezier curve (relative) */
    data class QuadToRelative(val dx1: Float, val dy1: Float, val dx: Float, val dy: Float) : PathCommand
    /** Smooth quadratic bezier curve (absolute) */
    data class SmoothQuadTo(val x: Float, val y: Float) : PathCommand
    /** Smooth quadratic bezier curve (relative) */
    data class SmoothQuadToRelative(val dx: Float, val dy: Float) : PathCommand
    /** Arc (absolute) */
    data class ArcTo(val rx: Float, val ry: Float, val xAxisRotation: Float, val largeArcFlag: Boolean, val sweepFlag: Boolean, val x: Float, val y: Float) : PathCommand
    /** Arc (relative) */
    data class ArcToRelative(val rx: Float, val ry: Float, val xAxisRotation: Float, val largeArcFlag: Boolean, val sweepFlag: Boolean, val dx: Float, val dy: Float) : PathCommand
    /** Close path */
    data object Close : PathCommand
}

/**
 * SVG circle element.
 * @param cx Center x coordinate
 * @param cy Center y coordinate
 * @param r Radius
 */
data class SvgCircle(
    val cx: Float,
    val cy: Float,
    val r: Float
) : SvgElement

/**
 * SVG ellipse element.
 * @param cx Center x coordinate
 * @param cy Center y coordinate
 * @param rx Radius in x direction
 * @param ry Radius in y direction
 */
data class SvgEllipse(
    val cx: Float,
    val cy: Float,
    val rx: Float,
    val ry: Float
) : SvgElement

/**
 * SVG rectangle element.
 * @param x X coordinate of top-left corner
 * @param y Y coordinate of top-left corner
 * @param width Width of the rectangle
 * @param height Height of the rectangle
 * @param rx Horizontal corner radius (optional)
 * @param ry Vertical corner radius (optional, defaults to rx)
 */
data class SvgRect(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float,
    val height: Float,
    val rx: Float = 0f,
    val ry: Float = rx
) : SvgElement

/**
 * SVG line element.
 * @param x1 Start x coordinate
 * @param y1 Start y coordinate
 * @param x2 End x coordinate
 * @param y2 End y coordinate
 */
data class SvgLine(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
) : SvgElement

/**
 * SVG polyline element (open shape).
 * @param points List of coordinate points
 */
data class SvgPolyline(val points: List<Offset>) : SvgElement

/**
 * SVG polygon element (closed shape).
 * @param points List of coordinate points
 */
data class SvgPolygon(val points: List<Offset>) : SvgElement

/**
 * SVG group element (g tag).
 * @param children Child elements
 */
data class SvgGroup(val children: List<SvgElement>) : SvgElement

/**
 * SVG clipPath element.
 * Defines a clipping region that restricts the visible area of elements.
 * @param id Unique identifier for referencing this clip path
 * @param children Child elements that define the clipping shape
 * @param clipPathUnits Coordinate system: "userSpaceOnUse" or "objectBoundingBox"
 */
data class SvgClipPath(
    val id: String,
    val children: List<SvgElement>,
    val clipPathUnits: ClipPathUnits = ClipPathUnits.USER_SPACE_ON_USE
) : SvgElement

/**
 * SVG mask element.
 * Defines a mask that controls the transparency of elements.
 * @param id Unique identifier for referencing this mask
 * @param children Child elements that define the mask
 * @param maskUnits Coordinate system for mask positioning
 * @param maskContentUnits Coordinate system for mask content
 */
data class SvgMask(
    val id: String,
    val children: List<SvgElement>,
    val maskUnits: MaskUnits = MaskUnits.OBJECT_BOUNDING_BOX,
    val maskContentUnits: MaskUnits = MaskUnits.USER_SPACE_ON_USE,
    val x: Float = -0.1f,
    val y: Float = -0.1f,
    val width: Float = 1.2f,
    val height: Float = 1.2f
) : SvgElement

/**
 * SVG defs element.
 * Container for reusable elements like clipPath, mask, gradients, etc.
 * @param children Child definition elements
 */
data class SvgDefs(val children: List<SvgElement>) : SvgElement

enum class ClipPathUnits {
    USER_SPACE_ON_USE,
    OBJECT_BOUNDING_BOX
}

enum class MaskUnits {
    USER_SPACE_ON_USE,
    OBJECT_BOUNDING_BOX
}

// ============================================
// Animation DSL
// ============================================

/** Default animation duration */
val DefaultAnimationDuration = 500.milliseconds

/**
 * SVG calcMode attribute for animation timing.
 * Controls how intermediate values are calculated between keyframes.
 */
enum class CalcMode {
    /** Linear interpolation between values (default) */
    LINEAR,
    /** Jump directly between values with no interpolation */
    DISCRETE,
    /** Constant velocity interpolation */
    PACED,
    /** Cubic bezier interpolation using keySplines */
    SPLINE
}

/**
 * Cubic bezier control points for spline easing.
 * Used when calcMode is SPLINE.
 *
 * @param x1 X coordinate of first control point (0.0-1.0)
 * @param y1 Y coordinate of first control point
 * @param x2 X coordinate of second control point (0.0-1.0)
 * @param y2 Y coordinate of second control point
 */
data class KeySplines(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
) {
    companion object {
        /** CSS ease: cubic-bezier(0.25, 0.1, 0.25, 1) */
        val EASE = KeySplines(0.25f, 0.1f, 0.25f, 1f)
        /** CSS ease-in: cubic-bezier(0.42, 0, 1, 1) */
        val EASE_IN = KeySplines(0.42f, 0f, 1f, 1f)
        /** CSS ease-out: cubic-bezier(0, 0, 0.58, 1) */
        val EASE_OUT = KeySplines(0f, 0f, 0.58f, 1f)
        /** CSS ease-in-out: cubic-bezier(0.42, 0, 0.58, 1) */
        val EASE_IN_OUT = KeySplines(0.42f, 0f, 0.58f, 1f)
    }
}

/**
 * SVG animate element.
 * Corresponds to SVG <animate>, <animateTransform>, <animateMotion> elements.
 */
sealed interface SvgAnimate {
    val dur: Duration
    val delay: Duration
    val calcMode: CalcMode
    val keySplines: KeySplines?
    /** Number of animation iterations. Negative value (e.g., -1) means infinite. */
    val iterations: Int

    /** Returns true if this animation repeats infinitely. */
    val isInfinite: Boolean get() = iterations < 0

    companion object {
        /** Constant representing infinite iterations. */
        const val INFINITE = -1
    }

    // ============================================
    // Stroke Properties
    // ============================================

    /**
     * Stroke drawing animation (stroke-dashoffset).
     * Draws the stroke from start to end.
     */
    data class StrokeDraw(
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        val reverse: Boolean = false,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * stroke-width animation.
     */
    data class StrokeWidth(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * stroke-opacity animation.
     */
    data class StrokeOpacity(
        val from: Float = 0f,
        val to: Float = 1f,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * stroke-dasharray animation.
     */
    data class StrokeDasharray(
        val from: List<Float>,
        val to: List<Float>,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * stroke-dashoffset animation (raw value, not drawing effect).
     */
    data class StrokeDashoffset(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    // ============================================
    // Fill Properties
    // ============================================

    /**
     * fill-opacity animation.
     */
    data class FillOpacity(
        val from: Float = 0f,
        val to: Float = 1f,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    // ============================================
    // Opacity & Visibility
    // ============================================

    /**
     * opacity animation.
     */
    data class Opacity(
        val from: Float = 0f,
        val to: Float = 1f,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    // ============================================
    // Geometric Properties
    // ============================================

    /**
     * cx (center x) animation for circle/ellipse.
     */
    data class Cx(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * cy (center y) animation for circle/ellipse.
     */
    data class Cy(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * r (radius) animation for circle.
     */
    data class R(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * rx animation for ellipse/rect.
     */
    data class Rx(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * ry animation for ellipse/rect.
     */
    data class Ry(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * x position animation.
     */
    data class X(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * y position animation.
     */
    data class Y(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * width animation.
     */
    data class Width(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * height animation.
     */
    data class Height(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * x1 animation for line.
     */
    data class X1(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * y1 animation for line.
     */
    data class Y1(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * x2 animation for line.
     */
    data class X2(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * y2 animation for line.
     */
    data class Y2(
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * d (path data) animation - path morphing.
     */
    data class D(
        val from: String,
        val to: String,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    /**
     * points animation for polygon/polyline.
     */
    data class Points(
        val from: List<Offset>,
        val to: List<Offset>,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    // ============================================
    // Transform (animateTransform)
    // ============================================

    /**
     * Transform animation (animateTransform).
     */
    data class Transform(
        val type: TransformType,
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate

    // ============================================
    // Motion (animateMotion)
    // ============================================

    /**
     * Motion animation (animateMotion).
     * Moves the element along a path.
     */
    data class Motion(
        val path: String,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        val rotate: MotionRotate = MotionRotate.NONE,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE
    ) : SvgAnimate
}

enum class TransformType {
    TRANSLATE,   // translate(x, y)
    TRANSLATE_X,
    TRANSLATE_Y,
    SCALE,       // scale(s) or scale(sx, sy)
    SCALE_X,
    SCALE_Y,
    ROTATE,      // rotate(angle)
    SKEW_X,
    SKEW_Y
}

enum class MotionRotate {
    NONE,       // No rotation
    AUTO,       // Rotate to follow the path direction
    AUTO_REVERSE // Rotate to follow path direction + 180°
}

/**
 * SVG element with animation.
 */
data class SvgAnimated(
    val element: SvgElement,
    val animations: List<SvgAnimate>
) : SvgElement

/**
 * DSL builder for SVG elements.
 */
@DslMarker
annotation class SvgDslMarker

/**
 * DSL builder for type-safe SVG path commands.
 */
@SvgDslMarker
class PathBuilder {
    private val commands = mutableListOf<PathCommand>()

    /** Move to absolute position (M) */
    fun moveTo(x: Float, y: Float) {
        commands.add(PathCommand.MoveTo(x, y))
    }

    /** Move to relative position (m) */
    fun moveToRelative(dx: Float, dy: Float) {
        commands.add(PathCommand.MoveToRelative(dx, dy))
    }

    /** Line to absolute position (L) */
    fun lineTo(x: Float, y: Float) {
        commands.add(PathCommand.LineTo(x, y))
    }

    /** Line to relative position (l) */
    fun lineToRelative(dx: Float, dy: Float) {
        commands.add(PathCommand.LineToRelative(dx, dy))
    }

    /** Horizontal line to absolute x (H) */
    fun horizontalLineTo(x: Float) {
        commands.add(PathCommand.HorizontalLineTo(x))
    }

    /** Horizontal line to relative dx (h) */
    fun horizontalLineToRelative(dx: Float) {
        commands.add(PathCommand.HorizontalLineToRelative(dx))
    }

    /** Vertical line to absolute y (V) */
    fun verticalLineTo(y: Float) {
        commands.add(PathCommand.VerticalLineTo(y))
    }

    /** Vertical line to relative dy (v) */
    fun verticalLineToRelative(dy: Float) {
        commands.add(PathCommand.VerticalLineToRelative(dy))
    }

    /** Cubic bezier curve absolute (C) */
    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) {
        commands.add(PathCommand.CubicTo(x1, y1, x2, y2, x, y))
    }

    /** Cubic bezier curve relative (c) */
    fun cubicToRelative(dx1: Float, dy1: Float, dx2: Float, dy2: Float, dx: Float, dy: Float) {
        commands.add(PathCommand.CubicToRelative(dx1, dy1, dx2, dy2, dx, dy))
    }

    /** Smooth cubic bezier absolute (S) */
    fun smoothCubicTo(x2: Float, y2: Float, x: Float, y: Float) {
        commands.add(PathCommand.SmoothCubicTo(x2, y2, x, y))
    }

    /** Smooth cubic bezier relative (s) */
    fun smoothCubicToRelative(dx2: Float, dy2: Float, dx: Float, dy: Float) {
        commands.add(PathCommand.SmoothCubicToRelative(dx2, dy2, dx, dy))
    }

    /** Quadratic bezier curve absolute (Q) */
    fun quadTo(x1: Float, y1: Float, x: Float, y: Float) {
        commands.add(PathCommand.QuadTo(x1, y1, x, y))
    }

    /** Quadratic bezier curve relative (q) */
    fun quadToRelative(dx1: Float, dy1: Float, dx: Float, dy: Float) {
        commands.add(PathCommand.QuadToRelative(dx1, dy1, dx, dy))
    }

    /** Smooth quadratic bezier absolute (T) */
    fun smoothQuadTo(x: Float, y: Float) {
        commands.add(PathCommand.SmoothQuadTo(x, y))
    }

    /** Smooth quadratic bezier relative (t) */
    fun smoothQuadToRelative(dx: Float, dy: Float) {
        commands.add(PathCommand.SmoothQuadToRelative(dx, dy))
    }

    /** Arc absolute (A) */
    fun arcTo(rx: Float, ry: Float, xAxisRotation: Float, largeArcFlag: Boolean, sweepFlag: Boolean, x: Float, y: Float) {
        commands.add(PathCommand.ArcTo(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y))
    }

    /** Arc relative (a) */
    fun arcToRelative(rx: Float, ry: Float, xAxisRotation: Float, largeArcFlag: Boolean, sweepFlag: Boolean, dx: Float, dy: Float) {
        commands.add(PathCommand.ArcToRelative(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, dx, dy))
    }

    /** Close path (Z/z) */
    fun close() {
        commands.add(PathCommand.Close)
    }

    fun build(): List<PathCommand> = commands.toList()
}

@SvgDslMarker
class SvgBuilder {
    private val elements = mutableListOf<SvgElement>()

    /** Helper to wrap element with style if any style properties are set */
    private fun addWithStyle(
        element: SvgElement,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        if (stroke != null || fill != null || strokeWidth != null || opacity != null) {
            elements.add(SvgStyled(element, SvgStyle(stroke = stroke, fill = fill, strokeWidth = strokeWidth, opacity = opacity)))
        } else {
            elements.add(element)
        }
    }

    /**
     * Path from string data.
     */
    fun path(
        d: String,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        addWithStyle(SvgPath(d), stroke, fill, strokeWidth, opacity)
    }

    /**
     * Path from string with animation.
     */
    fun path(d: String, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgPath(d), animations))
    }

    /**
     * Type-safe path builder.
     */
    fun path(
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        block: PathBuilder.() -> Unit
    ) {
        val commands = PathBuilder().apply(block).build()
        addWithStyle(SvgPath(commands = commands), stroke, fill, strokeWidth, opacity)
    }

    /**
     * Type-safe path with animation builder.
     */
    fun animatedPath(pathBlock: PathBuilder.() -> Unit, animBlock: AnimationBuilder.() -> Unit) {
        val commands = PathBuilder().apply(pathBlock).build()
        val animations = AnimationBuilder().apply(animBlock).build()
        elements.add(SvgAnimated(SvgPath(commands = commands), animations))
    }

    fun circle(
        cx: Number,
        cy: Number,
        r: Number,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        addWithStyle(SvgCircle(cx.toFloat(), cy.toFloat(), r.toFloat()), stroke, fill, strokeWidth, opacity)
    }

    fun circle(cx: Number, cy: Number, r: Number, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgCircle(cx.toFloat(), cy.toFloat(), r.toFloat()), animations))
    }

    fun ellipse(
        cx: Number,
        cy: Number,
        rx: Number,
        ry: Number,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        addWithStyle(SvgEllipse(cx.toFloat(), cy.toFloat(), rx.toFloat(), ry.toFloat()), stroke, fill, strokeWidth, opacity)
    }

    fun rect(
        x: Number = 0,
        y: Number = 0,
        width: Number,
        height: Number,
        rx: Number = 0,
        ry: Number = rx,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        addWithStyle(SvgRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), rx.toFloat(), ry.toFloat()), stroke, fill, strokeWidth, opacity)
    }

    fun line(
        x1: Number,
        y1: Number,
        x2: Number,
        y2: Number,
        stroke: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        addWithStyle(SvgLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat()), stroke, null, strokeWidth, opacity)
    }

    fun line(x1: Number, y1: Number, x2: Number, y2: Number, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat()), animations))
    }

    /**
     * Polyline with vararg points: polyline(5 to 12, 12 to 5, 19 to 12)
     */
    fun polyline(
        vararg points: Pair<Number, Number>,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        addWithStyle(SvgPolyline(offsets), stroke, fill, strokeWidth, opacity)
    }

    /**
     * Polyline from points string: polyline("5,12 12,5 19,12")
     */
    fun polyline(
        points: String,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        addWithStyle(SvgPolyline(parsePointsString(points)), stroke, fill, strokeWidth, opacity)
    }

    /**
     * Polygon with vararg points: polygon(12 to 2, 22 to 22, 2 to 22)
     */
    fun polygon(
        vararg points: Pair<Number, Number>,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        addWithStyle(SvgPolygon(offsets), stroke, fill, strokeWidth, opacity)
    }

    /**
     * Polygon from points string: polygon("12,2 22,22 2,22")
     */
    fun polygon(
        points: String,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        addWithStyle(SvgPolygon(parsePointsString(points)), stroke, fill, strokeWidth, opacity)
    }

    private fun parsePointsString(points: String): List<Offset> {
        val numbers = points.trim()
            .split(Regex("[\\s,]+"))
            .mapNotNull { it.toFloatOrNull() }
        val result = mutableListOf<Offset>()
        for (i in numbers.indices step 2) {
            if (i + 1 < numbers.size) {
                result.add(Offset(numbers[i], numbers[i + 1]))
            }
        }
        return result
    }

    fun group(block: SvgBuilder.() -> Unit) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgGroup(children))
    }

    /**
     * Defines a clipping region.
     * @param id Unique identifier for referencing this clip path
     * @param clipPathUnits Coordinate system (default: userSpaceOnUse)
     * @param block Builder for clip path children
     */
    fun clipPath(
        id: String,
        clipPathUnits: ClipPathUnits = ClipPathUnits.USER_SPACE_ON_USE,
        block: SvgBuilder.() -> Unit
    ) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgClipPath(id, children, clipPathUnits))
    }

    /**
     * Defines a mask.
     * @param id Unique identifier for referencing this mask
     * @param block Builder for mask children
     */
    fun mask(
        id: String,
        maskUnits: MaskUnits = MaskUnits.OBJECT_BOUNDING_BOX,
        maskContentUnits: MaskUnits = MaskUnits.USER_SPACE_ON_USE,
        block: SvgBuilder.() -> Unit
    ) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgMask(id, children, maskUnits, maskContentUnits))
    }

    /**
     * Defs container for reusable elements.
     * @param block Builder for defs children (clipPath, mask, etc.)
     */
    fun defs(block: SvgBuilder.() -> Unit) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgDefs(children))
    }

    /**
     * Animated path with stroke draw animation.
     */
    fun animatedPath(
        d: String,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        reverse: Boolean = false
    ) {
        elements.add(SvgAnimated(
            element = SvgPath(d),
            animations = listOf(SvgAnimate.StrokeDraw(dur, delay, reverse))
        ))
    }

    /**
     * Animated circle with stroke draw animation.
     */
    fun animatedCircle(
        cx: Number,
        cy: Number,
        r: Number,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO
    ) {
        elements.add(SvgAnimated(
            element = SvgCircle(cx.toFloat(), cy.toFloat(), r.toFloat()),
            animations = listOf(SvgAnimate.StrokeDraw(dur, delay))
        ))
    }

    /**
     * Animated line with stroke draw animation.
     */
    fun animatedLine(
        x1: Number,
        y1: Number,
        x2: Number,
        y2: Number,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO
    ) {
        elements.add(SvgAnimated(
            element = SvgLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat()),
            animations = listOf(SvgAnimate.StrokeDraw(dur, delay))
        ))
    }

    /**
     * Generic animated element builder.
     */
    fun animated(element: SvgElement, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(element, animations))
    }

    fun build(): List<SvgElement> = elements.toList()
}

/**
 * Builder for animations.
 */
@SvgDslMarker
class AnimationBuilder {
    private val animations = mutableListOf<SvgAnimate>()

    fun strokeDraw(
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        reverse: Boolean = false,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.StrokeDraw(dur, delay, reverse, calcMode, keySplines))
    }

    fun opacity(
        from: Float = 0f,
        to: Float = 1f,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Opacity(from, to, dur, delay, calcMode, keySplines))
    }

    fun translateX(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Transform(TransformType.TRANSLATE_X, from, to, dur, delay, calcMode, keySplines))
    }

    fun translateY(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Transform(TransformType.TRANSLATE_Y, from, to, dur, delay, calcMode, keySplines))
    }

    fun scale(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Transform(TransformType.SCALE, from, to, dur, delay, calcMode, keySplines))
    }

    fun rotate(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Transform(TransformType.ROTATE, from, to, dur, delay, calcMode, keySplines))
    }

    fun skewX(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Transform(TransformType.SKEW_X, from, to, dur, delay, calcMode, keySplines))
    }

    fun skewY(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Transform(TransformType.SKEW_Y, from, to, dur, delay, calcMode, keySplines))
    }

    fun motion(
        path: String,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        rotate: MotionRotate = MotionRotate.NONE,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Motion(path, dur, delay, rotate, calcMode, keySplines))
    }

    // Stroke properties
    fun strokeWidth(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.StrokeWidth(from, to, dur, delay, calcMode, keySplines))
    }

    fun strokeOpacity(
        from: Float = 0f,
        to: Float = 1f,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.StrokeOpacity(from, to, dur, delay, calcMode, keySplines))
    }

    fun strokeDasharray(
        from: List<Float>,
        to: List<Float>,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.StrokeDasharray(from, to, dur, delay, calcMode, keySplines))
    }

    fun strokeDashoffset(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.StrokeDashoffset(from, to, dur, delay, calcMode, keySplines))
    }

    // Fill properties
    fun fillOpacity(
        from: Float = 0f,
        to: Float = 1f,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.FillOpacity(from, to, dur, delay, calcMode, keySplines))
    }

    // Geometric properties
    fun cx(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Cx(from, to, dur, delay, calcMode, keySplines))
    }

    fun cy(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Cy(from, to, dur, delay, calcMode, keySplines))
    }

    fun r(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.R(from, to, dur, delay, calcMode, keySplines))
    }

    fun rx(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Rx(from, to, dur, delay, calcMode, keySplines))
    }

    fun ry(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Ry(from, to, dur, delay, calcMode, keySplines))
    }

    fun x(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.X(from, to, dur, delay, calcMode, keySplines))
    }

    fun y(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Y(from, to, dur, delay, calcMode, keySplines))
    }

    fun width(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Width(from, to, dur, delay, calcMode, keySplines))
    }

    fun height(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Height(from, to, dur, delay, calcMode, keySplines))
    }

    fun x1(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.X1(from, to, dur, delay, calcMode, keySplines))
    }

    fun y1(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Y1(from, to, dur, delay, calcMode, keySplines))
    }

    fun x2(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.X2(from, to, dur, delay, calcMode, keySplines))
    }

    fun y2(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Y2(from, to, dur, delay, calcMode, keySplines))
    }

    fun d(
        from: String,
        to: String,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.D(from, to, dur, delay, calcMode, keySplines))
    }

    fun points(
        from: List<Offset>,
        to: List<Offset>,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null
    ) {
        animations.add(SvgAnimate.Points(from, to, dur, delay, calcMode, keySplines))
    }

    fun build(): List<SvgAnimate> = animations.toList()
}

/**
 * DSL entry point for building SVG elements.
 * Returns a list of elements to be used as children of an Svg.
 *
 * Example:
 * ```kotlin
 * val elements = svg {
 *     path("M20 6L9 17l-5-5")
 *     circle(12, 12, 10)
 * }
 * ```
 */
inline fun svg(block: SvgBuilder.() -> Unit): List<SvgElement> {
    return SvgBuilder().apply(block).build()
}

/**
 * DSL entry point for building a complete Svg object with customizable attributes.
 * Returns an Svg object that can be used directly in SvgIcon.
 *
 * Color handling:
 * - Color.Unspecified = "currentColor" (uses tint from SvgIcon composable)
 * - null = "none" (no color / transparent)
 * - Any other Color = that specific color
 *
 * Example:
 * ```kotlin
 * object MyIcon : SvgIcon {
 *     override val svg = svg(
 *         strokeWidth = 3f,
 *         stroke = Color.Red,
 *         fill = Color.Blue.copy(alpha = 0.3f)
 *     ) {
 *         path("M20 6L9 17l-5-5")
 *         circle(12, 12, 10)
 *     }
 * }
 * ```
 *
 * @param width Width of the SVG (default: 24)
 * @param height Height of the SVG (default: 24)
 * @param viewBox ViewBox (default: derived from width/height as "0 0 width height")
 * @param fill Default fill color (default: null = no fill)
 * @param stroke Default stroke color (default: Unspecified = uses tint color)
 * @param strokeWidth Default stroke width (default: 2f)
 * @param strokeLinecap Default stroke line cap (default: ROUND)
 * @param strokeLinejoin Default stroke line join (default: ROUND)
 * @param block Builder block for adding SVG elements
 */
inline fun svg(
    width: Int = 24,
    height: Int = 24,
    viewBox: ViewBox? = null,
    fill: Color? = null,
    stroke: Color? = Color.Unspecified,
    strokeWidth: Float = 2f,
    strokeLinecap: LineCap = LineCap.ROUND,
    strokeLinejoin: LineJoin = LineJoin.ROUND,
    block: SvgBuilder.() -> Unit
): Svg {
    return Svg(
        viewBox = viewBox ?: ViewBox(0f, 0f, width.toFloat(), height.toFloat()),
        fill = fill,
        stroke = stroke,
        strokeWidth = strokeWidth,
        strokeLinecap = strokeLinecap,
        strokeLinejoin = strokeLinejoin,
        children = SvgBuilder().apply(block).build()
    )
}

/**
 * Exception thrown when SVG path data parsing fails.
 */
class SvgPathParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Parses SVG path data string into a list of PathCommands.
 * @throws SvgPathParseException if the path data is invalid
 */
internal fun parsePathCommands(pathData: String): List<PathCommand> {
    val commands = mutableListOf<PathCommand>()
    val tokens = tokenizePathData(pathData)
    var i = 0
    var lastCommand = ' '

    try {
        while (i < tokens.size) {
            val token = tokens[i]
            val command = if (token.length == 1 && token[0].isLetter()) {
                i++
                token[0]
            } else {
                // Repeat last command (except for M which becomes L)
                when (lastCommand) {
                    'M' -> 'L'
                    'm' -> 'l'
                    ' ' -> throw SvgPathParseException("Path must start with a move command (M or m), got: $token")
                    else -> lastCommand
                }
            }

            when (command) {
                'M' -> {
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.MoveTo(x, y))
                }
                'm' -> {
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.MoveToRelative(dx, dy))
                }
                'L' -> {
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.LineTo(x, y))
                }
                'l' -> {
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.LineToRelative(dx, dy))
                }
                'H' -> {
                    val x = tokens[i++].toFloat()
                    commands.add(PathCommand.HorizontalLineTo(x))
                }
                'h' -> {
                    val dx = tokens[i++].toFloat()
                    commands.add(PathCommand.HorizontalLineToRelative(dx))
                }
                'V' -> {
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.VerticalLineTo(y))
                }
                'v' -> {
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.VerticalLineToRelative(dy))
                }
                'C' -> {
                    val x1 = tokens[i++].toFloat()
                    val y1 = tokens[i++].toFloat()
                    val x2 = tokens[i++].toFloat()
                    val y2 = tokens[i++].toFloat()
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.CubicTo(x1, y1, x2, y2, x, y))
                }
                'c' -> {
                    val dx1 = tokens[i++].toFloat()
                    val dy1 = tokens[i++].toFloat()
                    val dx2 = tokens[i++].toFloat()
                    val dy2 = tokens[i++].toFloat()
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.CubicToRelative(dx1, dy1, dx2, dy2, dx, dy))
                }
                'S' -> {
                    val x2 = tokens[i++].toFloat()
                    val y2 = tokens[i++].toFloat()
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.SmoothCubicTo(x2, y2, x, y))
                }
                's' -> {
                    val dx2 = tokens[i++].toFloat()
                    val dy2 = tokens[i++].toFloat()
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.SmoothCubicToRelative(dx2, dy2, dx, dy))
                }
                'Q' -> {
                    val x1 = tokens[i++].toFloat()
                    val y1 = tokens[i++].toFloat()
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.QuadTo(x1, y1, x, y))
                }
                'q' -> {
                    val dx1 = tokens[i++].toFloat()
                    val dy1 = tokens[i++].toFloat()
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.QuadToRelative(dx1, dy1, dx, dy))
                }
                'T' -> {
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.SmoothQuadTo(x, y))
                }
                't' -> {
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.SmoothQuadToRelative(dx, dy))
                }
                'A' -> {
                    val rx = tokens[i++].toFloat()
                    val ry = tokens[i++].toFloat()
                    val xAxisRotation = tokens[i++].toFloat()
                    val largeArcFlag = tokens[i++].toFloat() != 0f
                    val sweepFlag = tokens[i++].toFloat() != 0f
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.ArcTo(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y))
                }
                'a' -> {
                    val rx = tokens[i++].toFloat()
                    val ry = tokens[i++].toFloat()
                    val xAxisRotation = tokens[i++].toFloat()
                    val largeArcFlag = tokens[i++].toFloat() != 0f
                    val sweepFlag = tokens[i++].toFloat() != 0f
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.ArcToRelative(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, dx, dy))
                }
                'Z', 'z' -> {
                    commands.add(PathCommand.Close)
                }
                else -> throw SvgPathParseException("Unknown path command: $command")
            }
            lastCommand = command
        }
    } catch (e: SvgPathParseException) {
        throw e
    } catch (e: IndexOutOfBoundsException) {
        throw SvgPathParseException("Unexpected end of path data: not enough parameters for command", e)
    } catch (e: NumberFormatException) {
        throw SvgPathParseException("Invalid number in path data: ${e.message}", e)
    }

    return commands
}

private fun tokenizePathData(pathData: String): List<String> {
    val tokens = mutableListOf<String>()
    val current = StringBuilder()

    for (char in pathData) {
        when {
            char.isLetter() -> {
                if (current.isNotEmpty()) {
                    tokens.add(current.toString())
                    current.clear()
                }
                tokens.add(char.toString())
            }
            char == ',' || char == ' ' || char == '\n' || char == '\t' -> {
                if (current.isNotEmpty()) {
                    tokens.add(current.toString())
                    current.clear()
                }
            }
            char == '-' -> {
                if (current.isNotEmpty() && !current.endsWith("e") && !current.endsWith("E")) {
                    tokens.add(current.toString())
                    current.clear()
                }
                current.append(char)
            }
            char == '.' -> {
                // Handle consecutive decimals like ".5.5" which means "0.5 0.5"
                if (current.contains('.')) {
                    tokens.add(current.toString())
                    current.clear()
                }
                current.append(char)
            }
            else -> {
                current.append(char)
            }
        }
    }

    if (current.isNotEmpty()) {
        tokens.add(current.toString())
    }

    return tokens
}
