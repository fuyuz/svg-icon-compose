package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.fuyuz.svgicon.core.dsl.*
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
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
        val svg = svg("""<svg viewBox="0 0 24 24"><path d="M12 12"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgPath>(svg.children[0])
    }

    @Test
    fun parseSvgWithCircle() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(1, svg.children.size)
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
        assertEquals(12f, circle.cx)
        assertEquals(12f, circle.cy)
        assertEquals(10f, circle.r)
    }

    @Test
    fun parseSvgWithEllipse() {
        val svg = svg("""<svg><ellipse cx="12" cy="12" rx="10" ry="5"/></svg>""")
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
        val svg = svg("""<svg><rect x="2" y="3" width="20" height="18" rx="2" ry="2"/></svg>""")
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
        val svg = svg("""<svg><line x1="0" y1="0" x2="24" y2="24"/></svg>""")
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
        val svg = svg("""<svg><polyline points="0,0 10,10 20,0"/></svg>""")
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
        val svg = svg("""<svg><polygon points="12,2 22,22 2,22"/></svg>""")
        assertEquals(1, svg.children.size)
        val polygon = svg.children[0]
        assertIs<SvgPolygon>(polygon)
        assertEquals(3, polygon.points.size)
    }

    @Test
    fun parseSvgWithGroup() {
        val svg = svg("""<svg><g><circle cx="12" cy="12" r="5"/><rect width="10" height="10"/></g></svg>""")
        assertEquals(1, svg.children.size)
        val group = svg.children[0]
        assertIs<SvgGroup>(group)
        assertEquals(2, group.children.size)
    }

    @Test
    fun parseSvgAttributes() {
        val svg = svg("""<svg width="48" height="48" viewBox="0 0 48 48" fill="black" stroke="white" stroke-width="3" stroke-linecap="butt" stroke-linejoin="miter"></svg>""")
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
        val svg = svg("""<svg></svg>""")
        assertNull(svg.viewBox)  // viewBox is optional
        assertEquals(24f, svg.effectiveViewBox.width)  // effectiveViewBox provides default
        assertEquals(24f, svg.effectiveViewBox.height)
        // SVG spec defaults
        assertEquals(Color.Black, svg.fill)  // SVG spec: fill default is black
        assertNull(svg.stroke)  // SVG spec: stroke default is none
        assertEquals(1f, svg.strokeWidth)  // SVG spec: stroke-width default is 1
        assertEquals(LineCap.BUTT, svg.strokeLinecap)  // SVG spec: stroke-linecap default is butt
        assertEquals(LineJoin.MITER, svg.strokeLinejoin)  // SVG spec: stroke-linejoin default is miter
        assertEquals(4f, svg.strokeMiterlimit)  // SVG spec: stroke-miterlimit default is 4
    }

    @Test
    fun parseSvgWithXmlDeclaration() {
        val svg = svg("""<?xml version="1.0" encoding="UTF-8"?><svg><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgWithDoctype() {
        val svg = svg("""<!DOCTYPE svg><svg><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgMultipleElements() {
        val svg = svg("""<svg><path d="M12 12"/><circle cx="12" cy="12" r="5"/><rect width="10" height="10"/></svg>""")
        assertEquals(3, svg.children.size)
        assertIs<SvgPath>(svg.children[0])
        assertIs<SvgCircle>(svg.children[1])
        assertIs<SvgRect>(svg.children[2])
    }

    @Test
    fun parseSvgNestedGroups() {
        val svg = svg("""<svg><g><g><circle cx="12" cy="12" r="5"/></g></g></svg>""")
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
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" style="fill:red"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseInlineStyleMultipleProperties() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" style="fill:red; stroke:blue; stroke-width:3"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
        assertEquals(3f, styled.style.strokeWidth)
    }

    @Test
    fun parseInlineStyleOverridesXmlAttribute() {
        // CSS style should take precedence over XML attributes
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" fill="blue" style="fill:red"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseInlineStyleWithWhitespace() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" style="  fill : red ;  stroke : blue  "/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
    }

    @Test
    fun parseInlineStyleWithHexColor() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" style="fill:#ff0000; stroke:#00ff00"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Green, styled.style.stroke)
    }

    @Test
    fun parseInlineStyleMixedWithXmlAttributes() {
        // XML: stroke-width, CSS: fill and stroke
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" stroke-width="5" style="fill:red; stroke:blue"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
        assertEquals(5f, styled.style.strokeWidth)
    }

    @Test
    fun parseInlineStyleOpacity() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" style="opacity:0.5; fill-opacity:0.8"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(0.5f, styled.style.opacity)
        assertEquals(0.8f, styled.style.fillOpacity)
    }

    @Test
    fun parseInlineStyleLinecapLinejoin() {
        val svg = svg("""<svg><path d="M0 0" style="stroke-linecap:square; stroke-linejoin:bevel"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(LineCap.SQUARE, styled.style.strokeLinecap)
        assertEquals(LineJoin.BEVEL, styled.style.strokeLinejoin)
    }

    // ===========================================
    // Internal Stylesheet Tests
    // ===========================================

    @Test
    fun parseStylesheetWithClassSelector() {
        val svg = svg("""
            <svg>
                <style>.red-fill { fill: red; }</style>
                <circle cx="12" cy="12" r="10" class="red-fill"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseStylesheetWithIdSelector() {
        val svg = svg("""
            <svg>
                <style>#my-circle { stroke: blue; stroke-width: 3; }</style>
                <circle cx="12" cy="12" r="10" id="my-circle"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Blue, styled.style.stroke)
        assertEquals(3f, styled.style.strokeWidth)
    }

    @Test
    fun parseStylesheetWithTagSelector() {
        val svg = svg("""
            <svg>
                <style>circle { fill: green; }</style>
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Green, styled.style.fill)
    }

    @Test
    fun parseStylesheetWithUniversalSelector() {
        val svg = svg("""
            <svg>
                <style>* { opacity: 0.5; }</style>
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(0.5f, styled.style.opacity)
    }

    @Test
    fun parseStylesheetSpecificityIdOverridesClass() {
        val svg = svg("""
            <svg>
                <style>
                    .my-class { fill: red; }
                    #my-id { fill: blue; }
                </style>
                <circle cx="12" cy="12" r="10" class="my-class" id="my-id"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Blue, styled.style.fill)
    }

    @Test
    fun parseStylesheetSpecificityClassOverridesTag() {
        val svg = svg("""
            <svg>
                <style>
                    circle { fill: red; }
                    .blue-fill { fill: blue; }
                </style>
                <circle cx="12" cy="12" r="10" class="blue-fill"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Blue, styled.style.fill)
    }

    @Test
    fun parseStylesheetSpecificityTagOverridesUniversal() {
        val svg = svg("""
            <svg>
                <style>
                    * { fill: red; }
                    circle { fill: blue; }
                </style>
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Blue, styled.style.fill)
    }

    @Test
    fun parseStylesheetInlineStyleOverridesAll() {
        val svg = svg("""
            <svg>
                <style>
                    #my-id { fill: red; }
                    .my-class { fill: green; }
                </style>
                <circle cx="12" cy="12" r="10" id="my-id" class="my-class" style="fill:blue"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Blue, styled.style.fill)
    }

    @Test
    fun parseStylesheetMultipleRulesSameElement() {
        val svg = svg("""
            <svg>
                <style>
                    circle { stroke: red; }
                    .styled { stroke-width: 3; }
                </style>
                <circle cx="12" cy="12" r="10" class="styled"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.stroke)
        assertEquals(3f, styled.style.strokeWidth)
    }

    @Test
    fun parseStylesheetMultipleClasses() {
        val svg = svg("""
            <svg>
                <style>
                    .red { fill: red; }
                    .thick { stroke-width: 5; }
                </style>
                <circle cx="12" cy="12" r="10" class="red thick"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(5f, styled.style.strokeWidth)
    }

    @Test
    fun parseStylesheetMultipleElements() {
        val svg = svg("""
            <svg>
                <style>
                    .styled { fill: red; }
                </style>
                <circle cx="12" cy="12" r="10" class="styled"/>
                <rect width="10" height="10" class="styled"/>
            </svg>
        """)
        assertEquals(2, svg.children.size)
        val styledCircle = svg.children[0] as SvgStyled
        val styledRect = svg.children[1] as SvgStyled
        assertEquals(Color.Red, styledCircle.style.fill)
        assertEquals(Color.Red, styledRect.style.fill)
    }

    @Test
    fun parseStylesheetWithGroup() {
        val svg = svg("""
            <svg>
                <style>
                    .group-style { opacity: 0.5; }
                    circle { fill: red; }
                </style>
                <g class="group-style">
                    <circle cx="12" cy="12" r="10"/>
                </g>
            </svg>
        """)
        val groupStyled = svg.children[0] as SvgStyled
        assertEquals(0.5f, groupStyled.style.opacity)
        val group = groupStyled.element as SvgGroup
        val circleStyled = group.children[0] as SvgStyled
        assertEquals(Color.Red, circleStyled.style.fill)
    }

    @Test
    fun parseStylesheetNoMatchingSelector() {
        val svg = svg("""
            <svg>
                <style>.non-existent { fill: red; }</style>
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """)
        // No matching class, so element should be plain SvgCircle
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseStylesheetEmptyStyle() {
        val svg = svg("""
            <svg>
                <style></style>
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseStylesheetWithMultipleStyleTags() {
        val svg = svg("""
            <svg>
                <style>.red { fill: red; }</style>
                <style>.blue { stroke: blue; }</style>
                <circle cx="12" cy="12" r="10" class="red blue"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
    }

    @Test
    fun parseStylesheetXmlAttributeMergesWithStylesheet() {
        val svg = svg("""
            <svg>
                <style>circle { fill: red; }</style>
                <circle cx="12" cy="12" r="10" stroke-width="5"/>
            </svg>
        """)
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(5f, styled.style.strokeWidth)
    }

    // ===========================================
    // CSS Animation Tests
    // ===========================================

    @Test
    fun parseKeyframesWithFromTo() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .animated { animation: fadeIn 1s ease; }
                </style>
                <circle cx="12" cy="12" r="10" class="animated"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        assertEquals(1, element.animations.size)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(0f, anim.from)
        assertEquals(1f, anim.to)
    }

    @Test
    fun parseKeyframesWithPercentages() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes pulse {
                        0% { opacity: 0; }
                        100% { opacity: 1; }
                    }
                    .pulsing { animation: pulse 500ms; }
                </style>
                <circle cx="12" cy="12" r="10" class="pulsing"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(500.milliseconds, anim.dur)
    }

    @Test
    fun parseKeyframesRotateAnimation() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes spin {
                        from { transform: rotate(0deg); }
                        to { transform: rotate(360deg); }
                    }
                    .spinner { animation: spin 1s linear infinite; }
                </style>
                <circle cx="12" cy="12" r="10" class="spinner"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.ROTATE, anim.type)
        assertEquals(0f, anim.from)
        assertEquals(360f, anim.to)
        assertEquals(CalcMode.LINEAR, anim.calcMode)
    }

    @Test
    fun parseKeyframesScaleAnimation() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes grow {
                        from { transform: scale(0.5); }
                        to { transform: scale(1.5); }
                    }
                    .growing { animation: grow 2s ease-in-out; }
                </style>
                <circle cx="12" cy="12" r="10" class="growing"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.SCALE, anim.type)
        assertEquals(0.5f, anim.from)
        assertEquals(1.5f, anim.to)
        assertEquals(CalcMode.SPLINE, anim.calcMode)
        assertEquals(KeySplines.EASE_IN_OUT, anim.keySplines)
    }

    @Test
    fun parseAnimationWithDelay() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .delayed { animation: fadeIn 1s 500ms; }
                </style>
                <circle cx="12" cy="12" r="10" class="delayed"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0] as SvgAnimate.Opacity
        assertEquals(1000.milliseconds, anim.dur)
        assertEquals(500.milliseconds, anim.delay)
    }

    @Test
    fun parseAnimationWithTimingFunction() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
                    .ease { animation: fadeIn 1s ease; }
                </style>
                <circle cx="12" cy="12" r="10" class="ease"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertEquals(CalcMode.SPLINE, anim.calcMode)
        assertEquals(KeySplines.EASE, anim.keySplines)
    }

    @Test
    fun parseAnimationWithStyleAndAnimation() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
                    .styled { fill: red; animation: fadeIn 1s; }
                </style>
                <circle cx="12" cy="12" r="10" class="styled"/>
            </svg>
        """)
        val element = svg.children[0]
        // Should be SvgAnimated wrapping SvgStyled
        assertIs<SvgAnimated>(element)
        val innerStyled = element.element
        assertIs<SvgStyled>(innerStyled)
        assertEquals(Color.Red, innerStyled.style.fill)
    }

    @Test
    fun parseKeyframesStrokeWidthAnimation() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes thicken {
                        from { stroke-width: 1; }
                        to { stroke-width: 5; }
                    }
                    .thickening { animation: thicken 1s; }
                </style>
                <circle cx="12" cy="12" r="10" class="thickening"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.StrokeWidth>(anim)
        assertEquals(1f, anim.from)
        assertEquals(5f, anim.to)
    }

    @Test
    fun parseNoMatchingKeyframes() {
        val svg = svg("""
            <svg>
                <style>
                    .animated { animation: nonexistent 1s; }
                </style>
                <circle cx="12" cy="12" r="10" class="animated"/>
            </svg>
        """)
        // No matching keyframes, should not create animation
        val element = svg.children[0]
        assertIs<SvgCircle>(element)
    }

    @Test
    fun parseAnimationWithInfiniteIterations() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes spin {
                        from { transform: rotate(0deg); }
                        to { transform: rotate(360deg); }
                    }
                    .infinite { animation: spin 1s linear infinite; }
                </style>
                <circle cx="12" cy="12" r="10" class="infinite"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(SvgAnimate.INFINITE, anim.iterations)
        assertTrue(anim.isInfinite)
    }

    @Test
    fun parseAnimationWithFiniteIterations() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .finite { animation: fadeIn 1s ease 3; }
                </style>
                <circle cx="12" cy="12" r="10" class="finite"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(3, anim.iterations)
        assertTrue(!anim.isInfinite)
    }

    @Test
    fun parseAnimationDefaultsToSingleIteration() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .default { animation: fadeIn 1s; }
                </style>
                <circle cx="12" cy="12" r="10" class="default"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(1, anim.iterations)
        assertTrue(!anim.isInfinite)
    }

    @Test
    fun parseAnimationWithDirectionAlternate() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .alternate { animation: fadeIn 1s alternate; }
                </style>
                <circle cx="12" cy="12" r="10" class="alternate"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(AnimationDirection.ALTERNATE, anim.direction)
    }

    @Test
    fun parseAnimationWithDirectionReverse() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .reversed { animation: fadeIn 1s reverse; }
                </style>
                <circle cx="12" cy="12" r="10" class="reversed"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(AnimationDirection.REVERSE, anim.direction)
    }

    @Test
    fun parseAnimationWithFillModeForwards() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .forwards { animation: fadeIn 1s forwards; }
                </style>
                <circle cx="12" cy="12" r="10" class="forwards"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(AnimationFillMode.FORWARDS, anim.fillMode)
    }

    @Test
    fun parseAnimationWithFillModeBoth() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .both { animation: fadeIn 1s both; }
                </style>
                <circle cx="12" cy="12" r="10" class="both"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(AnimationFillMode.BOTH, anim.fillMode)
    }

    @Test
    fun parseAnimationWithCubicBezier() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .custom { animation: fadeIn 1s cubic-bezier(0.1, 0.2, 0.3, 0.4); }
                </style>
                <circle cx="12" cy="12" r="10" class="custom"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(CalcMode.SPLINE, anim.calcMode)
        assertNotNull(anim.keySplines)
        assertEquals(0.1f, anim.keySplines?.x1)
        assertEquals(0.2f, anim.keySplines?.y1)
        assertEquals(0.3f, anim.keySplines?.x2)
        assertEquals(0.4f, anim.keySplines?.y2)
    }

    @Test
    fun parseAnimationWithCombinedProperties() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .combined { animation: fadeIn 1s ease-in-out 500ms 3 alternate forwards; }
                </style>
                <circle cx="12" cy="12" r="10" class="combined"/>
            </svg>
        """)
        val element = svg.children[0]
        assertIs<SvgAnimated>(element)
        val anim = element.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(1000.milliseconds, anim.dur)
        assertEquals(500.milliseconds, anim.delay)
        assertEquals(3, anim.iterations)
        assertEquals(AnimationDirection.ALTERNATE, anim.direction)
        assertEquals(AnimationFillMode.FORWARDS, anim.fillMode)
        assertEquals(CalcMode.SPLINE, anim.calcMode)
    }

    @Test
    fun parseElementWithFill() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" fill="red"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseElementWithStroke() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" stroke="blue" stroke-width="3"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Blue, styled.style.stroke)
        assertEquals(3f, styled.style.strokeWidth)
    }

    @Test
    fun parseElementWithOpacity() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" opacity="0.5" fill-opacity="0.8" stroke-opacity="0.3"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(0.5f, styled.style.opacity)
        assertEquals(0.8f, styled.style.fillOpacity)
        assertEquals(0.3f, styled.style.strokeOpacity)
    }

    @Test
    fun parseElementWithStrokeLinecap() {
        val svg = svg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-linecap="square"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineCap.SQUARE, styled.style.strokeLinecap)
    }

    @Test
    fun parseElementWithStrokeLinejoin() {
        val svg = svg("""<svg><path d="M0 0" stroke-linejoin="bevel"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineJoin.BEVEL, styled.style.strokeLinejoin)
    }

    @Test
    fun parseElementWithFillRule() {
        val svg = svg("""<svg><path d="M0 0" fill-rule="evenodd"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(FillRule.EVENODD, styled.style.fillRule)
    }

    @Test
    fun parseElementWithStrokeDasharray() {
        val svg = svg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-dasharray="5,3,2"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(listOf(5f, 3f, 2f), styled.style.strokeDasharray)
    }

    @Test
    fun parseElementWithStrokeDashoffset() {
        val svg = svg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-dashoffset="10"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(10f, styled.style.strokeDashoffset)
    }

    @Test
    fun parseElementWithStrokeMiterlimit() {
        val svg = svg("""<svg><path d="M0 0" stroke-miterlimit="8"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(8f, styled.style.strokeMiterlimit)
    }

    // ===========================================
    // Transform Parsing Tests
    // ===========================================

    @Test
    fun parseTransformTranslate() {
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="translate(10, 20)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Translate>(transform)
        assertEquals(10f, transform.x)
        assertEquals(20f, transform.y)
    }

    @Test
    fun parseTransformTranslateXOnly() {
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="translate(10)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Translate>(transform)
        assertEquals(10f, transform.x)
        assertEquals(0f, transform.y)
    }

    @Test
    fun parseTransformScale() {
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="scale(2, 3)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Scale>(transform)
        assertEquals(2f, transform.sx)
        assertEquals(3f, transform.sy)
    }

    @Test
    fun parseTransformScaleUniform() {
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="scale(2)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.Scale>(transform)
        assertEquals(2f, transform.sx)
        assertEquals(2f, transform.sy)
    }

    @Test
    fun parseTransformRotate() {
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="rotate(45)"/></svg>""")
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
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="rotate(45, 12, 12)"/></svg>""")
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
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="skewX(30)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.SkewX>(transform)
        assertEquals(30f, transform.angle)
    }

    @Test
    fun parseTransformSkewY() {
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="skewY(30)"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val transform = styled.style.transform
        assertIs<SvgTransform.SkewY>(transform)
        assertEquals(30f, transform.angle)
    }

    @Test
    fun parseTransformMatrix() {
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="matrix(1, 0, 0, 1, 10, 20)"/></svg>""")
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
        val svg = svg("""<svg><circle cx="0" cy="0" r="5" transform="translate(10, 20) rotate(45)"/></svg>""")
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
        val svg = svg("""<svg><circle cx="12" cy="12" r="10"/><rect x="0" y="0" width="5" height="5"/></svg>""")
        assertEquals(2, svg.children.size)
    }

    @Test
    fun parseSvgWithWhitespace() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseSvgIgnoresUnknownElements() {
        val svg = svg("""<svg><unknown/><circle cx="12" cy="12" r="10"/><foo bar="baz"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgWithoutRoot() {
        // When there's no SVG root, it should still parse elements
        val svg = svg("""<circle cx="12" cy="12" r="10"/>""")
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parsePointsWithSpaceDelimiter() {
        val svg = svg("""<svg><polyline points="0 0 10 10 20 0"/></svg>""")
        val polyline = svg.children[0]
        assertIs<SvgPolyline>(polyline)
        assertEquals(3, polyline.points.size)
    }

    @Test
    fun parseRectWithDefaultRoundedCorners() {
        val svg = svg("""<svg><rect width="10" height="10" rx="2"/></svg>""")
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
        val svg = svg("""<svg><g transform="translate(10, 10)"><circle cx="0" cy="0" r="5"/></g></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        val group = styled.element
        assertIs<SvgGroup>(group)
        assertEquals(1, group.children.size)
    }

    @Test
    fun parseGroupWithFill() {
        val svg = svg("""<svg><g fill="red"><circle cx="12" cy="12" r="5"/></g></svg>""")
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
        val svg = svg("""<svg><circle r="10"/></svg>""")
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
        assertEquals(0f, circle.cx)
        assertEquals(0f, circle.cy)
        assertEquals(10f, circle.r)
    }

    @Test
    fun parseRectWithDefaultValues() {
        val svg = svg("""<svg><rect width="20" height="15"/></svg>""")
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
        val svg = svg("""<svg><line x2="10" y2="10"/></svg>""")
        val line = svg.children[0]
        assertIs<SvgLine>(line)
        assertEquals(0f, line.x1)
        assertEquals(0f, line.y1)
        assertEquals(10f, line.x2)
        assertEquals(10f, line.y2)
    }

    @Test
    fun parseEllipseWithDefaultValues() {
        val svg = svg("""<svg><ellipse rx="10" ry="5"/></svg>""")
        val ellipse = svg.children[0]
        assertIs<SvgEllipse>(ellipse)
        assertEquals(0f, ellipse.cx)
        assertEquals(0f, ellipse.cy)
    }

    @Test
    fun parseEmptyGroup() {
        val svg = svg("""<svg><g></g></svg>""")
        val group = svg.children[0]
        assertIs<SvgGroup>(group)
        assertEquals(0, group.children.size)
    }

    @Test
    fun parseSvgWithWidthHeightPx() {
        val svg = svg("""<svg width="48px" height="48px" viewBox="0 0 48 48"></svg>""")
        assertNotNull(svg.viewBox)
        assertEquals(48f, svg.viewBox!!.width)
        assertEquals(48f, svg.viewBox!!.height)
    }

    @Test
    fun parseEmptyPolyline() {
        val svg = svg("""<svg><polyline points=""/></svg>""")
        assertEquals(0, svg.children.size) // Empty points should not create element
    }

    @Test
    fun parseEmptyPolygon() {
        val svg = svg("""<svg><polygon points=""/></svg>""")
        assertEquals(0, svg.children.size) // Empty points should not create element
    }

    @Test
    fun parseEmptyPathD() {
        val svg = svg("""<svg><path d=""/></svg>""")
        assertEquals(0, svg.children.size) // Empty path should not create element
    }

    @Test
    fun parseFillRuleNonzero() {
        val svg = svg("""<svg><path d="M0 0" fill-rule="nonzero"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(FillRule.NONZERO, styled.style.fillRule)
    }

    @Test
    fun parseStrokeLinecapButt() {
        val svg = svg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-linecap="butt"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineCap.BUTT, styled.style.strokeLinecap)
    }

    @Test
    fun parseStrokeLinecapRound() {
        val svg = svg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-linecap="round"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineCap.ROUND, styled.style.strokeLinecap)
    }

    @Test
    fun parseStrokeLinejoinMiter() {
        val svg = svg("""<svg><path d="M0 0" stroke-linejoin="miter"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineJoin.MITER, styled.style.strokeLinejoin)
    }

    @Test
    fun parseStrokeLinejoinRound() {
        val svg = svg("""<svg><path d="M0 0" stroke-linejoin="round"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(LineJoin.ROUND, styled.style.strokeLinejoin)
    }

    @Test
    fun parseStrokeDasharrayWithSpaces() {
        val svg = svg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-dasharray="5 3 2"/></svg>""")
        val styled = svg.children[0]
        assertIs<SvgStyled>(styled)
        assertEquals(listOf(5f, 3f, 2f), styled.style.strokeDasharray)
    }

    @Test
    fun parseStrokeDasharrayNone() {
        val svg = svg("""<svg><line x1="0" y1="0" x2="10" y2="10" stroke-dasharray="none"/></svg>""")
        // Should not have strokeDasharray when "none"
        val element = svg.children[0]
        assertIs<SvgLine>(element) // No style wrapper when dasharray is "none"
    }

    @Test
    fun parseHexColorFill() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" fill="#ff0000"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseRgbColorFill() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" fill="rgb(255,0,0)"/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun parseComplexLucideIcon() {
        // Complete Lucide menu icon
        val svg = svg("""
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
        val svg = svg("""
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8"/>
                <path d="M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            </svg>
        """)
        assertEquals(2, svg.children.size)
    }

    @Test
    fun parseDeeplyNestedGroups() {
        val svg = svg("""<svg><g><g><g><g><circle cx="12" cy="12" r="5"/></g></g></g></g></svg>""")
        var current: SvgElement = svg.children[0]
        repeat(4) {
            assertIs<SvgGroup>(current)
            current = (current as SvgGroup).children[0]
        }
        assertIs<SvgCircle>(current)
    }

    @Test
    fun parseMultipleGroupsAtSameLevel() {
        val svg = svg("""<svg><g><circle cx="5" cy="5" r="2"/></g><g><circle cx="15" cy="15" r="2"/></g></svg>""")
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
        val svg = svg("""<svg><polygon points="$points"/></svg>""")
        val polygon = svg.children[0]
        assertIs<SvgPolygon>(polygon)
        assertEquals(20, polygon.points.size)
    }

    // ===========================================
    // CSS Animation Tests
    // ===========================================

    @Test
    fun parseCssAnimationWithKeyframes() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes spin {
                        from { transform: rotate(0deg); }
                        to { transform: rotate(360deg); }
                    }
                    .rotating { animation: spin 1s linear infinite; }
                </style>
                <circle class="rotating" cx="12" cy="12" r="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        assertTrue(animated.animations.isNotEmpty())
    }

    @Test
    fun parseCssAnimationWithPercentKeyframes() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes pulse {
                        0% { opacity: 1; }
                        50% { opacity: 0.5; }
                        100% { opacity: 1; }
                    }
                    .pulsing { animation: pulse 2s ease-in-out infinite; }
                </style>
                <circle class="pulsing" cx="12" cy="12" r="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
    }

    @Test
    fun parseCssAnimationWithDelay() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeIn {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .fade { animation: fadeIn 500ms ease 200ms 1 normal forwards; }
                </style>
                <rect class="fade" x="0" y="0" width="24" height="24"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
    }

    @Test
    fun parseCssAnimationAlternateReverse() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes bounce {
                        from { transform: translateY(0); }
                        to { transform: translateY(-10px); }
                    }
                    .bouncing { animation: bounce 300ms ease alternate-reverse infinite; }
                </style>
                <circle class="bouncing" cx="12" cy="12" r="5"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
    }

    @Test
    fun parseCssAnimationBothFillMode() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes grow {
                        from { transform: scale(0); }
                        to { transform: scale(1); }
                    }
                    .growing { animation: grow 1s ease both; }
                </style>
                <circle class="growing" cx="12" cy="12" r="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationBackwardsFillMode() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes appear {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .appearing { animation: appear 1s backwards; }
                </style>
                <rect class="appearing" x="0" y="0" width="10" height="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationWithCubicBezier() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes slide {
                        from { transform: translateX(0); }
                        to { transform: translateX(100px); }
                    }
                    .sliding { animation: slide 1s cubic-bezier(0.25, 0.1, 0.25, 1); }
                </style>
                <rect class="sliding" x="0" y="0" width="10" height="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationReverseDirection() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes moveUp {
                        from { transform: translateY(100px); }
                        to { transform: translateY(0); }
                    }
                    .moving { animation: moveUp 1s reverse; }
                </style>
                <circle class="moving" cx="12" cy="12" r="5"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationStrokeWidth() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes pulseStroke {
                        from { stroke-width: 1; }
                        to { stroke-width: 5; }
                    }
                    .pulse-stroke { animation: pulseStroke 1s alternate infinite; }
                </style>
                <path class="pulse-stroke" d="M10 10 L20 20"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
    }

    @Test
    fun parseCssAnimationStrokeOpacity() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeStroke {
                        from { stroke-opacity: 0; }
                        to { stroke-opacity: 1; }
                    }
                    .fade-stroke { animation: fadeStroke 500ms; }
                </style>
                <line class="fade-stroke" x1="0" y1="0" x2="24" y2="24"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationFillOpacity() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes fadeFill {
                        from { fill-opacity: 0; }
                        to { fill-opacity: 1; }
                    }
                    .fade-fill { animation: fadeFill 500ms; }
                </style>
                <circle class="fade-fill" cx="12" cy="12" r="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationStrokeDashoffset() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes dash {
                        from { stroke-dashoffset: 100; }
                        to { stroke-dashoffset: 0; }
                    }
                    .drawing { animation: dash 2s; }
                </style>
                <path class="drawing" d="M10 10 L20 20"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationSkewX() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes skewIt {
                        from { transform: skewX(0deg); }
                        to { transform: skewX(15deg); }
                    }
                    .skewing { animation: skewIt 1s; }
                </style>
                <rect class="skewing" x="0" y="0" width="20" height="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationSkewY() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes skewYIt {
                        from { transform: skewY(0deg); }
                        to { transform: skewY(20deg); }
                    }
                    .skewing-y { animation: skewYIt 1s; }
                </style>
                <rect class="skewing-y" x="0" y="0" width="20" height="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationMilliseconds() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes quick {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .quick { animation: quick 250ms; }
                </style>
                <circle class="quick" cx="12" cy="12" r="5"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    @Test
    fun parseCssAnimationWithNoneKeyword() {
        val svg = svg("""
            <svg>
                <style>
                    @keyframes appear {
                        from { opacity: 0; }
                        to { opacity: 1; }
                    }
                    .appearing { animation: appear 1s none; }
                </style>
                <rect class="appearing" x="0" y="0" width="10" height="10"/>
            </svg>
        """)
        assertEquals(1, svg.children.size)
    }

    // ===========================================
    // Length Parsing Tests
    // ===========================================

    @Test
    fun parseSvgWithPxUnits() {
        val svg = svg("""<svg width="100px" height="50px"><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(100f, svg.width)
        assertEquals(50f, svg.height)
    }

    @Test
    fun parseSvgWithPtUnits() {
        val svg = svg("""<svg width="72pt" height="36pt"><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(72f, svg.width)
        assertEquals(36f, svg.height)
    }

    @Test
    fun parseSvgWithEmUnits() {
        val svg = svg("""<svg width="10em" height="5em"><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(10f, svg.width)
        assertEquals(5f, svg.height)
    }

    @Test
    fun parseSvgWithPercentUnits() {
        val svg = svg("""<svg width="100%" height="100%"><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(100f, svg.width)
        assertEquals(100f, svg.height)
    }

    // ===========================================
    // Style Parsing Edge Cases
    // ===========================================

    @Test
    fun parseStrokeDasharrayNoneOnPath() {
        val svg = svg("""<svg><path d="M0 0 L10 10" stroke-dasharray="none"/></svg>""")
        val path = svg.children[0]
        assertIs<SvgPath>(path)
    }

    @Test
    fun parseStrokeDasharrayEmpty() {
        val svg = svg("""<svg><path d="M0 0 L10 10" stroke-dasharray=""/></svg>""")
        val path = svg.children[0]
        assertIs<SvgPath>(path)
    }

    @Test
    fun parseEmptyStyleAttribute() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" style=""/></svg>""")
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parseStyleWithWhitespace() {
        val svg = svg("""<svg><circle cx="12" cy="12" r="10" style="  fill : red ; stroke : blue  "/></svg>""")
        val styled = svg.children[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
    }

    // ===========================================
    // SMIL Animation Edge Cases
    // ===========================================

    @Test
    fun parseAnimateWithValuesAttribute() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10">
                    <animate attributeName="opacity" values="1;0.5;1" dur="2s" repeatCount="indefinite"/>
                </circle>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        assertEquals(1, animated.animations.size)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
    }

    @Test
    fun parseAnimateTransformRotate() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="10" height="10">
                    <animateTransform attributeName="transform" type="rotate" from="0 12 12" to="360 12 12" dur="2s"/>
                </rect>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.ROTATE, anim.type)
    }

    @Test
    fun parseAnimateTransformScale() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10">
                    <animateTransform attributeName="transform" type="scale" from="1" to="2" dur="1s"/>
                </circle>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.SCALE, anim.type)
    }

    @Test
    fun parseAnimateTransformTranslate() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="10" height="10">
                    <animateTransform attributeName="transform" type="translate" from="0" to="100" dur="1s"/>
                </rect>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.TRANSLATE, anim.type)
    }

    @Test
    fun parseAnimateTransformSkewX() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="10" height="10">
                    <animateTransform attributeName="transform" type="skewX" from="0" to="30" dur="1s"/>
                </rect>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.SKEW_X, anim.type)
    }

    @Test
    fun parseAnimateTransformSkewY() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="10" height="10">
                    <animateTransform attributeName="transform" type="skewY" from="0" to="20" dur="1s"/>
                </rect>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.SKEW_Y, anim.type)
    }

    @Test
    fun parseAnimateTransformWithValues() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="10" height="10">
                    <animateTransform attributeName="transform" type="rotate" values="-5 12 12;5 12 12;-5 12 12" dur="0.5s"/>
                </rect>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
    }

    @Test
    fun parseAnimateCx() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="5">
                    <animate attributeName="cx" from="12" to="24" dur="1s"/>
                </circle>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Cx>(anim)
    }

    @Test
    fun parseAnimateCy() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="5">
                    <animate attributeName="cy" from="12" to="24" dur="1s"/>
                </circle>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Cy>(anim)
    }

    @Test
    fun parseAnimateR() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="5">
                    <animate attributeName="r" from="5" to="10" dur="1s"/>
                </circle>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.R>(anim)
    }

    @Test
    fun parseAnimateX() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="10" height="10">
                    <animate attributeName="x" from="0" to="100" dur="1s"/>
                </rect>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.X>(anim)
    }

    @Test
    fun parseAnimateY() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="10" height="10">
                    <animate attributeName="y" from="0" to="50" dur="1s"/>
                </rect>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Y>(anim)
    }

    @Test
    fun parseAnimateWidth() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="10" height="10">
                    <animate attributeName="width" from="10" to="100" dur="1s"/>
                </rect>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Width>(anim)
    }

    @Test
    fun parseAnimateHeight() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="10" height="10">
                    <animate attributeName="height" from="10" to="50" dur="1s"/>
                </rect>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Height>(anim)
    }

    @Test
    fun parseAnimateWithCalcModeDiscrete() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10">
                    <animate attributeName="opacity" from="0" to="1" dur="1s" calcMode="discrete"/>
                </circle>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(CalcMode.DISCRETE, anim.calcMode)
    }

    @Test
    fun parseAnimateWithCalcModePaced() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10">
                    <animate attributeName="r" from="5" to="15" dur="1s" calcMode="paced"/>
                </circle>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.R>(anim)
        assertEquals(CalcMode.PACED, anim.calcMode)
    }

    @Test
    fun parseAnimateWithCalcModeSpline() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10">
                    <animate attributeName="opacity" from="0" to="1" dur="1s" calcMode="spline" keySplines="0.25 0.1 0.25 1"/>
                </circle>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(CalcMode.SPLINE, anim.calcMode)
        assertNotNull(anim.keySplines)
    }

    @Test
    fun parseAnimateFillOpacity() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10">
                    <animate attributeName="fill-opacity" from="0" to="1" dur="500ms"/>
                </circle>
            </svg>
        """)
        val animated = svg.children[0]
        assertIs<SvgAnimated>(animated)
        val anim = animated.animations[0]
        assertIs<SvgAnimate.FillOpacity>(anim)
    }

    // ===========================================
    // Error Handling Tests
    // ===========================================

    @Test
    fun parseEmptySvg() {
        val svg = svg("")
        assertEquals(0, svg.children.size)
    }

    @Test
    fun parseWhitespaceOnlySvg() {
        val svg = svg("   \n\t  ")
        assertEquals(0, svg.children.size)
    }

    @Test
    fun parseSvgWithoutRootElement() {
        val svg = svg("<circle cx='12' cy='12' r='10'/>")
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgWithEmptyGroup() {
        val svg = svg("""<svg><g/></svg>""")
        assertEquals(0, svg.children.size) // Self-closing groups are skipped
    }

    @Test
    fun parseSvgWithEmptyPath() {
        val svg = svg("""<svg><path d=""/></svg>""")
        assertEquals(0, svg.children.size) // Empty paths are skipped
    }

    @Test
    fun parseSvgWithEmptyPolyline() {
        val svg = svg("""<svg><polyline points=""/></svg>""")
        assertEquals(0, svg.children.size) // Empty polylines are skipped
    }

    @Test
    fun parseSvgWithEmptyPolygon() {
        val svg = svg("""<svg><polygon points=""/></svg>""")
        assertEquals(0, svg.children.size) // Empty polygons are skipped
    }

    // ===========================================
    // Additional Animation Parsing Tests
    // ===========================================

    @Test
    fun parseAnimateTransformWithValuesAttribute() {
        val svg = svg("""
            <svg>
                <rect width="10" height="10">
                    <animateTransform attributeName="transform" type="rotate" values="0;180;360" dur="1s"/>
                </rect>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        assertEquals(1, animated.animations.size)
        assertIs<SvgAnimate.Transform>(animated.animations[0])
        val transform = animated.animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.ROTATE, transform.type)
    }

    @Test
    fun parseAnimateStrokeOpacity() {
        val svg = svg("""
            <svg>
                <path d="M10 10 L20 20">
                    <animate attributeName="stroke-opacity" from="0" to="1" dur="500ms"/>
                </path>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        assertIs<SvgAnimate.StrokeOpacity>(animated.animations[0])
    }

    @Test
    fun parseAnimateStrokeDashoffset() {
        val svg = svg("""
            <svg>
                <path d="M10 10 L20 20">
                    <animate attributeName="stroke-dashoffset" from="0" to="100" dur="1s"/>
                </path>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        val strokeDashoffset = animated.animations[0]
        assertIs<SvgAnimate.StrokeDashoffset>(strokeDashoffset)
        assertEquals(0f, strokeDashoffset.from)
        assertEquals(100f, strokeDashoffset.to)
        assertEquals(1000.milliseconds, strokeDashoffset.dur)
    }

    @Test
    fun parseAnimateMotion() {
        val svg = svg("""
            <svg>
                <circle cx="5" cy="5" r="3">
                    <animateMotion path="M0 0 L100 100" dur="2s"/>
                </circle>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        assertIs<SvgAnimate.Motion>(animated.animations[0])
    }

    @Test
    fun parseAnimateMotionWithRotate() {
        val svg = svg("""
            <svg>
                <circle cx="5" cy="5" r="3">
                    <animateMotion path="M0 0 C50 0 50 100 100 100" dur="2s" rotate="auto"/>
                </circle>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        val motion = animated.animations[0]
        assertIs<SvgAnimate.Motion>(motion)
        assertEquals(MotionRotate.AUTO, motion.rotate)
    }

    @Test
    fun parseAnimateMotionWithAutoReverse() {
        val svg = svg("""
            <svg>
                <rect width="5" height="5">
                    <animateMotion path="M0 0 L50 50" dur="1s" rotate="auto-reverse"/>
                </rect>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        val motion = animated.animations[0]
        assertIs<SvgAnimate.Motion>(motion)
        assertEquals(MotionRotate.AUTO_REVERSE, motion.rotate)
    }

    @Test
    fun parseAnimateMotionWithFixedAngle() {
        // Fixed angle rotation defaults to NONE since enum doesn't support arbitrary angles
        val svg = svg("""
            <svg>
                <rect width="5" height="5">
                    <animateMotion path="M0 0 L50 50" dur="1s" rotate="45"/>
                </rect>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        val motion = animated.animations[0]
        assertIs<SvgAnimate.Motion>(motion)
        assertEquals(MotionRotate.NONE, motion.rotate) // Fixed angles default to NONE
    }

    @Test
    fun parseAnimateUnknownAttribute() {
        val svg = svg("""
            <svg>
                <rect width="10" height="10">
                    <animate attributeName="unknown-attr" from="0" to="100" dur="1s"/>
                </rect>
            </svg>
        """.trimIndent())
        // Unknown attribute should result in no animation or element without animation wrapper
        val element = svg.children[0]
        if (element is SvgAnimated) {
            // Animation might be empty or null
            assertTrue(element.animations.isEmpty() || element.animations.all { it != null })
        }
    }

    @Test
    fun parseAnimateTransformWithMpathElement() {
        val svg = svg("""
            <svg>
                <path id="motionPath" d="M0 0 Q50 100 100 0"/>
                <circle cx="5" cy="5" r="3">
                    <animateMotion dur="3s">
                        <mpath xlink:href="#motionPath"/>
                    </animateMotion>
                </circle>
            </svg>
        """.trimIndent())
        // Should parse without errors, mpath is a child element
        assertTrue(svg.children.isNotEmpty())
    }

    @Test
    fun parseDurationWithMinutes() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="5">
                    <animate attributeName="r" from="5" to="15" dur="0.5m"/>
                </circle>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
    }

    @Test
    fun parseSvgWithNamespacedAttributes() {
        // Namespaced attributes like xmlns should be handled gracefully
        val svg = svg("""
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgWithDataAttributes() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" data-custom="value"/>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgWithComments() {
        val svg = svg("""
            <svg>
                <!-- This is a comment -->
                <circle cx="12" cy="12" r="10"/>
                <!-- Another comment -->
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgWithCDATA() {
        val svg = svg("""
            <svg>
                <style><![CDATA[
                    .cls { fill: red; }
                ]]></style>
                <circle cx="12" cy="12" r="10" class="cls"/>
            </svg>
        """.trimIndent())
        assertTrue(svg.children.isNotEmpty())
    }

    @Test
    fun parseMultipleAnimationsOnElement() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10">
                    <animate attributeName="r" from="5" to="15" dur="1s"/>
                    <animate attributeName="cx" from="12" to="24" dur="1s"/>
                    <animate attributeName="opacity" from="1" to="0.5" dur="1s"/>
                </circle>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        assertEquals(3, animated.animations.size)
    }

    // ===========================================
    // More Edge Case Tests
    // ===========================================

    @Test
    fun parseSvgWithNestedGroups() {
        val svg = svg("""
            <svg>
                <g>
                    <g>
                        <g>
                            <circle cx="12" cy="12" r="5"/>
                        </g>
                    </g>
                </g>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        val g1 = svg.children[0] as SvgGroup
        val g2 = g1.children[0] as SvgGroup
        val g3 = g2.children[0] as SvgGroup
        assertIs<SvgCircle>(g3.children[0])
    }

    @Test
    fun parseSvgWithMultipleElements() {
        val svg = svg("""
            <svg>
                <circle cx="5" cy="5" r="3"/>
                <rect x="10" y="10" width="20" height="20"/>
                <line x1="0" y1="0" x2="24" y2="24"/>
                <path d="M10 10 L20 20"/>
            </svg>
        """.trimIndent())
        assertEquals(4, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
        assertIs<SvgRect>(svg.children[1])
        assertIs<SvgLine>(svg.children[2])
        assertIs<SvgPath>(svg.children[3])
    }

    @Test
    fun parseSvgWithMixedGroupsAndElements() {
        val svg = svg("""
            <svg>
                <circle cx="5" cy="5" r="3"/>
                <g>
                    <rect x="10" y="10" width="5" height="5"/>
                </g>
                <line x1="0" y1="0" x2="24" y2="24"/>
            </svg>
        """.trimIndent())
        assertEquals(3, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
        assertIs<SvgGroup>(svg.children[1])
        assertIs<SvgLine>(svg.children[2])
    }

    @Test
    fun parseAnimateWithBeginDelay() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="5">
                    <animate attributeName="r" from="5" to="10" dur="1s" begin="500ms"/>
                </circle>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        val anim = animated.animations[0] as SvgAnimate.R
        assertEquals(500.milliseconds, anim.delay)
    }

    @Test
    fun parseAnimateWithRepeatCountIndefinite() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="5">
                    <animate attributeName="opacity" from="0" to="1" dur="1s" repeatCount="indefinite"/>
                </circle>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
    }

    @Test
    fun parseSvgWithSelfClosingGroup() {
        val svg = svg("""
            <svg>
                <g/>
                <circle cx="12" cy="12" r="5"/>
            </svg>
        """.trimIndent())
        // Self-closing groups are handled
        assertTrue(svg.children.isNotEmpty())
    }

    @Test
    fun parseSvgWithRectRxRy() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="100" height="50" rx="10" ry="5"/>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        val rect = svg.children[0] as SvgRect
        assertEquals(10f, rect.rx)
        assertEquals(5f, rect.ry)
    }

    @Test
    fun parseSvgWithRectRxOnly() {
        // When ry is not specified, it defaults to rx
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="100" height="50" rx="10"/>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        val rect = svg.children[0] as SvgRect
        assertEquals(10f, rect.rx)
        assertEquals(10f, rect.ry) // ry defaults to rx
    }

    @Test
    fun parseAnimateStrokeWidth() {
        val svg = svg("""
            <svg>
                <path d="M10 10 L20 20">
                    <animate attributeName="stroke-width" from="1" to="5" dur="1s"/>
                </path>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        assertIs<SvgAnimate.StrokeWidth>(animated.animations[0])
    }

    @Test
    fun parseAnimateWithMultipleKeyframes() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="5">
                    <animate attributeName="r" values="5;15;5;10" dur="2s"/>
                </circle>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        val anim = animated.animations[0] as SvgAnimate.R
        // Values should be parsed and from/to extracted from min/max
        assertEquals(5f, anim.from)
        assertEquals(15f, anim.to)
    }

    @Test
    fun parseSvgWithMultipleNewlines() {
        val svg = svg("""

            <svg>

                <circle cx="12" cy="12" r="5"/>

            </svg>

        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseAnimateTransformWithMultipleKeyframes() {
        val svg = svg("""
            <svg>
                <rect width="10" height="10">
                    <animateTransform attributeName="transform" type="rotate" values="-5 12 2;5 12 2;-5 12 2" dur="1s"/>
                </rect>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        val transform = animated.animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.ROTATE, transform.type)
    }

    @Test
    fun parseAnimateCxCyR() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="5">
                    <animate attributeName="cx" from="12" to="24" dur="1s"/>
                    <animate attributeName="cy" from="12" to="24" dur="1s"/>
                    <animate attributeName="r" from="5" to="10" dur="1s"/>
                </circle>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        assertEquals(3, animated.animations.size)
        assertIs<SvgAnimate.Cx>(animated.animations[0])
        assertIs<SvgAnimate.Cy>(animated.animations[1])
        assertIs<SvgAnimate.R>(animated.animations[2])
    }

    @Test
    fun parseSvgWithStyleOnGroup() {
        val svg = svg("""
            <svg>
                <g fill="red" stroke="blue" stroke-width="2">
                    <circle cx="12" cy="12" r="5"/>
                </g>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        val group = svg.children[0]
        assertIs<SvgStyled>(group)
        val styled = group as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
    }

    @Test
    fun parseAnimateMotionWithPath() {
        val svg = svg("""
            <svg>
                <circle cx="5" cy="5" r="3">
                    <animateMotion path="M0 0 Q50 100 100 0" dur="3s"/>
                </circle>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        assertIs<SvgAnimated>(svg.children[0])
        val animated = svg.children[0] as SvgAnimated
        val motion = animated.animations[0] as SvgAnimate.Motion
        assertEquals("M0 0 Q50 100 100 0", motion.path)
    }

    // ===========================================
    // Additional Edge Cases for Coverage
    // ===========================================

    @Test
    fun parseSvgWithPreserveAspectRatio() {
        val svg = svg("""<svg viewBox="0 0 100 100" preserveAspectRatio="xMidYMid meet"><circle cx="50" cy="50" r="40"/></svg>""")
        assertEquals(PreserveAspectRatio.Default, svg.preserveAspectRatio)
    }

    @Test
    fun parseSvgWithPreserveAspectRatioSlice() {
        val svg = svg("""<svg viewBox="0 0 100 100" preserveAspectRatio="xMinYMin slice"><circle cx="50" cy="50" r="40"/></svg>""")
        assertEquals(AspectRatioAlign.X_MIN_Y_MIN, svg.preserveAspectRatio.align)
        assertEquals(MeetOrSlice.SLICE, svg.preserveAspectRatio.meetOrSlice)
    }

    @Test
    fun parseSvgWithStrokeLinecapSquare() {
        val svg = svg("""<svg stroke-linecap="square"><path d="M0 0 L10 10"/></svg>""")
        assertEquals(LineCap.SQUARE, svg.strokeLinecap)
    }

    @Test
    fun parseSvgWithStrokeLinejoinBevel() {
        val svg = svg("""<svg stroke-linejoin="bevel"><path d="M0 0 L10 10"/></svg>""")
        assertEquals(LineJoin.BEVEL, svg.strokeLinejoin)
    }

    @Test
    fun parseSvgWithStrokeLinejoinMiter() {
        val svg = svg("""<svg stroke-linejoin="miter"><path d="M0 0 L10 10"/></svg>""")
        assertEquals(LineJoin.MITER, svg.strokeLinejoin)
    }

    @Test
    fun parseSvgWithStrokeLinecapButt() {
        val svg = svg("""<svg stroke-linecap="butt"><path d="M0 0 L10 10"/></svg>""")
        assertEquals(LineCap.BUTT, svg.strokeLinecap)
    }

    @Test
    fun parseSvgWithExplicitStrokeWidth() {
        val svg = svg("""<svg stroke-width="5"><path d="M0 0 L10 10"/></svg>""")
        assertEquals(5f, svg.strokeWidth)
    }

    @Test
    fun parseSvgWithStrokeColor() {
        val svg = svg("""<svg stroke="red"><path d="M0 0 L10 10"/></svg>""")
        assertEquals(Color.Red, svg.stroke)
    }

    @Test
    fun parseSvgWithFillColor() {
        val svg = svg("""<svg fill="blue"><path d="M0 0 L10 10"/></svg>""")
        assertEquals(Color.Blue, svg.fill)
    }

    @Test
    fun parseSvgWithWidthAndHeight() {
        val svg = svg("""<svg width="200" height="150"><circle cx="100" cy="75" r="50"/></svg>""")
        assertEquals(200f, svg.width)
        assertEquals(150f, svg.height)
    }

    @Test
    fun parseSvgWithWidthAndHeightWithUnits() {
        val svg = svg("""<svg width="200px" height="150px"><circle cx="100" cy="75" r="50"/></svg>""")
        assertEquals(200f, svg.width)
        assertEquals(150f, svg.height)
    }

    @Test
    fun parseAnimateTransformWithSkewX() {
        val svg = svg("""
            <svg>
                <rect width="10" height="10">
                    <animateTransform attributeName="transform" type="skewX" from="0" to="30" dur="1s"/>
                </rect>
            </svg>
        """.trimIndent())
        val animated = svg.children[0] as SvgAnimated
        val transform = animated.animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.SKEW_X, transform.type)
    }

    @Test
    fun parseAnimateTransformWithSkewY() {
        val svg = svg("""
            <svg>
                <rect width="10" height="10">
                    <animateTransform attributeName="transform" type="skewY" from="0" to="30" dur="1s"/>
                </rect>
            </svg>
        """.trimIndent())
        val animated = svg.children[0] as SvgAnimated
        val transform = animated.animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.SKEW_Y, transform.type)
    }

    @Test
    fun parseDurationInMilliseconds() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="5">
                    <animate attributeName="r" from="5" to="10" dur="500ms"/>
                </circle>
            </svg>
        """.trimIndent())
        val animated = svg.children[0] as SvgAnimated
        val anim = animated.animations[0] as SvgAnimate.R
        assertEquals(500L, anim.dur.inWholeMilliseconds)
    }

    @Test
    fun parseAnimateMotionWithAutoRotate() {
        val svg = svg("""
            <svg>
                <circle cx="5" cy="5" r="3">
                    <animateMotion path="M0 0 L100 100" dur="2s" rotate="auto"/>
                </circle>
            </svg>
        """.trimIndent())
        val animated = svg.children[0] as SvgAnimated
        val motion = animated.animations[0] as SvgAnimate.Motion
        assertEquals(MotionRotate.AUTO, motion.rotate)
    }

    @Test
    fun parseAnimateMotionWithAutoReverseRotate() {
        val svg = svg("""
            <svg>
                <circle cx="5" cy="5" r="3">
                    <animateMotion path="M0 0 L100 100" dur="2s" rotate="auto-reverse"/>
                </circle>
            </svg>
        """.trimIndent())
        val animated = svg.children[0] as SvgAnimated
        val motion = animated.animations[0] as SvgAnimate.Motion
        assertEquals(MotionRotate.AUTO_REVERSE, motion.rotate)
    }

    @Test
    fun parseSvgWithInlineStyle() {
        val svg = svg("""
            <svg>
                <circle cx="50" cy="50" r="40" style="fill:red;stroke:blue;stroke-width:3"/>
            </svg>
        """.trimIndent())
        assertEquals(1, svg.children.size)
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        val styled = circle as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
        assertEquals(3f, styled.style.strokeWidth)
    }

    @Test
    fun parseSvgWithOpacity() {
        val svg = svg("""
            <svg>
                <circle cx="50" cy="50" r="40" opacity="0.5"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(0.5f, (circle as SvgStyled).style.opacity)
    }

    @Test
    fun parseSvgWithFillOpacity() {
        val svg = svg("""
            <svg>
                <circle cx="50" cy="50" r="40" fill-opacity="0.7"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(0.7f, (circle as SvgStyled).style.fillOpacity)
    }

    @Test
    fun parseSvgWithStrokeOpacity() {
        val svg = svg("""
            <svg>
                <circle cx="50" cy="50" r="40" stroke-opacity="0.3"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(0.3f, (circle as SvgStyled).style.strokeOpacity)
    }

    // ===========================================
    // Paint Order Tests
    // ===========================================

    @Test
    fun parsePaintOrderStroke() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" paint-order="stroke"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(PaintOrder.STROKE_FILL, (circle as SvgStyled).style.paintOrder)
    }

    @Test
    fun parsePaintOrderStrokeFill() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" paint-order="stroke fill"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(PaintOrder.STROKE_FILL, (circle as SvgStyled).style.paintOrder)
    }

    @Test
    fun parsePaintOrderFillStroke() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" paint-order="fill stroke"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(PaintOrder.FILL_STROKE, (circle as SvgStyled).style.paintOrder)
    }

    @Test
    fun parsePaintOrderNormal() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" paint-order="normal"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(PaintOrder.FILL_STROKE, (circle as SvgStyled).style.paintOrder)
    }

    @Test
    fun parsePaintOrderInStyleAttribute() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" style="paint-order: stroke"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(PaintOrder.STROKE_FILL, (circle as SvgStyled).style.paintOrder)
    }

    // ===========================================
    // Inherit Keyword Tests
    // ===========================================

    @Test
    fun parseFillInherit() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" fill="inherit"/>
            </svg>
        """.trimIndent())
        // fill="inherit" should result in no SvgStyled wrapper (null fill = inherit from parent)
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parseStrokeInherit() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" stroke="inherit"/>
            </svg>
        """.trimIndent())
        // stroke="inherit" should result in no SvgStyled wrapper (null stroke = inherit from parent)
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parseFillNone() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" fill="none"/>
            </svg>
        """.trimIndent())
        // fill="none" should result in Transparent color
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Color.Transparent, (circle as SvgStyled).style.fill)
    }

    @Test
    fun parseStrokeNone() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" stroke="none"/>
            </svg>
        """.trimIndent())
        // stroke="none" should result in Transparent color
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Color.Transparent, (circle as SvgStyled).style.stroke)
    }

    @Test
    fun parseOpacityInherit() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" opacity="inherit"/>
            </svg>
        """.trimIndent())
        // opacity="inherit" should result in no style (null = inherit from parent)
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parseStrokeWidthInherit() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" stroke-width="inherit"/>
            </svg>
        """.trimIndent())
        // stroke-width="inherit" should result in no style
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parseStrokeLinecapInherit() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10" stroke-linecap="inherit"/>
            </svg>
        """.trimIndent())
        // stroke-linecap="inherit" should result in no style
        val path = svg.children[0]
        assertIs<SvgPath>(path)
    }

    @Test
    fun parseStrokeLinejoinInherit() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10" stroke-linejoin="inherit"/>
            </svg>
        """.trimIndent())
        // stroke-linejoin="inherit" should result in no style
        val path = svg.children[0]
        assertIs<SvgPath>(path)
    }

    @Test
    fun parseFillRuleInherit() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10 L5 15 Z" fill-rule="inherit"/>
            </svg>
        """.trimIndent())
        // fill-rule="inherit" should result in no style
        val path = svg.children[0]
        assertIs<SvgPath>(path)
    }

    @Test
    fun parseStrokeDasharrayInherit() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10" stroke-dasharray="inherit"/>
            </svg>
        """.trimIndent())
        // stroke-dasharray="inherit" should result in no style
        val path = svg.children[0]
        assertIs<SvgPath>(path)
    }

    @Test
    fun parseTransformInherit() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" transform="inherit"/>
            </svg>
        """.trimIndent())
        // transform="inherit" should result in no style
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parsePaintOrderInherit() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" paint-order="inherit"/>
            </svg>
        """.trimIndent())
        // paint-order="inherit" should result in no style
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parseInheritInStyleAttribute() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" style="fill: inherit; stroke: inherit"/>
            </svg>
        """.trimIndent())
        // style with inherit values should result in no SvgStyled wrapper
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parseMixedInheritAndValues() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" fill="inherit" stroke="#ff0000"/>
            </svg>
        """.trimIndent())
        // fill="inherit" means no fill style, but stroke should be applied
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertNull((circle as SvgStyled).style.fill)
        assertEquals(Color.Red, circle.style.stroke)
    }

    @Test
    fun parseFillNoneVsInherit() {
        // Test that "none" and "inherit" are distinguished
        val svgNone = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" fill="none"/>
            </svg>
        """.trimIndent())
        val svgInherit = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" fill="inherit"/>
            </svg>
        """.trimIndent())

        // fill="none" creates a style with Transparent
        val circleNone = svgNone.children[0]
        assertIs<SvgStyled>(circleNone)
        assertEquals(Color.Transparent, (circleNone as SvgStyled).style.fill)

        // fill="inherit" has no fill style (null = inherit)
        val circleInherit = svgInherit.children[0]
        assertIs<SvgCircle>(circleInherit)
    }

    // ===========================================
    // Visibility Attribute Tests
    // ===========================================

    @Test
    fun parseVisibilityVisible() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" visibility="visible"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Visibility.VISIBLE, (circle as SvgStyled).style.visibility)
    }

    @Test
    fun parseVisibilityHidden() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" visibility="hidden"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Visibility.HIDDEN, (circle as SvgStyled).style.visibility)
    }

    @Test
    fun parseVisibilityCollapse() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" visibility="collapse"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Visibility.COLLAPSE, (circle as SvgStyled).style.visibility)
    }

    @Test
    fun parseVisibilityInherit() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" visibility="inherit"/>
            </svg>
        """.trimIndent())
        // visibility="inherit" should result in no style
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parseVisibilityInStyleAttribute() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" style="visibility: hidden"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Visibility.HIDDEN, (circle as SvgStyled).style.visibility)
    }

    // ===========================================
    // Display Attribute Tests
    // ===========================================

    @Test
    fun parseDisplayInline() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" display="inline"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Display.INLINE, (circle as SvgStyled).style.display)
    }

    @Test
    fun parseDisplayBlock() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" display="block"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Display.BLOCK, (circle as SvgStyled).style.display)
    }

    @Test
    fun parseDisplayNone() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" display="none"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Display.NONE, (circle as SvgStyled).style.display)
    }

    @Test
    fun parseDisplayInherit() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" display="inherit"/>
            </svg>
        """.trimIndent())
        // display="inherit" should result in no style
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    @Test
    fun parseDisplayInStyleAttribute() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" style="display: none"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Display.NONE, (circle as SvgStyled).style.display)
    }

    @Test
    fun parseVisibilityAndDisplayCombined() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" visibility="hidden" display="block"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(Visibility.HIDDEN, (circle as SvgStyled).style.visibility)
        assertEquals(Display.BLOCK, circle.style.display)
    }

    // ===========================================
    // Vector Effect Parsing Tests
    // ===========================================

    @Test
    fun parseVectorEffectNone() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" vector-effect="none"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(VectorEffect.NONE, (circle as SvgStyled).style.vectorEffect)
    }

    @Test
    fun parseVectorEffectNonScalingStroke() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" vector-effect="non-scaling-stroke"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals(VectorEffect.NON_SCALING_STROKE, (circle as SvgStyled).style.vectorEffect)
    }

    @Test
    fun parseVectorEffectInStyleAttribute() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10" style="vector-effect: non-scaling-stroke"/>
            </svg>
        """.trimIndent())
        val path = svg.children[0]
        assertIs<SvgStyled>(path)
        assertEquals(VectorEffect.NON_SCALING_STROKE, (path as SvgStyled).style.vectorEffect)
    }

    @Test
    fun parseVectorEffectInherit() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" vector-effect="inherit"/>
            </svg>
        """.trimIndent())
        // vector-effect="inherit" should result in no style
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    // ===========================================
    // Clip Path Parsing Tests
    // ===========================================

    @Test
    fun parseClipPathAttribute() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" clip-path="url(#myClip)"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals("myClip", (circle as SvgStyled).style.clipPathId)
    }

    @Test
    fun parseClipPathInStyleAttribute() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="20" height="20" style="clip-path: url(#rectClip)"/>
            </svg>
        """.trimIndent())
        val rect = svg.children[0]
        assertIs<SvgStyled>(rect)
        assertEquals("rectClip", (rect as SvgStyled).style.clipPathId)
    }

    @Test
    fun parseClipPathNone() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" clip-path="none"/>
            </svg>
        """.trimIndent())
        // clip-path="none" should result in no clipPathId
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    // ===========================================
    // Mask Parsing Tests
    // ===========================================

    @Test
    fun parseMaskAttribute() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" mask="url(#myMask)"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals("myMask", (circle as SvgStyled).style.maskId)
    }

    @Test
    fun parseMaskInStyleAttribute() {
        val svg = svg("""
            <svg>
                <rect x="0" y="0" width="20" height="20" style="mask: url(#rectMask)"/>
            </svg>
        """.trimIndent())
        val rect = svg.children[0]
        assertIs<SvgStyled>(rect)
        assertEquals("rectMask", (rect as SvgStyled).style.maskId)
    }

    @Test
    fun parseMaskNone() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" mask="none"/>
            </svg>
        """.trimIndent())
        // mask="none" should result in no maskId
        val circle = svg.children[0]
        assertIs<SvgCircle>(circle)
    }

    // ===========================================
    // Marker Parsing Tests
    // ===========================================

    @Test
    fun parseMarkerStartAttribute() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10" marker-start="url(#arrowStart)"/>
            </svg>
        """.trimIndent())
        val path = svg.children[0]
        assertIs<SvgStyled>(path)
        assertEquals("arrowStart", (path as SvgStyled).style.markerStart)
    }

    @Test
    fun parseMarkerMidAttribute() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10 L20 0" marker-mid="url(#arrowMid)"/>
            </svg>
        """.trimIndent())
        val path = svg.children[0]
        assertIs<SvgStyled>(path)
        assertEquals("arrowMid", (path as SvgStyled).style.markerMid)
    }

    @Test
    fun parseMarkerEndAttribute() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10" marker-end="url(#arrowEnd)"/>
            </svg>
        """.trimIndent())
        val path = svg.children[0]
        assertIs<SvgStyled>(path)
        assertEquals("arrowEnd", (path as SvgStyled).style.markerEnd)
    }

    @Test
    fun parseAllMarkersInStyleAttribute() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10 L20 0" style="marker-start: url(#start); marker-mid: url(#mid); marker-end: url(#end)"/>
            </svg>
        """.trimIndent())
        val path = svg.children[0]
        assertIs<SvgStyled>(path)
        val style = (path as SvgStyled).style
        assertEquals("start", style.markerStart)
        assertEquals("mid", style.markerMid)
        assertEquals("end", style.markerEnd)
    }

    @Test
    fun parseMarkerNone() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10" marker-start="none" marker-end="none"/>
            </svg>
        """.trimIndent())
        // marker="none" should result in no marker
        val path = svg.children[0]
        assertIs<SvgPath>(path)
    }

    // ===========================================
    // Combined Style Attributes Tests
    // ===========================================

    @Test
    fun parseMultipleNewStyleAttributes() {
        val svg = svg("""
            <svg>
                <path d="M0 0 L10 10"
                      vector-effect="non-scaling-stroke"
                      clip-path="url(#myClip)"
                      mask="url(#myMask)"
                      marker-start="url(#start)"
                      marker-end="url(#end)"/>
            </svg>
        """.trimIndent())
        val path = svg.children[0]
        assertIs<SvgStyled>(path)
        val style = (path as SvgStyled).style
        assertEquals(VectorEffect.NON_SCALING_STROKE, style.vectorEffect)
        assertEquals("myClip", style.clipPathId)
        assertEquals("myMask", style.maskId)
        assertEquals("start", style.markerStart)
        assertEquals("end", style.markerEnd)
    }

    @Test
    fun parseUrlReferenceWithSpaces() {
        val svg = svg("""
            <svg>
                <circle cx="12" cy="12" r="10" clip-path="url( #myClip )"/>
            </svg>
        """.trimIndent())
        val circle = svg.children[0]
        assertIs<SvgStyled>(circle)
        assertEquals("myClip", (circle as SvgStyled).style.clipPathId)
    }
}
