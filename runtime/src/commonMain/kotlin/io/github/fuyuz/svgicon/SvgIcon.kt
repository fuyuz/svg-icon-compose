package io.github.fuyuz.svgicon

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.tan
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.fuyuz.svgicon.core.AspectRatioAlign
import io.github.fuyuz.svgicon.core.ClipPathUnits
import io.github.fuyuz.svgicon.core.FillRule
import io.github.fuyuz.svgicon.core.LineCap
import io.github.fuyuz.svgicon.core.LineJoin
import io.github.fuyuz.svgicon.core.MeetOrSlice
import io.github.fuyuz.svgicon.core.PaintOrder
import io.github.fuyuz.svgicon.core.PreserveAspectRatio
import io.github.fuyuz.svgicon.core.Svg
import io.github.fuyuz.svgicon.core.SvgAnimate
import io.github.fuyuz.svgicon.core.SvgAnimated
import io.github.fuyuz.svgicon.core.SvgCircle
import io.github.fuyuz.svgicon.core.SvgClipPath
import io.github.fuyuz.svgicon.core.SvgDefs
import io.github.fuyuz.svgicon.core.SvgElement
import io.github.fuyuz.svgicon.core.SvgEllipse
import io.github.fuyuz.svgicon.core.SvgGroup
import io.github.fuyuz.svgicon.core.SvgLine
import io.github.fuyuz.svgicon.core.SvgMask
import io.github.fuyuz.svgicon.core.SvgPath
import io.github.fuyuz.svgicon.core.SvgPolygon
import io.github.fuyuz.svgicon.core.SvgPolyline
import io.github.fuyuz.svgicon.core.SvgRect
import io.github.fuyuz.svgicon.core.SvgStyle
import io.github.fuyuz.svgicon.core.SvgStyled
import io.github.fuyuz.svgicon.core.SvgTransform
import io.github.fuyuz.svgicon.core.TransformType
import io.github.fuyuz.svgicon.core.CalcMode
import io.github.fuyuz.svgicon.core.KeySplines
import io.github.fuyuz.svgicon.core.VectorEffect
import io.github.fuyuz.svgicon.core.toPath


// ============================================
// External Animation State Control API
// ============================================

/**
 * Read-only animation state for SvgIcon.
 * Use [SvgIconAnimatable] for mutable state.
 */
@Stable
interface SvgIconAnimationState {
    /** Current animation progress (0.0 to 1.0) */
    val progress: Float

    /** Whether the animation is currently playing */
    val isPlaying: Boolean

    /** Whether the animation is at the end (progress >= 1.0) */
    val isAtEnd: Boolean get() = progress >= 1f

    /** Whether the animation is at the start (progress <= 0.0) */
    val isAtStart: Boolean get() = progress <= 0f
}

/**
 * Mutable animation state for SvgIcon with control methods.
 * Allows external control of animation playback similar to Lottie's API.
 *
 * Example usage:
 * ```kotlin
 * val animationState = rememberSvgIconAnimationState()
 *
 * Box(
 *     modifier = Modifier.pointerInput(Unit) {
 *         detectTapGestures(
 *             onPress = {
 *                 animationState.animateTo(1f)  // Play forward
 *                 tryAwaitRelease()
 *                 animationState.animateTo(0f)  // Play reverse
 *             }
 *         )
 *     }
 * ) {
 *     AnimatedSvgIcon(
 *         icon = Icons.Check,
 *         animationState = animationState,
 *         contentDescription = "Check"
 *     )
 * }
 * ```
 */
@Stable
interface SvgIconAnimatable : SvgIconAnimationState {
    /**
     * Instantly jump to the specified progress value.
     * @param progress Target progress (0.0 to 1.0)
     */
    suspend fun snapTo(progress: Float)

    /**
     * Animate to the specified progress value.
     * @param progress Target progress (0.0 to 1.0)
     * @param durationMillis Animation duration in milliseconds. If null, uses a default duration.
     */
    suspend fun animateTo(progress: Float, durationMillis: Int? = null)

    /**
     * Stop the current animation.
     */
    suspend fun stop()
}

/**
 * Internal implementation of [SvgIconAnimatable].
 * Uses MutatorMutex to ensure mutual exclusion between animation operations,
 * similar to Lottie Compose's approach.
 */
@Stable
private class SvgIconAnimatableImpl : SvgIconAnimatable {
    private val animatable = Animatable(0f)
    private val mutatorMutex = MutatorMutex()

    override val progress: Float get() = animatable.value
    override val isPlaying: Boolean get() = animatable.isRunning

    override suspend fun snapTo(progress: Float) {
        mutatorMutex.mutate {
            animatable.snapTo(progress.coerceIn(0f, 1f))
        }
    }

    override suspend fun animateTo(progress: Float, durationMillis: Int?) {
        mutatorMutex.mutate {
            val targetProgress = progress.coerceIn(0f, 1f)
            val duration = durationMillis ?: calculateDefaultDuration(animatable.value, targetProgress)
            animatable.animateTo(
                targetValue = targetProgress,
                animationSpec = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                )
            )
        }
    }

    override suspend fun stop() {
        mutatorMutex.mutate {
            animatable.stop()
        }
    }

    private fun calculateDefaultDuration(from: Float, to: Float): Int {
        // Default: 300ms for full animation, proportional for partial
        val distance = kotlin.math.abs(to - from)
        return (300 * distance).toInt().coerceAtLeast(50)
    }
}

/**
 * Creates and remembers a [SvgIconAnimatable] for controlling SVG icon animations.
 *
 * Example:
 * ```kotlin
 * val animationState = rememberSvgIconAnimationState()
 *
 * // Control animation externally
 * LaunchedEffect(isHovered) {
 *     if (isHovered) {
 *         animationState.animateTo(1f)
 *     } else {
 *         animationState.animateTo(0f)
 *     }
 * }
 *
 * AnimatedSvgIcon(
 *     icon = Icons.Check,
 *     animationState = animationState,
 *     contentDescription = "Check"
 * )
 * ```
 */
@Composable
fun rememberSvgIconAnimationState(): SvgIconAnimatable {
    return remember { SvgIconAnimatableImpl() }
}

/**
 * Composable that renders an SVG icon with Material 3-style API.
 *
 * Example:
 * ```kotlin
 * SvgIcon(svg = Icons.Check, contentDescription = "Check")
 * ```
 *
 * @param svg The Svg object to render
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 *   This should always be provided unless this icon is used for decorative purposes, and does not
 *   represent a meaningful action that a user can take.
 * @param modifier Modifier to be applied to the icon. Use Modifier.size() to control the icon size.
 * @param tint Color to tint the icon. Defaults to LocalContentColor.
 * @param strokeWidth Override the stroke width. If null, uses the SVG's default strokeWidth.
 */
@Composable
fun SvgIcon(
    svg: Svg,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    strokeWidth: Float? = null
) {
    SvgIconLayout(
        svg = svg,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
        strokeWidth = strokeWidth
    )
}

/**
 * Internal layout composable that handles sizing based on viewBox.
 * Uses Modifier.size() if specified, otherwise falls back to viewBox dimensions.
 */
@Composable
private fun SvgIconLayout(
    svg: Svg,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    strokeWidth: Float?
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    Layout(
        modifier = modifier.then(semanticsModifier),
        content = {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawSvg(svg, tint, strokeWidth)
            }
        }
    ) { measurables, constraints ->
        // Use effectiveWidth/Height which considers both width/height attrs and viewBox
        val defaultWidth = (svg.effectiveWidth * density).toInt()
        val defaultHeight = (svg.effectiveHeight * density).toInt()

        val width = when {
            constraints.hasFixedWidth -> constraints.maxWidth
            constraints.hasBoundedWidth -> constraints.maxWidth
            else -> defaultWidth
        }
        val height = when {
            constraints.hasFixedHeight -> constraints.maxHeight
            constraints.hasBoundedHeight -> constraints.maxHeight
            else -> defaultHeight
        }

        val placeable = measurables.first().measure(
            Constraints.fixed(width, height)
        )

        layout(width, height) {
            placeable.place(0, 0)
        }
    }
}

