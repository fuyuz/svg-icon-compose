package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class SvgParserTest {

    // ===========================================
    // Path Command Parsing Tests
    // ===========================================

    @Test
    fun parseMoveTo() {
        val path = SvgPath("M10 20")
        assertEquals(1, path.commands.size)
        val cmd = path.commands[0]
        assertIs<PathCommand.MoveTo>(cmd)
        assertEquals(10f, cmd.x)
        assertEquals(20f, cmd.y)
    }

    @Test
    fun parseMoveToRelative() {
        val path = SvgPath("m10 20")
        assertEquals(1, path.commands.size)
        val cmd = path.commands[0]
        assertIs<PathCommand.MoveToRelative>(cmd)
        assertEquals(10f, cmd.dx)
        assertEquals(20f, cmd.dy)
    }

    @Test
    fun parseLineTo() {
        val path = SvgPath("M0 0 L10 20")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.LineTo>(cmd)
        assertEquals(10f, cmd.x)
        assertEquals(20f, cmd.y)
    }

    @Test
    fun parseLineToRelative() {
        val path = SvgPath("M0 0 l10 20")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.LineToRelative>(cmd)
        assertEquals(10f, cmd.dx)
        assertEquals(20f, cmd.dy)
    }

    @Test
    fun parseHorizontalLineTo() {
        val path = SvgPath("M0 0 H15")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.HorizontalLineTo>(cmd)
        assertEquals(15f, cmd.x)
    }

    @Test
    fun parseHorizontalLineToRelative() {
        val path = SvgPath("M0 0 h15")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.HorizontalLineToRelative>(cmd)
        assertEquals(15f, cmd.dx)
    }

    @Test
    fun parseVerticalLineTo() {
        val path = SvgPath("M0 0 V25")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.VerticalLineTo>(cmd)
        assertEquals(25f, cmd.y)
    }

    @Test
    fun parseVerticalLineToRelative() {
        val path = SvgPath("M0 0 v25")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.VerticalLineToRelative>(cmd)
        assertEquals(25f, cmd.dy)
    }

    @Test
    fun parseCubicTo() {
        val path = SvgPath("M0 0 C1 2 3 4 5 6")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.CubicTo>(cmd)
        assertEquals(1f, cmd.x1)
        assertEquals(2f, cmd.y1)
        assertEquals(3f, cmd.x2)
        assertEquals(4f, cmd.y2)
        assertEquals(5f, cmd.x)
        assertEquals(6f, cmd.y)
    }

    @Test
    fun parseCubicToRelative() {
        val path = SvgPath("M0 0 c1 2 3 4 5 6")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.CubicToRelative>(cmd)
        assertEquals(1f, cmd.dx1)
        assertEquals(2f, cmd.dy1)
        assertEquals(3f, cmd.dx2)
        assertEquals(4f, cmd.dy2)
        assertEquals(5f, cmd.dx)
        assertEquals(6f, cmd.dy)
    }

    @Test
    fun parseSmoothCubicTo() {
        val path = SvgPath("M0 0 S3 4 5 6")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.SmoothCubicTo>(cmd)
        assertEquals(3f, cmd.x2)
        assertEquals(4f, cmd.y2)
        assertEquals(5f, cmd.x)
        assertEquals(6f, cmd.y)
    }

    @Test
    fun parseSmoothCubicToRelative() {
        val path = SvgPath("M0 0 s3 4 5 6")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.SmoothCubicToRelative>(cmd)
        assertEquals(3f, cmd.dx2)
        assertEquals(4f, cmd.dy2)
        assertEquals(5f, cmd.dx)
        assertEquals(6f, cmd.dy)
    }

    @Test
    fun parseQuadTo() {
        val path = SvgPath("M0 0 Q1 2 3 4")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.QuadTo>(cmd)
        assertEquals(1f, cmd.x1)
        assertEquals(2f, cmd.y1)
        assertEquals(3f, cmd.x)
        assertEquals(4f, cmd.y)
    }

    @Test
    fun parseQuadToRelative() {
        val path = SvgPath("M0 0 q1 2 3 4")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.QuadToRelative>(cmd)
        assertEquals(1f, cmd.dx1)
        assertEquals(2f, cmd.dy1)
        assertEquals(3f, cmd.dx)
        assertEquals(4f, cmd.dy)
    }

    @Test
    fun parseSmoothQuadTo() {
        val path = SvgPath("M0 0 T3 4")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.SmoothQuadTo>(cmd)
        assertEquals(3f, cmd.x)
        assertEquals(4f, cmd.y)
    }

    @Test
    fun parseSmoothQuadToRelative() {
        val path = SvgPath("M0 0 t3 4")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.SmoothQuadToRelative>(cmd)
        assertEquals(3f, cmd.dx)
        assertEquals(4f, cmd.dy)
    }

    @Test
    fun parseArcTo() {
        val path = SvgPath("M0 0 A5 5 0 0 1 10 10")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.ArcTo>(cmd)
        assertEquals(5f, cmd.rx)
        assertEquals(5f, cmd.ry)
        assertEquals(0f, cmd.xAxisRotation)
        assertEquals(false, cmd.largeArcFlag)
        assertEquals(true, cmd.sweepFlag)
        assertEquals(10f, cmd.x)
        assertEquals(10f, cmd.y)
    }

    @Test
    fun parseArcToRelative() {
        val path = SvgPath("M0 0 a5 5 0 1 0 10 10")
        assertEquals(2, path.commands.size)
        val cmd = path.commands[1]
        assertIs<PathCommand.ArcToRelative>(cmd)
        assertEquals(5f, cmd.rx)
        assertEquals(5f, cmd.ry)
        assertEquals(0f, cmd.xAxisRotation)
        assertEquals(true, cmd.largeArcFlag)
        assertEquals(false, cmd.sweepFlag)
        assertEquals(10f, cmd.dx)
        assertEquals(10f, cmd.dy)
    }

    @Test
    fun parseClose() {
        val path = SvgPath("M0 0 L10 10 Z")
        assertEquals(3, path.commands.size)
        assertIs<PathCommand.Close>(path.commands[2])
    }

    @Test
    fun parseCloseLowercase() {
        val path = SvgPath("M0 0 L10 10 z")
        assertEquals(3, path.commands.size)
        assertIs<PathCommand.Close>(path.commands[2])
    }

    @Test
    fun parseMultipleCommands() {
        val path = SvgPath("M5 5 L10 10 L15 5 Z")
        assertEquals(4, path.commands.size)
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.LineTo>(path.commands[1])
        assertIs<PathCommand.LineTo>(path.commands[2])
        assertIs<PathCommand.Close>(path.commands[3])
    }

    @Test
    fun parseImplicitLineTo() {
        // After M, subsequent coordinate pairs become L commands
        val path = SvgPath("M0 0 10 10 20 20")
        assertEquals(3, path.commands.size)
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.LineTo>(path.commands[1])
        assertIs<PathCommand.LineTo>(path.commands[2])
    }

    @Test
    fun parseImplicitLineToRelative() {
        // After m, subsequent coordinate pairs become l commands
        val path = SvgPath("m0 0 10 10 20 20")
        assertEquals(3, path.commands.size)
        assertIs<PathCommand.MoveToRelative>(path.commands[0])
        assertIs<PathCommand.LineToRelative>(path.commands[1])
        assertIs<PathCommand.LineToRelative>(path.commands[2])
    }

    @Test
    fun parseNegativeNumbers() {
        val path = SvgPath("M-10 -20 L-5 -15")
        assertEquals(2, path.commands.size)
        val move = path.commands[0] as PathCommand.MoveTo
        assertEquals(-10f, move.x)
        assertEquals(-20f, move.y)
        val line = path.commands[1] as PathCommand.LineTo
        assertEquals(-5f, line.x)
        assertEquals(-15f, line.y)
    }

    @Test
    fun parseDecimalNumbers() {
        val path = SvgPath("M1.5 2.5 L3.25 4.75")
        assertEquals(2, path.commands.size)
        val move = path.commands[0] as PathCommand.MoveTo
        assertEquals(1.5f, move.x)
        assertEquals(2.5f, move.y)
    }

    @Test
    fun parseCommaDelimited() {
        val path = SvgPath("M10,20 L30,40")
        assertEquals(2, path.commands.size)
        val move = path.commands[0] as PathCommand.MoveTo
        assertEquals(10f, move.x)
        assertEquals(20f, move.y)
    }

    @Test
    fun parseMixedDelimiters() {
        val path = SvgPath("M10,20L30 40,50,60")
        assertEquals(3, path.commands.size)
    }

    @Test
    fun parseNoSpaceBetweenCommandAndNumber() {
        val path = SvgPath("M10 20L30 40")
        assertEquals(2, path.commands.size)
    }

    @Test
    fun parseEmptyPath() {
        val path = SvgPath("")
        assertEquals(0, path.commands.size)
    }

    @Test
    fun parsePathStartingWithNumberThrows() {
        // Path starting with number (no command) throws exception
        assertFailsWith<SvgPathParseException> {
            SvgPath("10 20")
        }
    }

    @Test
    fun parsePathStartingWithLineToWorks() {
        // L (LineTo) is a valid command even at start (some SVG renderers allow it)
        val path = SvgPath("L10 20")
        assertEquals(1, path.commands.size)
        assertIs<PathCommand.LineTo>(path.commands[0])
    }

    // ===========================================
    // SVG Document Parsing Tests
    // ===========================================

    @Test
    fun parseSvgWithPath() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24"><path d="M12 12"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgPath>(svg.children[0])
    }

    @Test
    fun parseSvgWithCircle() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(1, svg.children.size)
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
        assertEquals(12f, circle.cx)
        assertEquals(12f, circle.cy)
        assertEquals(10f, circle.r)
    }

    @Test
    fun parseSvgWithEllipse() {
        val svg = parseSvg("""<svg><ellipse cx="12" cy="12" rx="10" ry="5"/></svg>""")
        assertEquals(1, svg.children.size)
        val ellipse = svg.children[0]
        assertIs<SvgEllipse>(ellipse)
        assertEquals(12f, ellipse.cx)
        assertEquals(12f, ellipse.cy)
        assertEquals(10f, ellipse.rx)
        assertEquals(5f, ellipse.ry)
    }

    @Test
    fun parseSvgWithRect() {
        val svg = parseSvg("""<svg><rect x="2" y="3" width="20" height="18" rx="2" ry="2"/></svg>""")
        assertEquals(1, svg.children.size)
        val rect = svg.children[0]
        assertIs<SvgRect>(rect)
        assertEquals(2f, rect.x)
        assertEquals(3f, rect.y)
        assertEquals(20f, rect.width)
        assertEquals(18f, rect.height)
        assertEquals(2f, rect.rx)
        assertEquals(2f, rect.ry)
    }

    @Test
    fun parseSvgWithLine() {
        val svg = parseSvg("""<svg><line x1="0" y1="0" x2="24" y2="24"/></svg>""")
        assertEquals(1, svg.children.size)
        val line = svg.children[0]
        assertIs<SvgLine>(line)
        assertEquals(0f, line.x1)
        assertEquals(0f, line.y1)
        assertEquals(24f, line.x2)
        assertEquals(24f, line.y2)
    }

    @Test
    fun parseSvgWithPolyline() {
        val svg = parseSvg("""<svg><polyline points="0,0 10,10 20,0"/></svg>""")
        assertEquals(1, svg.children.size)
        val polyline = svg.children[0]
        assertIs<SvgPolyline>(polyline)
        assertEquals(3, polyline.points.size)
        assertEquals(Offset(0f, 0f), polyline.points[0])
        assertEquals(Offset(10f, 10f), polyline.points[1])
        assertEquals(Offset(20f, 0f), polyline.points[2])
    }

    @Test
    fun parseSvgWithPolygon() {
        val svg = parseSvg("""<svg><polygon points="12,2 22,22 2,22"/></svg>""")
        assertEquals(1, svg.children.size)
        val polygon = svg.children[0]
        assertIs<SvgPolygon>(polygon)
        assertEquals(3, polygon.points.size)
    }

    @Test
    fun parseSvgWithGroup() {
        val svg = parseSvg("""<svg><g><circle cx="12" cy="12" r="5"/><rect width="10" height="10"/></g></svg>""")
        assertEquals(1, svg.children.size)
        val group = svg.children[0]
        assertIs<SvgGroup>(group)
        assertEquals(2, group.children.size)
    }

    @Test
    fun parseSvgAttributes() {
        val svg = parseSvg("""<svg width="48" height="48" viewBox="0 0 48 48" fill="black" stroke="white" stroke-width="3" stroke-linecap="butt" stroke-linejoin="miter"></svg>""")
        assertNotNull(svg.viewBox)
        assertEquals(48f, svg.viewBox!!.width)
        assertEquals(48f, svg.viewBox!!.height)
        assertEquals(Color.Black, svg.fill)
        assertEquals(Color.White, svg.stroke)
        assertEquals(3f, svg.strokeWidth)
        assertEquals(LineCap.BUTT, svg.strokeLinecap)
        assertEquals(LineJoin.MITER, svg.strokeLinejoin)
    }

    @Test
    fun parseSvgDefaultAttributes() {
        val svg = parseSvg("""<svg></svg>""")
        assertNull(svg.viewBox)  // viewBox is optional
        assertEquals(24f, svg.effectiveViewBox.width)  // effectiveViewBox provides default
        assertEquals(24f, svg.effectiveViewBox.height)
        assertNull(svg.fill)  // none = null
        assertEquals(Color.Unspecified, svg.stroke)  // currentColor = Unspecified
        assertEquals(2f, svg.strokeWidth)
        assertEquals(LineCap.ROUND, svg.strokeLinecap)
        assertEquals(LineJoin.ROUND, svg.strokeLinejoin)
    }

    @Test
    fun parseSvgWithXmlDeclaration() {
        val svg = parseSvg("""<?xml version="1.0" encoding="UTF-8"?><svg><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgWithDoctype() {
        val svg = parseSvg("""<!DOCTYPE svg><svg><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgMultipleElements() {
        val svg = parseSvg("""<svg><path d="M12 12"/><circle cx="12" cy="12" r="5"/><rect width="10" height="10"/></svg>""")
        assertEquals(3, svg.children.size)
        assertIs<SvgPath>(svg.children[0])
        assertIs<SvgCircle>(svg.children[1])
        assertIs<SvgRect>(svg.children[2])
    }

    @Test
    fun parseSvgNestedGroups() {
        val svg = parseSvg("""<svg><g><g><circle cx="12" cy="12" r="5"/></g></g></svg>""")
        assertEquals(1, svg.children.size)
        val outerGroup = svg.children[0]
        assertIs<SvgGroup>(outerGroup)
        val innerGroup = outerGroup.children[0]
        assertIs<SvgGroup>(innerGroup)
        assertIs<SvgCircle>(innerGroup.children[0])
    }

    // ===========================================
    // Style Parsing Tests
    // ===========================================

    // ===========================================
    // Inline CSS Style Attribute Tests
    // ===========================================

    @Test
    fun parseInlineStyleFill() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" style="fill:red"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseInlineStyleMultipleProperties() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" style="fill:red; stroke:blue; stroke-width:3"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
        assertEquals(3f, styled.style.strokeWidth)
    }

    @Test
    fun parseInlineStyleOverridesXmlAttribute() {
        // CSS style should take precedence over XML attributes
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" fill="blue" style="fill:red"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseInlineStyleWithWhitespace() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" style="  fill : red ;  stroke : blue  "/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
    }

    @Test
    fun parseInlineStyleWithHexColor() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" style="fill:#ff0000; stroke:#00ff00"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Green, styled.style.stroke)
    }

    @Test
    fun parseInlineStyleMixedWithXmlAttributes() {
        // XML: stroke-width, CSS: fill and stroke
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" stroke-width="5" style="fill:red; stroke:blue"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
        assertEquals(5f, styled.style.strokeWidth)
    }

    @Test
    fun parseInlineStyleOpacity() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" style="opacity:0.5; fill-opacity:0.8"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(0.5f, styled.style.opacity)
        assertEquals(0.8f, styled.style.fillOpacity)
    }

    @Test
    fun parseInlineStyleLinecapLinejoin() {
        val svg = parseSvg("""<svg><path d="M0 0" style="stroke-linecap:square; stroke-linejoin:bevel"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(LineCap.SQUARE, styled.style.strokeLinecap)
        assertEquals(LineJoin.BEVEL, styled.style.strokeLinejoin)
    }

    @Test
    fun parseElementWithFill() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" fill="red"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseElementWithStroke() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" stroke="blue" stroke-width="3"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Blue, styled.style.stroke)
        assertEquals(3f, styled.style.strokeWidth)
    }

    @Test
    fun parseElementWithOpacity() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" opacity="0.5" fill-opacity="0.8" stroke-opacity="0.3"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(0.5f, styled.style.opacity)
        assertEquals(0.8f, styled.style.fillOpacity)
        assertEquals(0.3f, styled.style.strokeOpacity)
    }

    @Test
    fun parseElementWithStrokeLinecap() {
        val svg = parseSvg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-linecap="square"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineCap.SQUARE, styled.style.strokeLinecap)
    }

    @Test
    fun parseElementWithStrokeLinejoin() {
        val svg = parseSvg("""<svg><path d="M0 0" stroke-linejoin="bevel"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineJoin.BEVEL, styled.style.strokeLinejoin)
    }

    @Test
    fun parseElementWithFillRule() {
        val svg = parseSvg("""<svg><path d="M0 0" fill-rule="evenodd"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(FillRule.EVENODD, styled.style.fillRule)
    }

    @Test
    fun parseElementWithStrokeDasharray() {
        val svg = parseSvg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-dasharray="5,3,2"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(listOf(5f, 3f, 2f), styled.style.strokeDasharray)
    }

    @Test
    fun parseElementWithStrokeDashoffset() {
        val svg = parseSvg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-dashoffset="10"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(10f, styled.style.strokeDashoffset)
    }

    @Test
    fun parseElementWithStrokeMiterlimit() {
        val svg = parseSvg("""<svg><path d="M0 0" stroke-miterlimit="8"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(8f, styled.style.strokeMiterlimit)
    }

    // ===========================================
    // Transform Parsing Tests
    // ===========================================

    @Test
    fun parseTransformTranslate() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="translate(10, 20)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Translate>(transform)
        assertEquals(10f, transform.x)
        assertEquals(20f, transform.y)
    }

    @Test
    fun parseTransformTranslateXOnly() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="translate(10)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Translate>(transform)
        assertEquals(10f, transform.x)
        assertEquals(0f, transform.y)
    }

    @Test
    fun parseTransformScale() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="scale(2, 3)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Scale>(transform)
        assertEquals(2f, transform.sx)
        assertEquals(3f, transform.sy)
    }

    @Test
    fun parseTransformScaleUniform() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="scale(2)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Scale>(transform)
        assertEquals(2f, transform.sx)
        assertEquals(2f, transform.sy)
    }

    @Test
    fun parseTransformRotate() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="rotate(45)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Rotate>(transform)
        assertEquals(45f, transform.angle)
        assertEquals(0f, transform.cx)
        assertEquals(0f, transform.cy)
    }

    @Test
    fun parseTransformRotateWithCenter() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="rotate(45, 12, 12)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Rotate>(transform)
        assertEquals(45f, transform.angle)
        assertEquals(12f, transform.cx)
        assertEquals(12f, transform.cy)
    }

    @Test
    fun parseTransformSkewX() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="skewX(30)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.SkewX>(transform)
        assertEquals(30f, transform.angle)
    }

    @Test
    fun parseTransformSkewY() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="skewY(30)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.SkewY>(transform)
        assertEquals(30f, transform.angle)
    }

    @Test
    fun parseTransformMatrix() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="matrix(1, 0, 0, 1, 10, 20)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Matrix>(transform)
        assertEquals(1f, transform.a)
        assertEquals(0f, transform.b)
        assertEquals(0f, transform.c)
        assertEquals(1f, transform.d)
        assertEquals(10f, transform.e)
        assertEquals(20f, transform.f)
    }

    @Test
    fun parseTransformCombined() {
        val svg = parseSvg("""<svg><circle cx="0" cy="0" r="5" transform="translate(10, 20) rotate(45)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Combined>(transform)
        assertEquals(2, transform.transforms.size)
        assertIs<SvgTransform.Translate>(transform.transforms[0])
        assertIs<SvgTransform.Rotate>(transform.transforms[1])
    }

    // ===========================================
    // unsafeSvg Tests
    // ===========================================

    @Test
    fun unsafeSvgParsesViewBox() {
        val result = unsafeSvg("""<svg viewBox="0 0 48 48"><circle cx="24" cy="24" r="20"/></svg>""")
        assertNotNull(result.viewBox)
        assertEquals(0f, result.viewBox!!.minX)
        assertEquals(0f, result.viewBox!!.minY)
        assertEquals(48f, result.viewBox!!.width)
        assertEquals(48f, result.viewBox!!.height)
    }

    @Test
    fun unsafeSvgWithoutViewBox() {
        val result = unsafeSvg("""<svg><circle cx="12" cy="12" r="10"/></svg>""")
        assertNull(result.viewBox)
    }

    @Test
    fun unsafeSvgParsesElements() {
        val result = unsafeSvg("""<svg><path d="M12 12"/><circle cx="6" cy="6" r="3"/></svg>""")
        assertEquals(2, result.elements.size)
    }

    // ===========================================
    // Edge Cases
    // ===========================================

    @Test
    fun parseSvgWithSelfClosingElements() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10"/><rect x="0" y="0" width="5" height="5"/></svg>""")
        assertEquals(2, svg.children.size)
    }

    @Test
    fun parseSvgWithWhitespace() {
        val svg = parseSvg("""
            <svg>
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseSvgIgnoresUnknownElements() {
        val svg = parseSvg("""<svg><unknown/><circle cx="12" cy="12" r="10"/><foo bar="baz"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgWithoutRoot() {
        // When there's no SVG root, it should still parse elements
        val svg = parseSvg("""<circle cx="12" cy="12" r="10"/>""")
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parsePointsWithSpaceDelimiter() {
        val svg = parseSvg("""<svg><polyline points="0 0 10 10 20 0"/></svg>""")
        val polyline = svg.children[0]
        assertIs<SvgPolyline>(polyline)
        assertEquals(3, polyline.points.size)
    }

    @Test
    fun parseRectWithDefaultRoundedCorners() {
        val svg = parseSvg("""<svg><rect width="10" height="10" rx="2"/></svg>""")
        val rect = svg.children[0]
        assertIs<SvgRect>(rect)
        assertEquals(2f, rect.rx)
        assertEquals(2f, rect.ry) // ry defaults to rx
    }

    // ===========================================
    // Additional Path Parsing Tests
    // ===========================================

    @Test
    fun parseConsecutiveDecimals() {
        // ".5.5" means "0.5 0.5"
        val path = SvgPath("M.5.5")
        assertEquals(1, path.commands.size)
        val cmd = path.commands[0] as PathCommand.MoveTo
        assertEquals(0.5f, cmd.x)
        assertEquals(0.5f, cmd.y)
    }

    @Test
    fun parseLeadingDecimal() {
        val path = SvgPath("M.25 .75")
        assertEquals(1, path.commands.size)
        val cmd = path.commands[0] as PathCommand.MoveTo
        assertEquals(0.25f, cmd.x)
        assertEquals(0.75f, cmd.y)
    }

    @Test
    fun parsePathWithNewlines() {
        val path = SvgPath("M0 0\nL10 10\nL20 0")
        assertEquals(3, path.commands.size)
    }

    @Test
    fun parsePathWithTabs() {
        val path = SvgPath("M0\t0\tL10\t10")
        assertEquals(2, path.commands.size)
    }

    @Test
    fun parseRepeatedLineTo() {
        // Multiple coordinate pairs after L become multiple LineTo commands
        val path = SvgPath("M0 0 L1 1 2 2 3 3")
        assertEquals(4, path.commands.size)
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.LineTo>(path.commands[1])
        assertIs<PathCommand.LineTo>(path.commands[2])
        assertIs<PathCommand.LineTo>(path.commands[3])
    }

    @Test
    fun parseRepeatedCubicTo() {
        val path = SvgPath("M0 0 C1 2 3 4 5 6 7 8 9 10 11 12")
        assertEquals(3, path.commands.size)
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.CubicTo>(path.commands[1])
        assertIs<PathCommand.CubicTo>(path.commands[2])
    }

    @Test
    fun parseMultipleSubpaths() {
        val path = SvgPath("M0 0 L10 10 Z M20 20 L30 30 Z")
        assertEquals(6, path.commands.size)
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.LineTo>(path.commands[1])
        assertIs<PathCommand.Close>(path.commands[2])
        assertIs<PathCommand.MoveTo>(path.commands[3])
        assertIs<PathCommand.LineTo>(path.commands[4])
        assertIs<PathCommand.Close>(path.commands[5])
    }

    @Test
    fun parseNegativeWithoutSpace() {
        // "-5-10" means "-5 -10"
        val path = SvgPath("M-5-10")
        assertEquals(1, path.commands.size)
        val cmd = path.commands[0] as PathCommand.MoveTo
        assertEquals(-5f, cmd.x)
        assertEquals(-10f, cmd.y)
    }

    @Test
    fun parseArcWithAllFlagCombinations() {
        // Test all 4 combinations of largeArcFlag and sweepFlag
        val path = SvgPath("M0 0 A5 5 0 0 0 10 10 A5 5 0 0 1 20 20 A5 5 0 1 0 30 30 A5 5 0 1 1 40 40")
        assertEquals(5, path.commands.size)

        val arc1 = path.commands[1] as PathCommand.ArcTo
        assertEquals(false, arc1.largeArcFlag)
        assertEquals(false, arc1.sweepFlag)

        val arc2 = path.commands[2] as PathCommand.ArcTo
        assertEquals(false, arc2.largeArcFlag)
        assertEquals(true, arc2.sweepFlag)

        val arc3 = path.commands[3] as PathCommand.ArcTo
        assertEquals(true, arc3.largeArcFlag)
        assertEquals(false, arc3.sweepFlag)

        val arc4 = path.commands[4] as PathCommand.ArcTo
        assertEquals(true, arc4.largeArcFlag)
        assertEquals(true, arc4.sweepFlag)
    }

    @Test
    fun parseArcWithRotation() {
        val path = SvgPath("M0 0 A10 5 45 0 1 20 20")
        val arc = path.commands[1] as PathCommand.ArcTo
        assertEquals(10f, arc.rx)
        assertEquals(5f, arc.ry)
        assertEquals(45f, arc.xAxisRotation)
    }

    @Test
    fun parseComplexRealWorldPath() {
        // Heart shape path
        val path = SvgPath("M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z")
        assertTrue(path.commands.isNotEmpty())
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.Close>(path.commands.last())
    }

    @Test
    fun parseLucideCheckIcon() {
        // Lucide check icon path
        val path = SvgPath("M20 6 9 17l-5-5")
        assertEquals(3, path.commands.size)
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.LineTo>(path.commands[1])
        assertIs<PathCommand.LineToRelative>(path.commands[2])
    }

    // ===========================================
    // Additional SVG Parsing Tests
    // ===========================================

    @Test
    fun parseGroupWithTransform() {
        val svg = parseSvg("""<svg><g transform="translate(10, 10)"><circle cx="0" cy="0" r="5"/></g></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val group = styled.element
        assertIs<SvgGroup>(group)
        assertEquals(1, group.children.size)
    }

    @Test
    fun parseGroupWithFill() {
        val svg = parseSvg("""<svg><g fill="red"><circle cx="12" cy="12" r="5"/></g></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseViewBoxWithCommas() {
        val result = unsafeSvg("""<svg viewBox="0,0,48,48"><circle cx="24" cy="24" r="20"/></svg>""")
        assertNotNull(result.viewBox)
        assertEquals(48f, result.viewBox!!.width)
        assertEquals(48f, result.viewBox!!.height)
    }

    @Test
    fun parseCircleWithDefaultValues() {
        val svg = parseSvg("""<svg><circle r="10"/></svg>""")
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
        assertEquals(0f, circle.cx)
        assertEquals(0f, circle.cy)
        assertEquals(10f, circle.r)
    }

    @Test
    fun parseRectWithDefaultValues() {
        val svg = parseSvg("""<svg><rect width="20" height="15"/></svg>""")
        val rect = svg.children[0]
        assertIs<SvgRect>(rect)
        assertEquals(0f, rect.x)
        assertEquals(0f, rect.y)
        assertEquals(20f, rect.width)
        assertEquals(15f, rect.height)
        assertEquals(0f, rect.rx)
        assertEquals(0f, rect.ry)
    }

    @Test
    fun parseLineWithDefaultValues() {
        val svg = parseSvg("""<svg><line x2="10" y2="10"/></svg>""")
        val line = svg.children[0]
        assertIs<SvgLine>(line)
        assertEquals(0f, line.x1)
        assertEquals(0f, line.y1)
        assertEquals(10f, line.x2)
        assertEquals(10f, line.y2)
    }

    @Test
    fun parseEllipseWithDefaultValues() {
        val svg = parseSvg("""<svg><ellipse rx="10" ry="5"/></svg>""")
        val ellipse = svg.children[0]
        assertIs<SvgEllipse>(ellipse)
        assertEquals(0f, ellipse.cx)
        assertEquals(0f, ellipse.cy)
    }

    @Test
    fun parseEmptyGroup() {
        val svg = parseSvg("""<svg><g></g></svg>""")
        val group = svg.children[0]
        assertIs<SvgGroup>(group)
        assertEquals(0, group.children.size)
    }

    @Test
    fun parseSvgWithWidthHeightPx() {
        val svg = parseSvg("""<svg width="48px" height="48px" viewBox="0 0 48 48"></svg>""")
        assertNotNull(svg.viewBox)
        assertEquals(48f, svg.viewBox!!.width)
        assertEquals(48f, svg.viewBox!!.height)
    }

    @Test
    fun parseEmptyPolyline() {
        val svg = parseSvg("""<svg><polyline points=""/></svg>""")
        assertEquals(0, svg.children.size) // Empty points should not create element
    }

    @Test
    fun parseEmptyPolygon() {
        val svg = parseSvg("""<svg><polygon points=""/></svg>""")
        assertEquals(0, svg.children.size) // Empty points should not create element
    }

    @Test
    fun parseEmptyPathD() {
        val svg = parseSvg("""<svg><path d=""/></svg>""")
        assertEquals(0, svg.children.size) // Empty path should not create element
    }

    @Test
    fun parseFillRuleNonzero() {
        val svg = parseSvg("""<svg><path d="M0 0" fill-rule="nonzero"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(FillRule.NONZERO, styled.style.fillRule)
    }

    @Test
    fun parseStrokeLinecapButt() {
        val svg = parseSvg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-linecap="butt"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineCap.BUTT, styled.style.strokeLinecap)
    }

    @Test
    fun parseStrokeLinecapRound() {
        val svg = parseSvg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-linecap="round"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineCap.ROUND, styled.style.strokeLinecap)
    }

    @Test
    fun parseStrokeLinejoinMiter() {
        val svg = parseSvg("""<svg><path d="M0 0" stroke-linejoin="miter"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineJoin.MITER, styled.style.strokeLinejoin)
    }

    @Test
    fun parseStrokeLinejoinRound() {
        val svg = parseSvg("""<svg><path d="M0 0" stroke-linejoin="round"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineJoin.ROUND, styled.style.strokeLinejoin)
    }

    @Test
    fun parseStrokeDasharrayWithSpaces() {
        val svg = parseSvg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-dasharray="5 3 2"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(listOf(5f, 3f, 2f), styled.style.strokeDasharray)
    }

    @Test
    fun parseStrokeDasharrayNone() {
        val svg = parseSvg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-dasharray="none"/></svg>""")
        // Should not have strokeDasharray when "none"
        val element = svg.children[0]
        assertIs<SvgLine>(element) // No style wrapper when dasharray is "none"
    }

    @Test
    fun parseHexColorFill() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" fill="#ff0000"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseRgbColorFill() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10" fill="rgb(255,0,0)"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseComplexLucideIcon() {
        // Complete Lucide menu icon
        val svg = parseSvg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="4" x2="20" y1="12" y2="12"/>
                <line x1="4" x2="20" y1="6" y2="6"/>
                <line x1="4" x2="20" y1="18" y2="18"/>
            </svg>
        """)
        assertEquals(3, svg.children.size)
        svg.children.forEach { assertIs<SvgLine>(it) }
    }

    @Test
    fun parseComplexLucideIconWithPath() {
        // Lucide home icon with path and polyline
        val svg = parseSvg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8"/>
                <path d="M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            </svg>
        """)
        assertEquals(2, svg.children.size)
    }

    @Test
    fun parseDeeplyNestedGroups() {
        val svg = parseSvg("""<svg><g><g><g><g><circle cx="12" cy="12" r="5"/></g></g></g></g></svg>""")
        var current: SvgElement = svg.children[0]
        repeat(4) {
            assertIs<SvgGroup>(current)
            current = (current as SvgGroup).children[0]
        }
        assertIs<SvgCircle>(current)
    }

    @Test
    fun parseMultipleGroupsAtSameLevel() {
        val svg = parseSvg("""<svg><g><circle cx="5" cy="5" r="2"/></g><g><circle cx="15" cy="15" r="2"/></g></svg>""")
        assertEquals(2, svg.children.size)
        assertIs<SvgGroup>(svg.children[0])
        assertIs<SvgGroup>(svg.children[1])
    }

    // ===========================================
    // Stress Tests
    // ===========================================

    @Test
    fun parseLongPath() {
        // Generate a long path with many commands
        val pathData = buildString {
            append("M0 0")
            repeat(100) { i ->
                append(" L${i * 2} ${i * 2}")
            }
            append(" Z")
        }
        val path = SvgPath(pathData)
        assertEquals(102, path.commands.size) // 1 MoveTo + 100 LineTo + 1 Close
    }

    @Test
    fun parsePolygonWithManyPoints() {
        val points = (0 until 20).joinToString(" ") { "$it,${it * 2}" }
        val svg = parseSvg("""<svg><polygon points="$points"/></svg>""")
        val polygon = svg.children[0]
        assertIs<SvgPolygon>(polygon)
        assertEquals(20, polygon.points.size)
    }
}
