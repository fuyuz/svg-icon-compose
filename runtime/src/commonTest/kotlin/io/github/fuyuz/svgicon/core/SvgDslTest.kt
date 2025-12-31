package io.github.fuyuz.svgicon.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SvgDslTest {

    @Test
    fun testPathDsl() {
        val elements = svg {
            path("M10 10 L20 20")
        }

        assertEquals(1, elements.size)
        assertIs<SvgPath>(elements[0])
        val path = elements[0] as SvgPath
        assertEquals(2, path.commands.size)
        assertIs<PathCommand.MoveTo>(path.commands[0])
        assertIs<PathCommand.LineTo>(path.commands[1])
    }

    @Test
    fun testCircleDsl() {
        val elements = svg {
            circle(10, 20, 5)
        }

        assertEquals(1, elements.size)
        assertIs<SvgCircle>(elements[0])
        val circle = elements[0] as SvgCircle
        assertEquals(10f, circle.cx)
        assertEquals(20f, circle.cy)
        assertEquals(5f, circle.r)
    }

    @Test
    fun testRectDsl() {
        val elements = svg {
            rect(x = 5, y = 10, width = 100, height = 50, rx = 3)
        }

        assertEquals(1, elements.size)
        assertIs<SvgRect>(elements[0])
        val rect = elements[0] as SvgRect
        assertEquals(5f, rect.x)
        assertEquals(10f, rect.y)
        assertEquals(100f, rect.width)
        assertEquals(50f, rect.height)
        assertEquals(3f, rect.rx)
        assertEquals(3f, rect.ry)
    }

    @Test
    fun testLineDsl() {
        val elements = svg {
            line(0, 0, 100, 100)
        }

        assertEquals(1, elements.size)
        assertIs<SvgLine>(elements[0])
        val line = elements[0] as SvgLine
        assertEquals(0f, line.x1)
        assertEquals(0f, line.y1)
        assertEquals(100f, line.x2)
        assertEquals(100f, line.y2)
    }

    @Test
    fun testEllipseDsl() {
        val elements = svg {
            ellipse(12, 12, 8, 4)
        }

        assertEquals(1, elements.size)
        assertIs<SvgEllipse>(elements[0])
        val ellipse = elements[0] as SvgEllipse
        assertEquals(12f, ellipse.cx)
        assertEquals(12f, ellipse.cy)
        assertEquals(8f, ellipse.rx)
        assertEquals(4f, ellipse.ry)
    }

    @Test
    fun testPolylineDsl() {
        val elements = svg {
            polyline(0 to 0, 10 to 10, 20 to 0)
        }

        assertEquals(1, elements.size)
        assertIs<SvgPolyline>(elements[0])
        val polyline = elements[0] as SvgPolyline
        assertEquals(3, polyline.points.size)
        assertEquals(0f to 0f, polyline.points[0])
        assertEquals(10f to 10f, polyline.points[1])
        assertEquals(20f to 0f, polyline.points[2])
    }

    @Test
    fun testPolygonDsl() {
        val elements = svg {
            polygon(0 to 0, 10 to 10, 20 to 0)
        }

        assertEquals(1, elements.size)
        assertIs<SvgPolygon>(elements[0])
        val polygon = elements[0] as SvgPolygon
        assertEquals(3, polygon.points.size)
    }

    @Test
    fun testGroupDsl() {
        val elements = svg {
            group {
                circle(10, 10, 5)
                path("M0 0 L10 10")
            }
        }

        assertEquals(1, elements.size)
        assertIs<SvgGroup>(elements[0])
        val group = elements[0] as SvgGroup
        assertEquals(2, group.children.size)
        assertIs<SvgCircle>(group.children[0])
        assertIs<SvgPath>(group.children[1])
    }

    @Test
    fun testAnimatedPathDsl() {
        val elements = svg {
            path("M10 10 L20 20") {
                strokeDraw(dur = 300, delay = 100)
            }
        }

        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
        val animated = elements[0] as SvgAnimated
        assertIs<SvgPath>(animated.element)
        assertEquals(1, animated.animations.size)
        assertIs<SvgAnimate.StrokeDraw>(animated.animations[0])
        val strokeDraw = animated.animations[0] as SvgAnimate.StrokeDraw
        assertEquals(300, strokeDraw.dur)
        assertEquals(100, strokeDraw.delay)
    }

    @Test
    fun testAnimatedCircleDsl() {
        val elements = svg {
            circle(12, 12, 8) {
                strokeDraw(dur = 500)
                opacity(from = 0f, to = 1f, dur = 300)
            }
        }

        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
        val animated = elements[0] as SvgAnimated
        assertIs<SvgCircle>(animated.element)
        assertEquals(2, animated.animations.size)
    }

    @Test
    fun testMultipleElements() {
        val elements = svg {
            path("M4 12h16")
            circle(12, 12, 8)
            rect(x = 2, y = 2, width = 20, height = 20)
            line(0, 0, 24, 24)
        }

        assertEquals(4, elements.size)
        assertIs<SvgPath>(elements[0])
        assertIs<SvgCircle>(elements[1])
        assertIs<SvgRect>(elements[2])
        assertIs<SvgLine>(elements[3])
    }

    @Test
    fun testTransformAnimation() {
        val elements = svg {
            circle(12, 12, 5) {
                rotate(from = 0f, to = 360f, dur = 1000)
                scale(from = 1f, to = 1.5f, dur = 500)
            }
        }

        assertEquals(1, elements.size)
        assertIs<SvgAnimated>(elements[0])
        val animated = elements[0] as SvgAnimated
        assertEquals(2, animated.animations.size)
        assertIs<SvgAnimate.Transform>(animated.animations[0])
        assertIs<SvgAnimate.Transform>(animated.animations[1])
    }
}