/**
 * Composable that renders an animated SVG icon with SMIL animations.
 *
 * Example:
 * ```kotlin
 * AnimatedSvgIcon(svg = Icons.Check, contentDescription = "Check")
 * ```
 *
 * @param svg The Svg object to render (must contain SvgAnimated elements for animation)
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 * @param modifier Modifier to be applied to the icon. Use Modifier.size() to control the icon size.
 * @param tint Color to tint the icon. Defaults to LocalContentColor.
 * @param strokeWidth Override the stroke width. If null, uses the SVG's default strokeWidth.
 * @param animate Whether to animate. When false, renders the final state statically.
 * @param iterations Number of animation iterations. Use [Int.MAX_VALUE] for infinite.
 * @param onAnimationEnd Callback invoked when animation completes (not called for infinite iterations).
 */
@Composable
fun AnimatedSvgIcon(
    svg: Svg,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    strokeWidth: Float? = null,
    animate: Boolean = true,
    iterations: Int = Int.MAX_VALUE,
    onAnimationEnd: (() -> Unit)? = null
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    val hasAnimations = remember(svg) { hasAnimatedElements(svg.children) }

    Layout(
        modifier = modifier.then(semanticsModifier),
        content = {
            if (hasAnimations && animate) {
                AnimatedSvgIconCanvas(
                    svg = svg,
                    tint = tint,
                    strokeWidthOverride = strokeWidth,
                    iterations = iterations,
                    onAnimationEnd = onAnimationEnd,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawSvg(svg, tint, strokeWidth)
                }
            }
        }
    ) { measurables, constraints ->
        val defaultWidth = (svg.effectiveWidth * density).toInt()
        val defaultHeight = (svg.effectiveHeight * density).toInt()

        val width = when {
            constraints.hasFixedWidth -> constraints.maxWidth
            constraints.hasBoundedWidth -> constraints.maxWidth
            else -> defaultWidth
        }
        val height = when {
            constraints.hasFixedHeight -> constraints.maxHeight
            constraints.hasBoundedHeight -> constraints.maxHeight
            else -> defaultHeight
        }

        val placeable = measurables.first().measure(
            Constraints.fixed(width, height)
        )

        layout(width, height) {
            placeable.place(0, 0)
        }
    }
}

/**
 * Composable that renders an animated SVG icon with external animation state control.
 * This overload allows you to control the animation progress from outside the composable,
 * enabling interactive animations like onPress, onHover, and scroll-linked effects.
 *
 * Example:
 * ```kotlin
 * val animationState = rememberSvgIconAnimationState()
 *
 * Box(
 *     modifier = Modifier.pointerInput(Unit) {
 *         detectTapGestures(
 *             onPress = {
 *                 animationState.animateTo(1f)  // Play forward while pressed
 *                 tryAwaitRelease()
 *                 animationState.animateTo(0f)  // Play reverse on release
 *             }
 *         )
 *     }
 * ) {
 *     AnimatedSvgIcon(
 *         svg = Icons.Check,
 *         animationState = animationState,
 *         contentDescription = "Check"
 *     )
 * }
 * ```
 *
 * @param svg The Svg object to render (must contain SvgAnimated elements for animation)
 * @param animationState External animation state for controlling the animation
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 * @param modifier Modifier to be applied to the icon. Use Modifier.size() to control the icon size.
 * @param tint Color to tint the icon. Defaults to LocalContentColor.
 * @param strokeWidth Override the stroke width. If null, uses the SVG's default strokeWidth.
 */
@Composable
fun AnimatedSvgIcon(
    svg: Svg,
    animationState: SvgIconAnimatable,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    strokeWidth: Float? = null
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    // Collect animations to build progress map
    val animations = remember(svg) { collectAllAnimations(svg.children) }

    // Build path cache once to avoid per-frame Path and PathMeasure creation
    val pathCache = remember(svg) { buildPathCache(svg.children) }

    // Build progress map based on external state using derivedStateOf for proper Compose state tracking
    val progressMap = animations.associate { entry ->
        val anim = entry.animation
        val totalDuration = (anim.delay.inWholeMilliseconds + anim.dur.inWholeMilliseconds).coerceAtLeast(1L)
        val delayRatio = anim.delay.inWholeMilliseconds.toFloat() / totalDuration
        val durationRatio = anim.dur.inWholeMilliseconds.toFloat() / totalDuration

        entry.key to derivedStateOf {
            val masterProgress = animationState.progress
            when {
                masterProgress < delayRatio -> 0f
                masterProgress >= delayRatio + durationRatio -> 1f
                else -> {
                    val localProgress = ((masterProgress - delayRatio) / durationRatio).coerceIn(0f, 1f)
                    applyEasing(localProgress, anim.calcMode, anim.keySplines)
                }
            }
        }
    }

    Layout(
        modifier = modifier.then(semanticsModifier),
        content = {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawAnimatedSvg(svg, tint, strokeWidth, progressMap, pathCache)
            }
        }
    ) { measurables, constraints ->
        val defaultWidth = (svg.effectiveWidth * density).toInt()
        val defaultHeight = (svg.effectiveHeight * density).toInt()

        val width = when {
            constraints.hasFixedWidth -> constraints.maxWidth
            constraints.hasBoundedWidth -> constraints.maxWidth
            else -> defaultWidth
        }
        val height = when {
            constraints.hasFixedHeight -> constraints.maxHeight
            constraints.hasBoundedHeight -> constraints.maxHeight
            else -> defaultHeight
        }

        val placeable = measurables.first().measure(
            Constraints.fixed(width, height)
        )

        layout(width, height) {
            placeable.place(0, 0)
        }
    }
}

/**
 * Applies easing based on calcMode and keySplines.
 */
private fun applyEasing(progress: Float, calcMode: CalcMode, keySplines: KeySplines?): Float {
    return when (calcMode) {
        CalcMode.LINEAR -> progress
        CalcMode.DISCRETE -> if (progress >= 1f) 1f else 0f
        CalcMode.PACED -> progress // Same as linear for single segment
        CalcMode.SPLINE -> {
            if (keySplines != null) {
                cubicBezierEasing(progress, keySplines.x1, keySplines.y1, keySplines.x2, keySplines.y2)
            } else {
                progress
            }
        }
    }
}

/**
 * Cubic bezier easing implementation.
 * Matches CSS cubic-bezier() behavior.
 */
private fun cubicBezierEasing(t: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
    // Newton-Raphson iteration to find t for given x
    var guess = t
    for (i in 0 until 8) {
        val x = bezierX(guess, x1, x2) - t
        if (kotlin.math.abs(x) < 0.001f) break
        val dx = bezierDx(guess, x1, x2)
        if (kotlin.math.abs(dx) < 0.0001f) break
        guess -= x / dx
    }
    return bezierY(guess, y1, y2).coerceIn(0f, 1f)
}

private fun bezierX(t: Float, x1: Float, x2: Float): Float {
    val t2 = t * t
    val t3 = t2 * t
    val mt = 1 - t
    val mt2 = mt * mt
    return 3 * mt2 * t * x1 + 3 * mt * t2 * x2 + t3
}

private fun bezierY(t: Float, y1: Float, y2: Float): Float {
    val t2 = t * t
    val t3 = t2 * t
    val mt = 1 - t
    val mt2 = mt * mt
    return 3 * mt2 * t * y1 + 3 * mt * t2 * y2 + t3
}

