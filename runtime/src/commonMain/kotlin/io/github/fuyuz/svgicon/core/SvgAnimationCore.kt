package io.github.fuyuz.svgicon.core

import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.fuyuz.svgicon.core.*

/**
 * Key for animation state mapping.
 */
data class AnimationKey(val element: SvgElement, val animation: SvgAnimate)

/**
 * Entry for collected animations.
 */
data class AnimationEntry(val element: SvgElement, val animation: SvgAnimate, val index: Int)

/**
 * State container for all animation values.
 */
data class AnimatedElementState(
    var rotationAngle: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var translateX: Float = 0f,
    var translateY: Float = 0f,
    var skewX: Float = 0f,
    var skewY: Float = 0f,
    var opacity: Float = 1f,
    var strokeOpacity: Float = 1f,
    var fillOpacity: Float = 1f,
    var strokeWidth: Float? = null,
    var strokeDrawProgress: Float? = null,
    var strokeDasharray: List<Float>? = null,
    var strokeDashoffset: Float? = null,
    var cx: Float? = null,
    var cy: Float? = null,
    var r: Float? = null,
    var rx: Float? = null,
    var ry: Float? = null,
    var x: Float? = null,
    var y: Float? = null,
    var width: Float? = null,
    var height: Float? = null,
    var x1: Float? = null,
    var y1: Float? = null,
    var x2: Float? = null,
    var y2: Float? = null,
    var morphedPath: List<PathCommand>? = null,
    var morphedPoints: List<Offset>? = null,
    var motionPath: String? = null,
    var motionProgress: Float = 0f,
    var motionRotate: MotionRotate = MotionRotate.NONE
)

internal fun collectAllAnimations(elements: List<SvgElement>): List<AnimationEntry> {
    val animations = mutableListOf<AnimationEntry>()
    var currentIndex = 0

    fun collect(element: SvgElement) {
        when (element) {
            is SvgAnimated -> {
                element.animations.forEach { anim ->
                    animations.add(AnimationEntry(element.element, anim, currentIndex++))
                }
                collect(element.element)
            }
            is SvgGroup -> element.children.forEach { collect(it) }
            is SvgStyled -> collect(element.element)
            else -> {}
        }
    }

    elements.forEach { collect(it) }
    return animations
}

val Svg.duration: Float
    get() {
        var maxDuration = 0f
        fun collect(element: SvgElement) {
            when (element) {
                is SvgAnimated -> {
                    element.animations.forEach { anim ->
                        val total = anim.delay.inWholeMilliseconds.toFloat() + anim.dur.inWholeMilliseconds.toFloat()
                        if (total > maxDuration) maxDuration = total
                    }
                    collect(element.element)
                }
                is SvgGroup -> element.children.forEach { collect(it) }
                is SvgStyled -> collect(element.element)
                else -> {}
            }
        }
        children.forEach { collect(it) }
        return if (maxDuration == 0f) 1000f else maxDuration
    }

fun getFillProgress(
    timeInAnimation: Float,
    totalAnimationMs: Float,
    fillMode: AnimationFillMode,
    direction: AnimationDirection,
    iteration: Int
): Float? {
    // Basic implementation of progress calculation
    var progress = if (totalAnimationMs > 0) (timeInAnimation / totalAnimationMs) else 1f
    
    val effectiveDirection = when (direction) {
        AnimationDirection.NORMAL -> AnimationDirection.NORMAL
        AnimationDirection.REVERSE -> AnimationDirection.REVERSE
        AnimationDirection.ALTERNATE -> if (iteration % 2 == 0) AnimationDirection.NORMAL else AnimationDirection.REVERSE
        AnimationDirection.ALTERNATE_REVERSE -> if (iteration % 2 == 0) AnimationDirection.REVERSE else AnimationDirection.NORMAL
    }

    if (effectiveDirection == AnimationDirection.REVERSE) {
        progress = 1f - progress
    }

    return progress.coerceIn(0f, 1f)
}

internal fun interpolateDasharray(from: List<Float>, to: List<Float>, progress: Float): List<Float> {
    if (from.size != to.size) return if (progress < 0.5f) from else to
    return from.zip(to).map { (f, t) -> f + (t - f) * progress }
}

internal fun interpolatePoints(from: List<Offset>, to: List<Offset>, progress: Float): List<Offset> {
    if (from.size != to.size) return if (progress < 0.5f) from else to
    return from.zip(to).map { (f, t) -> 
        Offset(
            f.x + (t.x - f.x) * progress,
            f.y + (t.y - f.y) * progress
        )
    }
}

internal fun interpolatePathCommands(fromData: String, toData: String, progress: Float): List<PathCommand> {
    val from = parsePathCommands(fromData)
    val to = parsePathCommands(toData)
    if (from.size != to.size) return if (progress < 0.5f) from else to
    return from.zip(to).map { (f, t) -> interpolateCommand(f, t, progress) }
}

