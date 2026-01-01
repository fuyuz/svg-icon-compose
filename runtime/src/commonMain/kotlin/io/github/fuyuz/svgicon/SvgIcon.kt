package io.github.fuyuz.svgicon

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fuyuz.svgicon.core.ClipPathUnits
import io.github.fuyuz.svgicon.core.FillRule
import io.github.fuyuz.svgicon.core.LineCap
import io.github.fuyuz.svgicon.core.LineJoin
import io.github.fuyuz.svgicon.core.PaintOrder
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
import io.github.fuyuz.svgicon.core.VectorEffect
import io.github.fuyuz.svgicon.core.toPath

/**
 * Interface representing an SVG icon.
 */
interface SvgIcon {
    /**
     * SVG data for this icon.
     */
    val svg: Svg
}

/**
 * Default values for SVG icons.
 */
object SvgIconDefaults {
    val Size: Dp = 24.dp
}

/**
 * Composable that renders an SVG icon with Material 3-style API.
 *
 * @param icon The SvgIcon to render
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 *   This should always be provided unless this icon is used for decorative purposes, and does not
 *   represent a meaningful action that a user can take.
 * @param modifier Modifier to be applied to the icon
 * @param tint Color to tint the icon. Defaults to LocalContentColor.
 */
@Composable
fun SvgIcon(
    icon: SvgIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    SvgIcon(
        icon = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
        size = SvgIconDefaults.Size
    )
}

/**
 * Composable that renders an SVG icon with size control.
 *
 * @param icon The SvgIcon to render
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 * @param modifier Modifier to be applied to the icon
 * @param tint Color to tint the icon. Defaults to LocalContentColor.
 * @param size Size of the icon. Defaults to 24.dp.
 */
@Composable
fun SvgIcon(
    icon: SvgIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = SvgIconDefaults.Size
) {
    val svg = remember(icon) { icon.svg }
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    Canvas(modifier = modifier.size(size).then(semanticsModifier)) {
        drawSvg(svg, tint, null)
    }
}

/**
 * Composable that renders an SVG icon with full control over rendering.
 * This renders the icon statically without animations.
 * For animated icons, use [AnimatedSvgIcon] instead.
 *
 * @param icon The SvgIcon to render
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 * @param modifier Modifier to be applied to the icon
 * @param tint Color to tint the icon. Defaults to LocalContentColor.
 * @param size Size of the icon. Defaults to 24.dp.
 * @param strokeWidth Override the stroke width. If null, uses the SVG's default strokeWidth.
 */
@Composable
fun SvgIcon(
    icon: SvgIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = SvgIconDefaults.Size,
    strokeWidth: Float? = null
) {
    val svg = remember(icon) { icon.svg }
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    Canvas(modifier = modifier.size(size).then(semanticsModifier)) {
        drawSvg(svg, tint, strokeWidth)
    }
}

/**
 * Composable that renders an SVG directly from a parsed [Svg] object.
 * Use this with [parseSvg] for runtime SVG parsing.
 *
 * Example:
 * ```kotlin
 * val svg = remember { parseSvg("<svg>...</svg>") }
 * SvgIcon(svg = svg, contentDescription = "My icon", tint = Color.White)
 * ```
 *
 * @param svg The parsed Svg object to render
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 * @param modifier Modifier to be applied to the icon
 * @param tint Color to tint the icon. Defaults to LocalContentColor.
 * @param size Size of the icon. Defaults to 24.dp.
 * @param strokeWidth Override the stroke width. If null, uses the SVG's default strokeWidth.
 */
@Composable
fun SvgIcon(
    svg: Svg,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = SvgIconDefaults.Size,
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

    Canvas(modifier = modifier.size(size).then(semanticsModifier)) {
        drawSvg(svg, tint, strokeWidth)
    }
}

/**
 * Composable that renders an animated SVG icon with SMIL animations.
 *
 * @param icon The SvgIcon to render (must contain SvgAnimated elements for animation)
 * @param contentDescription Text used by accessibility services to describe what this icon represents.
 * @param modifier Modifier to be applied to the icon
 * @param tint Color to tint the icon. Defaults to LocalContentColor.
 * @param size Size of the icon. Defaults to 24.dp.
 * @param strokeWidth Override the stroke width. If null, uses the SVG's default strokeWidth.
 * @param animate Whether to animate. When false, renders the final state statically.
 * @param iterations Number of animation iterations. Use [Int.MAX_VALUE] for infinite.
 * @param onAnimationEnd Callback invoked when animation completes (not called for infinite iterations).
 */