private fun bezierDx(t: Float, x1: Float, x2: Float): Float {
    val t2 = t * t
    val mt = 1 - t
    return 3 * mt * mt * x1 + 6 * mt * t * (x2 - x1) + 3 * t2 * (1 - x2)
}

/**
 * Checks if any element in the tree has animations.
 */
private fun hasAnimatedElements(elements: List<SvgElement>): Boolean {
    fun check(element: SvgElement): Boolean = when (element) {
        is SvgAnimated -> true
        is SvgGroup -> element.children.any { check(it) }
        is SvgStyled -> check(element.element)
        else -> false
    }
    return elements.any { check(it) }
}

/**
 * Key for efficient animation progress lookup.
 */
private data class AnimationKey(
    val element: SvgElement,
    val animation: SvgAnimate
)

/**
 * Collects all animations from the element tree.
 */
private data class AnimationEntry(
    val element: SvgElement,
    val animation: SvgAnimate,
    val index: Int
) {
    val key: AnimationKey get() = AnimationKey(element, animation)
}

private fun collectAllAnimations(elements: List<SvgElement>): List<AnimationEntry> {
    val result = mutableListOf<AnimationEntry>()
    var index = 0

    fun collect(element: SvgElement) {
        when (element) {
            is SvgAnimated -> {
                element.animations.forEach { anim ->
                    result.add(AnimationEntry(element.element, anim, index++))
                }
            }
            is SvgGroup -> element.children.forEach { collect(it) }
            is SvgStyled -> collect(element.element)
            else -> {}
        }
    }

    elements.forEach { collect(it) }
    return result
}

/**
 * Cached path information for stroke-draw animations.
 * Pre-computes Path and pathLength to avoid per-frame allocations.
 */
private data class CachedPathInfo(
    val path: Path,
    val pathLength: Float
)

/**
 * Builds a cache of Path objects and their lengths for animated elements.
 * This avoids creating new Path and PathMeasure objects on every frame.
 */
private fun buildPathCache(elements: List<SvgElement>): Map<SvgElement, CachedPathInfo> {
    val cache = mutableMapOf<SvgElement, CachedPathInfo>()

    fun cacheElement(element: SvgElement) {
        when (element) {
            is SvgAnimated -> {
                // Check if this element has stroke-draw animation
                val hasStrokeDraw = element.animations.any { it is SvgAnimate.StrokeDraw }
                if (hasStrokeDraw) {
                    val path = elementToPath(element.element)
                    if (path != null) {
                        val pathMeasure = androidx.compose.ui.graphics.PathMeasure()
                        pathMeasure.setPath(path, false)
                        cache[element.element] = CachedPathInfo(path, pathMeasure.length)
                    }
                }
            }
            is SvgGroup -> element.children.forEach { cacheElement(it) }
            is SvgStyled -> cacheElement(element.element)
            else -> {}
        }
    }

    elements.forEach { cacheElement(it) }
    return cache
}

/**
 * Converts an SvgElement to a Path, or null if not applicable.
 */
private fun elementToPath(element: SvgElement): Path? = when (element) {
    is SvgPath -> element.toPath()
    is SvgCircle -> Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                element.cx - element.r, element.cy - element.r,
                element.cx + element.r, element.cy + element.r
            )
        )
    }
    is SvgRect -> Path().apply {
        if (element.rx > 0f || element.ry > 0f) {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    element.x, element.y,
                    element.x + element.width, element.y + element.height,
                    CornerRadius(element.rx, element.ry)
                )
            )
        } else {
            addRect(
                androidx.compose.ui.geometry.Rect(
                    element.x, element.y,
                    element.x + element.width, element.y + element.height
                )
            )
        }
    }
    is SvgEllipse -> Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                element.cx - element.rx, element.cy - element.ry,
                element.cx + element.rx, element.cy + element.ry
            )
        )
    }
    is SvgLine -> Path().apply {
        moveTo(element.x1, element.y1)
        lineTo(element.x2, element.y2)
    }
    is SvgPolyline -> if (element.points.isNotEmpty()) {
        Path().apply {
            val first = element.points.first()
            moveTo(first.x, first.y)
            for (i in 1 until element.points.size) {
                val point = element.points[i]
                lineTo(point.x, point.y)
            }
        }
    } else null
    is SvgPolygon -> if (element.points.isNotEmpty()) {
        Path().apply {
            val first = element.points.first()
            moveTo(first.x, first.y)
            for (i in 1 until element.points.size) {
                val point = element.points[i]
                lineTo(point.x, point.y)
            }
            close()
        }
    } else null
    else -> null
}

/**
 * Internal composable for rendering animated SVG icons with SMIL animations.
 */
@Composable
private fun AnimatedSvgIconCanvas(
    svg: Svg,
    tint: Color,
    strokeWidthOverride: Float?,
    iterations: Int,
    onAnimationEnd: (() -> Unit)?,
    modifier: Modifier
) {
    // Collect all animations
    val animations = remember(svg) { collectAllAnimations(svg.children) }

    // Build path cache once to avoid per-frame Path and PathMeasure creation
    val pathCache = remember(svg) { buildPathCache(svg.children) }

    // Find the maximum duration among all animations
    val maxDuration = remember(animations) {
        animations.maxOfOrNull { it.animation.dur.inWholeMilliseconds.toInt() } ?: 1000
    }

    val isInfinite = iterations == Int.MAX_VALUE

    // Calculate total cycle duration (max of delay + duration * iterations across all animations)
    // For infinite per-animation iterations, we use 1 iteration for the cycle calculation
    val totalCycleDuration = remember(animations) {
        animations.maxOfOrNull { entry ->
            val anim = entry.animation
            val effectiveIterations = if (anim.isInfinite) 1 else anim.iterations.coerceAtLeast(1)
            anim.delay.inWholeMilliseconds + (anim.dur.inWholeMilliseconds * effectiveIterations)
        }?.toInt()?.coerceAtLeast(1) ?: 1000
    }

    if (isInfinite) {
        // Use infinite transition for continuous animations
        val infiniteTransition = rememberInfiniteTransition(label = "svg_animation")

        // Single master timeline for the entire cycle
        val masterProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(totalCycleDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "master_timeline"
        )

        // Calculate individual animation progress based on master timeline with easing
        val progressMap = remember(animations) {
            animations.associate { entry ->
                entry.key to object : State<Float> {
                    override val value: Float
                        get() {
                            val anim = entry.animation
                            val delayMs = anim.delay.inWholeMilliseconds.toFloat()
                            val durationMs = anim.dur.inWholeMilliseconds.toFloat()
                            val currentTimeMs = masterProgress * totalCycleDuration
                            val effectiveIterations = if (anim.isInfinite) 1 else anim.iterations.coerceAtLeast(1)
                            val totalAnimationMs = durationMs * effectiveIterations

                            val rawProgress = when {
                                currentTimeMs < delayMs -> 0f  // Still in delay period
                                currentTimeMs >= delayMs + totalAnimationMs -> 1f  // All iterations complete
                                else -> {
                                    // During animation - calculate progress within current iteration
                                    val timeInAnimation = currentTimeMs - delayMs
                                    if (anim.isInfinite) {
                                        // Infinite: cycle forever
                                        (timeInAnimation / durationMs) % 1f
                                    } else {
                                        // Finite: cycle within iterations, end at 1.0
                                        val iterationProgress = (timeInAnimation / durationMs) % 1f
                                        val currentIteration = (timeInAnimation / durationMs).toInt()
                                        if (currentIteration >= effectiveIterations - 1 && iterationProgress >= 1f - 0.001f) {
                                            1f
                                        } else {
                                            iterationProgress
                                        }
                                    }
                                }
                            }
                            // Apply easing based on calcMode and keySplines
                            return applyEasing(rawProgress, anim.calcMode, anim.keySplines)
                        }
                }
            }
        }

        Canvas(modifier = modifier) {
            drawAnimatedSvg(svg, tint, strokeWidthOverride, progressMap, pathCache)
        }
    } else {
        // Use finite animation with iteration count using master timeline approach
        // This ensures proper delay handling and reliable onAnimationEnd callback
        var currentIteration by remember { mutableStateOf(0) }
        val masterProgress = remember { Animatable(0f) }

        // Run the animation for the specified number of iterations
        LaunchedEffect(iterations) {
            for (i in 0 until iterations) {
                currentIteration = i
                masterProgress.snapTo(0f)
                masterProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(totalCycleDuration, easing = LinearEasing)
                )
            }
            // Animation completed - call callback reliably after actual animation finishes
            onAnimationEnd?.invoke()
        }

        // Calculate individual animation progress based on master timeline with proper delay handling
        val progressMap = remember(animations) {
            animations.associate { entry ->
                entry.key to object : State<Float> {
                    override val value: Float
                        get() {
                            val anim = entry.animation
                            val delayMs = anim.delay.inWholeMilliseconds.toFloat()
                            val durationMs = anim.dur.inWholeMilliseconds.toFloat()
                            val currentTimeMs = masterProgress.value * totalCycleDuration
                            val effectiveIterations = if (anim.isInfinite) 1 else anim.iterations.coerceAtLeast(1)
                            val totalAnimationMs = durationMs * effectiveIterations

                            val rawProgress = when {
                                currentTimeMs < delayMs -> 0f  // Still in delay period
                                currentTimeMs >= delayMs + totalAnimationMs -> 1f  // All iterations complete
                                else -> {
                                    // During animation - calculate progress within current iteration
                                    val timeInAnimation = currentTimeMs - delayMs
                                    if (anim.isInfinite) {
                                        // Infinite: cycle forever
                                        (timeInAnimation / durationMs) % 1f
                                    } else {
                                        // Finite: cycle within iterations, end at 1.0
                                        val iterationProgress = (timeInAnimation / durationMs) % 1f
                                        val currentIteration = (timeInAnimation / durationMs).toInt()
                                        if (currentIteration >= effectiveIterations - 1 && iterationProgress >= 1f - 0.001f) {
                                            1f
                                        } else {
                                            iterationProgress
                                        }
                                    }
                                }
                            }
                            // Apply easing based on calcMode and keySplines
                            return applyEasing(rawProgress, anim.calcMode, anim.keySplines)
                        }
                }
            }
        }

        Canvas(modifier = modifier) {
            drawAnimatedSvg(svg, tint, strokeWidthOverride, progressMap, pathCache)
        }
    }
}

