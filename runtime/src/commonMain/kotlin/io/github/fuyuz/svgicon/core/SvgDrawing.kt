package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.fuyuz.svgicon.core.*
import kotlin.math.PI
import kotlin.math.tan

/**
 * Core drawing functions for SVG elements.
 */

internal fun DrawScope.drawSvg(svg: Svg, tint: Color, strokeWidthOverride: Float?, textMeasurer: TextMeasurer? = null) {
    val viewBox = svg.effectiveViewBox
    val strokeWidth = strokeWidthOverride ?: svg.strokeWidth

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
                drawSvgElement(context = drawingContext, element = svg.children)
            }
        }
    }
}

private fun DrawScope.drawSvgElement(context: SvgDrawingContext, element: List<SvgElement>) {
    element.forEach { 
        drawSvgElement(context, it)
    }
}

internal fun calculateViewBoxTransform(
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

    if (preserveAspectRatio.align == AspectRatioAlign.NONE) {
        return ViewBoxTransform(scaleX, scaleY, 0f, 0f)
    }

    val scale = when (preserveAspectRatio.meetOrSlice) {
        MeetOrSlice.MEET -> minOf(scaleX, scaleY)
        MeetOrSlice.SLICE -> maxOf(scaleX, scaleY)
    }

    val scaledViewBoxWidth = viewBoxWidth * scale
    val scaledViewBoxHeight = viewBoxHeight * scale

    val (alignX, alignY) = when (preserveAspectRatio.align) {
        AspectRatioAlign.NONE -> 0f to 0f
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

internal fun collectDefs(elements: List<SvgElement>, textMeasurer: TextMeasurer? = null): DefsRegistry {
    val clipPaths = mutableMapOf<String, SvgClipPath>()
    val masks = mutableMapOf<String, SvgMask>()
    val symbols = mutableMapOf<String, SvgSymbol>()
    val markers = mutableMapOf<String, SvgMarker>()
    val patterns = mutableMapOf<String, SvgPattern>()

    fun collect(element: SvgElement) {
        when (element) {
            is SvgClipPath -> clipPaths[element.id] = element
            is SvgMask -> masks[element.id] = element
            is SvgSymbol -> symbols[element.id] = element
            is SvgMarker -> markers[element.id] = element
            is SvgPattern -> patterns[element.id] = element
            is SvgDefs -> element.children.forEach { collect(it) }
            is SvgGroup -> element.children.forEach { collect(it) }
            is SvgStyled -> collect(element.element)
            is SvgAnimated -> collect(element.element)
            else -> {}
        }
    }

    elements.forEach { collect(it) }
    return DefsRegistry(clipPaths, masks, symbols, markers, patterns, textMeasurer)
}

internal fun DrawScope.drawSvgElement(
    context: SvgDrawingContext,
    element: SvgElement
) {
    when (element) {
        is SvgPath -> drawSvgPath(context.ctx, element)
        is SvgCircle -> drawSvgCircle(context.ctx, element)
        is SvgEllipse -> drawSvgEllipse(context.ctx, element)
        is SvgRect -> drawSvgRect(context.ctx, element)
        is SvgLine -> drawSvgLine(context.ctx, element)
        is SvgPolyline -> drawSvgPolyline(context.ctx, element)
        is SvgPolygon -> drawSvgPolygon(context.ctx, element)
        is SvgGroup -> {
            val newCtx = element.style?.let { applyStyle(context.ctx, it) } ?: context.ctx
            val drawingCtx = context.withCtx(newCtx)
            element.children.forEach { drawSvgElement(drawingCtx, it) }
        }
        is SvgStyled -> drawStyledElement(context, element)
        is SvgText -> drawSvgText(context.ctx, element, context.registry.textMeasurer)
        is SvgUse -> drawSvgUse(context, element)
        else -> {} // Defs, etc. are handled separately
    }
}

private fun DrawScope.drawStyledElement(
    context: SvgDrawingContext,
    styled: SvgStyled
) {
    val newCtx = applyStyle(context.ctx, styled.style)
    val drawingCtx = context.withCtx(newCtx)

    fun draw() {
        drawSvgElement(drawingCtx, styled.element)
    }

    val transform = styled.style.transform
    if (transform != null) {
        withTransform(transform) {
            draw()
        }
    } else {
        draw()
    }
}

internal fun DrawScope.drawSvgPath(ctx: DrawContext, path: SvgPath) {
    val composePath = path.commands.toPath().apply { fillType = ctx.fillRule }
    drawSvgPath(ctx, composePath)
}

internal fun DrawScope.drawSvgCircle(ctx: DrawContext, circle: SvgCircle) {
    drawSvgCircle(ctx, circle.r, Offset(circle.cx, circle.cy))
}

internal fun DrawScope.drawSvgEllipse(ctx: DrawContext, ellipse: SvgEllipse) {
    val topLeft = Offset(ellipse.cx - ellipse.rx, ellipse.cy - ellipse.ry)
    val size = Size(ellipse.rx * 2, ellipse.ry * 2)
    drawSvgEllipse(ctx, topLeft, size)
}

internal fun DrawScope.drawSvgRect(ctx: DrawContext, rect: SvgRect) {
    val topLeft = Offset(rect.x, rect.y)
    val size = Size(rect.width, rect.height)
    drawSvgRect(ctx, topLeft, size, rect.rx, rect.ry)
}

internal fun DrawScope.drawSvgLine(ctx: DrawContext, line: SvgLine) {
    if (ctx.hasStroke) {
        drawLine(ctx.strokeColor, Offset(line.x1, line.y1), Offset(line.x2, line.y2), strokeWidth = ctx.getEffectiveStroke().width, cap = ctx.getEffectiveStroke().cap, pathEffect = ctx.getEffectiveStroke().pathEffect)
    }
}

internal fun DrawScope.drawSvgPolyline(ctx: DrawContext, polyline: SvgPolyline) {
    if (polyline.points.size < 2) return
    val path = Path().apply {
        moveTo(polyline.points[0].x, polyline.points[0].y)
        for (i in 1 until polyline.points.size) {
            lineTo(polyline.points[i].x, polyline.points[i].y)
        }
        fillType = ctx.fillRule
    }
    drawSvgPath(ctx, path)
}

internal fun DrawScope.drawSvgPolygon(ctx: DrawContext, polygon: SvgPolygon) {
    if (polygon.points.size < 2) return
    val path = Path().apply {
        moveTo(polygon.points[0].x, polygon.points[0].y)
        for (i in 1 until polygon.points.size) {
            lineTo(polygon.points[i].x, polygon.points[i].y)
        }
        close()
        fillType = ctx.fillRule
    }
    drawSvgPath(ctx, path)
}

internal fun DrawScope.drawSvgText(ctx: DrawContext, text: SvgText, textMeasurer: TextMeasurer?) {
    if (textMeasurer == null) return
    val color = ctx.fillColor ?: ctx.strokeColor
    val style = TextStyle(
        color = color,
        fontSize = text.fontSize?.sp ?: 16.sp,
        fontWeight = when (text.fontWeight) {
            "bold" -> FontWeight.Bold
            "normal" -> FontWeight.Normal
            else -> FontWeight.Normal
        },
        fontFamily = text.fontFamily?.let { FontFamily.Default } ?: FontFamily.Default
    )

    val textLayoutResult = textMeasurer.measure(text.text, style)
    
    val xOffset = when (text.textAnchor) {
        TextAnchor.START -> 0f
        TextAnchor.MIDDLE -> -textLayoutResult.size.width / 2f
        TextAnchor.END -> -textLayoutResult.size.width.toFloat()
        null -> 0f
    }
    
    val yOffset = when (text.dominantBaseline) {
        DominantBaseline.MIDDLE, DominantBaseline.CENTRAL -> -textLayoutResult.size.height / 2f
        else -> -textLayoutResult.firstBaseline
    }

    drawText(textMeasurer, text.text, Offset(text.x + xOffset + (text.dx ?: 0f), text.y + yOffset + (text.dy ?: 0f)), style)
}

private fun DrawScope.drawSvgUse(
    context: SvgDrawingContext,
    use: SvgUse
) {
    val symbol = context.registry.symbols[use.href.removePrefix("#")] ?: return
    translate(use.x, use.y) {
        symbol.children.forEach { drawSvgElement(context, it) }
    }
}

internal fun buildClipPath(clipPath: SvgClipPath, ctx: DrawContext): Path {
    val path = Path()
    clipPath.children.forEach { element ->
        when (element) {
            is SvgPath -> path.addPath(element.commands.toPath())
            is SvgCircle -> path.addOval(androidx.compose.ui.geometry.Rect(element.cx - element.r, element.cy - element.r, element.cx + element.r, element.cy + element.r))
            is SvgEllipse -> path.addOval(androidx.compose.ui.geometry.Rect(element.cx - element.rx, element.cy - element.ry, element.cx + element.rx, element.cy + element.ry))
            is SvgRect -> path.addRoundRect(androidx.compose.ui.geometry.RoundRect(element.x, element.y, element.x + element.width, element.y + element.height, CornerRadius(element.rx, element.ry)))
            is SvgPolygon -> {
                if (element.points.isNotEmpty()) {
                    path.moveTo(element.points[0].x, element.points[0].y)
                    for (i in 1 until element.points.size) {
                        path.lineTo(element.points[i].x, element.points[i].y)
                    }
                    path.close()
                }
            }
            else -> {}
        }
    }
    return path
}

internal fun DrawScope.withTransform(transform: SvgTransform, block: DrawScope.() -> Unit) {
    when (transform) {
        is SvgTransform.Translate -> translate(transform.x, transform.y) { block() }
        is SvgTransform.Scale -> scale(transform.sx, transform.sy, pivot = Offset.Zero) { block() }
        is SvgTransform.Rotate -> rotate(transform.angle, pivot = Offset(transform.cx, transform.cy)) { block() }
        is SvgTransform.SkewX -> {
            val matrix = Matrix()
            matrix.values[Matrix.SkewX] = tan(transform.angle * PI.toFloat() / 180f)
            withTransform({ this.transform(matrix) }) { block() }
        }
        is SvgTransform.SkewY -> {
            val matrix = Matrix()
            matrix.values[Matrix.SkewY] = tan(transform.angle * PI.toFloat() / 180f)
            withTransform({ this.transform(matrix) }) { block() }
        }
        is SvgTransform.Matrix -> {
            val matrix = Matrix()
            matrix.values[Matrix.ScaleX] = transform.a
            matrix.values[Matrix.SkewY] = transform.b
            matrix.values[Matrix.SkewX] = transform.c
            matrix.values[Matrix.ScaleY] = transform.d
            matrix.values[Matrix.TranslateX] = transform.e
            matrix.values[Matrix.TranslateY] = transform.f
            withTransform({ this.transform(matrix) }) { block() }
        }
        is SvgTransform.Combined -> {
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

internal fun DrawContext.getEffectiveStroke(): Stroke {
    return if (vectorEffect == VectorEffect.NON_SCALING_STROKE) {
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

internal fun DrawScope.drawSvgPath(ctx: DrawContext, path: Path) {
    if (ctx.fillColor != null) {
        drawPath(path, ctx.fillColor, style = Fill)
    }
    if (ctx.hasStroke) {
        drawPath(path, ctx.strokeColor, style = ctx.getEffectiveStroke())
    }
}

internal fun DrawScope.drawSvgCircle(ctx: DrawContext, radius: Float, center: Offset) {
    if (ctx.fillColor != null) {
        drawCircle(ctx.fillColor, radius, center, style = Fill)
    }
    if (ctx.hasStroke) {
        drawCircle(ctx.strokeColor, radius, center, style = ctx.getEffectiveStroke())
    }
}

internal fun DrawScope.drawSvgEllipse(ctx: DrawContext, topLeft: Offset, size: Size) {
    if (ctx.fillColor != null) {
        drawOval(ctx.fillColor, topLeft, size, style = Fill)
    }
    if (ctx.hasStroke) {
        drawOval(ctx.strokeColor, topLeft, size, style = ctx.getEffectiveStroke())
    }
}

internal fun DrawScope.drawSvgRect(ctx: DrawContext, topLeft: Offset, size: Size, rx: Float, ry: Float) {
    if (rx == 0f && ry == 0f) {
        if (ctx.fillColor != null) {
            drawRect(ctx.fillColor, topLeft, size, style = Fill)
        }
        if (ctx.hasStroke) {
            drawRect(ctx.strokeColor, topLeft, size, style = ctx.getEffectiveStroke())
        }
    } else {
        val cornerRadius = CornerRadius(rx, ry)
        if (ctx.fillColor != null) {
            drawRoundRect(ctx.fillColor, topLeft, size, cornerRadius, style = Fill)
        }
        if (ctx.hasStroke) {
            drawRoundRect(ctx.strokeColor, topLeft, size, cornerRadius, style = ctx.getEffectiveStroke())
        }
    }
}
