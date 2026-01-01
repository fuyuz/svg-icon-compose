package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import io.github.fuyuz.svgicon.core.dsl.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class SvgDslTest {

    @Test
    fun testPathDsl() {
        val result = svg {
            path("M10 10 L20 20")
        }

        assertEquals(1, result.children.size)
        assertIs<SvgPath>(result.children[0])
        val path = result.children[0] as SvgPath
        assertEquals(2, path.commands.size)
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.LineTo>(path.commands[1])
    }

    @Test
    fun testCircleDsl() {
        val result = svg {
            circle(10, 20, 5)
        }

        assertEquals(1, result.children.size)
        assertIs<SvgCircle>(result.children[0])
        val circle = result.children[0] as SvgCircle
        assertEquals(10f, circle.cx)
        assertEquals(20f, circle.cy)
        assertEquals(5f, circle.r)
    }

    @Test
    fun testRectDsl() {
        val result = svg {
            rect(x = 5, y = 10, width = 100, height = 50, rx = 3)
        }

        assertEquals(1, result.children.size)
        assertIs<SvgRect>(result.children[0])
        val rect = result.children[0] as SvgRect
        assertEquals(5f, rect.x)
        assertEquals(10f, rect.y)
        assertEquals(100f, rect.width)
        assertEquals(50f, rect.height)
        assertEquals(3f, rect.rx)
        assertEquals(3f, rect.ry)
    }

    @Test
    fun testLineDsl() {
        val result = svg {
            line(0, 0, 100, 100)
        }

        assertEquals(1, result.children.size)
        assertIs<SvgLine>(result.children[0])
        val line = result.children[0] as SvgLine
        assertEquals(0f, line.x1)
        assertEquals(0f, line.y1)
        assertEquals(100f, line.x2)
        assertEquals(100f, line.y2)
    }

    @Test
    fun testEllipseDsl() {
        val result = svg {
            ellipse(12, 12, 8, 4)
        }

        assertEquals(1, result.children.size)
        assertIs<SvgEllipse>(result.children[0])
        val ellipse = result.children[0] as SvgEllipse
        assertEquals(12f, ellipse.cx)
        assertEquals(12f, ellipse.cy)
        assertEquals(8f, ellipse.rx)
        assertEquals(4f, ellipse.ry)
    }

    @Test
    fun testPolylineDsl() {
        val result = svg {
            polyline(0 to 0, 10 to 10, 20 to 0)
        }

        assertEquals(1, result.children.size)
        assertIs<SvgPolyline>(result.children[0])
        val polyline = result.children[0] as SvgPolyline
        assertEquals(3, polyline.points.size)
        assertEquals(Offset(0f, 0f), polyline.points[0])
        assertEquals(Offset(10f, 10f), polyline.points[1])
        assertEquals(Offset(20f, 0f), polyline.points[2])
    }

    @Test
    fun testPolygonDsl() {
        val result = svg {
            polygon(0 to 0, 10 to 10, 20 to 0)
        }

        assertEquals(1, result.children.size)
        assertIs<SvgPolygon>(result.children[0])
        val polygon = result.children[0] as SvgPolygon
        assertEquals(3, polygon.points.size)
    }

    @Test
    fun testGroupDsl() {
        val result = svg {
            group {
                circle(10, 10, 5)
                path("M0 0 L10 10")
            }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgGroup>(result.children[0])
        val group = result.children[0] as SvgGroup
        assertEquals(2, group.children.size)
        assertIs<SvgCircle>(group.children[0])
        assertIs<SvgPath>(group.children[1])
    }

    @Test
    fun testAnimatedPathDsl() {
        val result = svg {
            path("M10 10 L20 20") {
                strokeDraw(dur = 300.milliseconds, delay = 100.milliseconds)
            }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgAnimated>(result.children[0])
        val animated = result.children[0] as SvgAnimated
        assertIs<SvgPath>(animated.element)
        assertEquals(1, animated.animations.size)
        assertIs<SvgAnimate.StrokeDraw>(animated.animations[0])
        val strokeDraw = animated.animations[0] as SvgAnimate.StrokeDraw
        assertEquals(300.milliseconds, strokeDraw.dur)
        assertEquals(100.milliseconds, strokeDraw.delay)
    }

    @Test
    fun testAnimatedCircleDsl() {
        val result = svg {
            circle(12, 12, 8) {
                strokeDraw(dur = 500.milliseconds)
                opacity(from = 0f, to = 1f, dur = 300.milliseconds)
            }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgAnimated>(result.children[0])
        val animated = result.children[0] as SvgAnimated
        assertIs<SvgCircle>(animated.element)
        assertEquals(2, animated.animations.size)
    }

    @Test
    fun testMultipleElements() {
        val result = svg {
            path("M4 12h16")
            circle(12, 12, 8)
            rect(x = 2, y = 2, width = 20, height = 20)
            line(0, 0, 24, 24)
        }

        assertEquals(4, result.children.size)
        assertIs<SvgPath>(result.children[0])
        assertIs<SvgCircle>(result.children[1])
        assertIs<SvgRect>(result.children[2])
        assertIs<SvgLine>(result.children[3])
    }

    @Test
    fun testTransformAnimation() {
        val result = svg {
            circle(12, 12, 5) {
                rotate(from = 0f, to = 360f, dur = 1000.milliseconds)
                scale(from = 1f, to = 1.5f, dur = 500.milliseconds)
            }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgAnimated>(result.children[0])
        val animated = result.children[0] as SvgAnimated
        assertEquals(2, animated.animations.size)
        assertIs<SvgAnimate.Transform>(animated.animations[0])
        assertIs<SvgAnimate.Transform>(animated.animations[1])
    }

    // ============================================
    // Tests for new APIs
    // ============================================

    @Test
    fun testPathBuilderDsl() {
        val result = svg {
            path {
                moveTo(20f, 6f)
                lineTo(9f, 17f)
                lineToRelative(-5f, -5f)
                close()
            }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgPath>(result.children[0])
        val path = result.children[0] as SvgPath
        assertEquals(4, path.commands.size)
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.LineTo>(path.commands[1])
        assertIs<PathCommand.LineToRelative>(path.commands[2])
        assertIs<PathCommand.Close>(path.commands[3])
    }

    @Test
    fun testPathWithStyleParams() {
        val result = svg {
            path("M10 10") styled { strokeWidth = 3f }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgStyled>(result.children[0])
        val styled = result.children[0] as SvgStyled
        assertIs<SvgPath>(styled.element)
        assertEquals(3f, styled.style.strokeWidth)
    }

    @Test
    fun testCircleWithStyleParams() {
        val result = svg {
            circle(12, 12, 10) styled {
                strokeWidth = 2f
                opacity = 0.5f
            }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgStyled>(result.children[0])
        val styled = result.children[0] as SvgStyled
        assertIs<SvgCircle>(styled.element)
        assertEquals(2f, styled.style.strokeWidth)
        assertEquals(0.5f, styled.style.opacity)
    }

    @Test
    fun testPolylineVarargSyntax() {
        val result = svg {
            polyline(5 to 12, 12 to 5, 19 to 12)
        }

        assertEquals(1, result.children.size)
        assertIs<SvgPolyline>(result.children[0])
        val polyline = result.children[0] as SvgPolyline
        assertEquals(3, polyline.points.size)
        assertEquals(Offset(5f, 12f), polyline.points[0])
        assertEquals(Offset(12f, 5f), polyline.points[1])
        assertEquals(Offset(19f, 12f), polyline.points[2])
    }

    @Test
    fun testPolygonVarargSyntax() {
        val result = svg {
            polygon(12 to 2, 22 to 22, 2 to 22)
        }

        assertEquals(1, result.children.size)
        assertIs<SvgPolygon>(result.children[0])
        val polygon = result.children[0] as SvgPolygon
        assertEquals(3, polygon.points.size)
        assertEquals(Offset(12f, 2f), polygon.points[0])
        assertEquals(Offset(22f, 22f), polygon.points[1])
        assertEquals(Offset(2f, 22f), polygon.points[2])
    }

    @Test
    fun testPolylineStringFormat() {
        val result = svg {
            polyline("5,12 12,5 19,12")
        }

        assertEquals(1, result.children.size)
        assertIs<SvgPolyline>(result.children[0])
        val polyline = result.children[0] as SvgPolyline
        assertEquals(3, polyline.points.size)
        assertEquals(Offset(5f, 12f), polyline.points[0])
    }

    @Test
    fun testPolygonWithStyleParams() {
        val result = svg {
            polygon(12 to 2, 22 to 22, 2 to 22) styled { strokeWidth = 2f }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgStyled>(result.children[0])
        val styled = result.children[0] as SvgStyled
        assertIs<SvgPolygon>(styled.element)
        assertEquals(2f, styled.style.strokeWidth)
    }

    @Test
    fun testRectWithStyleParams() {
        val result = svg {
            rect(4, 4, 16, 16) styled { strokeWidth = 1f }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgStyled>(result.children[0])
        val styled = result.children[0] as SvgStyled
        assertIs<SvgRect>(styled.element)
        assertEquals(1f, styled.style.strokeWidth)
    }

    @Test
    fun testLineWithStyleParams() {
        val result = svg {
            line(0, 0, 24, 24) styled { strokeWidth = 2f }
        }

        assertEquals(1, result.children.size)
        assertIs<SvgStyled>(result.children[0])
        val styled = result.children[0] as SvgStyled
        assertIs<SvgLine>(styled.element)
        assertEquals(2f, styled.style.strokeWidth)
    }

    @Test
    fun testViewBox() {
        val viewBox = ViewBox(0f, 0f, 24f, 24f)
        assertEquals(0f, viewBox.minX)
        assertEquals(0f, viewBox.minY)
        assertEquals(24f, viewBox.width)
        assertEquals(24f, viewBox.height)

        val parsed = ViewBox.parse("0 0 48 48")
        assertEquals(48f, parsed.width)
        assertEquals(48f, parsed.height)
    }

    @Test
    fun testAnimatedPathWithPathBuilder() {
        val result = svg {
            animatedPath(
                pathBlock = {
                    moveTo(10f, 10f)
                    lineTo(20f, 20f)
                },
                animBlock = {
                    strokeDraw(dur = 500.milliseconds)
                }
            )
        }

        assertEquals(1, result.children.size)
        assertIs<SvgAnimated>(result.children[0])
        val animated = result.children[0] as SvgAnimated
        assertIs<SvgPath>(animated.element)
        val path = animated.element as SvgPath
        assertEquals(2, path.commands.size)
    }

    @Test
    fun testSvgReturnsCorrectType() {
        val result = svg {
            circle(12, 12, 10)
        }

        // svg {} should return Svg, not List<SvgElement>
        assertIs<Svg>(result)
        assertEquals(1, result.children.size)
    }
}
