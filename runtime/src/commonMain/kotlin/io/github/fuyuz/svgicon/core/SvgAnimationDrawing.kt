package io.github.fuyuz.svgicon.core

import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import io.github.fuyuz.svgicon.core.*

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

    val drawingContext = SvgDrawingContext(context, registry)

    translate(translateX, translateY) {
        scale(scaleX, scaleY, pivot = Offset.Zero) {
            translate(-viewBox.minX, -viewBox.minY) {
                svg.children.forEach { element ->
                    drawAnimatedSvgElement(element, drawingContext, progressMap, pathCache)
                }
            }
        }
    }
}

private fun DrawScope.drawAnimatedSvgElement(
    element: SvgElement,
    context: SvgDrawingContext,
    progressMap: Map<AnimationKey, State<Float>>,
    pathCache: Map<SvgElement, CachedPathInfo>
) {
    // This should handle SvgAnimated wrapper and apply its animations
    if (element is SvgAnimated) {
        val state = AnimatedElementState()
        element.animations.forEach { anim ->
            val progress = progressMap[AnimationKey(element.element, anim)]?.value ?: 0f
            applyAnimationToState(anim, progress, state)
        }
        
        // Now draw the inner element with this state
        drawElementWithState(element.element, state, context, progressMap, pathCache)
    } else {
        // Fallback to regular drawing or recursive call
        drawSvgElement(context, element)
    }
}

private fun applyAnimationToState(anim: SvgAnimate, progress: Float, state: AnimatedElementState) {
    // Implementation of applying different animation types to state
    when (anim) {
        is SvgAnimate.Opacity -> state.opacity = anim.from + (anim.to - anim.from) * progress
        is SvgAnimate.Transform -> {
            val value = anim.from + (anim.to - anim.from) * progress
            when (anim.type) {
                TransformType.ROTATE -> state.rotationAngle = value
                TransformType.SCALE -> { state.scaleX = value; state.scaleY = value }
                TransformType.SCALE_X -> state.scaleX = value
                TransformType.SCALE_Y -> state.scaleY = value
                TransformType.TRANSLATE_X -> state.translateX = value
                TransformType.TRANSLATE_Y -> state.translateY = value
                else -> {}
            }
        }
        is SvgAnimate.StrokeDraw -> state.strokeDrawProgress = progress
        else -> {} // Handle other types
    }
}

private fun DrawScope.drawElementWithState(
    element: SvgElement,
    state: AnimatedElementState,
    context: SvgDrawingContext,
    progressMap: Map<AnimationKey, State<Float>>,
    pathCache: Map<SvgElement, CachedPathInfo>
) {
    val animatedCtx = context.ctx.copy(
        opacity = context.ctx.opacity * state.opacity,
        strokeColor = context.ctx.strokeColor.copy(alpha = context.ctx.strokeColor.alpha * state.opacity),
        fillColor = context.ctx.fillColor?.copy(alpha = context.ctx.fillColor.alpha * state.opacity)
    )
    val drawingCtx = context.withCtx(animatedCtx)

    withTransform(SvgTransform.Combined(listOf(
        SvgTransform.Translate(state.translateX, state.translateY),
        SvgTransform.Rotate(state.rotationAngle, 0f, 0f), // Should use center
        SvgTransform.Scale(state.scaleX, state.scaleY)
    ))) {
        if (state.strokeDrawProgress != null && element is SvgPath) {
             val pathInfo = pathCache[element]
             if (pathInfo != null) {
                 val path = Path()
                 androidx.compose.ui.graphics.PathMeasure().apply {
                     setPath(pathInfo.path, false)
                     getSegment(0f, length * state.strokeDrawProgress!!, path, true)
                 }
                 drawSvgPath(drawingCtx.ctx, path)
             } else {
                 drawSvgElement(drawingCtx, element)
             }
        } else {
            drawSvgElement(drawingCtx, element)
        }
    }
}
