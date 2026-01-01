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
        maskId = maskId
    )

    fun isEmpty(): Boolean =
        fill == null && fillOpacity == null && fillRule == null &&
        stroke == null && strokeWidth == null && strokeOpacity == null &&
        strokeLinecap == null && strokeLinejoin == null &&
        strokeDasharray == null && strokeDashoffset == null &&
        strokeMiterlimit == null && opacity == null && transform == null &&
        paintOrder == null && vectorEffect == null &&
        clipPathId == null && maskId == null
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
            // Simple rectangle
            moveTo(x, y)
            lineTo(x + width, y)
            lineTo(x + width, y + height)
            lineTo(x, y + height)
            close()
        } else {
            // Rounded rectangle
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
     * @param cx Center x coordinate
     * @param cy Center y coordinate
     * @param points Number of points (5 = classic star)
     * @param outerRadius Radius to outer points
     * @param innerRadius Radius to inner points
     * @param rotation Rotation angle in degrees (0 = first point at top)
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
            if (i == 0) {
                moveTo(px, py)
            } else {
                lineTo(px, py)
            }
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
            if (i == 0) {
                moveTo(px, py)
            } else {
                lineTo(px, py)
            }
        }
        close()
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
        maskId: String? = null
    ) {
        val hasStyle = stroke != null || fill != null || strokeWidth != null || opacity != null ||
            strokeLinecap != null || strokeLinejoin != null || strokeDasharray != null ||
            strokeDashoffset != null || fillOpacity != null || strokeOpacity != null ||
            fillRule != null || transform != null || clipPathId != null || maskId != null

        if (hasStyle) {
            elements.add(SvgStyled(element, SvgStyle(
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
            )))
        } else {
            elements.add(element)
        }
    }

    // ============================================
    // Path
    // ============================================

    /**
     * Path from string data.
     */
    fun path(
        d: String,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null
    ) {
        addWithStyle(SvgPath(d), stroke, fill, strokeWidth, opacity, strokeLinecap, strokeLinejoin, strokeDasharray, strokeDashoffset)
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
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null,
        block: PathBuilder.() -> Unit
    ) {
        val commands = PathBuilder().apply(block).build()
        addWithStyle(SvgPath(commands = commands), stroke, fill, strokeWidth, opacity, strokeLinecap, strokeLinejoin, strokeDasharray, strokeDashoffset)
    }

    /**
     * Type-safe path with animation builder.
     */
    fun animatedPath(pathBlock: PathBuilder.() -> Unit, animBlock: AnimationBuilder.() -> Unit) {
        val commands = PathBuilder().apply(pathBlock).build()
        val animations = AnimationBuilder().apply(animBlock).build()
        elements.add(SvgAnimated(SvgPath(commands = commands), animations))
    }

    // ============================================
    // Circle
    // ============================================

    fun circle(
        cx: Number,
        cy: Number,
        r: Number,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null
    ) {
        addWithStyle(SvgCircle(cx.toFloat(), cy.toFloat(), r.toFloat()), stroke, fill, strokeWidth, opacity, strokeLinecap, strokeLinejoin, strokeDasharray, strokeDashoffset)
    }

    fun circle(cx: Number, cy: Number, r: Number, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgCircle(cx.toFloat(), cy.toFloat(), r.toFloat()), animations))
    }

    // ============================================
    // Ellipse
    // ============================================

    fun ellipse(
        cx: Number,
        cy: Number,
        rx: Number,
        ry: Number,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null
    ) {
        addWithStyle(SvgEllipse(cx.toFloat(), cy.toFloat(), rx.toFloat(), ry.toFloat()), stroke, fill, strokeWidth, opacity, strokeLinecap, strokeLinejoin, strokeDasharray, strokeDashoffset)
    }

    fun ellipse(cx: Number, cy: Number, rx: Number, ry: Number, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgEllipse(cx.toFloat(), cy.toFloat(), rx.toFloat(), ry.toFloat()), animations))
    }

    // ============================================
    // Rectangle
    // ============================================

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
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null
    ) {
        addWithStyle(SvgRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), rx.toFloat(), ry.toFloat()), stroke, fill, strokeWidth, opacity, strokeLinecap, strokeLinejoin, strokeDasharray, strokeDashoffset)
    }

    fun rect(x: Number, y: Number, width: Number, height: Number, rx: Number = 0, ry: Number = rx, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), rx.toFloat(), ry.toFloat()), animations))
    }

    // ============================================
    // Line
    // ============================================

    fun line(
        x1: Number,
        y1: Number,
        x2: Number,
        y2: Number,
        stroke: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null
    ) {
        addWithStyle(SvgLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat()), stroke, null, strokeWidth, opacity, strokeLinecap, null, strokeDasharray, strokeDashoffset)
    }

    fun line(x1: Number, y1: Number, x2: Number, y2: Number, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat()), animations))
    }

    // ============================================
    // Text
    // ============================================

    /**
     * Text element with full options.
     */
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
        dy: Float? = null,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null
    ) {
        val textElement = SvgText(
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
        addWithStyle(textElement, stroke, fill, strokeWidth, opacity)
    }

    /**
     * Text element with animation.
     */
    fun text(
        text: String,
        x: Number = 0,
        y: Number = 0,
        textAnchor: TextAnchor? = null,
        dominantBaseline: DominantBaseline? = null,
        fontSize: Float? = null,
        block: AnimationBuilder.() -> Unit
    ) {
        val textElement = SvgText(
            text = text,
            x = x.toFloat(),
            y = y.toFloat(),
            textAnchor = textAnchor,
            dominantBaseline = dominantBaseline,
            fontSize = fontSize
        )
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(textElement, animations))
    }

    // ============================================
    // Polyline
    // ============================================

    /**
     * Polyline with vararg points: polyline(5 to 12, 12 to 5, 19 to 12)
     */
    fun polyline(
        vararg points: Pair<Number, Number>,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null
    ) {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        addWithStyle(SvgPolyline(offsets), stroke, fill, strokeWidth, opacity, strokeLinecap, strokeLinejoin, strokeDasharray, strokeDashoffset)
    }

    /**
     * Polyline from points string: polyline("5,12 12,5 19,12")
     */
    fun polyline(
        points: String,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null
    ) {
        addWithStyle(SvgPolyline(parsePointsString(points)), stroke, fill, strokeWidth, opacity, strokeLinecap, strokeLinejoin, strokeDasharray, strokeDashoffset)
    }

    fun polyline(vararg points: Pair<Number, Number>, block: AnimationBuilder.() -> Unit) {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgPolyline(offsets), animations))
    }

    fun polyline(points: String, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgPolyline(parsePointsString(points)), animations))
    }

    // ============================================
    // Polygon
    // ============================================

    /**
     * Polygon with vararg points: polygon(12 to 2, 22 to 22, 2 to 22)
     */
    fun polygon(
        vararg points: Pair<Number, Number>,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null
    ) {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        addWithStyle(SvgPolygon(offsets), stroke, fill, strokeWidth, opacity, strokeLinecap, strokeLinejoin, strokeDasharray, strokeDashoffset)
    }

    /**
     * Polygon from points string: polygon("12,2 22,22 2,22")
     */
    fun polygon(
        points: String,
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null
    ) {
        addWithStyle(SvgPolygon(parsePointsString(points)), stroke, fill, strokeWidth, opacity, strokeLinecap, strokeLinejoin, strokeDasharray, strokeDashoffset)
    }

    fun polygon(vararg points: Pair<Number, Number>, block: AnimationBuilder.() -> Unit) {
        val offsets = points.map { Offset(it.first.toFloat(), it.second.toFloat()) }
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgPolygon(offsets), animations))
    }

    fun polygon(points: String, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(SvgPolygon(parsePointsString(points)), animations))
    }

    // ============================================
    // Group
    // ============================================

    /**
     * Group without style.
     */
    fun group(block: SvgBuilder.() -> Unit) {
        val children = SvgBuilder().apply(block).build()
        elements.add(SvgGroup(children))
    }

    /**
     * Group with style that applies to all children.
     */
    fun group(
        stroke: Color? = null,
        fill: Color? = null,
        strokeWidth: Float? = null,
        opacity: Float? = null,
        strokeLinecap: LineCap? = null,
        strokeLinejoin: LineJoin? = null,
        strokeDasharray: List<Float>? = null,
        strokeDashoffset: Float? = null,
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
            transform = transform,
            clipPathId = clipPathId,
            maskId = maskId
        )
        val hasStyle = stroke != null || fill != null || strokeWidth != null || opacity != null ||
            strokeLinecap != null || strokeLinejoin != null || strokeDasharray != null ||
            strokeDashoffset != null || transform != null || clipPathId != null || maskId != null
        elements.add(SvgGroup(children, if (hasStyle) style else null))
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
    // Gradients
    // ============================================

    /**
     * Define a linear gradient.
     */
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

    /**
     * Define a radial gradient.
     */
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

    fun animated(element: SvgElement, block: AnimationBuilder.() -> Unit) {
        val animations = AnimationBuilder().apply(block).build()
        elements.add(SvgAnimated(element, animations))
    }

    // ============================================
    // Utilities
    // ============================================

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

    fun build(): List<SvgElement> = elements.toList()
}