// --- Private implementation details ---

/**
 * Drawing context containing current style state.
 */
private data class DrawContext(
    val strokeColor: Color,
    val fillColor: Color?,
    val stroke: Stroke,
    val opacity: Float = 1f,
    val fillRule: PathFillType = PathFillType.NonZero,
    val hasStroke: Boolean = true,
    val paintOrder: PaintOrder = PaintOrder.FILL_STROKE,
    val vectorEffect: VectorEffect = VectorEffect.NONE,
    val scaleFactor: Float = 1f,
    val clipPathId: String? = null,
    val maskId: String? = null
)

/**
 * Registry for storing clip paths and masks by ID.
 */
private data class DefsRegistry(
    val clipPaths: Map<String, SvgClipPath> = emptyMap(),
    val masks: Map<String, SvgMask> = emptyMap()
)

/**
 * Draws an SVG onto the canvas.
 * Handles viewBox, viewport, and preserveAspectRatio correctly.
 */
private fun DrawScope.drawSvg(svg: Svg, tint: Color, strokeWidthOverride: Float?) {
    val viewBox = svg.effectiveViewBox
    val strokeWidth = strokeWidthOverride ?: svg.strokeWidth

    // Calculate scale factors based on preserveAspectRatio
    val (scaleX, scaleY, translateX, translateY) = calculateViewBoxTransform(
        viewportWidth = size.width,
        viewportHeight = size.height,
        viewBoxMinX = viewBox.minX,
        viewBoxMinY = viewBox.minY,
        viewBoxWidth = viewBox.width,
        viewBoxHeight = viewBox.height,
        preserveAspectRatio = svg.preserveAspectRatio
    )

    val defaultStroke = Stroke(
        width = strokeWidth,
        cap = svg.strokeLinecap.toCompose(),
        join = svg.strokeLinejoin.toCompose()
    )

    // Color handling:
    // - null = none (no color)
    // - Color.Unspecified = currentColor (use tint)
    // - other Color = use that color
    val fillColor = svg.fill?.let { if (it == Color.Unspecified) tint else it }
    val strokeColor = svg.stroke?.let { if (it == Color.Unspecified) tint else it }

    // Collect defs (clipPaths, masks) from children
    val registry = collectDefs(svg.children)

    val context = DrawContext(
        strokeColor = strokeColor ?: tint,
        fillColor = fillColor,
        stroke = defaultStroke,
        hasStroke = strokeColor != null,
        scaleFactor = scaleX // Use scaleX for stroke scaling (assuming uniform scale for most cases)
    )

    // Apply viewBox transformation: translate then scale
    translate(translateX, translateY) {
        scale(scaleX, scaleY, pivot = Offset.Zero) {
            // Translate to handle viewBox minX/minY
            translate(-viewBox.minX, -viewBox.minY) {
                svg.children.forEach { element ->
                    drawSvgElement(element, context, registry)
                }
            }
        }
    }
}

/**
 * Result of viewBox transformation calculation.
 */
private data class ViewBoxTransform(
    val scaleX: Float,
    val scaleY: Float,
    val translateX: Float,
    val translateY: Float
)

/**
 * Calculates the transformation needed to map viewBox to viewport
 * according to preserveAspectRatio.
 */
private fun calculateViewBoxTransform(
    viewportWidth: Float,
    viewportHeight: Float,
    viewBoxMinX: Float,
    viewBoxMinY: Float,
    viewBoxWidth: Float,
    viewBoxHeight: Float,
    preserveAspectRatio: PreserveAspectRatio
): ViewBoxTransform {
    if (viewBoxWidth <= 0 || viewBoxHeight <= 0) {
        return ViewBoxTransform(1f, 1f, 0f, 0f)
    }

    val scaleX = viewportWidth / viewBoxWidth
    val scaleY = viewportHeight / viewBoxHeight

    // If align is NONE, use non-uniform scaling
    if (preserveAspectRatio.align == AspectRatioAlign.NONE) {
        return ViewBoxTransform(scaleX, scaleY, 0f, 0f)
    }

    // Uniform scaling based on meet/slice
    val scale = when (preserveAspectRatio.meetOrSlice) {
        MeetOrSlice.MEET -> minOf(scaleX, scaleY)  // Fit entirely
        MeetOrSlice.SLICE -> maxOf(scaleX, scaleY)  // Cover entirely
    }

    // Calculate the scaled viewBox size
    val scaledViewBoxWidth = viewBoxWidth * scale
    val scaledViewBoxHeight = viewBoxHeight * scale

    // Calculate alignment offset
    val (alignX, alignY) = when (preserveAspectRatio.align) {
        AspectRatioAlign.NONE -> 0f to 0f  // Already handled above
        AspectRatioAlign.X_MIN_Y_MIN -> 0f to 0f
        AspectRatioAlign.X_MID_Y_MIN -> (viewportWidth - scaledViewBoxWidth) / 2 to 0f
        AspectRatioAlign.X_MAX_Y_MIN -> (viewportWidth - scaledViewBoxWidth) to 0f
        AspectRatioAlign.X_MIN_Y_MID -> 0f to (viewportHeight - scaledViewBoxHeight) / 2
        AspectRatioAlign.X_MID_Y_MID -> (viewportWidth - scaledViewBoxWidth) / 2 to (viewportHeight - scaledViewBoxHeight) / 2
        AspectRatioAlign.X_MAX_Y_MID -> (viewportWidth - scaledViewBoxWidth) to (viewportHeight - scaledViewBoxHeight) / 2
        AspectRatioAlign.X_MIN_Y_MAX -> 0f to (viewportHeight - scaledViewBoxHeight)
        AspectRatioAlign.X_MID_Y_MAX -> (viewportWidth - scaledViewBoxWidth) / 2 to (viewportHeight - scaledViewBoxHeight)
        AspectRatioAlign.X_MAX_Y_MAX -> (viewportWidth - scaledViewBoxWidth) to (viewportHeight - scaledViewBoxHeight)
    }

    return ViewBoxTransform(scale, scale, alignX, alignY)
}