@Composable
fun AnimatedSvgIcon(
    icon: SvgIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = SvgIconDefaults.Size,
    strokeWidth: Float? = null,
    animate: Boolean = true,
    iterations: Int = Int.MAX_VALUE,
    onAnimationEnd: (() -> Unit)? = null
) {
    val svg = remember(icon) { icon.svg }
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    // Check if the SVG has animations
    val hasAnimations = remember(svg) { hasAnimatedElements(svg.children) }

    if (hasAnimations && animate) {
        AnimatedSvgIconCanvas(
            svg = svg,
            tint = tint,
            strokeWidthOverride = strokeWidth,
            iterations = iterations,
            onAnimationEnd = onAnimationEnd,
            modifier = modifier.size(size).then(semanticsModifier)
        )
    } else {
        // Static rendering
        Canvas(modifier = modifier.size(size).then(semanticsModifier)) {
            drawSvg(svg, tint, strokeWidth)
        }
    }
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

    // Find the maximum duration among all animations
    val maxDuration = remember(animations) {
        animations.maxOfOrNull { it.animation.dur.inWholeMilliseconds.toInt() } ?: 1000
    }

    val isInfinite = iterations == Int.MAX_VALUE

    if (isInfinite) {
        // Use infinite transition for continuous animations
        val infiniteTransition = rememberInfiniteTransition(label = "svg_animation")

        val animationValues = animations.map { entry ->
            val anim = entry.animation
            val duration = anim.dur.inWholeMilliseconds.toInt().coerceAtLeast(1)

            val repeatMode = when (anim) {
                is SvgAnimate.Opacity -> RepeatMode.Reverse
                is SvgAnimate.StrokeWidth -> RepeatMode.Reverse
                is SvgAnimate.Transform -> when (anim.type) {
                    TransformType.ROTATE -> RepeatMode.Restart
                    else -> RepeatMode.Reverse
                }
                else -> RepeatMode.Restart
            }

            infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = LinearEasing),
                    repeatMode = repeatMode
                ),
                label = "anim_${entry.index}"
            )
        }

        val progressMap = remember(animations, animationValues) {
            animations.mapIndexed { idx, entry ->
                entry.key to animationValues[idx]
            }.toMap()
        }

        Canvas(modifier = modifier) {
            drawAnimatedSvg(svg, tint, strokeWidthOverride, progressMap)
        }
    } else {
        // Use finite animation with iteration count
        var currentIteration by remember { mutableStateOf(0) }
        var animationCompleted by remember { mutableStateOf(false) }

        val animationValues = animations.map { entry ->
            val anim = entry.animation
            val duration = anim.dur.inWholeMilliseconds.toInt().coerceAtLeast(1)

            val progress = remember { Animatable(0f) }

            LaunchedEffect(currentIteration, animationCompleted) {
                if (!animationCompleted && currentIteration < iterations) {
                    progress.snapTo(0f)
                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(duration, easing = LinearEasing)
                    )
                }
            }

            progress
        }

        // Track when all animations in an iteration complete
        LaunchedEffect(Unit) {
            while (currentIteration < iterations) {
                kotlinx.coroutines.delay(maxDuration.toLong())
                currentIteration++
                if (currentIteration >= iterations) {
                    animationCompleted = true
                    onAnimationEnd?.invoke()
                }
            }
        }

        val progressMap = remember(animations, animationValues) {
            animations.mapIndexed { idx, entry ->
                entry.key to animationValues[idx].asState()
            }.toMap()
        }

        Canvas(modifier = modifier) {
            drawAnimatedSvg(svg, tint, strokeWidthOverride, progressMap)
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
 */
private fun DrawScope.drawSvg(svg: Svg, tint: Color, strokeWidthOverride: Float?) {
    val scaleFactor = size.width / svg.viewBox.width
    val strokeWidth = strokeWidthOverride ?: svg.strokeWidth

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
        scaleFactor = scaleFactor
    )

    scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
        svg.children.forEach { element ->
            drawSvgElement(element, context, registry)
        }
    }
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
 */
private fun DrawScope.drawAnimatedSvg(
    svg: Svg,
    tint: Color,
    strokeWidthOverride: Float?,
    progressMap: Map<AnimationKey, State<Float>>
) {
    val scaleFactor = size.width / svg.viewBox.width
    val strokeWidth = strokeWidthOverride ?: svg.strokeWidth

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
        scaleFactor = scaleFactor
    )

    scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
        svg.children.forEach { element ->
            drawAnimatedSvgElement(element, ctx, registry, progressMap)
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
    progressMap: Map<AnimationKey, State<Float>>
) {
    when (element) {
        is SvgAnimated -> {
            drawAnimatedElement(element, ctx, registry, progressMap)
        }
        is SvgPath -> drawSvgPath(element, ctx)
        is SvgCircle -> drawSvgCircle(element, ctx)
        is SvgEllipse -> drawSvgEllipse(element, ctx)
        is SvgRect -> drawSvgRect(element, ctx)
        is SvgLine -> drawSvgLine(element, ctx)
        is SvgPolyline -> drawSvgPolyline(element, ctx)
        is SvgPolygon -> drawSvgPolygon(element, ctx)
        is SvgGroup -> element.children.forEach { drawAnimatedSvgElement(it, ctx, registry, progressMap) }
        is SvgStyled -> drawAnimatedStyledElement(element, ctx, registry, progressMap)
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
    progressMap: Map<AnimationKey, State<Float>>
) {
    val style = styled.style
    val ctx = applyStyle(parentCtx, style)

    val clipPathId = ctx.clipPathId
    val clipPath = clipPathId?.let { registry.clipPaths[it] }

    val drawContent: DrawScope.() -> Unit = {
        val transform = style.transform
        if (transform != null) {
            withTransform(transform) {
                drawAnimatedSvgElement(styled.element, ctx, registry, progressMap)
            }
        } else {
            drawAnimatedSvgElement(styled.element, ctx, registry, progressMap)
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
    progressMap: Map<AnimationKey, State<Float>>
) {
    val innerElement = animated.element

    // Find animation progress values
    var rotationAngle = 0f
    var scaleValue = 1f
    var translateX = 0f
    var translateY = 0f
    var opacity = 1f
    var strokeWidthMultiplier = 1f

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
            else -> {}
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
                drawSvgElement(innerElement, finalCtx, registry)
            }
        }
    }
}