/**
 * Builder for gradient stops.
 */
@SvgDslMarker
class GradientBuilder {
    private val stops = mutableListOf<GradientStop>()

    /**
     * Add a gradient stop.
     */
    fun stop(offset: Float, color: Color, opacity: Float = 1f) {
        stops.add(GradientStop(offset, color, opacity))
    }

    /**
     * Add a stop at 0%.
     */
    fun start(color: Color, opacity: Float = 1f) {
        stops.add(GradientStop(0f, color, opacity))
    }

    /**
     * Add a stop at 100%.
     */
    fun end(color: Color, opacity: Float = 1f) {
        stops.add(GradientStop(1f, color, opacity))
    }

    /**
     * Add a stop at 50%.
     */
    fun middle(color: Color, opacity: Float = 1f) {
        stops.add(GradientStop(0.5f, color, opacity))
    }

    fun build(): List<GradientStop> = stops.toList()
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
// DSL Entry Points
// ============================================

/**
 * DSL entry point for building a complete Svg object with default attributes.
 * Returns an Svg object that can be used directly in SvgIcon.
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
 * @param preserveAspectRatio Aspect ratio handling (default: xMidYMid meet)
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
    preserveAspectRatio: PreserveAspectRatio = PreserveAspectRatio.Default,
    block: SvgBuilder.() -> Unit
): Svg {
    return Svg(
        width = width.toFloat(),
        height = height.toFloat(),
        viewBox = viewBox ?: ViewBox(0f, 0f, width.toFloat(), height.toFloat()),
        preserveAspectRatio = preserveAspectRatio,
        fill = fill,
        stroke = stroke,
        strokeWidth = strokeWidth,
        strokeLinecap = strokeLinecap,
        strokeLinejoin = strokeLinejoin,
        children = SvgBuilder().apply(block).build()
    )
}
