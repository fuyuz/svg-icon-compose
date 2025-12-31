package io.github.fuyuz.svgicon.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PathCommandTest {

    @Test
    fun testMoveToAbsolute() {
        val commands = parsePathCommands("M10 20")
        assertEquals(1, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        val moveTo = commands[0] as PathCommand.MoveTo
        assertEquals(10f, moveTo.x)
        assertEquals(20f, moveTo.y)
    }

    @Test
    fun testMoveToRelative() {
        val commands = parsePathCommands("m5 10")
        assertEquals(1, commands.size)
        assertIs<PathCommand.MoveToRelative>(commands[0])
        val moveTo = commands[0] as PathCommand.MoveToRelative
        assertEquals(5f, moveTo.dx)
        assertEquals(10f, moveTo.dy)
    }

    @Test
    fun testLineTo() {
        val commands = parsePathCommands("M0 0 L10 20")
        assertEquals(2, commands.size)
        assertIs<PathCommand.LineTo>(commands[1])
        val lineTo = commands[1] as PathCommand.LineTo
        assertEquals(10f, lineTo.x)
        assertEquals(20f, lineTo.y)
    }

    @Test
    fun testLineToRelative() {
        val commands = parsePathCommands("M0 0 l5 -10")
        assertEquals(2, commands.size)
        assertIs<PathCommand.LineToRelative>(commands[1])
        val lineTo = commands[1] as PathCommand.LineToRelative
        assertEquals(5f, lineTo.dx)
        assertEquals(-10f, lineTo.dy)
    }

    @Test
    fun testHorizontalLineTo() {
        val commands = parsePathCommands("M0 0 H20")
        assertEquals(2, commands.size)
        assertIs<PathCommand.HorizontalLineTo>(commands[1])
        assertEquals(20f, (commands[1] as PathCommand.HorizontalLineTo).x)
    }

    @Test
    fun testVerticalLineTo() {
        val commands = parsePathCommands("M0 0 V30")
        assertEquals(2, commands.size)
        assertIs<PathCommand.VerticalLineTo>(commands[1])
        assertEquals(30f, (commands[1] as PathCommand.VerticalLineTo).y)
    }

    @Test
    fun testCubicBezier() {
        val commands = parsePathCommands("M0 0 C10 20 30 40 50 60")
        assertEquals(2, commands.size)
        assertIs<PathCommand.CubicTo>(commands[1])
        val cubic = commands[1] as PathCommand.CubicTo
        assertEquals(10f, cubic.x1)
        assertEquals(20f, cubic.y1)
        assertEquals(30f, cubic.x2)
        assertEquals(40f, cubic.y2)
        assertEquals(50f, cubic.x)
        assertEquals(60f, cubic.y)
    }

    @Test
    fun testSmoothCubic() {
        val commands = parsePathCommands("M0 0 S30 40 50 60")
        assertEquals(2, commands.size)
        assertIs<PathCommand.SmoothCubicTo>(commands[1])
        val smooth = commands[1] as PathCommand.SmoothCubicTo
        assertEquals(30f, smooth.x2)
        assertEquals(40f, smooth.y2)
        assertEquals(50f, smooth.x)
        assertEquals(60f, smooth.y)
    }

    @Test
    fun testQuadraticBezier() {
        val commands = parsePathCommands("M0 0 Q10 20 30 40")
        assertEquals(2, commands.size)
        assertIs<PathCommand.QuadTo>(commands[1])
        val quad = commands[1] as PathCommand.QuadTo
        assertEquals(10f, quad.x1)
        assertEquals(20f, quad.y1)
        assertEquals(30f, quad.x)
        assertEquals(40f, quad.y)
    }

    @Test
    fun testArc() {
        val commands = parsePathCommands("M0 0 A5 10 30 1 0 20 40")
        assertEquals(2, commands.size)
        assertIs<PathCommand.ArcTo>(commands[1])
        val arc = commands[1] as PathCommand.ArcTo
        assertEquals(5f, arc.rx)
        assertEquals(10f, arc.ry)
        assertEquals(30f, arc.xAxisRotation)
        assertTrue(arc.largeArcFlag)
        assertTrue(!arc.sweepFlag)
        assertEquals(20f, arc.x)
        assertEquals(40f, arc.y)
    }

    @Test
    fun testClose() {
        val commands = parsePathCommands("M0 0 L10 10 Z")
        assertEquals(3, commands.size)
        assertIs<PathCommand.Close>(commands[2])
    }

    @Test
    fun testComplexPath() {
        // Check icon path
        val commands = parsePathCommands("M20 6 9 17l-5-5")
        assertEquals(3, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        assertIs<PathCommand.LineTo>(commands[1])
        assertIs<PathCommand.LineToRelative>(commands[2])

        val moveTo = commands[0] as PathCommand.MoveTo
        assertEquals(20f, moveTo.x)
        assertEquals(6f, moveTo.y)

        val lineTo = commands[1] as PathCommand.LineTo
        assertEquals(9f, lineTo.x)
        assertEquals(17f, lineTo.y)

        val lineToRel = commands[2] as PathCommand.LineToRelative
        assertEquals(-5f, lineToRel.dx)
        assertEquals(-5f, lineToRel.dy)
    }

    @Test
    fun testSearchIconPath() {
        // Search icon path
        val commands = parsePathCommands("m21 21-4.35-4.35")
        assertEquals(2, commands.size)
        assertIs<PathCommand.MoveToRelative>(commands[0])
        assertIs<PathCommand.LineToRelative>(commands[1])

        val moveToRel = commands[0] as PathCommand.MoveToRelative
        assertEquals(21f, moveToRel.dx)
        assertEquals(21f, moveToRel.dy)

        val lineToRel = commands[1] as PathCommand.LineToRelative
        assertEquals(-4.35f, lineToRel.dx)
        assertEquals(-4.35f, lineToRel.dy)
    }

    @Test
    fun testNegativeNumbers() {
        val commands = parsePathCommands("M-10-20 L-30-40")
        assertEquals(2, commands.size)

        val moveTo = commands[0] as PathCommand.MoveTo
        assertEquals(-10f, moveTo.x)
        assertEquals(-20f, moveTo.y)

        val lineTo = commands[1] as PathCommand.LineTo
        assertEquals(-30f, lineTo.x)
        assertEquals(-40f, lineTo.y)
    }

    @Test
    fun testDecimalNumbers() {
        val commands = parsePathCommands("M1.5 2.75 L3.125 4.5")
        assertEquals(2, commands.size)

        val moveTo = commands[0] as PathCommand.MoveTo
        assertEquals(1.5f, moveTo.x)
        assertEquals(2.75f, moveTo.y)

        val lineTo = commands[1] as PathCommand.LineTo
        assertEquals(3.125f, lineTo.x)
        assertEquals(4.5f, lineTo.y)
    }

    @Test
    fun testImplicitLineTo() {
        // After M, additional coordinate pairs become implicit L commands
        val commands = parsePathCommands("M0 0 10 10 20 20")
        assertEquals(3, commands.size)
        assertIs<PathCommand.MoveTo>(commands[0])
        assertIs<PathCommand.LineTo>(commands[1])
        assertIs<PathCommand.LineTo>(commands[2])
    }
}
