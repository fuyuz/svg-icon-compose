package io.github.fuyuz.svgicon.core

import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.tan

/**
 * State container for cached path information.
 */
data class CachedPathInfo(val path: Path, val pathLength: Float)

internal fun buildPathCache(elements: List<SvgElement>): Map<SvgElement, CachedPathInfo> {
    val cache = mutableMapOf<SvgElement, CachedPathInfo>()
    val pathMeasure = PathMeasure()

    fun cacheElement(element: SvgElement) {
        val path = elementToPath(element)
        if (path != null) {
            pathMeasure.setPath(path, false)
            cache[element] = CachedPathInfo(path, pathMeasure.length)
        }
        when (element) {
            is SvgGroup -> element.children.forEach { cacheElement(it) }
            is SvgStyled -> cacheElement(element.element)
            is SvgAnimated -> cacheElement(element.element)
            else -> {}
        }
    }

    elements.forEach { cacheElement(it) }
    return cache
}

internal fun elementToPath(element: SvgElement): Path? {
    return when (element) {
        is SvgPath -> element.commands.toPath()
        is SvgCircle -> Path().apply { addOval(Rect(element.cx - element.r, element.cy - element.r, element.cx + element.r, element.cy + element.r)) }
        is SvgEllipse -> Path().apply { addOval(Rect(element.cx - element.rx, element.cy - element.ry, element.cx + element.rx, element.cy + element.ry)) }
        is SvgRect -> Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(element.x, element.y, element.x + element.width, element.y + element.height, androidx.compose.ui.geometry.CornerRadius(element.rx, element.ry))) }
        is SvgPolygon -> if (element.points.size < 2) null else Path().apply {
            moveTo(element.points[0].x, element.points[0].y)
            for (i in 1 until element.points.size) lineTo(element.points[i].x, element.points[i].y)
            close()
        }
        is SvgPolyline -> if (element.points.size < 2) null else Path().apply {
            moveTo(element.points[0].x, element.points[0].y)
            for (i in 1 until element.points.size) lineTo(element.points[i].x, element.points[i].y)
        }
        is SvgStyled -> elementToPath(element.element)
        is SvgAnimated -> elementToPath(element.element)
        else -> null
    }
}

internal fun DrawScope.drawAnimatedSvg(
    svg: Svg,
    tint: Color,
    strokeWidthOverride: Float?,
    progressMap: Map<AnimationKey, State<Float>>,
    pathCache: Map<SvgElement, CachedPathInfo>,
    textMeasurer: TextMeasurer? = null
) {
    val viewBox = svg.effectiveViewBox
    val (scaleX, scaleY, translateX, translateY) = calculateViewBoxTransform(
        viewportWidth = size.width,
        viewportHeight = size.height,
        viewBoxMinX = viewBox.minX,
        viewBoxMinY = viewBox.minY,
        viewBoxWidth = viewBox.width,
        viewBoxHeight = viewBox.height,
        preserveAspectRatio = svg.preserveAspectRatio
    )

    val strokeWidth = strokeWidthOverride ?: svg.strokeWidth
    val defaultStroke = Stroke(
        width = strokeWidth,
        cap = svg.strokeLinecap.toCompose(),
        join = svg.strokeLinejoin.toCompose()
    )

    val fillColor = svg.fill?.let { if (it == Color.Unspecified) tint else it }
    val strokeColor = svg.stroke?.let { if (it == Color.Unspecified) tint else it }

    val registry = collectDefs(svg.children, textMeasurer)
    val context = DrawContext(
        strokeColor = strokeColor ?: tint,
        fillColor = fillColor,
        stroke = defaultStroke,
        hasStroke = strokeColor != null,
        scaleFactor = scaleX
    )

    translate(translateX, translateY) {
        scale(scaleX, scaleY, pivot = Offset.Zero) {
            translate(-viewBox.minX, -viewBox.minY) {
                svg.children.forEach { element ->
                    drawAnimatedSvgElement(element, context, registry, progressMap, pathCache)
                }
            }
        }
    }
}

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
        is SvgPath -> drawSvgPath(ctx, element)
        is SvgCircle -> drawSvgCircle(ctx, element)
        is SvgEllipse -> drawSvgEllipse(ctx, element)
        is SvgRect -> drawSvgRect(ctx, element)
        is SvgLine -> drawSvgLine(ctx, element)
        is SvgPolyline -> drawSvgPolyline(ctx, element)
        is SvgPolygon -> drawSvgPolygon(ctx, element)
        is SvgGroup -> {
            val groupCtx = element.style?.let { applyStyle(ctx, it) } ?: ctx
            element.children.forEach { drawAnimatedSvgElement(it, groupCtx, registry, progressMap, pathCache) }
        }
        is SvgStyled -> drawAnimatedStyledElement(element, ctx, registry, progressMap, pathCache)
        is SvgDefs -> {}
        is SvgClipPath -> {}
        is SvgMask -> {}
        is SvgText -> drawSvgText(ctx, element, registry.textMeasurer)
        is SvgLinearGradient -> {}
        is SvgRadialGradient -> {}
        is SvgMarker -> {}
        is SvgPattern -> {}
        is SvgSymbol -> {}
        is SvgUse -> drawAnimatedSvgUse(element, ctx, registry, progressMap, pathCache)
    }
}