internal fun interpolateCommand(from: PathCommand, to: PathCommand, progress: Float): PathCommand {
    if (from::class != to::class) return if (progress < 0.5f) from else to
    
    fun lerp(a: Float, b: Float) = a + (b - a) * progress

    return when (from) {
        is PathCommand.MoveTo -> {
            val t = to as PathCommand.MoveTo
            PathCommand.MoveTo(lerp(from.x, t.x), lerp(from.y, t.y))
        }
        is PathCommand.MoveToRelative -> {
            val t = to as PathCommand.MoveToRelative
            PathCommand.MoveToRelative(lerp(from.dx, t.dx), lerp(from.dy, t.dy))
        }
        is PathCommand.LineTo -> {
            val t = to as PathCommand.LineTo
            PathCommand.LineTo(lerp(from.x, t.x), lerp(from.y, t.y))
        }
        is PathCommand.LineToRelative -> {
            val t = to as PathCommand.LineToRelative
            PathCommand.LineToRelative(lerp(from.dx, t.dx), lerp(from.dy, t.dy))
        }
        is PathCommand.HorizontalLineTo -> {
            val t = to as PathCommand.HorizontalLineTo
            PathCommand.HorizontalLineTo(lerp(from.x, t.x))
        }
        is PathCommand.HorizontalLineToRelative -> {
            val t = to as PathCommand.HorizontalLineToRelative
            PathCommand.HorizontalLineToRelative(lerp(from.dx, t.dx))
        }
        is PathCommand.VerticalLineTo -> {
            val t = to as PathCommand.VerticalLineTo
            PathCommand.VerticalLineTo(lerp(from.y, t.y))
        }
        is PathCommand.VerticalLineToRelative -> {
            val t = to as PathCommand.VerticalLineToRelative
            PathCommand.VerticalLineToRelative(lerp(from.dy, t.dy))
        }
        is PathCommand.CubicTo -> {
            val t = to as PathCommand.CubicTo
            PathCommand.CubicTo(
                lerp(from.x1, t.x1), lerp(from.y1, t.y1),
                lerp(from.x2, t.x2), lerp(from.y2, t.y2),
                lerp(from.x, t.x), lerp(from.y, t.y)
            )
        }
        is PathCommand.CubicToRelative -> {
            val t = to as PathCommand.CubicToRelative
            PathCommand.CubicToRelative(
                lerp(from.dx1, t.dx1), lerp(from.dy1, t.dy1),
                lerp(from.dx2, t.dx2), lerp(from.dy2, t.dy2),
                lerp(from.dx, t.dx), lerp(from.dy, t.dy)
            )
        }
        is PathCommand.SmoothCubicTo -> {
            val t = to as PathCommand.SmoothCubicTo
            PathCommand.SmoothCubicTo(
                lerp(from.x2, t.x2), lerp(from.y2, t.y2),
                lerp(from.x, t.x), lerp(from.y, t.y)
            )
        }
        is PathCommand.SmoothCubicToRelative -> {
            val t = to as PathCommand.SmoothCubicToRelative
            PathCommand.SmoothCubicToRelative(
                lerp(from.dx2, t.dx2), lerp(from.dy2, t.dy2),
                lerp(from.dx, t.dx), lerp(from.dy, t.dy)
            )
        }
        is PathCommand.QuadTo -> {
            val t = to as PathCommand.QuadTo
            PathCommand.QuadTo(
                lerp(from.x1, t.x1), lerp(from.y1, t.y1),
                lerp(from.x, t.x), lerp(from.y, t.y)
            )
        }
        is PathCommand.QuadToRelative -> {
            val t = to as PathCommand.QuadToRelative
            PathCommand.QuadToRelative(
                lerp(from.dx1, t.dx1), lerp(from.dy1, t.dy1),
                lerp(from.dx, t.dx), lerp(from.dy, t.dy)
            )
        }
        is PathCommand.SmoothQuadTo -> {
            val t = to as PathCommand.SmoothQuadTo
            PathCommand.SmoothQuadTo(lerp(from.x, t.x), lerp(from.y, t.y))
        }
        is PathCommand.SmoothQuadToRelative -> {
            val t = to as PathCommand.SmoothQuadToRelative
            PathCommand.SmoothQuadToRelative(lerp(from.dx, t.dx), lerp(from.dy, t.dy))
        }
        is PathCommand.ArcTo -> {
            val t = to as PathCommand.ArcTo
            PathCommand.ArcTo(
                lerp(from.rx, t.rx), lerp(from.ry, t.ry),
                lerp(from.xAxisRotation, t.xAxisRotation),
                if (progress < 0.5f) from.largeArcFlag else t.largeArcFlag,
                if (progress < 0.5f) from.sweepFlag else t.sweepFlag,
                lerp(from.x, t.x), lerp(from.y, t.y)
            )
        }
        is PathCommand.ArcToRelative -> {
            val t = to as PathCommand.ArcToRelative
            PathCommand.ArcToRelative(
                lerp(from.rx, t.rx), lerp(from.ry, t.ry),
                lerp(from.xAxisRotation, t.xAxisRotation),
                if (progress < 0.5f) from.largeArcFlag else t.largeArcFlag,
                if (progress < 0.5f) from.sweepFlag else t.sweepFlag,
                lerp(from.dx, t.dx), lerp(from.dy, t.dy)
            )
        }
        is PathCommand.Close -> PathCommand.Close
    }
}