/**
 * Collects clip paths and masks from defs elements.
 */
private fun collectDefs(elements: List<SvgElement>): DefsRegistry {
    val clipPaths = mutableMapOf<String, SvgClipPath>()
    val masks = mutableMapOf<String, SvgMask>()

    fun collect(element: SvgElement) {
        when (element) {
            is SvgDefs -> element.children.forEach { collect(it) }
            is SvgClipPath -> clipPaths[element.id] = element
            is SvgMask -> masks[element.id] = element
            is SvgGroup -> element.children.forEach { collect(it) }
            else -> {}
        }
    }

    elements.forEach { collect(it) }
    return DefsRegistry(clipPaths, masks)
}

private fun DrawScope.drawSvgElement(element: SvgElement, ctx: DrawContext, registry: DefsRegistry = DefsRegistry()) {
    when (element) {
        is SvgPath -> drawSvgPath(element, ctx)
        is SvgCircle -> drawSvgCircle(element, ctx)
        is SvgEllipse -> drawSvgEllipse(element, ctx)
        is SvgRect -> drawSvgRect(element, ctx)
        is SvgLine -> drawSvgLine(element, ctx)
        is SvgPolyline -> drawSvgPolyline(element, ctx)
        is SvgPolygon -> drawSvgPolygon(element, ctx)
        is SvgGroup -> element.children.forEach { drawSvgElement(it, ctx, registry) }
        is SvgAnimated -> drawSvgElement(element.element, ctx, registry)
        is SvgStyled -> drawStyledElement(element, ctx, registry)
        is SvgDefs -> {} // Defs are processed separately, not drawn
        is SvgClipPath -> {} // ClipPaths are applied via style, not drawn directly
        is SvgMask -> {} // Masks are applied via style, not drawn directly
    }
}

private fun DrawScope.drawStyledElement(styled: SvgStyled, parentCtx: DrawContext, registry: DefsRegistry) {
    val style = styled.style
    val ctx = applyStyle(parentCtx, style)

    // Apply clip path if present
    val clipPathId = ctx.clipPathId
    val clipPath = clipPathId?.let { registry.clipPaths[it] }

    val drawContent: DrawScope.() -> Unit = {
        // Apply transform if present
        val transform = style.transform
        if (transform != null) {
            withTransform(transform) {
                drawSvgElement(styled.element, ctx, registry)
            }
        } else {
            drawSvgElement(styled.element, ctx, registry)
        }
    }

    if (clipPath != null) {
        val path = buildClipPath(clipPath, ctx)
        clipPath(path, ClipOp.Intersect) {
            drawContent()
        }
    } else {
        drawContent()
    }
}

/**
 * Builds a Path from a SvgClipPath element.
 */
private fun buildClipPath(clipPath: SvgClipPath, ctx: DrawContext): Path {
    val path = Path()
    clipPath.children.forEach { element ->
        when (element) {
            is SvgPath -> path.addPath(element.toPath())
            is SvgRect -> {
                if (element.rx > 0f || element.ry > 0f) {
                    path.addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            element.x, element.y,
                            element.x + element.width, element.y + element.height,
                            CornerRadius(element.rx, element.ry)
                        )
                    )
                } else {
                    path.addRect(
                        androidx.compose.ui.geometry.Rect(
                            element.x, element.y,
                            element.x + element.width, element.y + element.height
                        )
                    )
                }
            }
            is SvgCircle -> {
                path.addOval(
                    androidx.compose.ui.geometry.Rect(
                        element.cx - element.r, element.cy - element.r,
                        element.cx + element.r, element.cy + element.r
                    )
                )
            }
            is SvgEllipse -> {
                path.addOval(
                    androidx.compose.ui.geometry.Rect(
                        element.cx - element.rx, element.cy - element.ry,
                        element.cx + element.rx, element.cy + element.ry
                    )
                )
            }
            is SvgPolygon -> {
                if (element.points.isNotEmpty()) {
                    val first = element.points.first()
                    path.moveTo(first.x, first.y)
                    for (i in 1 until element.points.size) {
                        val point = element.points[i]
                        path.lineTo(point.x, point.y)
                    }
                    path.close()
                }
            }
            else -> {} // Unsupported clip path element
        }
    }
    return path
}

