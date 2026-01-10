package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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
 * Transform type for animateTransform.
 */
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

/**
 * Additive mode for animateTransform (SVG SMIL spec).
 */
enum class AdditiveMode {
    /** Overwrite previous transforms (default) */
    REPLACE,
    /** Add to previous transforms */
    SUM
}

/**
 * Rotation mode for animateMotion.
 */
enum class MotionRotate {
    NONE,       // No rotation
    AUTO,       // Rotate to follow the path direction
    AUTO_REVERSE // Rotate to follow path direction + 180°
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
    /** Animation direction (normal, reverse, alternate, alternate-reverse). */
    val direction: AnimationDirection
    /** How styles are applied before/after animation. */
    val fillMode: AnimationFillMode

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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) : SvgAnimate

    // ============================================
    // Transform (animateTransform)
    // ============================================

    /**
     * Transform animation (animateTransform).
     *
     * For translate type, use [fromY] and [toY] to specify Y values separately.
     * If not specified, Y values default to 0.
     *
     * @param additive Additive mode (replace or sum). When sum, transforms accumulate.
     */
    data class Transform(
        val type: TransformType,
        val from: Float,
        val to: Float,
        override val dur: Duration = DefaultAnimationDuration,
        override val delay: Duration = Duration.ZERO,
        override val calcMode: CalcMode = CalcMode.LINEAR,
        override val keySplines: KeySplines? = null,
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE,
        /** Y value for translate (from). Only used when type is TRANSLATE. */
        val fromY: Float = 0f,
        /** Y value for translate (to). Only used when type is TRANSLATE. */
        val toY: Float = 0f,
        /** Additive mode for combining with other transforms. */
        val additive: AdditiveMode = AdditiveMode.REPLACE
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
        override val iterations: Int = INFINITE,
        override val direction: AnimationDirection = AnimationDirection.NORMAL,
        override val fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) : SvgAnimate
}
