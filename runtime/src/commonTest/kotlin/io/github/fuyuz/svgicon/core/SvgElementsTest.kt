package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class SvgElementsTest {

    // ===========================================
    // Svg Tests
    // ===========================================

    @Test
    fun svgDefaultValues() {
        val svg = Svg()
        assertNull(svg.width)
        assertNull(svg.height)
        assertNull(svg.viewBox)
        assertEquals(PreserveAspectRatio.Default, svg.preserveAspectRatio)
        // SVG spec defaults
        assertEquals(Color.Black, svg.fill)  // SVG spec: fill default is black
        assertNull(svg.stroke)  // SVG spec: stroke default is none
        assertEquals(1f, svg.strokeWidth)  // SVG spec: stroke-width default is 1
        assertEquals(LineCap.BUTT, svg.strokeLinecap)  // SVG spec: stroke-linecap default is butt
        assertEquals(LineJoin.MITER, svg.strokeLinejoin)  // SVG spec: stroke-linejoin default is miter
        assertEquals(4f, svg.strokeMiterlimit)  // SVG spec: stroke-miterlimit default is 4
        assertEquals(emptyList(), svg.children)
    }

    @Test
    fun svgEffectiveWidthWithExplicitWidth() {
        val svg = Svg(width = 48f, viewBox = ViewBox(0f, 0f, 24f, 24f))
        assertEquals(48f, svg.effectiveWidth)
    }

    @Test
    fun svgEffectiveWidthFromViewBox() {
        val svg = Svg(viewBox = ViewBox(0f, 0f, 32f, 32f))
        assertEquals(32f, svg.effectiveWidth)
    }

    @Test
    fun svgEffectiveWidthDefault() {
        val svg = Svg()
        assertEquals(24f, svg.effectiveWidth)
    }

    @Test
    fun svgEffectiveHeightWithExplicitHeight() {
        val svg = Svg(height = 96f, viewBox = ViewBox(0f, 0f, 24f, 24f))
        assertEquals(96f, svg.effectiveHeight)
    }

    @Test
    fun svgEffectiveHeightFromViewBox() {
        val svg = Svg(viewBox = ViewBox(0f, 0f, 16f, 48f))
        assertEquals(48f, svg.effectiveHeight)
    }

    @Test
    fun svgEffectiveHeightDefault() {
        val svg = Svg()
        assertEquals(24f, svg.effectiveHeight)
    }

    @Test
    fun svgEffectiveViewBoxWhenProvided() {
        val viewBox = ViewBox(10f, 20f, 100f, 50f)
        val svg = Svg(viewBox = viewBox)
        assertEquals(viewBox, svg.effectiveViewBox)
    }

    @Test
    fun svgEffectiveViewBoxFromWidthHeight() {
        val svg = Svg(width = 48f, height = 32f)
        val effective = svg.effectiveViewBox
        assertEquals(0f, effective.minX)
        assertEquals(0f, effective.minY)
        assertEquals(48f, effective.width)
        assertEquals(32f, effective.height)
    }

    @Test
    fun svgEffectiveViewBoxDefault() {
        val svg = Svg()
        val effective = svg.effectiveViewBox
        assertEquals(ViewBox.Default.width, effective.width)
        assertEquals(ViewBox.Default.height, effective.height)
    }

    @Test
    fun svgWithChildren() {
        val children = listOf(
            SvgCircle(12f, 12f, 10f),
            SvgRect(0f, 0f, 24f, 24f)
        )
        val svg = Svg(children = children)
        assertEquals(2, svg.children.size)
    }

    // ===========================================
    // SvgStyled Tests
    // ===========================================

    @Test
    fun svgStyledWrapsElement() {
        val circle = SvgCircle(12f, 12f, 10f)
        val style = SvgStyle(fill = Color.Red)
        val styled = SvgStyled(circle, style)
        assertEquals(circle, styled.element)
        assertEquals(style, styled.style)
    }

    // ===========================================
    // SvgAnimated Tests
    // ===========================================

    @Test
    fun svgAnimatedWrapsElement() {
        val path = SvgPath("M10 10")
        val animations = listOf(SvgAnimate.StrokeDraw(dur = 500.milliseconds))
        val animated = SvgAnimated(path, animations)
        assertEquals(path, animated.element)
        assertEquals(1, animated.animations.size)
    }

    // ===========================================
    // SvgPath Tests
    // ===========================================

    @Test
    fun svgPathFromCommands() {
        val commands = listOf(PathCommand.MoveTo(0f, 0f), PathCommand.LineTo(10f, 10f))
        val path = SvgPath(commands)
        assertEquals(2, path.commands.size)
    }

    @Test
    fun svgPathFromString() {
        val path = SvgPath("M0 0 L10 10")
        assertEquals(2, path.commands.size)
    }

    // ===========================================
    // SvgCircle Tests
    // ===========================================

    @Test
    fun svgCircleProperties() {
        val circle = SvgCircle(12f, 15f, 8f)
        assertEquals(12f, circle.cx)
        assertEquals(15f, circle.cy)
        assertEquals(8f, circle.r)
    }

    // ===========================================
    // SvgEllipse Tests
    // ===========================================

    @Test
    fun svgEllipseProperties() {
        val ellipse = SvgEllipse(12f, 12f, 10f, 5f)
        assertEquals(12f, ellipse.cx)
        assertEquals(12f, ellipse.cy)
        assertEquals(10f, ellipse.rx)
        assertEquals(5f, ellipse.ry)
    }

    // ===========================================
    // SvgRect Tests
    // ===========================================

    @Test
    fun svgRectDefaults() {
        val rect = SvgRect(width = 20f, height = 15f)
        assertEquals(0f, rect.x)
        assertEquals(0f, rect.y)
        assertEquals(20f, rect.width)
        assertEquals(15f, rect.height)
        assertEquals(0f, rect.rx)
        assertEquals(0f, rect.ry)
    }

    @Test
    fun svgRectWithRoundedCorners() {
        val rect = SvgRect(width = 20f, height = 15f, rx = 5f)
        assertEquals(5f, rect.rx)
        assertEquals(5f, rect.ry)  // ry defaults to rx
    }

    @Test
    fun svgRectWithDifferentRoundedCorners() {
        val rect = SvgRect(width = 20f, height = 15f, rx = 5f, ry = 3f)
        assertEquals(5f, rect.rx)
        assertEquals(3f, rect.ry)
    }

    // ===========================================
    // SvgLine Tests
    // ===========================================

    @Test
    fun svgLineProperties() {
        val line = SvgLine(0f, 0f, 24f, 24f)
        assertEquals(0f, line.x1)
        assertEquals(0f, line.y1)
        assertEquals(24f, line.x2)
        assertEquals(24f, line.y2)
    }

    // ===========================================
    // SvgPolyline Tests
    // ===========================================

    @Test
    fun svgPolylineProperties() {
        val points = listOf(Offset(0f, 0f), Offset(10f, 10f), Offset(20f, 0f))
        val polyline = SvgPolyline(points)
        assertEquals(3, polyline.points.size)
        assertEquals(Offset(10f, 10f), polyline.points[1])
    }

    // ===========================================
    // SvgPolygon Tests
    // ===========================================

    @Test
    fun svgPolygonProperties() {
        val points = listOf(Offset(12f, 2f), Offset(22f, 22f), Offset(2f, 22f))
        val polygon = SvgPolygon(points)
        assertEquals(3, polygon.points.size)
    }

    // ===========================================
    // SvgGroup Tests
    // ===========================================

    @Test
    fun svgGroupWithChildren() {
        val children = listOf(SvgCircle(12f, 12f, 5f))
        val group = SvgGroup(children)
        assertEquals(1, group.children.size)
        assertNull(group.style)
    }

    @Test
    fun svgGroupWithStyle() {
        val style = SvgStyle(fill = Color.Blue)
        val group = SvgGroup(emptyList(), style)
        assertEquals(style, group.style)
    }

    // ===========================================
    // SvgText Tests
    // ===========================================

    @Test
    fun svgTextDefaults() {
        val text = SvgText("Hello")
        assertEquals("Hello", text.text)
        assertEquals(0f, text.x)
        assertEquals(0f, text.y)
        assertNull(text.textAnchor)
        assertNull(text.dominantBaseline)
        assertNull(text.fontSize)
        assertNull(text.fontFamily)
        assertNull(text.fontWeight)
        assertNull(text.letterSpacing)
        assertNull(text.dx)
        assertNull(text.dy)
    }

    @Test
    fun svgTextWithAllProperties() {
        val text = SvgText(
            text = "World",
            x = 10f,
            y = 20f,
            textAnchor = TextAnchor.MIDDLE,
            dominantBaseline = DominantBaseline.CENTRAL,
            fontSize = 16f,
            fontFamily = "Arial",
            fontWeight = "bold",
            letterSpacing = 1.5f,
            dx = 2f,
            dy = -2f
        )
        assertEquals("World", text.text)
        assertEquals(10f, text.x)
        assertEquals(20f, text.y)
        assertEquals(TextAnchor.MIDDLE, text.textAnchor)
        assertEquals(DominantBaseline.CENTRAL, text.dominantBaseline)
        assertEquals(16f, text.fontSize)
        assertEquals("Arial", text.fontFamily)
        assertEquals("bold", text.fontWeight)
        assertEquals(1.5f, text.letterSpacing)
        assertEquals(2f, text.dx)
        assertEquals(-2f, text.dy)
    }

    // ===========================================
    // SvgClipPath Tests
    // ===========================================

    @Test
    fun svgClipPathDefaults() {
        val clipPath = SvgClipPath("clip1", listOf(SvgCircle(12f, 12f, 10f)))
        assertEquals("clip1", clipPath.id)
        assertEquals(1, clipPath.children.size)
        assertEquals(ClipPathUnits.USER_SPACE_ON_USE, clipPath.clipPathUnits)
    }

    // ===========================================
    // SvgMask Tests
    // ===========================================

    @Test
    fun svgMaskDefaults() {
        val mask = SvgMask("mask1", listOf(SvgRect(width = 24f, height = 24f)))
        assertEquals("mask1", mask.id)
        assertEquals(1, mask.children.size)
        assertEquals(MaskUnits.OBJECT_BOUNDING_BOX, mask.maskUnits)
        assertEquals(MaskUnits.USER_SPACE_ON_USE, mask.maskContentUnits)
        assertEquals(-0.1f, mask.x)
        assertEquals(-0.1f, mask.y)
        assertEquals(1.2f, mask.width)
        assertEquals(1.2f, mask.height)
    }

    // ===========================================
    // SvgDefs Tests
    // ===========================================

    @Test
    fun svgDefsContainsChildren() {
        val defs = SvgDefs(listOf(SvgCircle(12f, 12f, 10f)))
        assertEquals(1, defs.children.size)
    }

    // ===========================================
    // Gradient Tests
    // ===========================================

    @Test
    fun gradientStopProperties() {
        val stop = GradientStop(0.5f, Color.Red, 0.8f)
        assertEquals(0.5f, stop.offset)
        assertEquals(Color.Red, stop.color)
        assertEquals(0.8f, stop.opacity)
    }

    @Test
    fun linearGradientDefaults() {
        val gradient = SvgLinearGradient("grad1")
        assertEquals("grad1", gradient.id)
        assertEquals(0f, gradient.x1)
        assertEquals(0f, gradient.y1)
        assertEquals(1f, gradient.x2)
        assertEquals(0f, gradient.y2)
        assertEquals(emptyList(), gradient.stops)
        assertEquals(GradientUnits.OBJECT_BOUNDING_BOX, gradient.gradientUnits)
        assertEquals(SpreadMethod.PAD, gradient.spreadMethod)
        assertNull(gradient.gradientTransform)
    }

    @Test
    fun radialGradientDefaults() {
        val gradient = SvgRadialGradient("grad2")
        assertEquals("grad2", gradient.id)
        assertEquals(0.5f, gradient.cx)
        assertEquals(0.5f, gradient.cy)
        assertEquals(0.5f, gradient.r)
        assertNull(gradient.fx)
        assertNull(gradient.fy)
    }

    // ===========================================
    // SvgMarker Tests
    // ===========================================

    @Test
    fun svgMarkerDefaults() {
        val marker = SvgMarker("marker1")
        assertEquals("marker1", marker.id)
        assertNull(marker.viewBox)
        assertEquals(0f, marker.refX)
        assertEquals(0f, marker.refY)
        assertEquals(3f, marker.markerWidth)
        assertEquals(3f, marker.markerHeight)
        assertEquals(MarkerOrient.Auto, marker.orient)
        assertEquals(emptyList(), marker.children)
    }

    @Test
    fun markerOrientAngle() {
        val orient = MarkerOrient.Angle(45f)
        assertEquals(45f, orient.degrees)
    }

    // ===========================================
    // SvgSymbol Tests
    // ===========================================

    @Test
    fun svgSymbolProperties() {
        val symbol = SvgSymbol("symbol1", ViewBox(0f, 0f, 24f, 24f))
        assertEquals("symbol1", symbol.id)
        assertEquals(ViewBox(0f, 0f, 24f, 24f), symbol.viewBox)
    }

    // ===========================================
    // SvgUse Tests
    // ===========================================

    @Test
    fun svgUseDefaults() {
        val use = SvgUse("#mySymbol")
        assertEquals("#mySymbol", use.href)
        assertEquals(0f, use.x)
        assertEquals(0f, use.y)
        assertNull(use.width)
        assertNull(use.height)
    }

    @Test
    fun svgUseWithPosition() {
        val use = SvgUse("#symbol", 10f, 20f, 24f, 24f)
        assertEquals(10f, use.x)
        assertEquals(20f, use.y)
        assertEquals(24f, use.width)
        assertEquals(24f, use.height)
    }

    // ===========================================
    // SvgPattern Tests
    // ===========================================

    @Test
    fun svgPatternDefaults() {
        val pattern = SvgPattern("pattern1", 10f, 10f)
        assertEquals("pattern1", pattern.id)
        assertEquals(10f, pattern.width)
        assertEquals(10f, pattern.height)
        assertEquals(PatternUnits.USER_SPACE_ON_USE, pattern.patternUnits)
        assertEquals(PatternUnits.USER_SPACE_ON_USE, pattern.patternContentUnits)
        assertNull(pattern.patternTransform)
        assertEquals(emptyList(), pattern.children)
    }

    // ===========================================
    // Enum Tests
    // ===========================================

    @Test
    fun textAnchorValues() {
        assertEquals(3, TextAnchor.entries.size)
        kotlin.test.assertTrue(TextAnchor.entries.contains(TextAnchor.START))
        kotlin.test.assertTrue(TextAnchor.entries.contains(TextAnchor.MIDDLE))
        kotlin.test.assertTrue(TextAnchor.entries.contains(TextAnchor.END))
    }

    @Test
    fun dominantBaselineValues() {
        assertEquals(9, DominantBaseline.entries.size)
    }

    @Test
    fun spreadMethodValues() {
        assertEquals(3, SpreadMethod.entries.size)
    }

    @Test
    fun gradientUnitsValues() {
        assertEquals(2, GradientUnits.entries.size)
    }

    @Test
    fun patternUnitsValues() {
        assertEquals(2, PatternUnits.entries.size)
    }
}