private fun applyStyle(parent: DrawContext, style: SvgStyle): DrawContext {
    // Color handling:
    // - null = inherit from parent
    // - Color.Unspecified = currentColor (use parent's strokeColor as tint)
    // - Color.Transparent = none (no color)
    // - other Color = use that color

    // Determine stroke color
    val strokeColor = when (val c = style.stroke) {
        null -> parent.strokeColor
        Color.Unspecified -> parent.strokeColor  // currentColor
        else -> c
    }
    val hasStroke = style.stroke != Color.Transparent && (style.stroke != null || parent.hasStroke)

    // Determine fill color
    val fillColor = when (val c = style.fill) {
        null -> parent.fillColor
        Color.Unspecified -> parent.strokeColor  // currentColor
        Color.Transparent -> null
        else -> c
    }

    // Build stroke
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

    // Opacity
    val opacity = (style.opacity ?: 1f) * parent.opacity
    val strokeOpacity = style.strokeOpacity ?: 1f
    val fillOpacity = style.fillOpacity ?: 1f

    // Fill rule
    val fillRule = style.fillRule?.toCompose() ?: parent.fillRule

    // Paint order
    val paintOrder = style.paintOrder ?: parent.paintOrder

    // Vector effect
    val vectorEffect = style.vectorEffect ?: parent.vectorEffect

    // Clip path and mask references
    val clipPathId = style.clipPathId ?: parent.clipPathId
    val maskId = style.maskId ?: parent.maskId

    return DrawContext(
        strokeColor = strokeColor.copy(alpha = strokeColor.alpha * strokeOpacity * opacity),
        fillColor = fillColor?.copy(alpha = fillColor.alpha * fillOpacity * opacity),
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

private fun buildPathEffect(style: SvgStyle, parent: PathEffect?): PathEffect? {
    val dashArray = style.strokeDasharray
    if (dashArray != null && dashArray.isNotEmpty()) {
        val intervals = if (dashArray.size % 2 == 0) {
            dashArray.toFloatArray()
        } else {
            // SVG spec: odd number of values gets doubled
            // Build directly into FloatArray to avoid intermediate list allocation
            FloatArray(dashArray.size * 2) { i -> dashArray[i % dashArray.size] }
        }
        val phase = style.strokeDashoffset ?: 0f
        return PathEffect.dashPathEffect(intervals, phase)
    }
    return parent
}

private fun parseColor(colorStr: String, default: Color): Color {
    return when {
        colorStr == "currentColor" -> default
        colorStr.startsWith("#") -> parseHexColor(colorStr)
        colorStr.startsWith("rgb") -> parseRgbColor(colorStr)
        else -> namedColors[colorStr.lowercase()] ?: default
    }
}

private fun parseHexColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    return when (clean.length) {
        3 -> {
            val r = clean[0].toString().repeat(2).toInt(16)
            val g = clean[1].toString().repeat(2).toInt(16)
            val b = clean[2].toString().repeat(2).toInt(16)
            Color(r, g, b)
        }
        6 -> {
            val r = clean.substring(0, 2).toInt(16)
            val g = clean.substring(2, 4).toInt(16)
            val b = clean.substring(4, 6).toInt(16)
            Color(r, g, b)
        }
        8 -> {
            val r = clean.substring(0, 2).toInt(16)
            val g = clean.substring(2, 4).toInt(16)
            val b = clean.substring(4, 6).toInt(16)
            val a = clean.substring(6, 8).toInt(16)
            Color(r, g, b, a)
        }
        else -> Color.Black
    }
}

// Pre-compiled regex pattern for rgb/rgba color parsing
private val rgbColorPattern = Regex("""rgba?\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*([\d.]+)\s*)?\)""")

private fun parseRgbColor(rgb: String): Color {
    val match = rgbColorPattern.find(rgb)
    return if (match != null) {
        val r = match.groupValues[1].toInt()
        val g = match.groupValues[2].toInt()
        val b = match.groupValues[3].toInt()
        val a = match.groupValues[4].toFloatOrNull() ?: 1f
        Color(r, g, b, (a * 255).toInt())
    } else {
        Color.Black
    }
}

private val namedColors = mapOf(
    "black" to Color.Black,
    "white" to Color.White,
    "red" to Color.Red,
    "green" to Color.Green,
    "blue" to Color.Blue,
    "yellow" to Color.Yellow,
    "cyan" to Color.Cyan,
    "magenta" to Color.Magenta,
    "gray" to Color.Gray,
    "grey" to Color.Gray,
    "transparent" to Color.Transparent
)

private fun LineCap.toCompose(): StrokeCap = when (this) {
    LineCap.BUTT -> StrokeCap.Butt
    LineCap.ROUND -> StrokeCap.Round
    LineCap.SQUARE -> StrokeCap.Square
}

private fun LineJoin.toCompose(): StrokeJoin = when (this) {
    LineJoin.MITER -> StrokeJoin.Miter
    LineJoin.ROUND -> StrokeJoin.Round
    LineJoin.BEVEL -> StrokeJoin.Bevel
}

private fun FillRule.toCompose(): PathFillType = when (this) {
    FillRule.NONZERO -> PathFillType.NonZero
    FillRule.EVENODD -> PathFillType.EvenOdd
}

private fun DrawScope.withTransform(transform: SvgTransform, block: DrawScope.() -> Unit) {
    when (transform) {
        is SvgTransform.Translate -> translate(transform.x, transform.y, block)
        is SvgTransform.Scale -> scale(transform.sx, transform.sy, Offset.Zero, block)
        is SvgTransform.Rotate -> rotate(transform.angle, Offset(transform.cx, transform.cy), block)
        is SvgTransform.SkewX -> {
            val matrix = Matrix().apply {
                // skewX(angle) = matrix(1, 0, tan(angle), 1, 0, 0)
                val tanAngle = tan(transform.angle.toDouble() * PI / 180.0).toFloat()
                this[0, 1] = tanAngle // skewX affects the x-coordinate based on y
            }
            withTransform({ this.transform(matrix) }, block)
        }
        is SvgTransform.SkewY -> {
            val matrix = Matrix().apply {
                // skewY(angle) = matrix(1, tan(angle), 0, 1, 0, 0)
                val tanAngle = tan(transform.angle.toDouble() * PI / 180.0).toFloat()
                this[1, 0] = tanAngle // skewY affects the y-coordinate based on x
            }
            withTransform({ this.transform(matrix) }, block)
        }
        is SvgTransform.Matrix -> {
            // SVG matrix(a, b, c, d, e, f) maps to:
            // | a c e |   Compose Matrix (column-major 4x4):
            // | b d f | → [0,0]=a [1,0]=b [0,1]=c [1,1]=d [0,3]=e [1,3]=f
            // | 0 0 1 |
            val matrix = Matrix().apply {
                this[0, 0] = transform.a  // scaleX
                this[1, 0] = transform.b  // skewY
                this[0, 1] = transform.c  // skewX
                this[1, 1] = transform.d  // scaleY
                this[0, 3] = transform.e  // translateX
                this[1, 3] = transform.f  // translateY
            }
            withTransform({ this.transform(matrix) }, block)
        }
        is SvgTransform.Combined -> {
            if (transform.transforms.isEmpty()) {
                block()
            } else {
                // Apply transforms iteratively to avoid creating intermediate lists
                fun applyTransformsFrom(index: Int) {
                    if (index >= transform.transforms.size) {
                        block()
                    } else {
                        withTransform(transform.transforms[index]) {
                            applyTransformsFrom(index + 1)
                        }
                    }
                }
                applyTransformsFrom(0)
            }
        }
    }
}

// Drawing functions

/**
 * Gets the effective stroke for drawing, adjusting for vector-effect.
 * When vectorEffect is NON_SCALING_STROKE, the stroke width is divided by
 * the scale factor to maintain consistent visual stroke width.
 */
private fun DrawContext.getEffectiveStroke(): Stroke {
    return if (vectorEffect == VectorEffect.NON_SCALING_STROKE && scaleFactor != 0f) {
        Stroke(
            width = stroke.width / scaleFactor,
            cap = stroke.cap,
            join = stroke.join,
            miter = stroke.miter,
            pathEffect = stroke.pathEffect
        )
    } else {
        stroke
    }
}

private fun DrawScope.drawSvgPath(path: SvgPath, ctx: DrawContext) {
    val composePath = path.toPath().apply { fillType = ctx.fillRule }
    val effectiveStroke = ctx.getEffectiveStroke()

    when (ctx.paintOrder) {
        PaintOrder.FILL_STROKE -> {
            // Default: fill first, then stroke
            ctx.fillColor?.let { fill ->
                drawPath(path = composePath, color = fill, style = Fill)
            }
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawPath(path = composePath, color = ctx.strokeColor, style = effectiveStroke)
            }
        }
        PaintOrder.STROKE_FILL -> {
            // Stroke first, then fill on top
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawPath(path = composePath, color = ctx.strokeColor, style = effectiveStroke)
            }
            ctx.fillColor?.let { fill ->
                drawPath(path = composePath, color = fill, style = Fill)
            }
        }
    }
}

private fun DrawScope.drawSvgCircle(circle: SvgCircle, ctx: DrawContext) {
    val center = Offset(circle.cx, circle.cy)
    val effectiveStroke = ctx.getEffectiveStroke()

    when (ctx.paintOrder) {
        PaintOrder.FILL_STROKE -> {
            ctx.fillColor?.let { fill ->
                drawCircle(color = fill, radius = circle.r, center = center, style = Fill)
            }
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawCircle(color = ctx.strokeColor, radius = circle.r, center = center, style = effectiveStroke)
            }
        }
        PaintOrder.STROKE_FILL -> {
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawCircle(color = ctx.strokeColor, radius = circle.r, center = center, style = effectiveStroke)
            }
            ctx.fillColor?.let { fill ->
                drawCircle(color = fill, radius = circle.r, center = center, style = Fill)
            }
        }
    }
}

