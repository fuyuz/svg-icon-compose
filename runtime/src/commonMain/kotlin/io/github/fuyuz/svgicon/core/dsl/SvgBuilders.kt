package io.github.fuyuz.svgicon.core.dsl

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.fuyuz.svgicon.core.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration

/**
 * DSL marker for SVG builders.
 */
@DslMarker
annotation class SvgDslMarker

/**
 * Builder for SvgStyle with DSL support.
 */
@SvgDslMarker
class SvgStyleBuilder {
    var fill: Color? = null
    var fillOpacity: Float? = null
    var fillRule: FillRule? = null
    var stroke: Color? = null
    var strokeWidth: Float? = null
    var strokeOpacity: Float? = null
    var strokeLinecap: LineCap? = null
    var strokeLinejoin: LineJoin? = null
    var strokeDasharray: List<Float>? = null
    var strokeDashoffset: Float? = null
    var strokeMiterlimit: Float? = null
    var opacity: Float? = null
    var transform: SvgTransform? = null
    var paintOrder: PaintOrder? = null
    var vectorEffect: VectorEffect? = null
    var clipPathId: String? = null
    var maskId: String? = null
    var markerStart: String? = null
    var markerMid: String? = null
    var markerEnd: String? = null

    fun build(): SvgStyle = SvgStyle(
        fill = fill,
        fillOpacity = fillOpacity,
        fillRule = fillRule,
        stroke = stroke,
        strokeWidth = strokeWidth,
        strokeOpacity = strokeOpacity,
        strokeLinecap = strokeLinecap,
        strokeLinejoin = strokeLinejoin,
        strokeDasharray = strokeDasharray,
        strokeDashoffset = strokeDashoffset,
        strokeMiterlimit = strokeMiterlimit,
        opacity = opacity,
        transform = transform,
        paintOrder = paintOrder,
        vectorEffect = vectorEffect,
        clipPathId = clipPathId,
        maskId = maskId,
        markerStart = markerStart,
        markerMid = markerMid,
        markerEnd = markerEnd
    )

    fun isEmpty(): Boolean =
        fill == null && fillOpacity == null && fillRule == null &&
        stroke == null && strokeWidth == null && strokeOpacity == null &&
        strokeLinecap == null && strokeLinejoin == null &&
        strokeDasharray == null && strokeDashoffset == null &&
        strokeMiterlimit == null && opacity == null && transform == null &&
        paintOrder == null && vectorEffect == null &&
        clipPathId == null && maskId == null &&
        markerStart == null && markerMid == null && markerEnd == null
}

/**
 * DSL function to build SvgStyle.
 */
