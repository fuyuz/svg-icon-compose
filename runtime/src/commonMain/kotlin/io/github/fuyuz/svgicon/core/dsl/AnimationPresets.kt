package io.github.fuyuz.svgicon.core.dsl

import io.github.fuyuz.svgicon.core.AnimationDirection
import io.github.fuyuz.svgicon.core.SvgAnimate
import io.github.fuyuz.svgicon.core.TransformType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Predefined animation presets for common use cases.
 *
 * Example:
 * ```kotlin
 * svg {
 *     circle(12, 12, 10) with Animations.fadeIn
 *     path("M10 10") with Animations.strokeDraw()
 * }
 * ```
 */
object Animations {
    // ============================================
    // Opacity Animations
    // ============================================

    /** Fade in from transparent to opaque. */
    val fadeIn: List<SvgAnimate> = listOf(
        SvgAnimate.Opacity(from = 0f, to = 1f, dur = 300.milliseconds)
    )

    /** Fade out from opaque to transparent. */
    val fadeOut: List<SvgAnimate> = listOf(
        SvgAnimate.Opacity(from = 1f, to = 0f, dur = 300.milliseconds)
    )

    /** Fade in with custom duration. */
    fun fadeIn(dur: Duration): List<SvgAnimate> = listOf(
        SvgAnimate.Opacity(from = 0f, to = 1f, dur = dur)
    )

    /** Fade out with custom duration. */
    fun fadeOut(dur: Duration): List<SvgAnimate> = listOf(
        SvgAnimate.Opacity(from = 1f, to = 0f, dur = dur)
    )

    // ============================================
    // Stroke Draw Animations
    // ============================================

    /** Draw stroke progressively. */
    fun strokeDraw(dur: Duration = 1.seconds, delay: Duration = Duration.ZERO): List<SvgAnimate> = listOf(
        SvgAnimate.StrokeDraw(dur = dur, delay = delay)
    )

    // ============================================
    // Transform Animations
    // ============================================

    /** Spin continuously (infinite rotation). */
    val spin: List<SvgAnimate> = listOf(
        SvgAnimate.Transform(
            type = TransformType.ROTATE,
            from = 0f,
            to = 360f,
            dur = 1.seconds
        )
    )

    /** Spin with custom duration. */
    fun spin(dur: Duration): List<SvgAnimate> = listOf(
        SvgAnimate.Transform(
            type = TransformType.ROTATE,
            from = 0f,
            to = 360f,
            dur = dur
        )
    )

    /** Pulse effect (scale up and down). */
    val pulse: List<SvgAnimate> = listOf(
        SvgAnimate.Transform(
            type = TransformType.SCALE,
            from = 1f,
            to = 1.1f,
            dur = 500.milliseconds,
            direction = AnimationDirection.ALTERNATE
        )
    )

    /** Pulse with custom scale and duration. */
    fun pulse(scale: Float = 1.1f, dur: Duration = 500.milliseconds): List<SvgAnimate> = listOf(
        SvgAnimate.Transform(
            type = TransformType.SCALE,
            from = 1f,
            to = scale,
            dur = dur,
            direction = AnimationDirection.ALTERNATE
        )
    )

    /** Shake effect (rotate back and forth). */
    val shake: List<SvgAnimate> = listOf(
        SvgAnimate.Transform(
            type = TransformType.ROTATE,
            from = -5f,
            to = 5f,
            dur = 100.milliseconds,
            direction = AnimationDirection.ALTERNATE
        )
    )

    /** Bounce effect (translate up and down). */
    val bounce: List<SvgAnimate> = listOf(
        SvgAnimate.Transform(
            type = TransformType.TRANSLATE_Y,
            from = 0f,
            to = -4f,
            dur = 300.milliseconds,
            direction = AnimationDirection.ALTERNATE
        )
    )

    // ============================================
    // Scale Animations
    // ============================================

    /** Scale in from zero. */
    val scaleIn: List<SvgAnimate> = listOf(
        SvgAnimate.Transform(
            type = TransformType.SCALE,
            from = 0f,
            to = 1f,
            dur = 300.milliseconds
        )
    )

    /** Scale out to zero. */
    val scaleOut: List<SvgAnimate> = listOf(
        SvgAnimate.Transform(
            type = TransformType.SCALE,
            from = 1f,
            to = 0f,
            dur = 300.milliseconds
        )
    )

    /** Scale in with custom duration. */
    fun scaleIn(dur: Duration): List<SvgAnimate> = listOf(
        SvgAnimate.Transform(
            type = TransformType.SCALE,
            from = 0f,
            to = 1f,
            dur = dur
        )
    )
}