private fun DrawScope.drawSvgEllipse(ellipse: SvgEllipse, ctx: DrawContext) {
    val topLeft = Offset(ellipse.cx - ellipse.rx, ellipse.cy - ellipse.ry)
    val size = Size(ellipse.rx * 2, ellipse.ry * 2)
    val effectiveStroke = ctx.getEffectiveStroke()

    when (ctx.paintOrder) {
        PaintOrder.FILL_STROKE -> {
            ctx.fillColor?.let { fill ->
                drawOval(color = fill, topLeft = topLeft, size = size, style = Fill)
            }
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawOval(color = ctx.strokeColor, topLeft = topLeft, size = size, style = effectiveStroke)
            }
        }
        PaintOrder.STROKE_FILL -> {
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawOval(color = ctx.strokeColor, topLeft = topLeft, size = size, style = effectiveStroke)
            }
            ctx.fillColor?.let { fill ->
                drawOval(color = fill, topLeft = topLeft, size = size, style = Fill)
            }
        }
    }
}

private fun DrawScope.drawSvgRect(rect: SvgRect, ctx: DrawContext) {
    val topLeft = Offset(rect.x, rect.y)
    val size = Size(rect.width, rect.height)
    val hasRadius = rect.rx > 0f || rect.ry > 0f
    val effectiveStroke = ctx.getEffectiveStroke()

    when (ctx.paintOrder) {
        PaintOrder.FILL_STROKE -> {
            if (hasRadius) {
                val cornerRadius = CornerRadius(rect.rx, rect.ry)
                ctx.fillColor?.let { fill ->
                    drawRoundRect(color = fill, topLeft = topLeft, size = size, cornerRadius = cornerRadius, style = Fill)
                }
                if (ctx.hasStroke && effectiveStroke.width > 0) {
                    drawRoundRect(color = ctx.strokeColor, topLeft = topLeft, size = size, cornerRadius = cornerRadius, style = effectiveStroke)
                }
            } else {
                ctx.fillColor?.let { fill ->
                    drawRect(color = fill, topLeft = topLeft, size = size, style = Fill)
                }
                if (ctx.hasStroke && effectiveStroke.width > 0) {
                    drawRect(color = ctx.strokeColor, topLeft = topLeft, size = size, style = effectiveStroke)
                }
            }
        }
        PaintOrder.STROKE_FILL -> {
            if (hasRadius) {
                val cornerRadius = CornerRadius(rect.rx, rect.ry)
                if (ctx.hasStroke && effectiveStroke.width > 0) {
                    drawRoundRect(color = ctx.strokeColor, topLeft = topLeft, size = size, cornerRadius = cornerRadius, style = effectiveStroke)
                }
                ctx.fillColor?.let { fill ->
                    drawRoundRect(color = fill, topLeft = topLeft, size = size, cornerRadius = cornerRadius, style = Fill)
                }
            } else {
                if (ctx.hasStroke && effectiveStroke.width > 0) {
                    drawRect(color = ctx.strokeColor, topLeft = topLeft, size = size, style = effectiveStroke)
                }
                ctx.fillColor?.let { fill ->
                    drawRect(color = fill, topLeft = topLeft, size = size, style = Fill)
                }
            }
        }
    }
}

private fun DrawScope.drawSvgLine(line: SvgLine, ctx: DrawContext) {
    val effectiveStroke = ctx.getEffectiveStroke()
    if (ctx.hasStroke && effectiveStroke.width > 0) {
        drawLine(
            color = ctx.strokeColor,
            start = Offset(line.x1, line.y1),
            end = Offset(line.x2, line.y2),
            strokeWidth = effectiveStroke.width,
            cap = effectiveStroke.cap,
            pathEffect = effectiveStroke.pathEffect
        )
    }
}

private fun DrawScope.drawSvgPolyline(polyline: SvgPolyline, ctx: DrawContext) {
    if (polyline.points.size < 2) return

    val path = Path().apply {
        fillType = ctx.fillRule
        val first = polyline.points.first()
        moveTo(first.x, first.y)
        for (i in 1 until polyline.points.size) {
            val point = polyline.points[i]
            lineTo(point.x, point.y)
        }
    }
    val effectiveStroke = ctx.getEffectiveStroke()

    when (ctx.paintOrder) {
        PaintOrder.FILL_STROKE -> {
            ctx.fillColor?.let { fill ->
                drawPath(path, fill, style = Fill)
            }
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawPath(path, ctx.strokeColor, style = effectiveStroke)
            }
        }
        PaintOrder.STROKE_FILL -> {
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawPath(path, ctx.strokeColor, style = effectiveStroke)
            }
            ctx.fillColor?.let { fill ->
                drawPath(path, fill, style = Fill)
            }
        }
    }
}

private fun DrawScope.drawSvgPolygon(polygon: SvgPolygon, ctx: DrawContext) {
    if (polygon.points.size < 2) return

    val path = Path().apply {
        fillType = ctx.fillRule
        val first = polygon.points.first()
        moveTo(first.x, first.y)
        for (i in 1 until polygon.points.size) {
            val point = polygon.points[i]
            lineTo(point.x, point.y)
        }
        close()
    }
    val effectiveStroke = ctx.getEffectiveStroke()

    when (ctx.paintOrder) {
        PaintOrder.FILL_STROKE -> {
            ctx.fillColor?.let { fill ->
                drawPath(path, fill, style = Fill)
            }
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawPath(path, ctx.strokeColor, style = effectiveStroke)
            }
        }
        PaintOrder.STROKE_FILL -> {
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawPath(path, ctx.strokeColor, style = effectiveStroke)
            }
            ctx.fillColor?.let { fill ->
                drawPath(path, fill, style = Fill)
            }
        }
    }
}

// ============================================
// Animated SVG Drawing
// ============================================

/**
 * Draws an animated SVG onto the canvas.
 * Handles viewBox, viewport, and preserveAspectRatio correctly.
 */
private fun DrawScope.drawAnimatedSvg(
    svg: Svg,
    tint: Color,
    strokeWidthOverride: Float?,
    progressMap: Map<AnimationKey, State<Float>>,
    pathCache: Map<SvgElement, CachedPathInfo>
) {
    val viewBox = svg.effectiveViewBox
    val strokeWidth = strokeWidthOverride ?: svg.strokeWidth

    // Calculate scale factors based on preserveAspectRatio
    val (scaleX, scaleY, translateX, translateY) = calculateViewBoxTransform(
        viewportWidth = size.width,
        viewportHeight = size.height,
        viewBoxMinX = viewBox.minX,
        viewBoxMinY = viewBox.minY,
        viewBoxWidth = viewBox.width,
        viewBoxHeight = viewBox.height,
        preserveAspectRatio = svg.preserveAspectRatio
    )

    val defaultStroke = Stroke(
        width = strokeWidth,
        cap = svg.strokeLinecap.toCompose(),
        join = svg.strokeLinejoin.toCompose()
    )

    // Color handling:
    // - null = none (no color)
    // - Color.Unspecified = currentColor (use tint)
    // - other Color = use that color
    val fillColor = svg.fill?.let { if (it == Color.Unspecified) tint else it }
    val strokeColor = svg.stroke?.let { if (it == Color.Unspecified) tint else it }

    val registry = collectDefs(svg.children)

    val ctx = DrawContext(
        strokeColor = strokeColor ?: tint,
        fillColor = fillColor,
        stroke = defaultStroke,
        hasStroke = strokeColor != null,
        scaleFactor = scaleX
    )

    // Apply viewBox transformation: translate then scale
    translate(translateX, translateY) {
        scale(scaleX, scaleY, pivot = Offset.Zero) {
            // Translate to handle viewBox minX/minY
            translate(-viewBox.minX, -viewBox.minY) {
                svg.children.forEach { element ->
                    drawAnimatedSvgElement(element, ctx, registry, progressMap, pathCache)
                }
            }
        }
    }
}

/**
 * Draws an SVG element with animation support.
 */