inline fun svgStyle(block: SvgStyleBuilder.() -> Unit): SvgStyle =
    SvgStyleBuilder().apply(block).build()

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

    // ============================================
    // Helper Functions
    // ============================================

    /**
     * Draw a circle using cubic bezier curves.
     * Uses the mathematical approximation: control point distance = radius * 0.5522847498
     */
    fun circle(cx: Float, cy: Float, radius: Float) {
        val k = 0.5522847498f * radius
        moveTo(cx + radius, cy)
        cubicTo(cx + radius, cy + k, cx + k, cy + radius, cx, cy + radius)
        cubicTo(cx - k, cy + radius, cx - radius, cy + k, cx - radius, cy)
        cubicTo(cx - radius, cy - k, cx - k, cy - radius, cx, cy - radius)
        cubicTo(cx + k, cy - radius, cx + radius, cy - k, cx + radius, cy)
        close()
    }

    /**
     * Draw a rectangle, optionally with rounded corners.
     */
    fun rect(x: Float, y: Float, width: Float, height: Float, rx: Float = 0f, ry: Float = rx) {
        if (rx <= 0 && ry <= 0) {
            moveTo(x, y)
            lineTo(x + width, y)
            lineTo(x + width, y + height)
            lineTo(x, y + height)
            close()
        } else {
            val cornerRx = rx.coerceAtMost(width / 2)
            val cornerRy = ry.coerceAtMost(height / 2)
            moveTo(x + cornerRx, y)
            lineTo(x + width - cornerRx, y)
            arcTo(cornerRx, cornerRy, 0f, false, true, x + width, y + cornerRy)
            lineTo(x + width, y + height - cornerRy)
            arcTo(cornerRx, cornerRy, 0f, false, true, x + width - cornerRx, y + height)
            lineTo(x + cornerRx, y + height)
            arcTo(cornerRx, cornerRy, 0f, false, true, x, y + height - cornerRy)
            lineTo(x, y + cornerRy)
            arcTo(cornerRx, cornerRy, 0f, false, true, x + cornerRx, y)
            close()
        }
    }

    /**
     * Draw a star shape.
     */
    fun star(cx: Float, cy: Float, points: Int, outerRadius: Float, innerRadius: Float, rotation: Float = -90f) {
        require(points >= 3) { "Star must have at least 3 points" }
        val angleStep = PI.toFloat() / points
        val startAngle = rotation * PI.toFloat() / 180f

        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = startAngle + i * angleStep
            val px = cx + radius * cos(angle)
            val py = cy + radius * sin(angle)
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    /**
     * Draw an ellipse using cubic bezier curves.
     */
    fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float) {
        val kx = 0.5522847498f * rx
        val ky = 0.5522847498f * ry
        moveTo(cx + rx, cy)
        cubicTo(cx + rx, cy + ky, cx + kx, cy + ry, cx, cy + ry)
        cubicTo(cx - kx, cy + ry, cx - rx, cy + ky, cx - rx, cy)
        cubicTo(cx - rx, cy - ky, cx - kx, cy - ry, cx, cy - ry)
        cubicTo(cx + kx, cy - ry, cx + rx, cy - ky, cx + rx, cy)
        close()
    }

    /**
     * Draw an arc (partial circle/ellipse).
     */
    fun arc(cx: Float, cy: Float, radius: Float, startAngle: Float, sweepAngle: Float) {
        val startRad = startAngle * PI.toFloat() / 180f
        val endRad = (startAngle + sweepAngle) * PI.toFloat() / 180f
        val startX = cx + radius * cos(startRad)
        val startY = cy + radius * sin(startRad)
        val endX = cx + radius * cos(endRad)
        val endY = cy + radius * sin(endRad)
        val largeArc = kotlin.math.abs(sweepAngle) > 180f
        val sweep = sweepAngle > 0

        moveTo(startX, startY)
        arcTo(radius, radius, 0f, largeArc, sweep, endX, endY)
    }

    /**
     * Draw a regular polygon.
     */
    fun regularPolygon(cx: Float, cy: Float, radius: Float, sides: Int, rotation: Float = -90f) {
        require(sides >= 3) { "Polygon must have at least 3 sides" }
        val angleStep = 2 * PI.toFloat() / sides
        val startAngle = rotation * PI.toFloat() / 180f

        for (i in 0 until sides) {
            val angle = startAngle + i * angleStep
            val px = cx + radius * cos(angle)
            val py = cy + radius * sin(angle)
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    fun build(): List<PathCommand> = commands.toList()
}

@SvgDslMarker
class SvgBuilder {
    @PublishedApi
    internal val elements = mutableListOf<SvgElement>()

    // ============================================
    // Style Scope
    // ============================================

    /**
     * Apply styles to all nested elements.
     *
     * Example:
     * ```kotlin
     * svg {
     *     withStyle(stroke = Color.Blue, strokeWidth = 2f) {
     *         path("M10 10 L20 20")
     *         circle(12, 12, 5)
     *     }
     * }
     * ```
     */
    fun withStyle(
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null,
        fillOpacity: Float? = null,
        strokeOpacity: Float? = null,
        fillRule: FillRule? = null,
        transform: SvgTransform? = null,
        clipPathId: String? = null,
        maskId: String? = null,
        block: SvgBuilder.() -> Unit
    ) {
        val children = SvgBuilder().apply(block).build()
        val style = SvgStyle(
            stroke = stroke,
            fill = fill,
            strokeWidth = strokeWidth,
            opacity = opacity,
            strokeLinecap = strokeLinecap,
            strokeLinejoin = strokeLinejoin,
            strokeDasharray = strokeDasharray,
            strokeDashoffset = strokeDashoffset,
            fillOpacity = fillOpacity,
            strokeOpacity = strokeOpacity,
            fillRule = fillRule,
            transform = transform,
            clipPathId = clipPathId,
            maskId = maskId
        )
        elements.add(SvgGroup(children, style))
    }

    /**
     * Apply styles using builder syntax.
     *
     * Example:
     * ```kotlin
     * svg {
     *     styled({
     *         stroke = Color.Blue
     *         strokeWidth = 2f
     *     }) {
     *         path("M10 10 L20 20")
     *         circle(12, 12, 5)
     *     }
     * }
     * ```
     */
    fun styled(styleBlock: SvgStyleBuilder.() -> Unit, contentBlock: SvgBuilder.() -> Unit) {
        val style = SvgStyleBuilder().apply(styleBlock).build()
        val children = SvgBuilder().apply(contentBlock).build()
        elements.add(SvgGroup(children, style))
    }

    // ============================================
    // Infix Styled Functions
    // ============================================

    /**
     * Add an element directly to the builder.
     *
     * Example:
     * ```kotlin
     * svg {
     *     +SvgCircle(12f, 12f, 10f)
     * }
     * ```
     */
    operator fun SvgElement.unaryPlus() {
        elements.add(this)
    }

    /**
     * Apply style to an element using infix notation.
     * The element is replaced with a styled version.
     *
     * Example:
     * ```kotlin
     * svg {
     *     rect(4, 4, 16, 16) styled { fill = Color.Blue }
     *     circle(12, 12, 10) styled {
     *         stroke = Color.Red
     *         strokeWidth = 2f
     *     }
     * }
     * ```
     */
    infix fun SvgElement.styled(block: SvgStyleBuilder.() -> Unit): SvgStyled {
        // Remove the last added element (always the one just created)
        elements.removeAt(elements.lastIndex)
        val styledElement = SvgStyled(this, svgStyle(block))
        elements.add(styledElement)
        return styledElement
    }

    /**
     * Apply animation to an element using infix notation.
     * The element is replaced with an animated version.
     *
     * Example:
     * ```kotlin
     * svg {
     *     circle(12, 12, 10) animated { strokeDraw(dur = 1.seconds) }
     *     path("M8 12l3 3 5-6") animated {
     *         strokeDraw(dur = 500.milliseconds, delay = 1.seconds)
     *     }
     * }
     * ```
     */
    inline infix fun <reified E : SvgElement> E.animated(block: AnimationBuilder<E>.() -> Unit): SvgAnimated {
        // Remove the last added element (always the one just created)
        elements.removeAt(elements.lastIndex)
        val animations = AnimationBuilder(this).apply(block).build()
        val animatedElement = SvgAnimated(this, animations)
        elements.add(animatedElement)
        return animatedElement
    }

    /**
     * Apply predefined animations to an element using infix notation.
     * The element is replaced with an animated version.
     *
     * Example:
     * ```kotlin
     * svg {
     *     circle(12, 12, 10) with Animations.fadeIn
     *     path("M8 12l3 3 5-6") with Animations.strokeDraw()
     * }
     * ```
     */
    infix fun SvgElement.with(animations: List<SvgAnimate>): SvgAnimated {
        // Remove the last added element (always the one just created)
        elements.removeAt(elements.lastIndex)
        val animatedElement = SvgAnimated(this, animations)
        elements.add(animatedElement)
        return animatedElement
    }

    // ============================================
    // Path
    // ============================================

    /** Path from string data. */
    fun path(d: String): SvgPath {
        val elem = SvgPath(d)
        elements.add(elem)
        return elem
    }

    /** Path from string with animation. */
    inline fun path(d: String, block: AnimationBuilder<SvgPath>.() -> Unit): SvgAnimated {
        val path = SvgPath(d)
        val animations = AnimationBuilder(path).apply(block).build()
        val elem = SvgAnimated(path, animations)
        elements.add(elem)
        return elem
    }

    /** Path from string with style. */
    fun path(d: String, style: SvgStyle): SvgStyled {
        val elem = SvgStyled(SvgPath(d), style)
        elements.add(elem)
        return elem
    }

    /** Type-safe path builder. */
    fun path(block: PathBuilder.() -> Unit): SvgPath {
        val commands = PathBuilder().apply(block).build()
        val elem = SvgPath(commands = commands)
        elements.add(elem)
        return elem
    }

    /** Type-safe path with animation builder. */
    inline fun animatedPath(pathBlock: PathBuilder.() -> Unit, animBlock: AnimationBuilder<SvgPath>.() -> Unit): SvgAnimated {
        val commands = PathBuilder().apply(pathBlock).build()
        val path = SvgPath(commands = commands)
        val animations = AnimationBuilder(path).apply(animBlock).build()
        val elem = SvgAnimated(path, animations)
        elements.add(elem)
        return elem
    }

    // ============================================
    // Circle
    // ============================================

    fun circle(cx: Number, cy: Number, r: Number): SvgCircle {
        val elem = SvgCircle(cx.toFloat(), cy.toFloat(), r.toFloat())
        elements.add(elem)
        return elem
    }

    inline fun circle(cx: Number, cy: Number, r: Number, block: AnimationBuilder<SvgCircle>.() -> Unit): SvgAnimated {
        val circle = SvgCircle(cx.toFloat(), cy.toFloat(), r.toFloat())
        val animations = AnimationBuilder(circle).apply(block).build()
        val elem = SvgAnimated(circle, animations)
        elements.add(elem)
        return elem
    }

    fun circle(cx: Number, cy: Number, r: Number, style: SvgStyle): SvgStyled {
        val elem = SvgStyled(SvgCircle(cx.toFloat(), cy.toFloat(), r.toFloat()), style)
        elements.add(elem)
        return elem
    }

    // ============================================
    // Ellipse
    // ============================================

    fun ellipse(cx: Number, cy: Number, rx: Number, ry: Number): SvgEllipse {
        val elem = SvgEllipse(cx.toFloat(), cy.toFloat(), rx.toFloat(), ry.toFloat())
        elements.add(elem)
        return elem
    }

    inline fun ellipse(cx: Number, cy: Number, rx: Number, ry: Number, block: AnimationBuilder<SvgEllipse>.() -> Unit): SvgAnimated {
        val ellipse = SvgEllipse(cx.toFloat(), cy.toFloat(), rx.toFloat(), ry.toFloat())
        val animations = AnimationBuilder(ellipse).apply(block).build()
        val elem = SvgAnimated(ellipse, animations)
        elements.add(elem)
        return elem
    }

    fun ellipse(cx: Number, cy: Number, rx: Number, ry: Number, style: SvgStyle): SvgStyled {
        val elem = SvgStyled(SvgEllipse(cx.toFloat(), cy.toFloat(), rx.toFloat(), ry.toFloat()), style)
        elements.add(elem)
        return elem
    }

    // ============================================
    // Rectangle
    // ============================================

    fun rect(x: Number = 0, y: Number = 0, width: Number, height: Number, rx: Number = 0, ry: Number = rx): SvgRect {
        val elem = SvgRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), rx.toFloat(), ry.toFloat())
        elements.add(elem)
        return elem
    }

    inline fun rect(x: Number, y: Number, width: Number, height: Number, rx: Number = 0, ry: Number = rx, block: AnimationBuilder<SvgRect>.() -> Unit): SvgAnimated {
        val rect = SvgRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), rx.toFloat(), ry.toFloat())
        val animations = AnimationBuilder(rect).apply(block).build()
        val elem = SvgAnimated(rect, animations)
        elements.add(elem)
        return elem
    }

    fun rect(x: Number, y: Number, width: Number, height: Number, rx: Number = 0, ry: Number = rx, style: SvgStyle): SvgStyled {
        val elem = SvgStyled(SvgRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), rx.toFloat(), ry.toFloat()), style)
        elements.add(elem)
        return elem
    }

    // ============================================
    // Line
    // ============================================

    fun line(x1: Number, y1: Number, x2: Number, y2: Number): SvgLine {
        val elem = SvgLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
        elements.add(elem)
        return elem
    }

    inline fun line(x1: Number, y1: Number, x2: Number, y2: Number, block: AnimationBuilder<SvgLine>.() -> Unit): SvgAnimated {
        val line = SvgLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
        val animations = AnimationBuilder(line).apply(block).build()
        val elem = SvgAnimated(line, animations)
        elements.add(elem)
        return elem
    }

    fun line(x1: Number, y1: Number, x2: Number, y2: Number, style: SvgStyle): SvgStyled {
        val elem = SvgStyled(SvgLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat()), style)
        elements.add(elem)
        return elem
    }

    // ============================================
    // Text
    // ============================================

    fun text(
        text: String,
        x: Number = 0,
        y: Number = 0,
        textAnchor: TextAnchor? = null,
        dominantBaseline: DominantBaseline? = null,
        fontSize: Float? = null,
        fontFamily: String? = null,
        fontWeight: String? = null,
        letterSpacing: Float? = null,
        dx: Float? = null,
        dy: Float? = null
    ): SvgText {
        val elem = SvgText(
            text = text,
            x = x.toFloat(),
            y = y.toFloat(),
            textAnchor = textAnchor,
            dominantBaseline = dominantBaseline,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            dx = dx,
            dy = dy
        )
        elements.add(elem)
        return elem
    }

    inline fun text(
        text: String,
        x: Number = 0,
        y: Number = 0,
        textAnchor: TextAnchor? = null,
        dominantBaseline: DominantBaseline? = null,
        fontSize: Float? = null,
        block: AnimationBuilder<SvgText>.() -> Unit
    ): SvgAnimated {
        val textElement = SvgText(
            text = text,
            x = x.toFloat(),
            y = y.toFloat(),
            textAnchor = textAnchor,
            dominantBaseline = dominantBaseline,
            fontSize = fontSize
        )
        val animations = AnimationBuilder(textElement).apply(block).build()
        val elem = SvgAnimated(textElement, animations)
        elements.add(elem)
        return elem
    }

    // ============================================
    // Polyline
    // ============================================

    /** Polyline with vararg points: polyline(5 to 12, 12 to 5, 19 to 12) */
    fun polyline(vararg points: Pair<Number, Number>): SvgPolyline {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        val elem = SvgPolyline(offsets)
        elements.add(elem)
        return elem
    }

    /** Polyline from points string: polyline("5,12 12,5 19,12") */
    fun polyline(points: String): SvgPolyline {
        val elem = SvgPolyline(parsePointsString(points))
        elements.add(elem)
        return elem
    }

    inline fun polyline(vararg points: Pair<Number, Number>, block: AnimationBuilder<SvgPolyline>.() -> Unit): SvgAnimated {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        val polyline = SvgPolyline(offsets)
        val animations = AnimationBuilder(polyline).apply(block).build()
        val elem = SvgAnimated(polyline, animations)
        elements.add(elem)
        return elem
    }

    inline fun polyline(points: String, block: AnimationBuilder<SvgPolyline>.() -> Unit): SvgAnimated {
        val polyline = SvgPolyline(parsePointsString(points))
        val animations = AnimationBuilder(polyline).apply(block).build()
        val elem = SvgAnimated(polyline, animations)
        elements.add(elem)
        return elem
    }

    // ============================================
    // Polygon
    // ============================================

    /** Polygon with vararg points: polygon(12 to 2, 22 to 22, 2 to 22) */
    fun polygon(vararg points: Pair<Number, Number>): SvgPolygon {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        val elem = SvgPolygon(offsets)
        elements.add(elem)
        return elem
    }

    /** Polygon from points string: polygon("12,2 22,22 2,22") */
    fun polygon(points: String): SvgPolygon {
        val elem = SvgPolygon(parsePointsString(points))
        elements.add(elem)
        return elem
    }

    inline fun polygon(vararg points: Pair<Number, Number>, block: AnimationBuilder<SvgPolygon>.() -> Unit): SvgAnimated {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        val polygon = SvgPolygon(offsets)
        val animations = AnimationBuilder(polygon).apply(block).build()
        val elem = SvgAnimated(polygon, animations)
        elements.add(elem)
        return elem
    }

    inline fun polygon(points: String, block: AnimationBuilder<SvgPolygon>.() -> Unit): SvgAnimated {
        val polygon = SvgPolygon(parsePointsString(points))
        val animations = AnimationBuilder(polygon).apply(block).build()
        val elem = SvgAnimated(polygon, animations)
        elements.add(elem)
        return elem
    }

    // ============================================
    // Group
    // ============================================

    /** Group elements without style. */
    fun group(block: SvgBuilder.() -> Unit): SvgGroup {
        val children = SvgBuilder().apply(block).build()
        val elem = SvgGroup(children)
        elements.add(elem)
        return elem
    }

    /** Group elements with transform. */
    fun group(transform: SvgTransform, block: SvgBuilder.() -> Unit): SvgGroup {
        val children = SvgBuilder().apply(block).build()
        val elem = SvgGroup(children, SvgStyle(transform = transform))
        elements.add(elem)
        return elem
    }

    // ============================================
    // Definitions
    // ============================================

    fun clipPath(
        id: String,
        clipPathUnits: ClipPathUnits = ClipPathUnits.USER_SPACE_ON_USE,
        block: SvgBuilder.() -> Unit
    ) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgClipPath(id, children, clipPathUnits))
    }

    fun mask(
        id: String,
        maskUnits: MaskUnits = MaskUnits.OBJECT_BOUNDING_BOX,
        maskContentUnits: MaskUnits = MaskUnits.USER_SPACE_ON_USE,
        block: SvgBuilder.() -> Unit
    ) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgMask(id, children, maskUnits, maskContentUnits))
    }

    fun defs(block: SvgBuilder.() -> Unit) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgDefs(children))
    }

    // ============================================
    // Marker
    // ============================================

    /**
     * Define a marker (for arrowheads, dots, etc.)
     *
     * Example:
     * ```kotlin
     * svg {
     *     defs {
     *         marker(id = "arrow", refX = 5, refY = 5, viewBox = ViewBox.square(10)) {
     *             path("M0 0L10 5L0 10z")
     *         }
     *     }
     *     line(0, 12, 20, 12, svgStyle { markerEnd = "url(#arrow)" })
     * }
     * ```
     */
    fun marker(
        id: String,
        viewBox: ViewBox? = null,
        refX: Float = 0f,
        refY: Float = 0f,
        markerWidth: Float = 3f,
        markerHeight: Float = 3f,
        orient: MarkerOrient = MarkerOrient.Auto,
        block: SvgBuilder.() -> Unit
    ) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgMarker(id, viewBox, refX, refY, markerWidth, markerHeight, orient, children))
    }

    // ============================================
    // Symbol and Use
    // ============================================

    /**
     * Define a reusable symbol.
     *
     * Example:
     * ```kotlin
     * svg {
     *     defs {
     *         symbol(id = "checkIcon") {
     *             circle(12, 12, 10)
     *             path("M8 12l3 3 5-6")
     *         }
     *     }
     *     use(href = "#checkIcon", x = 0, y = 0)
     *     use(href = "#checkIcon", x = 30, y = 0)
     * }
     * ```
     */
    fun symbol(id: String, viewBox: ViewBox? = null, block: SvgBuilder.() -> Unit) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgSymbol(id, viewBox, children))
    }

    /** Reference and instantiate a symbol or other element. */
    fun use(href: String, x: Number = 0, y: Number = 0, width: Number? = null, height: Number? = null) {
        elements.add(SvgUse(href, x.toFloat(), y.toFloat(), width?.toFloat(), height?.toFloat()))
    }

    // ============================================
    // Pattern
    // ============================================

    /**
     * Define a repeating pattern.
     *
     * Example:
     * ```kotlin
     * svg {
     *     defs {
     *         pattern(id = "dots", width = 10, height = 10) {
     *             circle(5, 5, 2)
     *         }
     *     }
     *     rect(0, 0, 100, 100, svgStyle { fill = "url(#dots)".toPatternFill() })
     * }
     * ```
     */
    fun pattern(
        id: String,
        width: Number,
        height: Number,
        patternUnits: PatternUnits = PatternUnits.USER_SPACE_ON_USE,
        patternContentUnits: PatternUnits = PatternUnits.USER_SPACE_ON_USE,
        patternTransform: SvgTransform? = null,
        block: SvgBuilder.() -> Unit
    ) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgPattern(id, width.toFloat(), height.toFloat(), patternUnits, patternContentUnits, patternTransform, children))
    }

    // ============================================
    // Gradients
    // ============================================

    fun linearGradient(
        id: String,
        x1: Float = 0f,
        y1: Float = 0f,
        x2: Float = 1f,
        y2: Float = 0f,
        gradientUnits: GradientUnits = GradientUnits.OBJECT_BOUNDING_BOX,
        spreadMethod: SpreadMethod = SpreadMethod.PAD,
        gradientTransform: SvgTransform? = null,
        block: GradientBuilder.() -> Unit
    ) {
        val stops = GradientBuilder().apply(block).build()
        elements.add(SvgLinearGradient(id, x1, y1, x2, y2, stops, gradientUnits, spreadMethod, gradientTransform))
    }

    fun radialGradient(
        id: String,
        cx: Float = 0.5f,
        cy: Float = 0.5f,
        r: Float = 0.5f,
        fx: Float? = null,
        fy: Float? = null,
        gradientUnits: GradientUnits = GradientUnits.OBJECT_BOUNDING_BOX,
        spreadMethod: SpreadMethod = SpreadMethod.PAD,
        gradientTransform: SvgTransform? = null,
        block: GradientBuilder.() -> Unit
    ) {
        val stops = GradientBuilder().apply(block).build()
        elements.add(SvgRadialGradient(id, cx, cy, r, fx, fy, stops, gradientUnits, spreadMethod, gradientTransform))
    }

    // ============================================
    // Animated Shortcuts
    // ============================================

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

    inline fun <reified E : SvgElement> animated(element: E, block: AnimationBuilder<E>.() -> Unit) {
        val animations = AnimationBuilder(element).apply(block).build()
        elements.add(SvgAnimated(element, animations))
    }

    // ============================================
    // Utilities
    // ============================================

    @PublishedApi
    internal fun parsePointsString(points: String): List<Offset> {
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

    fun build(): List<SvgElement> = elements.toList()
}