/**
 * Draws an animated SvgUse element by looking up the referenced symbol and rendering it.
 */
private fun DrawScope.drawAnimatedSvgUse(
    use: SvgUse,
    ctx: DrawContext,
    registry: DefsRegistry,
    progressMap: Map<AnimationKey, State<Float>>,
    pathCache: Map<SvgElement, CachedPathInfo>
) {
    val refId = use.href.removePrefix("#")
    val symbol = registry.symbols[refId] ?: return

    translate(left = use.x, top = use.y) {
        symbol.children.forEach { child ->
            drawAnimatedSvgElement(child, ctx, registry, progressMap, pathCache)
        }
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
    val state = AnimatedElementState()

    // Process all animations
    animated.animations.forEach { anim ->
        val progress = progressMap[AnimationKey(innerElement, anim)]?.value ?: 1f

        when (anim) {
            is SvgAnimate.Transform -> {
                val value = anim.from + (anim.to - anim.from) * progress
                val isAdditive = anim.additive == AdditiveMode.SUM
                when (anim.type) {
                    TransformType.ROTATE -> {
                        state.rotationAngle = if (isAdditive) state.rotationAngle + value else value
                        // Set rotation center if specified in animation
                        if (anim.cx != null) state.rotateCx = anim.cx
                        if (anim.cy != null) state.rotateCy = anim.cy
                    }
                    TransformType.SCALE -> {
                        if (isAdditive) {
                            // For additive scale, multiply (scale of 1 is identity)
                            state.scaleX *= value
                            state.scaleY *= value
                        } else {
                            state.scaleX = value
                            state.scaleY = value
                        }
                    }
                    TransformType.SCALE_X -> {
                        state.scaleX = if (isAdditive) state.scaleX * value else value
                    }
                    TransformType.SCALE_Y -> {
                        state.scaleY = if (isAdditive) state.scaleY * value else value
                    }
                    TransformType.TRANSLATE -> {
                        // Use separate X and Y values for translate
                        val valueX = anim.from + (anim.to - anim.from) * progress
                        val valueY = anim.fromY + (anim.toY - anim.fromY) * progress
                        if (isAdditive) {
                            state.translateX += valueX
                            state.translateY += valueY
                        } else {
                            state.translateX = valueX
                            state.translateY = valueY
                        }
                    }
                    TransformType.TRANSLATE_X -> {
                        state.translateX = if (isAdditive) state.translateX + value else value
                    }
                    TransformType.TRANSLATE_Y -> {
                        state.translateY = if (isAdditive) state.translateY + value else value
                    }
                    TransformType.SKEW_X -> {
                        state.skewX = if (isAdditive) state.skewX + value else value
                    }
                    TransformType.SKEW_Y -> {
                        state.skewY = if (isAdditive) state.skewY + value else value
                    }
                }
            }
            is SvgAnimate.Opacity -> {
                state.opacity = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.StrokeOpacity -> {
                state.strokeOpacity = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.FillOpacity -> {
                state.fillOpacity = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.StrokeWidth -> {
                state.strokeWidth = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.StrokeDraw -> {
                state.strokeDrawProgress = if (anim.reverse) 1f - progress else progress
            }
            is SvgAnimate.StrokeDasharray -> {
                state.strokeDasharray = interpolateDasharray(anim.from, anim.to, progress)
            }
            is SvgAnimate.StrokeDashoffset -> {
                state.strokeDashoffset = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.Cx -> {
                state.cx = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.Cy -> {
                state.cy = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.R -> {
                state.r = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.Rx -> {
                state.rx = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.Ry -> {
                state.ry = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.X -> {
                state.x = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.Y -> {
                state.y = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.Width -> {
                state.width = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.Height -> {
                state.height = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.X1 -> {
                state.x1 = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.Y1 -> {
                state.y1 = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.X2 -> {
                state.x2 = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.Y2 -> {
                state.y2 = anim.from + (anim.to - anim.from) * progress
            }
            is SvgAnimate.D -> {
                state.morphedPath = interpolatePathCommands(anim.from, anim.to, progress)
            }
            is SvgAnimate.Points -> {
                state.morphedPoints = interpolatePoints(anim.from, anim.to, progress)
            }
            is SvgAnimate.Motion -> {
                state.motionPath = anim.path
                state.motionProgress = progress
                state.motionRotate = anim.rotate
            }
        }
    }

    // Build the effective context with opacity and stroke modifications
    val effectiveCtx = buildAnimatedContext(ctx, state)

    // Calculate pivot point based on element type
    val pivot = calculatePivot(innerElement)

    // Apply transforms including skew
    withAnimatedTransform(state, pivot) {
        // Apply motion path if present
        if (state.motionPath != null) {
            withMotionPath(state.motionPath!!, state.motionProgress, state.motionRotate) {
                drawAnimatedInnerElement(innerElement, effectiveCtx, registry, pathCache, state)
            }
        } else {
            drawAnimatedInnerElement(innerElement, effectiveCtx, registry, pathCache, state)
        }
    }
}

/**
 * Builds an animated context with opacity and stroke modifications applied.
 */
private fun buildAnimatedContext(ctx: DrawContext, state: AnimatedElementState): DrawContext {
    var result = ctx

    // Apply overall opacity
    if (state.opacity != 1f || state.strokeOpacity != 1f || state.fillOpacity != 1f) {
        val strokeAlpha = ctx.strokeColor.alpha * state.opacity * state.strokeOpacity
        val fillAlpha = ctx.fillColor?.alpha?.times(state.opacity)?.times(state.fillOpacity)
        result = result.copy(
            strokeColor = ctx.strokeColor.copy(alpha = strokeAlpha),
            fillColor = ctx.fillColor?.copy(alpha = fillAlpha ?: 0f)
        )
    }

    // Apply stroke width
    if (state.strokeWidth != null) {
        result = result.copy(
            stroke = Stroke(
                width = state.strokeWidth!!,
                cap = ctx.stroke.cap,
                join = ctx.stroke.join,
                miter = ctx.stroke.miter,
                pathEffect = ctx.stroke.pathEffect
            )
        )
    }

    // Apply dasharray and dashoffset
    if (state.strokeDasharray != null || state.strokeDashoffset != null) {
        val dasharray = state.strokeDasharray
        val dashoffset = state.strokeDashoffset ?: 0f

        val pathEffect = if (dasharray != null && dasharray.isNotEmpty()) {
            val intervals = if (dasharray.size % 2 == 0) {
                dasharray.toFloatArray()
            } else {
                FloatArray(dasharray.size * 2) { i -> dasharray[i % dasharray.size] }
            }
            PathEffect.dashPathEffect(intervals, dashoffset)
        } else {
            result.stroke.pathEffect
        }

        result = result.copy(
            stroke = Stroke(
                width = result.stroke.width,
                cap = result.stroke.cap,
                join = result.stroke.join,
                miter = result.stroke.miter,
                pathEffect = pathEffect
            )
        )
    }

    return result
}

/**
 * Calculates the pivot point for transforms based on element type.
 */
private fun calculatePivot(element: SvgElement): Offset {
    return when (element) {
        is SvgCircle -> Offset(element.cx, element.cy)
        is SvgEllipse -> Offset(element.cx, element.cy)
        is SvgRect -> Offset(element.x + element.width / 2, element.y + element.height / 2)
        else -> Offset(12f, 12f) // Default pivot for 24x24 icons
    }
}

/**
 * Applies animated transforms including skew.
 */
private fun DrawScope.withAnimatedTransform(
    state: AnimatedElementState,
    pivot: Offset,
    block: DrawScope.() -> Unit
) {
    // Use animation-specified rotation center if available, otherwise use element pivot
    val rotatePivot = if (state.rotateCx != null && state.rotateCy != null) {
        Offset(state.rotateCx!!, state.rotateCy!!)
    } else {
        pivot
    }

    translate(state.translateX, state.translateY) {
        // Apply skew if needed
        if (state.skewX != 0f || state.skewY != 0f) {
            val skewMatrix = Matrix().apply {
                if (state.skewX != 0f) {
                    val tanX = tan(state.skewX.toDouble() * PI / 180.0).toFloat()
                    this[0, 1] = tanX
                }
                if (state.skewY != 0f) {
                    val tanY = tan(state.skewY.toDouble() * PI / 180.0).toFloat()
                    this[1, 0] = tanY
                }
            }
            withTransform({ transform(skewMatrix) }) {
                scale(state.scaleX, state.scaleY, pivot = pivot) {
                    rotate(state.rotationAngle, pivot = rotatePivot) {
                        block()
                    }
                }
            }
        } else {
            scale(state.scaleX, state.scaleY, pivot = pivot) {
                rotate(state.rotationAngle, pivot = rotatePivot) {
                    block()
                }
            }
        }
    }
}

/**
 * Applies motion path animation.
 */
private fun DrawScope.withMotionPath(
    pathData: String,
    progress: Float,
    motionRotate: MotionRotate,
    block: DrawScope.() -> Unit
) {
    val commands = parsePathCommands(pathData)
    val path = commands.toPath()
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(path, false)

    val length = pathMeasure.length
    if (length <= 0f) {
        block()
        return
    }

    val distance = length * progress.coerceIn(0f, 1f)
    val position = pathMeasure.getPosition(distance)

    translate(position.x, position.y) {
        if (motionRotate != MotionRotate.NONE) {
            val tangent = pathMeasure.getTangent(distance)
            val angle = atan2(tangent.y, tangent.x) * 180f / PI.toFloat()
            val finalAngle = when (motionRotate) {
                MotionRotate.AUTO -> angle
                MotionRotate.AUTO_REVERSE -> angle + 180f
                else -> 0f
            }
            rotate(finalAngle, pivot = Offset.Zero) {
                block()
            }
        } else {
            block()
        }
    }
}

/**
 * Draws the inner element with animation state applied.
 */
private fun DrawScope.drawAnimatedInnerElement(
    element: SvgElement,
    ctx: DrawContext,
    registry: DefsRegistry,
    pathCache: Map<SvgElement, CachedPathInfo>,
    state: AnimatedElementState
) {
    // Handle stroke draw animation
    if (state.strokeDrawProgress != null) {
        drawElementWithStrokeDraw(element, ctx, state.strokeDrawProgress!!, pathCache)
        return
    }

    // Handle path morphing
    if (state.morphedPath != null && element is SvgPath) {
        drawMorphedPath(state.morphedPath!!, ctx)
        return
    }

    // Handle points morphing
    if (state.morphedPoints != null) {
        when (element) {
            is SvgPolygon -> drawMorphedPolygon(state.morphedPoints!!, ctx)
            is SvgPolyline -> drawMorphedPolyline(state.morphedPoints!!, ctx)
            else -> {}
        }
        return
    }

    // Handle geometric property animations
    when (element) {
        is SvgCircle -> {
            val animatedCircle = SvgCircle(
                cx = state.cx ?: element.cx,
                cy = state.cy ?: element.cy,
                r = state.r ?: element.r
            )
            drawSvgCircle(ctx, animatedCircle)
        }
        is SvgEllipse -> {
            val animatedEllipse = SvgEllipse(
                cx = state.cx ?: element.cx,
                cy = state.cy ?: element.cy,
                rx = state.rx ?: element.rx,
                ry = state.ry ?: element.ry
            )
            drawSvgEllipse(ctx, animatedEllipse)
        }
        is SvgRect -> {
            val animatedRect = SvgRect(
                x = state.x ?: element.x,
                y = state.y ?: element.y,
                width = state.width ?: element.width,
                height = state.height ?: element.height,
                rx = state.rx ?: element.rx,
                ry = state.ry ?: element.ry
            )
            drawSvgRect(ctx, animatedRect)
        }
        is SvgLine -> {
            val animatedLine = SvgLine(
                x1 = state.x1 ?: element.x1,
                y1 = state.y1 ?: element.y1,
                x2 = state.x2 ?: element.x2,
                y2 = state.y2 ?: element.y2
            )
            drawSvgLine(ctx, animatedLine)
        }
        is SvgStyled -> {
            // Apply SvgStyled's style on top of the animated context
            val styledCtx = applyStylePreservingAnimation(ctx, element.style, state)
            val transform = element.style.transform
            if (transform != null) {
                withTransform(transform) {
                    drawAnimatedInnerElement(element.element, styledCtx, registry, pathCache, state)
                }
            } else {
                drawAnimatedInnerElement(element.element, styledCtx, registry, pathCache, state)
            }
        }
        is SvgPath -> drawSvgPath(ctx, element)
        is SvgPolyline -> drawSvgPolyline(ctx, element)
        is SvgPolygon -> drawSvgPolygon(ctx, element)
        is SvgGroup -> {
            val groupCtx = element.style?.let { applyStylePreservingAnimation(ctx, it, state) } ?: ctx
            element.children.forEach { drawAnimatedInnerElement(it, groupCtx, registry, pathCache, state) }
        }
        is SvgText -> drawSvgText(ctx, element, registry.textMeasurer)
        else -> {
            // Fallback: delegate to static drawing
            val dc = SvgDrawingContext(ctx, registry)
            drawSvgElement(dc, element)
        }
    }
}

/**
 * Applies style while preserving animation state.
 */
private fun applyStylePreservingAnimation(
    ctx: DrawContext,
    style: SvgStyle,
    state: AnimatedElementState
): DrawContext {
    // First apply the base style
    var result = applyStyle(ctx, style)

    // Then re-apply animation state opacity values on top
    if (state.opacity != 1f || state.strokeOpacity != 1f || state.fillOpacity != 1f) {
        val strokeAlpha = result.strokeColor.alpha * state.opacity * state.strokeOpacity
        val fillAlpha = result.fillColor?.alpha?.times(state.opacity)?.times(state.fillOpacity)
        result = result.copy(
            strokeColor = result.strokeColor.copy(alpha = strokeAlpha),
            fillColor = result.fillColor?.copy(alpha = fillAlpha ?: 0f)
        )
    }

    // Apply animated stroke width if present
    if (state.strokeWidth != null) {
        result = result.copy(
            stroke = Stroke(
                width = state.strokeWidth!!,
                cap = result.stroke.cap,
                join = result.stroke.join,
                miter = result.stroke.miter,
                pathEffect = result.stroke.pathEffect
            )
        )
    }

    // Apply animated dasharray/dashoffset if present
    if (state.strokeDasharray != null || state.strokeDashoffset != null) {
        val dasharray = state.strokeDasharray
        val dashoffset = state.strokeDashoffset ?: 0f

        val pathEffect = if (dasharray != null && dasharray.isNotEmpty()) {
            val intervals = if (dasharray.size % 2 == 0) {
                dasharray.toFloatArray()
            } else {
                FloatArray(dasharray.size * 2) { i -> dasharray[i % dasharray.size] }
            }
            PathEffect.dashPathEffect(intervals, dashoffset)
        } else {
            result.stroke.pathEffect
        }

        result = result.copy(
            stroke = Stroke(
                width = result.stroke.width,
                cap = result.stroke.cap,
                join = result.stroke.join,
                miter = result.stroke.miter,
                pathEffect = pathEffect
            )
        )
    }

    return result
}

/**
 * Draws a morphed path from interpolated commands.
 */
private fun DrawScope.drawMorphedPath(commands: List<PathCommand>, ctx: DrawContext) {
    val path = commands.toPath().apply { fillType = ctx.fillRule }
    val effectiveStroke = ctx.getEffectiveStroke()

    when (ctx.paintOrder) {
        PaintOrder.FILL_STROKE -> {
            ctx.fillColor?.let { fill ->
                drawPath(path = path, color = fill, style = Fill)
            }
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawPath(path = path, color = ctx.strokeColor, style = effectiveStroke)
            }
        }
        PaintOrder.STROKE_FILL -> {
            if (ctx.hasStroke && effectiveStroke.width > 0) {
                drawPath(path = path, color = ctx.strokeColor, style = effectiveStroke)
            }
            ctx.fillColor?.let { fill ->
                drawPath(path = path, color = fill, style = Fill)
            }
        }
    }
}

/**
 * Draws a morphed polygon from interpolated points.
 */
private fun DrawScope.drawMorphedPolygon(points: List<Offset>, ctx: DrawContext) {
    if (points.size < 2) return

    val path = Path().apply {
        fillType = ctx.fillRule
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
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

/**
 * Draws a morphed polyline from interpolated points.
 */
private fun DrawScope.drawMorphedPolyline(points: List<Offset>, ctx: DrawContext) {
    if (points.size < 2) return

    val path = Path().apply {
        fillType = ctx.fillRule
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
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
        val pathMeasure = PathMeasure()
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
