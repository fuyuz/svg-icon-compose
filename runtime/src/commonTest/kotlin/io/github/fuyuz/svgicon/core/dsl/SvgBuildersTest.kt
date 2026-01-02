package io.github.fuyuz.svgicon.core.dsl

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.github.fuyuz.svgicon.core.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SvgBuildersTest {

    // ===========================================
    // SvgStyleBuilder Tests
    // ===========================================

    @Test
    fun svgStyleBuilderDefaultsAreNull() {
        val builder = SvgStyleBuilder()
        assertTrue(builder.isEmpty())
    }

    @Test
    fun svgStyleBuilderBuildCreatesStyle() {
        val builder = SvgStyleBuilder().apply {
            fill = Color.Red
            stroke = Color.Blue
            strokeWidth = 3f
        }
        val style = builder.build()
        assertEquals(Color.Red, style.fill)
        assertEquals(Color.Blue, style.stroke)
        assertEquals(3f, style.strokeWidth)
    }

    @Test
    fun svgStyleBuilderIsEmptyReturnsFalseWithValues() {
        val builder = SvgStyleBuilder().apply {
            fill = Color.Red
        }
        assertFalse(builder.isEmpty())
    }

    @Test
    fun svgStyleBuilderAllProperties() {
        val transform = SvgTransform.Rotate(45f)
        val builder = SvgStyleBuilder().apply {
            fill = Color.Red
            fillOpacity = 0.8f
            fillRule = FillRule.EVENODD
            stroke = Color.Blue
            strokeWidth = 2f
            strokeOpacity = 0.5f
            strokeLinecap = LineCap.ROUND
            strokeLinejoin = LineJoin.BEVEL
            strokeDasharray = listOf(5f, 3f)
            strokeDashoffset = 10f
            strokeMiterlimit = 8f
            opacity = 0.9f
            this.transform = transform
            paintOrder = PaintOrder.STROKE_FILL
            vectorEffect = VectorEffect.NON_SCALING_STROKE
            clipPathId = "clip1"
            maskId = "mask1"
            markerStart = "url(#start)"
            markerMid = "url(#mid)"
            markerEnd = "url(#end)"
        }
        val style = builder.build()
        assertEquals(Color.Red, style.fill)
        assertEquals(0.8f, style.fillOpacity)
        assertEquals(FillRule.EVENODD, style.fillRule)
        assertEquals(Color.Blue, style.stroke)
        assertEquals(2f, style.strokeWidth)
        assertEquals(0.5f, style.strokeOpacity)
        assertEquals(LineCap.ROUND, style.strokeLinecap)
        assertEquals(LineJoin.BEVEL, style.strokeLinejoin)
        assertEquals(listOf(5f, 3f), style.strokeDasharray)
        assertEquals(10f, style.strokeDashoffset)
        assertEquals(8f, style.strokeMiterlimit)
        assertEquals(0.9f, style.opacity)
        assertEquals(transform, style.transform)
        assertEquals(PaintOrder.STROKE_FILL, style.paintOrder)
        assertEquals(VectorEffect.NON_SCALING_STROKE, style.vectorEffect)
        assertEquals("clip1", style.clipPathId)
        assertEquals("mask1", style.maskId)
        assertEquals("url(#start)", style.markerStart)
        assertEquals("url(#mid)", style.markerMid)
        assertEquals("url(#end)", style.markerEnd)
    }

    @Test
    fun svgStyleFunctionBuildsStyle() {
        val style = svgStyle {
            fill = Color.Green
            strokeWidth = 4f
        }
        assertEquals(Color.Green, style.fill)
        assertEquals(4f, style.strokeWidth)
    }

    // ===========================================
    // PathBuilder Tests - Basic Commands
    // ===========================================

    @Test
    fun pathBuilderMoveTo() {
        val commands = PathBuilder().apply {
            moveTo(10f, 20f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        assertEquals(10f, (commands[0] as PathCommand.MoveTo).x)
        assertEquals(20f, (commands[0] as PathCommand.MoveTo).y)
    }

    @Test
    fun pathBuilderMoveToRelative() {
        val commands = PathBuilder().apply {
            moveToRelative(5f, 10f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.MoveToRelative>(commands[0])
    }

    @Test
    fun pathBuilderLineTo() {
        val commands = PathBuilder().apply {
            lineTo(30f, 40f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.LineTo>(commands[0])
    }

    @Test
    fun pathBuilderLineToRelative() {
        val commands = PathBuilder().apply {
            lineToRelative(15f, 20f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.LineToRelative>(commands[0])
    }

    @Test
    fun pathBuilderHorizontalLineTo() {
        val commands = PathBuilder().apply {
            horizontalLineTo(50f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.HorizontalLineTo>(commands[0])
        assertEquals(50f, (commands[0] as PathCommand.HorizontalLineTo).x)
    }

    @Test
    fun pathBuilderHorizontalLineToRelative() {
        val commands = PathBuilder().apply {
            horizontalLineToRelative(25f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.HorizontalLineToRelative>(commands[0])
    }

    @Test
    fun pathBuilderVerticalLineTo() {
        val commands = PathBuilder().apply {
            verticalLineTo(60f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.VerticalLineTo>(commands[0])
        assertEquals(60f, (commands[0] as PathCommand.VerticalLineTo).y)
    }

    @Test
    fun pathBuilderVerticalLineToRelative() {
        val commands = PathBuilder().apply {
            verticalLineToRelative(30f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.VerticalLineToRelative>(commands[0])
    }

    @Test
    fun pathBuilderCubicTo() {
        val commands = PathBuilder().apply {
            cubicTo(1f, 2f, 3f, 4f, 5f, 6f)
        }.build()
        assertEquals(1, commands.size)
        val cmd = commands[0]
        assertIs<PathCommand.CubicTo>(cmd)
        assertEquals(1f, cmd.x1)
        assertEquals(2f, cmd.y1)
        assertEquals(3f, cmd.x2)
        assertEquals(4f, cmd.y2)
        assertEquals(5f, cmd.x)
        assertEquals(6f, cmd.y)
    }

    @Test
    fun pathBuilderCubicToRelative() {
        val commands = PathBuilder().apply {
            cubicToRelative(1f, 2f, 3f, 4f, 5f, 6f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.CubicToRelative>(commands[0])
    }

    @Test
    fun pathBuilderSmoothCubicTo() {
        val commands = PathBuilder().apply {
            smoothCubicTo(3f, 4f, 5f, 6f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.SmoothCubicTo>(commands[0])
    }

    @Test
    fun pathBuilderSmoothCubicToRelative() {
        val commands = PathBuilder().apply {
            smoothCubicToRelative(3f, 4f, 5f, 6f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.SmoothCubicToRelative>(commands[0])
    }

    @Test
    fun pathBuilderQuadTo() {
        val commands = PathBuilder().apply {
            quadTo(1f, 2f, 3f, 4f)
        }.build()
        assertEquals(1, commands.size)
        val cmd = commands[0]
        assertIs<PathCommand.QuadTo>(cmd)
        assertEquals(1f, cmd.x1)
        assertEquals(2f, cmd.y1)
        assertEquals(3f, cmd.x)
        assertEquals(4f, cmd.y)
    }

    @Test
    fun pathBuilderQuadToRelative() {
        val commands = PathBuilder().apply {
            quadToRelative(1f, 2f, 3f, 4f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.QuadToRelative>(commands[0])
    }

    @Test
    fun pathBuilderSmoothQuadTo() {
        val commands = PathBuilder().apply {
            smoothQuadTo(5f, 6f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.SmoothQuadTo>(commands[0])
    }

    @Test
    fun pathBuilderSmoothQuadToRelative() {
        val commands = PathBuilder().apply {
            smoothQuadToRelative(5f, 6f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.SmoothQuadToRelative>(commands[0])
    }

    @Test
    fun pathBuilderArcTo() {
        val commands = PathBuilder().apply {
            arcTo(5f, 5f, 0f, true, false, 10f, 10f)
        }.build()
        assertEquals(1, commands.size)
        val cmd = commands[0]
        assertIs<PathCommand.ArcTo>(cmd)
        assertEquals(5f, cmd.rx)
        assertEquals(5f, cmd.ry)
        assertEquals(0f, cmd.xAxisRotation)
        assertTrue(cmd.largeArcFlag)
        assertFalse(cmd.sweepFlag)
        assertEquals(10f, cmd.x)
        assertEquals(10f, cmd.y)
    }

    @Test
    fun pathBuilderArcToRelative() {
        val commands = PathBuilder().apply {
            arcToRelative(5f, 5f, 0f, false, true, 10f, 10f)
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.ArcToRelative>(commands[0])
    }

    @Test
    fun pathBuilderClose() {
        val commands = PathBuilder().apply {
            close()
        }.build()
        assertEquals(1, commands.size)
        assertIs<PathCommand.Close>(commands[0])
    }

    // ===========================================
    // PathBuilder Tests - Helper Functions
    // ===========================================

    @Test
    fun pathBuilderCircleHelper() {
        val commands = PathBuilder().apply {
            circle(12f, 12f, 10f)
        }.build()
        // Circle: moveTo + 4 cubicTo + close = 6 commands
        assertEquals(6, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        assertIs<PathCommand.CubicTo>(commands[1])
        assertIs<PathCommand.CubicTo>(commands[2])
        assertIs<PathCommand.CubicTo>(commands[3])
        assertIs<PathCommand.CubicTo>(commands[4])
        assertIs<PathCommand.Close>(commands[5])
    }

    @Test
    fun pathBuilderRectHelper() {
        val commands = PathBuilder().apply {
            rect(0f, 0f, 20f, 10f)
        }.build()
        // Simple rect: moveTo + 3 lineTo + close = 5 commands
        assertEquals(5, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        assertIs<PathCommand.LineTo>(commands[1])
        assertIs<PathCommand.LineTo>(commands[2])
        assertIs<PathCommand.LineTo>(commands[3])
        assertIs<PathCommand.Close>(commands[4])
    }

    @Test
    fun pathBuilderRectWithRoundedCorners() {
        val commands = PathBuilder().apply {
            rect(0f, 0f, 20f, 10f, rx = 2f)
        }.build()
        // Rounded rect: moveTo + lineTo + arcTo + lineTo + arcTo + lineTo + arcTo + lineTo + arcTo + close
        assertTrue(commands.size > 5)
        assertIs<PathCommand.MoveTo>(commands[0])
        // Should contain arcTo commands
        assertTrue(commands.any { it is PathCommand.ArcTo })
    }

    @Test
    fun pathBuilderStarHelper() {
        val commands = PathBuilder().apply {
            star(12f, 12f, 5, 10f, 5f)
        }.build()
        // 5-point star: 10 points (5 outer + 5 inner) = moveTo + 9 lineTo + close = 11 commands
        assertEquals(11, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        assertIs<PathCommand.Close>(commands.last())
    }

    @Test
    fun pathBuilderEllipseHelper() {
        val commands = PathBuilder().apply {
            ellipse(12f, 12f, 10f, 5f)
        }.build()
        // Ellipse: moveTo + 4 cubicTo + close = 6 commands
        assertEquals(6, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        assertIs<PathCommand.Close>(commands.last())
    }

    @Test
    fun pathBuilderArcHelper() {
        val commands = PathBuilder().apply {
            arc(12f, 12f, 10f, 0f, 90f)
        }.build()
        // Arc: moveTo + arcTo = 2 commands
        assertEquals(2, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        assertIs<PathCommand.ArcTo>(commands[1])
    }

    @Test
    fun pathBuilderRegularPolygonHelper() {
        val commands = PathBuilder().apply {
            regularPolygon(12f, 12f, 10f, 6)
        }.build()
        // Hexagon: moveTo + 5 lineTo + close = 7 commands
        assertEquals(7, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        assertIs<PathCommand.Close>(commands.last())
    }

    // ===========================================
    // SvgBuilder Tests - Basic Elements
    // ===========================================

    @Test
    fun svgBuilderPath() {
        val elements = SvgBuilder().apply {
            path("M10 10 L20 20")
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgPath>(elements[0])
        val path = elements[0] as SvgPath
        assertTrue(path.commands.isNotEmpty())
    }

    @Test
    fun svgBuilderPathWithStyle() {
        val style = SvgStyle(fill = Color.Red)
        val elements = SvgBuilder().apply {
            path("M10 10 L20 20", style)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgStyled>(elements[0])
        val styled = elements[0] as SvgStyled
        assertIs<SvgPath>(styled.element)
        assertEquals(Color.Red, styled.style.fill)
    }

    @Test
    fun svgBuilderPathFromBuilder() {
        val elements = SvgBuilder().apply {
            path {
                moveTo(10f, 10f)
                lineTo(20f, 20f)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgPath>(elements[0])
        val path = elements[0] as SvgPath
        assertEquals(2, path.commands.size)
    }

    @Test
    fun svgBuilderCircle() {
        val elements = SvgBuilder().apply {
            circle(12, 12, 10)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgCircle>(elements[0])
        val circle = elements[0] as SvgCircle
        assertEquals(12f, circle.cx)
        assertEquals(12f, circle.cy)
        assertEquals(10f, circle.r)
    }

    @Test
    fun svgBuilderCircleWithStyle() {
        val style = SvgStyle(stroke = Color.Blue)
        val elements = SvgBuilder().apply {
            circle(12, 12, 10, style)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgStyled>(elements[0])
    }

    @Test
    fun svgBuilderEllipse() {
        val elements = SvgBuilder().apply {
            ellipse(12, 12, 8, 5)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgEllipse>(elements[0])
        val ellipse = elements[0] as SvgEllipse
        assertEquals(12f, ellipse.cx)
        assertEquals(12f, ellipse.cy)
        assertEquals(8f, ellipse.rx)
        assertEquals(5f, ellipse.ry)
    }

    @Test
    fun svgBuilderRect() {
        val elements = SvgBuilder().apply {
            rect(x = 0, y = 0, width = 20, height = 15)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgRect>(elements[0])
        val rect = elements[0] as SvgRect
        assertEquals(0f, rect.x)
        assertEquals(0f, rect.y)
        assertEquals(20f, rect.width)
        assertEquals(15f, rect.height)
    }

    @Test
    fun svgBuilderRectWithRounding() {
        val elements = SvgBuilder().apply {
            rect(x = 0, y = 0, width = 20, height = 15, rx = 3, ry = 4)
        }.build()
        assertEquals(1, elements.size)
        val rect = elements[0] as SvgRect
        assertEquals(3f, rect.rx)
        assertEquals(4f, rect.ry)
    }

    @Test
    fun svgBuilderLine() {
        val elements = SvgBuilder().apply {
            line(0, 0, 24, 24)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgLine>(elements[0])
        val line = elements[0] as SvgLine
        assertEquals(0f, line.x1)
        assertEquals(0f, line.y1)
        assertEquals(24f, line.x2)
        assertEquals(24f, line.y2)
    }

    @Test
    fun svgBuilderText() {
        val elements = SvgBuilder().apply {
            text("Hello", x = 10, y = 20)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgText>(elements[0])
        val text = elements[0] as SvgText
        assertEquals("Hello", text.text)
        assertEquals(10f, text.x)
        assertEquals(20f, text.y)
    }

    @Test
    fun svgBuilderTextWithOptions() {
        val elements = SvgBuilder().apply {
            text(
                "Hello",
                x = 12,
                y = 12,
                textAnchor = TextAnchor.MIDDLE,
                dominantBaseline = DominantBaseline.MIDDLE,
                fontSize = 14f,
                fontFamily = "Arial",
                fontWeight = "bold"
            )
        }.build()
        assertEquals(1, elements.size)
        val text = elements[0] as SvgText
        assertEquals(TextAnchor.MIDDLE, text.textAnchor)
        assertEquals(DominantBaseline.MIDDLE, text.dominantBaseline)
        assertEquals(14f, text.fontSize)
        assertEquals("Arial", text.fontFamily)
        assertEquals("bold", text.fontWeight)
    }

    @Test
    fun svgBuilderPolylineVararg() {
        val elements = SvgBuilder().apply {
            polyline(5 to 12, 12 to 5, 19 to 12)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgPolyline>(elements[0])
        val polyline = elements[0] as SvgPolyline
        assertEquals(3, polyline.points.size)
    }

    @Test
    fun svgBuilderPolylineString() {
        val elements = SvgBuilder().apply {
            polyline("5,12 12,5 19,12")
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgPolyline>(elements[0])
        val polyline = elements[0] as SvgPolyline
        assertEquals(3, polyline.points.size)
    }

    @Test
    fun svgBuilderPolygonVararg() {
        val elements = SvgBuilder().apply {
            polygon(12 to 2, 22 to 22, 2 to 22)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgPolygon>(elements[0])
        val polygon = elements[0] as SvgPolygon
        assertEquals(3, polygon.points.size)
    }

    @Test
    fun svgBuilderPolygonString() {
        val elements = SvgBuilder().apply {
            polygon("12,2 22,22 2,22")
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgPolygon>(elements[0])
    }

    // ===========================================
    // SvgBuilder Tests - Grouping
    // ===========================================

    @Test
    fun svgBuilderGroup() {
        val elements = SvgBuilder().apply {
            group {
                circle(12, 12, 5)
                path("M10 10 L20 20")
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgGroup>(elements[0])
        val group = elements[0] as SvgGroup
        assertEquals(2, group.children.size)
    }

    @Test
    fun svgBuilderGroupWithTransform() {
        val transform = SvgTransform.Rotate(45f)
        val elements = SvgBuilder().apply {
            group(transform) {
                circle(12, 12, 5)
            }
        }.build()
        assertEquals(1, elements.size)
        val group = elements[0] as SvgGroup
        assertEquals(transform, group.style?.transform)
    }

    @Test
    fun svgBuilderWithStyle() {
        val elements = SvgBuilder().apply {
            withStyle(stroke = Color.Blue, strokeWidth = 2f) {
                circle(12, 12, 5)
                path("M10 10 L20 20")
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgGroup>(elements[0])
        val group = elements[0] as SvgGroup
        assertEquals(Color.Blue, group.style?.stroke)
        assertEquals(2f, group.style?.strokeWidth)
    }

    @Test
    fun svgBuilderStyled() {
        val elements = SvgBuilder().apply {
            styled({
                stroke = Color.Red
                strokeWidth = 3f
            }) {
                line(0, 0, 24, 24)
            }
        }.build()
        assertEquals(1, elements.size)
        val group = elements[0] as SvgGroup
        assertEquals(Color.Red, group.style?.stroke)
    }

    // ===========================================
    // SvgBuilder Tests - Definitions
    // ===========================================

    @Test
    fun svgBuilderClipPath() {
        val elements = SvgBuilder().apply {
            clipPath("clip1") {
                circle(12, 12, 10)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgClipPath>(elements[0])
        val clipPath = elements[0] as SvgClipPath
        assertEquals("clip1", clipPath.id)
    }

    @Test
    fun svgBuilderMask() {
        val elements = SvgBuilder().apply {
            mask("mask1") {
                rect(width = 24, height = 24)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgMask>(elements[0])
        val mask = elements[0] as SvgMask
        assertEquals("mask1", mask.id)
    }

    @Test
    fun svgBuilderDefs() {
        val elements = SvgBuilder().apply {
            defs {
                clipPath("clip1") {
                    circle(12, 12, 10)
                }
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgDefs>(elements[0])
    }

    @Test
    fun svgBuilderMarker() {
        val elements = SvgBuilder().apply {
            marker(id = "arrow", refX = 5f, refY = 5f) {
                path("M0 0L10 5L0 10z")
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgMarker>(elements[0])
        val marker = elements[0] as SvgMarker
        assertEquals("arrow", marker.id)
        assertEquals(5f, marker.refX)
        assertEquals(5f, marker.refY)
    }

    @Test
    fun svgBuilderSymbol() {
        val elements = SvgBuilder().apply {
            symbol(id = "icon") {
                circle(12, 12, 10)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgSymbol>(elements[0])
        val symbol = elements[0] as SvgSymbol
        assertEquals("icon", symbol.id)
    }

    @Test
    fun svgBuilderUse() {
        val elements = SvgBuilder().apply {
            use(href = "#icon", x = 10, y = 20)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgUse>(elements[0])
        val use = elements[0] as SvgUse
        assertEquals("#icon", use.href)
        assertEquals(10f, use.x)
        assertEquals(20f, use.y)
    }

    @Test
    fun svgBuilderPattern() {
        val elements = SvgBuilder().apply {
            pattern(id = "dots", width = 10, height = 10) {
                circle(5, 5, 2)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgPattern>(elements[0])
        val pattern = elements[0] as SvgPattern
        assertEquals("dots", pattern.id)
        assertEquals(10f, pattern.width)
        assertEquals(10f, pattern.height)
    }

    // ===========================================
    // SvgBuilder Tests - Gradients
    // ===========================================

    @Test
    fun svgBuilderLinearGradient() {
        val elements = SvgBuilder().apply {
            linearGradient(id = "grad1", x1 = 0f, y1 = 0f, x2 = 1f, y2 = 0f) {
                start(Color.Red)
                end(Color.Blue)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgLinearGradient>(elements[0])
        val gradient = elements[0] as SvgLinearGradient
        assertEquals("grad1", gradient.id)
        assertEquals(2, gradient.stops.size)
    }

    @Test
    fun svgBuilderRadialGradient() {
        val elements = SvgBuilder().apply {
            radialGradient(id = "grad2", cx = 0.5f, cy = 0.5f, r = 0.5f) {
                stop(0f, Color.White)
                middle(Color.Gray)
                stop(1f, Color.Black)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgRadialGradient>(elements[0])
        val gradient = elements[0] as SvgRadialGradient
        assertEquals("grad2", gradient.id)
        assertEquals(3, gradient.stops.size)
    }

    // ===========================================
    // SvgBuilder Tests - Animations
    // ===========================================

    @Test
    fun svgBuilderAnimatedPath() {
        val elements = SvgBuilder().apply {
            animatedPath("M10 10 L20 20", dur = 1.seconds)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
        val animated = elements[0] as SvgAnimated
        assertIs<SvgPath>(animated.element)
        assertEquals(1, animated.animations.size)
        assertIs<SvgAnimate.StrokeDraw>(animated.animations[0])
    }

    @Test
    fun svgBuilderAnimatedCircle() {
        val elements = SvgBuilder().apply {
            animatedCircle(12, 12, 10, dur = 500.milliseconds, delay = 200.milliseconds)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
        val animated = elements[0] as SvgAnimated
        assertIs<SvgCircle>(animated.element)
    }

    @Test
    fun svgBuilderAnimatedLine() {
        val elements = SvgBuilder().apply {
            animatedLine(0, 0, 24, 24, dur = 300.milliseconds)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
        val animated = elements[0] as SvgAnimated
        assertIs<SvgLine>(animated.element)
    }

    @Test
    fun svgBuilderElementWithInfixAnimated() {
        val elements = SvgBuilder().apply {
            circle(12, 12, 10) animated {
                strokeDraw(dur = 1.seconds)
                opacity(from = 0f, to = 1f)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
        val animated = elements[0] as SvgAnimated
        assertEquals(2, animated.animations.size)
    }

    @Test
    fun svgBuilderElementWithInfixWith() {
        val elements = SvgBuilder().apply {
            circle(12, 12, 10) with Animations.fadeIn
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
    }

    @Test
    fun svgBuilderElementWithInfixStyled() {
        val elements = SvgBuilder().apply {
            circle(12, 12, 10) styled {
                fill = Color.Red
                stroke = Color.Blue
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgStyled>(elements[0])
        val styled = elements[0] as SvgStyled
        assertEquals(Color.Red, styled.style.fill)
        assertEquals(Color.Blue, styled.style.stroke)
    }

    @Test
    fun svgBuilderUnaryPlusOperator() {
        val customElement = SvgCircle(12f, 12f, 10f)
        val elements = SvgBuilder().apply {
            +customElement
        }.build()
        assertEquals(1, elements.size)
        assertEquals(customElement, elements[0])
    }

    // ===========================================
    // GradientBuilder Tests
    // ===========================================

    @Test
    fun gradientBuilderStop() {
        val stops = GradientBuilder().apply {
            stop(0.25f, Color.Red, 0.8f)
        }.build()
        assertEquals(1, stops.size)
        assertEquals(0.25f, stops[0].offset)
        assertEquals(Color.Red, stops[0].color)
        assertEquals(0.8f, stops[0].opacity)
    }

    @Test
    fun gradientBuilderStart() {
        val stops = GradientBuilder().apply {
            start(Color.Green)
        }.build()
        assertEquals(1, stops.size)
        assertEquals(0f, stops[0].offset)
    }

    @Test
    fun gradientBuilderEnd() {
        val stops = GradientBuilder().apply {
            end(Color.Blue)
        }.build()
        assertEquals(1, stops.size)
        assertEquals(1f, stops[0].offset)
    }

    @Test
    fun gradientBuilderMiddle() {
        val stops = GradientBuilder().apply {
            middle(Color.Yellow)
        }.build()
        assertEquals(1, stops.size)
        assertEquals(0.5f, stops[0].offset)
    }

    // ===========================================
    // AnimationBuilder Tests
    // ===========================================

    @Test
    fun animationBuilderStrokeDraw() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            strokeDraw(dur = 1.seconds, delay = 200.milliseconds, reverse = true)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.StrokeDraw>(anim)
        assertEquals(1.seconds, anim.dur)
        assertEquals(200.milliseconds, anim.delay)
        assertTrue(anim.reverse)
    }

    @Test
    fun animationBuilderOpacity() {
        val circle = SvgCircle(12f, 12f, 10f)
        val animations = AnimationBuilder(circle).apply {
            opacity(from = 0f, to = 1f, dur = 300.milliseconds)
        }.build()
        assertEquals(1, animations.size)
        assertIs<SvgAnimate.Opacity>(animations[0])
    }

    @Test
    fun animationBuilderTranslate() {
        val rect = SvgRect(0f, 0f, 10f, 10f)
        val animations = AnimationBuilder(rect).apply {
            translate(from = 0f, to = 100f)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.TRANSLATE, anim.type)
    }

    @Test
    fun animationBuilderTranslateX() {
        val rect = SvgRect(0f, 0f, 10f, 10f)
        val animations = AnimationBuilder(rect).apply {
            translateX(from = 0f, to = 50f)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.TRANSLATE_X, anim.type)
    }

    @Test
    fun animationBuilderTranslateY() {
        val rect = SvgRect(0f, 0f, 10f, 10f)
        val animations = AnimationBuilder(rect).apply {
            translateY(from = 0f, to = 50f)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.TRANSLATE_Y, anim.type)
    }

    @Test
    fun animationBuilderScale() {
        val circle = SvgCircle(12f, 12f, 10f)
        val animations = AnimationBuilder(circle).apply {
            scale(from = 1f, to = 2f)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.SCALE, anim.type)
    }

    @Test
    fun animationBuilderScaleX() {
        val circle = SvgCircle(12f, 12f, 10f)
        val animations = AnimationBuilder(circle).apply {
            scaleX(from = 1f, to = 1.5f)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.SCALE_X, anim.type)
    }

    @Test
    fun animationBuilderScaleY() {
        val circle = SvgCircle(12f, 12f, 10f)
        val animations = AnimationBuilder(circle).apply {
            scaleY(from = 1f, to = 1.5f)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.SCALE_Y, anim.type)
    }

    @Test
    fun animationBuilderRotate() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            rotate(from = 0f, to = 360f, dur = 2.seconds)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.ROTATE, anim.type)
        assertEquals(0f, anim.from)
        assertEquals(360f, anim.to)
    }

    @Test
    fun animationBuilderSkewX() {
        val rect = SvgRect(0f, 0f, 10f, 10f)
        val animations = AnimationBuilder(rect).apply {
            skewX(from = 0f, to = 15f)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.SKEW_X, anim.type)
    }

    @Test
    fun animationBuilderSkewY() {
        val rect = SvgRect(0f, 0f, 10f, 10f)
        val animations = AnimationBuilder(rect).apply {
            skewY(from = 0f, to = 20f)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.Transform
        assertEquals(TransformType.SKEW_Y, anim.type)
    }

    @Test
    fun animationBuilderMotion() {
        val circle = SvgCircle(5f, 5f, 3f)
        val animations = AnimationBuilder(circle).apply {
            motion(path = "M0 0 C50 0 50 100 100 100", rotate = MotionRotate.AUTO)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Motion>(anim)
        assertEquals(MotionRotate.AUTO, anim.rotate)
    }

    @Test
    fun animationBuilderStrokeWidth() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            strokeWidth(from = 1f, to = 5f)
        }.build()
        assertEquals(1, animations.size)
        assertIs<SvgAnimate.StrokeWidth>(animations[0])
    }

    @Test
    fun animationBuilderStrokeOpacity() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            strokeOpacity(from = 0f, to = 1f)
        }.build()
        assertEquals(1, animations.size)
        assertIs<SvgAnimate.StrokeOpacity>(animations[0])
    }

    @Test
    fun animationBuilderStrokeDasharray() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            strokeDasharray(from = listOf(5f, 5f), to = listOf(10f, 2f))
        }.build()
        assertEquals(1, animations.size)
        assertIs<SvgAnimate.StrokeDasharray>(animations[0])
    }

    @Test
    fun animationBuilderStrokeDashoffset() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            strokeDashoffset(from = 0f, to = 100f)
        }.build()
        assertEquals(1, animations.size)
        assertIs<SvgAnimate.StrokeDashoffset>(animations[0])
    }

    @Test
    fun animationBuilderFillOpacity() {
        val circle = SvgCircle(12f, 12f, 10f)
        val animations = AnimationBuilder(circle).apply {
            fillOpacity(from = 0f, to = 1f)
        }.build()
        assertEquals(1, animations.size)
        assertIs<SvgAnimate.FillOpacity>(animations[0])
    }

    @Test
    fun animationBuilderGeometricProperties() {
        val circle = SvgCircle(12f, 12f, 10f)
        val animations = AnimationBuilder(circle).apply {
            cx(from = 12f, to = 24f)
            cy(from = 12f, to = 24f)
            r(from = 10f, to = 20f)
        }.build()
        assertEquals(3, animations.size)
        assertIs<SvgAnimate.Cx>(animations[0])
        assertIs<SvgAnimate.Cy>(animations[1])
        assertIs<SvgAnimate.R>(animations[2])
    }

    @Test
    fun animationBuilderEllipseProperties() {
        val ellipse = SvgEllipse(12f, 12f, 8f, 5f)
        val animations = AnimationBuilder(ellipse).apply {
            rx(from = 8f, to = 12f)
            ry(from = 5f, to = 8f)
        }.build()
        assertEquals(2, animations.size)
        assertIs<SvgAnimate.Rx>(animations[0])
        assertIs<SvgAnimate.Ry>(animations[1])
    }

    @Test
    fun animationBuilderRectProperties() {
        val rect = SvgRect(0f, 0f, 10f, 10f)
        val animations = AnimationBuilder(rect).apply {
            x(from = 0f, to = 10f)
            y(from = 0f, to = 10f)
            width(from = 10f, to = 20f)
            height(from = 10f, to = 20f)
        }.build()
        assertEquals(4, animations.size)
        assertIs<SvgAnimate.X>(animations[0])
        assertIs<SvgAnimate.Y>(animations[1])
        assertIs<SvgAnimate.Width>(animations[2])
        assertIs<SvgAnimate.Height>(animations[3])
    }

    @Test
    fun animationBuilderLineProperties() {
        val line = SvgLine(0f, 0f, 24f, 24f)
        val animations = AnimationBuilder(line).apply {
            x1(from = 0f, to = 5f)
            y1(from = 0f, to = 5f)
            x2(from = 24f, to = 30f)
            y2(from = 24f, to = 30f)
        }.build()
        assertEquals(4, animations.size)
        assertIs<SvgAnimate.X1>(animations[0])
        assertIs<SvgAnimate.Y1>(animations[1])
        assertIs<SvgAnimate.X2>(animations[2])
        assertIs<SvgAnimate.Y2>(animations[3])
    }

    @Test
    fun animationBuilderPathD() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            d(from = "M10 10 L20 20", to = "M5 15 L25 15")
        }.build()
        assertEquals(1, animations.size)
        assertIs<SvgAnimate.D>(animations[0])
    }

    @Test
    fun animationBuilderPoints() {
        val polygon = SvgPolygon(listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(5f, 10f)))
        val animations = AnimationBuilder(polygon).apply {
            points(
                from = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(5f, 10f)),
                to = listOf(Offset(2f, 2f), Offset(12f, 2f), Offset(7f, 12f))
            )
        }.build()
        assertEquals(1, animations.size)
        assertIs<SvgAnimate.Points>(animations[0])
    }

    // ===========================================
    // Type-Safe Animation Extension Tests
    // ===========================================

    @Test
    fun morphToWithString() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            morphTo("M5 15 L25 15")
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.D>(anim)
        assertEquals("M5 15 L25 15", anim.to)
    }

    @Test
    fun morphToWithPathBuilder() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            morphTo {
                moveTo(5f, 15f)
                lineTo(25f, 15f)
            }
        }.build()
        assertEquals(1, animations.size)
        assertIs<SvgAnimate.D>(animations[0])
    }

    @Test
    fun morphPointsToPolygon() {
        val polygon = SvgPolygon(listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(5f, 10f)))
        val targetPoints = listOf(Offset(2f, 2f), Offset(12f, 2f), Offset(7f, 12f))
        val animations = AnimationBuilder(polygon).apply {
            morphPointsTo(targetPoints)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Points>(anim)
        assertEquals(polygon.points, anim.from)
        assertEquals(targetPoints, anim.to)
    }

    @Test
    fun morphPointsToPolyline() {
        val polyline = SvgPolyline(listOf(Offset(0f, 0f), Offset(10f, 10f), Offset(20f, 0f)))
        val targetPoints = listOf(Offset(5f, 5f), Offset(15f, 15f), Offset(25f, 5f))
        val animations = AnimationBuilder(polyline).apply {
            morphPointsTo(targetPoints)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Points>(anim)
        assertEquals(polyline.points, anim.from)
        assertEquals(targetPoints, anim.to)
    }

    // ===========================================
    // DSL Entry Point Tests
    // ===========================================

    @Test
    fun svgFunctionBasic() {
        val icon = svg {
            circle(12, 12, 10)
        }
        assertIs<Svg>(icon)
        assertEquals(1, icon.children.size)
    }

    @Test
    fun svgFunctionWithSize() {
        val icon = svg(size = 48) {
            circle(24, 24, 20)
        }
        assertEquals(48f, icon.width)
        assertEquals(48f, icon.height)
        assertEquals(ViewBox(0f, 0f, 48f, 48f), icon.viewBox)
    }

    @Test
    fun svgFunctionWithStyle() {
        val icon = svg(
            size = 24,
            stroke = Color.Blue,
            strokeWidth = 3f,
            strokeLinecap = LineCap.BUTT,
            strokeLinejoin = LineJoin.MITER
        ) {
            path("M10 10 L20 20")
        }
        assertEquals(Color.Blue, icon.stroke)
        assertEquals(3f, icon.strokeWidth)
        assertEquals(LineCap.BUTT, icon.strokeLinecap)
        assertEquals(LineJoin.MITER, icon.strokeLinejoin)
    }

    @Test
    fun svgFunctionWithWidthAndHeight() {
        val icon = svg(width = 100, height = 50) {
            rect(width = 100, height = 50)
        }
        assertEquals(100f, icon.width)
        assertEquals(50f, icon.height)
        assertEquals(ViewBox(0f, 0f, 100f, 50f), icon.viewBox)
    }

    @Test
    fun svgFunctionWithCustomViewBox() {
        val viewBox = ViewBox(0f, 0f, 48f, 48f)
        val icon = svg(size = 24, viewBox = viewBox) {
            circle(24, 24, 20)
        }
        assertEquals(24f, icon.width)
        assertEquals(24f, icon.height)
        assertEquals(viewBox, icon.viewBox)
    }

    @Test
    fun svgFunctionWithPreserveAspectRatio() {
        val par = PreserveAspectRatio(AspectRatioAlign.X_MID_Y_MID, MeetOrSlice.SLICE)
        val icon = svg(size = 24, preserveAspectRatio = par) {
            circle(12, 12, 10)
        }
        assertEquals(par, icon.preserveAspectRatio)
    }

    @Test
    fun svgFunctionWithFill() {
        val icon = svg(fill = Color.Red, stroke = null) {
            circle(12, 12, 10)
        }
        assertEquals(Color.Red, icon.fill)
        assertNull(icon.stroke)
    }

    // ===========================================
    // parsePointsString Tests
    // ===========================================

    @Test
    fun parsePointsStringWithCommas() {
        val builder = SvgBuilder()
        val points = builder.parsePointsString("5,12 12,5 19,12")
        assertEquals(3, points.size)
        assertEquals(Offset(5f, 12f), points[0])
        assertEquals(Offset(12f, 5f), points[1])
        assertEquals(Offset(19f, 12f), points[2])
    }

    @Test
    fun parsePointsStringWithSpaces() {
        val builder = SvgBuilder()
        val points = builder.parsePointsString("5 12 12 5 19 12")
        assertEquals(3, points.size)
    }

    @Test
    fun parsePointsStringWithMixedSeparators() {
        val builder = SvgBuilder()
        val points = builder.parsePointsString("5,12  12,5  19,12")
        assertEquals(3, points.size)
    }

    @Test
    fun parsePointsStringWithOddNumbers() {
        val builder = SvgBuilder()
        val points = builder.parsePointsString("5,12 12,5 19")
        // Should only parse complete pairs
        assertEquals(2, points.size)
    }

    // ===========================================
    // Additional SvgBuilder Tests for Coverage
    // ===========================================

    @Test
    fun svgBuilderPathWithAnimationBlock() {
        val elements = SvgBuilder().apply {
            path("M10 10 L20 20") {
                strokeDraw(dur = 1.seconds)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
        val animated = elements[0] as SvgAnimated
        assertIs<SvgPath>(animated.element)
    }

    @Test
    fun svgBuilderAnimatedPathWithBuilders() {
        val elements = SvgBuilder().apply {
            animatedPath(
                pathBlock = {
                    moveTo(10f, 10f)
                    lineTo(20f, 20f)
                    close()
                },
                animBlock = {
                    strokeDraw(dur = 500.milliseconds)
                    opacity(from = 0f, to = 1f)
                }
            )
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
        val animated = elements[0] as SvgAnimated
        assertEquals(2, animated.animations.size)
    }

    @Test
    fun svgBuilderCircleWithAnimationBlock() {
        val elements = SvgBuilder().apply {
            circle(12, 12, 10) {
                r(from = 5f, to = 15f, dur = 1.seconds)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
    }

    @Test
    fun svgBuilderEllipseWithStyle() {
        val style = SvgStyle(fill = Color.Green)
        val elements = SvgBuilder().apply {
            ellipse(12, 12, 8, 5, style)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgStyled>(elements[0])
        val styled = elements[0] as SvgStyled
        assertIs<SvgEllipse>(styled.element)
    }

    @Test
    fun svgBuilderEllipseWithAnimationBlock() {
        val elements = SvgBuilder().apply {
            ellipse(12, 12, 8, 5) {
                rx(from = 8f, to = 12f)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
    }

    @Test
    fun svgBuilderRectWithAnimationBlock() {
        val elements = SvgBuilder().apply {
            rect(0, 0, 20, 15) {
                width(from = 20f, to = 30f)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
    }

    @Test
    fun svgBuilderRectWithStyle() {
        val style = SvgStyle(stroke = Color.Red)
        val elements = SvgBuilder().apply {
            rect(0, 0, 20, 15, style = style)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgStyled>(elements[0])
    }

    @Test
    fun svgBuilderLineWithAnimationBlock() {
        val elements = SvgBuilder().apply {
            line(0, 0, 24, 24) {
                x2(from = 24f, to = 48f)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
    }

    @Test
    fun svgBuilderLineWithStyle() {
        val style = SvgStyle(stroke = Color.Black, strokeWidth = 3f)
        val elements = SvgBuilder().apply {
            line(0, 0, 24, 24, style)
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgStyled>(elements[0])
    }

    @Test
    fun svgBuilderTextWithAnimationBlock() {
        val elements = SvgBuilder().apply {
            text("Hello", x = 12, y = 12) {
                opacity(from = 0f, to = 1f)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
    }

    @Test
    fun svgBuilderPolylineWithAnimationBlock() {
        val elements = SvgBuilder().apply {
            polyline("5,12 12,5 19,12") {
                strokeDraw(dur = 1.seconds)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
    }

    @Test
    fun svgBuilderPolygonWithAnimationBlock() {
        val elements = SvgBuilder().apply {
            polygon("12,2 22,22 2,22") {
                strokeDraw(dur = 1.seconds)
            }
        }.build()
        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
    }

    @Test
    fun svgBuilderNestedGroups() {
        val elements = SvgBuilder().apply {
            group {
                group {
                    circle(12, 12, 5)
                }
            }
        }.build()
        assertEquals(1, elements.size)
        val outerGroup = elements[0] as SvgGroup
        assertEquals(1, outerGroup.children.size)
        assertIs<SvgGroup>(outerGroup.children[0])
    }

    @Test
    fun svgBuilderWithStyleAllOptions() {
        val elements = SvgBuilder().apply {
            withStyle(
                stroke = Color.Blue,
                fill = Color.Red,
                strokeWidth = 2f,
                opacity = 0.8f,
                strokeLinecap = LineCap.ROUND,
                strokeLinejoin = LineJoin.ROUND,
                strokeDasharray = listOf(5f, 3f),
                strokeDashoffset = 2f,
                fillOpacity = 0.9f,
                strokeOpacity = 0.7f,
                fillRule = FillRule.EVENODD,
                transform = SvgTransform.Rotate(45f),
                clipPathId = "clip1",
                maskId = "mask1"
            ) {
                circle(12, 12, 10)
            }
        }.build()
        assertEquals(1, elements.size)
        val group = elements[0] as SvgGroup
        assertEquals(Color.Blue, group.style?.stroke)
        assertEquals(Color.Red, group.style?.fill)
        assertEquals(2f, group.style?.strokeWidth)
        assertEquals(0.8f, group.style?.opacity)
        assertEquals(LineCap.ROUND, group.style?.strokeLinecap)
        assertEquals(LineJoin.ROUND, group.style?.strokeLinejoin)
        assertEquals(listOf(5f, 3f), group.style?.strokeDasharray)
        assertEquals(2f, group.style?.strokeDashoffset)
        assertEquals(0.9f, group.style?.fillOpacity)
        assertEquals(0.7f, group.style?.strokeOpacity)
        assertEquals(FillRule.EVENODD, group.style?.fillRule)
        assertIs<SvgTransform.Rotate>(group.style?.transform)
        assertEquals("clip1", group.style?.clipPathId)
        assertEquals("mask1", group.style?.maskId)
    }

    @Test
    fun svgBuilderMarkerWithAllOptions() {
        val elements = SvgBuilder().apply {
            marker(
                id = "arrowhead",
                refX = 10f,
                refY = 5f,
                markerWidth = 10f,
                markerHeight = 10f,
                orient = MarkerOrient.AutoStartReverse,
                viewBox = ViewBox(0f, 0f, 10f, 10f)
            ) {
                path("M0 0 L10 5 L0 10 z")
            }
        }.build()
        assertEquals(1, elements.size)
        val marker = elements[0] as SvgMarker
        assertEquals("arrowhead", marker.id)
        assertEquals(10f, marker.refX)
        assertEquals(5f, marker.refY)
        assertEquals(10f, marker.markerWidth)
        assertEquals(10f, marker.markerHeight)
        assertEquals(MarkerOrient.AutoStartReverse, marker.orient)
    }

    @Test
    fun svgBuilderPatternWithAllOptions() {
        val elements = SvgBuilder().apply {
            pattern(
                id = "grid",
                width = 10,
                height = 10,
                patternUnits = PatternUnits.USER_SPACE_ON_USE
            ) {
                path("M10 0 L0 0 0 10")
            }
        }.build()
        assertEquals(1, elements.size)
        val pattern = elements[0] as SvgPattern
        assertEquals("grid", pattern.id)
        assertEquals(PatternUnits.USER_SPACE_ON_USE, pattern.patternUnits)
    }

    @Test
    fun svgBuilderClipPathWithUnits() {
        val elements = SvgBuilder().apply {
            clipPath("clip1", clipPathUnits = ClipPathUnits.OBJECT_BOUNDING_BOX) {
                rect(width = 1, height = 1)
            }
        }.build()
        assertEquals(1, elements.size)
        val clipPath = elements[0] as SvgClipPath
        assertEquals(ClipPathUnits.OBJECT_BOUNDING_BOX, clipPath.clipPathUnits)
    }

    @Test
    fun svgBuilderMaskWithMaskUnits() {
        val elements = SvgBuilder().apply {
            mask(
                id = "gradient-mask",
                maskUnits = MaskUnits.OBJECT_BOUNDING_BOX
            ) {
                rect(width = 1, height = 1)
            }
        }.build()
        assertEquals(1, elements.size)
        val mask = elements[0] as SvgMask
        assertEquals(MaskUnits.OBJECT_BOUNDING_BOX, mask.maskUnits)
    }

    @Test
    fun svgBuilderSymbolWithViewBox() {
        val viewBox = ViewBox(0f, 0f, 100f, 100f)
        val elements = SvgBuilder().apply {
            symbol(id = "icon", viewBox = viewBox) {
                circle(50, 50, 40)
            }
        }.build()
        assertEquals(1, elements.size)
        val symbol = elements[0] as SvgSymbol
        assertEquals(viewBox, symbol.viewBox)
    }

    @Test
    fun svgBuilderUseWithDimensions() {
        val elements = SvgBuilder().apply {
            use(href = "#icon", x = 10, y = 20, width = 50, height = 50)
        }.build()
        assertEquals(1, elements.size)
        val use = elements[0] as SvgUse
        assertEquals(50f, use.width)
        assertEquals(50f, use.height)
    }

    @Test
    fun svgBuilderLinearGradientWithUnits() {
        val elements = SvgBuilder().apply {
            linearGradient(
                id = "grad",
                x1 = 0f,
                y1 = 0f,
                x2 = 100f,
                y2 = 0f,
                gradientUnits = GradientUnits.USER_SPACE_ON_USE
            ) {
                start(Color.Red)
                end(Color.Blue)
            }
        }.build()
        assertEquals(1, elements.size)
        val gradient = elements[0] as SvgLinearGradient
        assertEquals(GradientUnits.USER_SPACE_ON_USE, gradient.gradientUnits)
    }

    @Test
    fun svgBuilderRadialGradientWithFocalPoint() {
        val elements = SvgBuilder().apply {
            radialGradient(
                id = "grad",
                cx = 0.5f,
                cy = 0.5f,
                r = 0.5f,
                fx = 0.25f,
                fy = 0.25f
            ) {
                start(Color.White)
                end(Color.Black)
            }
        }.build()
        assertEquals(1, elements.size)
        val gradient = elements[0] as SvgRadialGradient
        assertEquals(0.25f, gradient.fx)
        assertEquals(0.25f, gradient.fy)
    }

    @Test
    fun svgBuilderTextWithLetterSpacing() {
        val elements = SvgBuilder().apply {
            text("Hello", x = 10, y = 20, letterSpacing = 2f, dx = 1f, dy = 0.5f)
        }.build()
        assertEquals(1, elements.size)
        val text = elements[0] as SvgText
        assertEquals(2f, text.letterSpacing)
        assertEquals(1f, text.dx)
        assertEquals(0.5f, text.dy)
    }

    // ===========================================
    // AnimationBuilder Additional Tests
    // ===========================================

    @Test
    fun animationBuilderWithAllAnimationOptions() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            strokeDraw(
                dur = 1.seconds,
                delay = 200.milliseconds,
                calcMode = CalcMode.SPLINE,
                keySplines = KeySplines.EASE_IN_OUT,
                iterations = 3,
                direction = AnimationDirection.ALTERNATE,
                fillMode = AnimationFillMode.FORWARDS,
                reverse = true
            )
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.StrokeDraw
        assertEquals(CalcMode.SPLINE, anim.calcMode)
        assertEquals(KeySplines.EASE_IN_OUT, anim.keySplines)
        assertEquals(3, anim.iterations)
        assertEquals(AnimationDirection.ALTERNATE, anim.direction)
        assertEquals(AnimationFillMode.FORWARDS, anim.fillMode)
    }

    @Test
    fun animationBuilderMorphToWithDuration() {
        val path = SvgPath("M10 10 L20 20")
        val animations = AnimationBuilder(path).apply {
            morphTo("M5 15 L25 15", dur = 500.milliseconds, delay = 100.milliseconds)
        }.build()
        assertEquals(1, animations.size)
        val anim = animations[0] as SvgAnimate.D
        assertEquals(500.milliseconds, anim.dur)
        assertEquals(100.milliseconds, anim.delay)
    }

    @Test
    fun svgFunctionWithAllOptions() {
        val icon = svg(
            size = 32,
            stroke = Color.Blue,
            strokeWidth = 2f,
            strokeLinecap = LineCap.ROUND,
            strokeLinejoin = LineJoin.ROUND,
            fill = null
        ) {
            circle(16, 16, 12)
        }
        assertEquals(32f, icon.width)
        assertEquals(32f, icon.height)
        assertEquals(Color.Blue, icon.stroke)
        assertEquals(2f, icon.strokeWidth)
        assertEquals(LineCap.ROUND, icon.strokeLinecap)
        assertEquals(LineJoin.ROUND, icon.strokeLinejoin)
        assertNull(icon.fill)
    }
}