/**
 * Builder for gradient stops.
 */
@SvgDslMarker
class GradientBuilder {
    private val stops = mutableListOf<GradientStop>()

    fun stop(offset: Float, color: Color, opacity: Float = 1f) {
        stops.add(GradientStop(offset, color, opacity))
    }

    fun start(color: Color, opacity: Float = 1f) = stop(0f, color, opacity)
    fun end(color: Color, opacity: Float = 1f) = stop(1f, color, opacity)
    fun middle(color: Color, opacity: Float = 1f) = stop(0.5f, color, opacity)

    fun build(): List<GradientStop> = stops.toList()
}

/**
 * Builder for animations with phantom type for type-safe element-specific animations.
 *
 * @param E The element type being animated (phantom type for compile-time safety)
 * @param element The element being animated
 */
@SvgDslMarker
class AnimationBuilder<E : SvgElement> @PublishedApi internal constructor(
    @PublishedApi internal val element: E
) {
    @PublishedApi
    internal val animations = mutableListOf<SvgAnimate>()

    fun strokeDraw(
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        reverse: Boolean = false,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.StrokeDraw(dur, delay, reverse, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun opacity(
        from: Float = 0f,
        to: Float = 1f,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Opacity(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun translate(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Transform(TransformType.TRANSLATE, from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun translateX(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Transform(TransformType.TRANSLATE_X, from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun translateY(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Transform(TransformType.TRANSLATE_Y, from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun scale(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Transform(TransformType.SCALE, from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun scaleX(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Transform(TransformType.SCALE_X, from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun scaleY(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Transform(TransformType.SCALE_Y, from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun rotate(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Transform(TransformType.ROTATE, from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun skewX(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Transform(TransformType.SKEW_X, from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun skewY(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Transform(TransformType.SKEW_Y, from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun motion(
        path: String,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        rotate: MotionRotate = MotionRotate.NONE,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Motion(path, dur, delay, rotate, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun strokeWidth(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.StrokeWidth(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun strokeOpacity(
        from: Float = 0f,
        to: Float = 1f,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.StrokeOpacity(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun strokeDasharray(
        from: List<Float>,
        to: List<Float>,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.StrokeDasharray(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun strokeDashoffset(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.StrokeDashoffset(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun fillOpacity(
        from: Float = 0f,
        to: Float = 1f,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.FillOpacity(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun cx(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Cx(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun cy(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Cy(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun r(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.R(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun rx(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Rx(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun ry(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Ry(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun x(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.X(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun y(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Y(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun width(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Width(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun height(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Height(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun x1(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.X1(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun y1(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Y1(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun x2(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.X2(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun y2(
        from: Float,
        to: Float,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Y2(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    /** Path data animation with explicit from/to values. */
    fun d(
        from: String,
        to: String,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.D(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun points(
        from: List<Offset>,
        to: List<Offset>,
        dur: Duration = DefaultAnimationDuration,
        delay: Duration = Duration.ZERO,
        calcMode: CalcMode = CalcMode.LINEAR,
        keySplines: KeySplines? = null,
        iterations: Int = SvgAnimate.INFINITE,
        direction: AnimationDirection = AnimationDirection.NORMAL,
        fillMode: AnimationFillMode = AnimationFillMode.NONE
    ) {
        animations.add(SvgAnimate.Points(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
    }

    fun build(): List<SvgAnimate> = animations.toList()
}

// ============================================
// Type-Safe Animation Extensions
// ============================================

/**
 * Path morphing animation using the current path data as the starting point.
 * Only available for SvgPath elements.
 *
 * Example:
 * ```kotlin
 * path("M10 10 L20 20") animated { morphTo("M5 15 L25 15") }
 * ```
 */
fun AnimationBuilder<SvgPath>.morphTo(
    to: String,
    dur: Duration = DefaultAnimationDuration,
    delay: Duration = Duration.ZERO,
    calcMode: CalcMode = CalcMode.LINEAR,
    keySplines: KeySplines? = null,
    iterations: Int = SvgAnimate.INFINITE,
    direction: AnimationDirection = AnimationDirection.NORMAL,
    fillMode: AnimationFillMode = AnimationFillMode.NONE
) {
    val from = element.commands.toPathString()
    animations.add(SvgAnimate.D(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
}

/**
 * Path morphing animation using path builder for the target path.
 * Only available for SvgPath elements.
 *
 * Example:
 * ```kotlin
 * path("M10 10 L20 20") animated {
 *     morphTo { moveTo(5f, 15f); lineTo(25f, 15f) }
 * }
 * ```
 */
inline fun AnimationBuilder<SvgPath>.morphTo(
    dur: Duration = DefaultAnimationDuration,
    delay: Duration = Duration.ZERO,
    calcMode: CalcMode = CalcMode.LINEAR,
    keySplines: KeySplines? = null,
    iterations: Int = SvgAnimate.INFINITE,
    direction: AnimationDirection = AnimationDirection.NORMAL,
    fillMode: AnimationFillMode = AnimationFillMode.NONE,
    pathBlock: PathBuilder.() -> Unit
) {
    val from = element.commands.toPathString()
    val to = PathBuilder().apply(pathBlock).build().toPathString()
    animations.add(SvgAnimate.D(from, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
}

/**
 * Morph points animation using the current points as the starting point.
 * Only available for SvgPolygon elements.
 */
@kotlin.jvm.JvmName("morphPointsToPolygon")
fun AnimationBuilder<SvgPolygon>.morphPointsTo(
    to: List<Offset>,
    dur: Duration = DefaultAnimationDuration,
    delay: Duration = Duration.ZERO,
    calcMode: CalcMode = CalcMode.LINEAR,
    keySplines: KeySplines? = null,
    iterations: Int = SvgAnimate.INFINITE,
    direction: AnimationDirection = AnimationDirection.NORMAL,
    fillMode: AnimationFillMode = AnimationFillMode.NONE
) {
    animations.add(SvgAnimate.Points(element.points, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
}

/**
 * Morph points animation using the current points as the starting point.
 * Only available for SvgPolyline elements.
 */
@kotlin.jvm.JvmName("morphPointsToPolyline")
fun AnimationBuilder<SvgPolyline>.morphPointsTo(
    to: List<Offset>,
    dur: Duration = DefaultAnimationDuration,
    delay: Duration = Duration.ZERO,
    calcMode: CalcMode = CalcMode.LINEAR,
    keySplines: KeySplines? = null,
    iterations: Int = SvgAnimate.INFINITE,
    direction: AnimationDirection = AnimationDirection.NORMAL,
    fillMode: AnimationFillMode = AnimationFillMode.NONE
) {
    animations.add(SvgAnimate.Points(element.points, to, dur, delay, calcMode, keySplines, iterations, direction, fillMode))
}

// ============================================
// DSL Entry Points
// ============================================

/**
 * DSL entry point for building a complete Svg object with default attributes.
 *
 * Example:
 * ```kotlin
 * val icon = svg {
 *     path("M20 6L9 17l-5-5")
 *     circle(12, 12, 10)
 * }
 * ```
 */
inline fun svg(block: SvgBuilder.() -> Unit): Svg {
    return Svg(children = SvgBuilder().apply(block).build())
}

/**
 * DSL entry point for building a complete Svg object with customizable attributes.
 *
 * Example:
 * ```kotlin
 * val icon = svg(
 *     size = 48,
 *     strokeWidth = 3f,
 *     stroke = Color.Red
 * ) {
 *     path("M20 6L9 17l-5-5")
 *     circle(12, 12, 10)
 * }
 * ```
 */
inline fun svg(
    size: Number = 24,
    viewBox: ViewBox? = null,
    fill: Color? = null,
    stroke: Color? = Color.Unspecified,
    strokeWidth: Float = 2f,
    strokeLinecap: LineCap = LineCap.ROUND,
    strokeLinejoin: LineJoin = LineJoin.ROUND,
    preserveAspectRatio: PreserveAspectRatio = PreserveAspectRatio.Default,
    block: SvgBuilder.() -> Unit
): Svg {
    val s = size.toFloat()
    return Svg(
        width = s,
        height = s,
        viewBox = viewBox ?: ViewBox(0f, 0f, s, s),
        preserveAspectRatio = preserveAspectRatio,
        fill = fill,
        stroke = stroke,
        strokeWidth = strokeWidth,
        strokeLinecap = strokeLinecap,
        strokeLinejoin = strokeLinejoin,
        children = SvgBuilder().apply(block).build()
    )
}

/**
 * DSL entry point for building a complete Svg object with width and height.
 *
 * Example:
 * ```kotlin
 * val icon = svg(width = 100, height = 50) {
 *     rect(0, 0, 100, 50)
 * }
 * ```
 */
inline fun svg(
    width: Number,
    height: Number,
    viewBox: ViewBox? = null,
    fill: Color? = null,
    stroke: Color? = Color.Unspecified,
    strokeWidth: Float = 2f,
    strokeLinecap: LineCap = LineCap.ROUND,
    strokeLinejoin: LineJoin = LineJoin.ROUND,
    preserveAspectRatio: PreserveAspectRatio = PreserveAspectRatio.Default,
    block: SvgBuilder.() -> Unit
): Svg {
    val w = width.toFloat()
    val h = height.toFloat()
    return Svg(
        width = w,
        height = h,
        viewBox = viewBox ?: ViewBox(0f, 0f, w, h),
        preserveAspectRatio = preserveAspectRatio,
        fill = fill,
        stroke = stroke,
        strokeWidth = strokeWidth,
        strokeLinecap = strokeLinecap,
        strokeLinejoin = strokeLinejoin,
        children = SvgBuilder().apply(block).build()
    )
}