private fun DrawScope.drawAnimatedSvgElement(
    element: SvgElement,
    ctx: DrawContext,
    registry: DefsRegistry,
    progressMap: Map<AnimationKey, State<Float>>,
    pathCache: Map<SvgElement, CachedPathInfo>
) {
    when (element) {
        is SvgAnimated -> {
            drawAnimatedElement(element, ctx, registry, progressMap, pathCache)
        }
        is SvgPath -> drawSvgPath(element, ctx)
        is SvgCircle -> drawSvgCircle(element, ctx)
        is SvgEllipse -> drawSvgEllipse(element, ctx)
        is SvgRect -> drawSvgRect(element, ctx)
        is SvgLine -> drawSvgLine(element, ctx)
        is SvgPolyline -> drawSvgPolyline(element, ctx)
        is SvgPolygon -> drawSvgPolygon(element, ctx)
        is SvgGroup -> element.children.forEach { drawAnimatedSvgElement(it, ctx, registry, progressMap, pathCache) }
        is SvgStyled -> drawAnimatedStyledElement(element, ctx, registry, progressMap, pathCache)
        is SvgDefs -> {}
        is SvgClipPath -> {}
        is SvgMask -> {}
    }
}

/**
 * Draws a styled element with animation support.
 */
private fun DrawScope.drawAnimatedStyledElement(
    styled: SvgStyled,
    parentCtx: DrawContext,
    registry: DefsRegistry,
    progressMap: Map<AnimationKey, State<Float>>,
    pathCache: Map<SvgElement, CachedPathInfo>
) {
    val style = styled.style
    val ctx = applyStyle(parentCtx, style)

    val clipPathId = ctx.clipPathId
    val clipPath = clipPathId?.let { registry.clipPaths[it] }

    val drawContent: DrawScope.() -> Unit = {
        val transform = style.transform
        if (transform != null) {
            withTransform(transform) {
                drawAnimatedSvgElement(styled.element, ctx, registry, progressMap, pathCache)
            }
        } else {
            drawAnimatedSvgElement(styled.element, ctx, registry, progressMap, pathCache)
        }
    }

    if (clipPath != null) {
        val path = buildClipPath(clipPath, ctx)
        clipPath(path, ClipOp.Intersect) {
            drawContent()
        }
    } else {
        drawContent()
    }
}

/**
 * Draws an animated element with its animations applied.
 */
private fun DrawScope.drawAnimatedElement(
    animated: SvgAnimated,
    ctx: DrawContext,
    registry: DefsRegistry,
    progressMap: Map<AnimationKey, State<Float>>,
    pathCache: Map<SvgElement, CachedPathInfo>
) {
    val innerElement = animated.element

    // Find animation progress values
    var rotationAngle = 0f
    var scaleValue = 1f
    var translateX = 0f
    var translateY = 0f
    var opacity = 1f
    var strokeWidthMultiplier = 1f
    var strokeDrawProgress: Float? = null
    var strokeDrawReverse = false

    animated.animations.forEach { anim ->
        // O(1) lookup using AnimationKey instead of O(N) linear search
        val progress = progressMap[AnimationKey(innerElement, anim)]?.value ?: 1f

        when (anim) {
            is SvgAnimate.Transform -> {
                val value = anim.from + (anim.to - anim.from) * progress
                when (anim.type) {
                    TransformType.ROTATE -> rotationAngle = value
                    TransformType.SCALE, TransformType.SCALE_X, TransformType.SCALE_Y -> scaleValue = value
                    TransformType.TRANSLATE -> { translateX = value; translateY = value }
                    TransformType.TRANSLATE_X -> translateX = value
                    TransformType.TRANSLATE_Y -> translateY = value
                    TransformType.SKEW_X, TransformType.SKEW_Y -> {}
                }
            }
            is SvgAnimate.Opacity -> {
                opacity = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.StrokeWidth -> {
                val from = anim.from
                val to = anim.to
                val currentWidth = from + (to - from) * progress
                strokeWidthMultiplier = currentWidth / ctx.stroke.width
            }
            is SvgAnimate.StrokeDraw -> {
                strokeDrawProgress = if (anim.reverse) 1f - progress else progress
                strokeDrawReverse = anim.reverse
            }
            is SvgAnimate.StrokeOpacity -> {
                // Apply stroke opacity animation
                opacity *= anim.from + (anim.to - anim.from) * progress
            }
            else -> {
                // Other animation types not yet implemented
            }
        }
    }

    // Apply opacity
    val effectiveCtx = if (opacity != 1f) {
        ctx.copy(
            strokeColor = ctx.strokeColor.copy(alpha = ctx.strokeColor.alpha * opacity),
            fillColor = ctx.fillColor?.copy(alpha = ctx.fillColor.alpha * opacity)
        )
    } else {
        ctx
    }

    // Apply stroke width multiplier
    val finalCtx = if (strokeWidthMultiplier != 1f) {
        effectiveCtx.copy(
            stroke = Stroke(
                width = effectiveCtx.stroke.width * strokeWidthMultiplier,
                cap = effectiveCtx.stroke.cap,
                join = effectiveCtx.stroke.join,
                miter = effectiveCtx.stroke.miter,
                pathEffect = effectiveCtx.stroke.pathEffect
            )
        )
    } else {
        effectiveCtx
    }

    // Apply transforms
    translate(translateX, translateY) {
        scale(scaleValue, scaleValue, pivot = Offset(12f, 12f)) {
            rotate(rotationAngle, pivot = Offset(12f, 12f)) {
                if (strokeDrawProgress != null) {
                    drawElementWithStrokeDraw(innerElement, finalCtx, strokeDrawProgress, pathCache)
                } else {
                    drawSvgElement(innerElement, finalCtx, registry)
                }
            }
        }
    }
}

/**
 * Draws an element with stroke-draw animation (progressive stroke reveal).
 * Uses cached Path and pathLength when available to avoid per-frame allocations.
 */
private fun DrawScope.drawElementWithStrokeDraw(
    element: SvgElement,
    ctx: DrawContext,
    progress: Float,
    pathCache: Map<SvgElement, CachedPathInfo>
) {
    // Don't draw anything if progress is 0 or less
    if (progress <= 0f) {
        return
    }

    // Try to use cached path info first, fall back to creating new path if not cached
    val cachedInfo = pathCache[element]
    val path: Path
    val pathLength: Float

    if (cachedInfo != null) {
        // Use cached path and length
        path = cachedInfo.path
        pathLength = cachedInfo.pathLength
    } else {
        // Fallback: create path (this shouldn't happen if caching is set up correctly)
        val generatedPath = elementToPath(element) ?: return
        path = generatedPath
        val pathMeasure = androidx.compose.ui.graphics.PathMeasure()
        pathMeasure.setPath(path, false)
        pathLength = pathMeasure.length
    }

    if (pathLength > 0f && progress < 1f) {
        // Draw partial stroke using dash path effect
        val drawnLength = pathLength * progress
        val gapLength = pathLength * 2  // Use larger gap to ensure nothing extra is drawn

        val dashEffect = PathEffect.dashPathEffect(
            floatArrayOf(drawnLength, gapLength),
            0f
        )

        val animatedStroke = Stroke(
            width = ctx.stroke.width,
            cap = ctx.stroke.cap,
            join = ctx.stroke.join,
            miter = ctx.stroke.miter,
            pathEffect = dashEffect
        )

        if (ctx.hasStroke) {
            drawPath(path = path, color = ctx.strokeColor, style = animatedStroke)
        }
    } else {
        // Full stroke (progress >= 1.0)
        val effectiveStroke = ctx.getEffectiveStroke()
        ctx.fillColor?.let { fill ->
            drawPath(path = path, color = fill, style = Fill)
        }
        if (ctx.hasStroke && effectiveStroke.width > 0) {
            drawPath(path = path, color = ctx.strokeColor, style = effectiveStroke)
        }
    }
}
