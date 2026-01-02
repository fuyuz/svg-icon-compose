package io.github.fuyuz.svgicon.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertFalse

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

    // ===========================================
    // Additional Relative Command Tests
    // ===========================================

    @Test
    fun testHorizontalLineToRelative() {
        val commands = parsePathCommands("M0 0 h25")
        assertEquals(2, commands.size)
        assertIs<PathCommand.HorizontalLineToRelative>(commands[1])
        assertEquals(25f, (commands[1] as PathCommand.HorizontalLineToRelative).dx)
    }

    @Test
    fun testVerticalLineToRelative() {
        val commands = parsePathCommands("M0 0 v35")
        assertEquals(2, commands.size)
        assertIs<PathCommand.VerticalLineToRelative>(commands[1])
        assertEquals(35f, (commands[1] as PathCommand.VerticalLineToRelative).dy)
    }

    @Test
    fun testCubicToRelative() {
        val commands = parsePathCommands("M0 0 c1 2 3 4 5 6")
        assertEquals(2, commands.size)
        assertIs<PathCommand.CubicToRelative>(commands[1])
        val cubic = commands[1] as PathCommand.CubicToRelative
        assertEquals(1f, cubic.dx1)
        assertEquals(2f, cubic.dy1)
        assertEquals(3f, cubic.dx2)
        assertEquals(4f, cubic.dy2)
        assertEquals(5f, cubic.dx)
        assertEquals(6f, cubic.dy)
    }

    @Test
    fun testSmoothCubicToRelative() {
        val commands = parsePathCommands("M0 0 s3 4 5 6")
        assertEquals(2, commands.size)
        assertIs<PathCommand.SmoothCubicToRelative>(commands[1])
        val smooth = commands[1] as PathCommand.SmoothCubicToRelative
        assertEquals(3f, smooth.dx2)
        assertEquals(4f, smooth.dy2)
        assertEquals(5f, smooth.dx)
        assertEquals(6f, smooth.dy)
    }

    @Test
    fun testQuadToRelative() {
        val commands = parsePathCommands("M0 0 q1 2 3 4")
        assertEquals(2, commands.size)
        assertIs<PathCommand.QuadToRelative>(commands[1])
        val quad = commands[1] as PathCommand.QuadToRelative
        assertEquals(1f, quad.dx1)
        assertEquals(2f, quad.dy1)
        assertEquals(3f, quad.dx)
        assertEquals(4f, quad.dy)
    }

    @Test
    fun testSmoothQuadTo() {
        val commands = parsePathCommands("M0 0 T5 6")
        assertEquals(2, commands.size)
        assertIs<PathCommand.SmoothQuadTo>(commands[1])
        val smooth = commands[1] as PathCommand.SmoothQuadTo
        assertEquals(5f, smooth.x)
        assertEquals(6f, smooth.y)
    }

    @Test
    fun testSmoothQuadToRelative() {
        val commands = parsePathCommands("M0 0 t5 6")
        assertEquals(2, commands.size)
        assertIs<PathCommand.SmoothQuadToRelative>(commands[1])
        val smooth = commands[1] as PathCommand.SmoothQuadToRelative
        assertEquals(5f, smooth.dx)
        assertEquals(6f, smooth.dy)
    }

    @Test
    fun testArcToRelative() {
        val commands = parsePathCommands("M0 0 a5 10 30 0 1 20 40")
        assertEquals(2, commands.size)
        assertIs<PathCommand.ArcToRelative>(commands[1])
        val arc = commands[1] as PathCommand.ArcToRelative
        assertEquals(5f, arc.rx)
        assertEquals(10f, arc.ry)
        assertEquals(30f, arc.xAxisRotation)
        assertFalse(arc.largeArcFlag)
        assertTrue(arc.sweepFlag)
        assertEquals(20f, arc.dx)
        assertEquals(40f, arc.dy)
    }

    @Test
    fun testCloseLowercase() {
        val commands = parsePathCommands("M0 0 L10 10 z")
        assertEquals(3, commands.size)
        assertIs<PathCommand.Close>(commands[2])
    }

    // ===========================================
    // Edge Case Tests
    // ===========================================

    @Test
    fun testImplicitLineToRelativeAfterMoveToRelative() {
        val commands = parsePathCommands("m0 0 10 20")
        assertEquals(2, commands.size)
        assertIs<PathCommand.MoveToRelative>(commands[0])
        assertIs<PathCommand.LineToRelative>(commands[1])
    }

    @Test
    fun testConsecutiveDecimals() {
        val commands = parsePathCommands("M.5.5")
        assertEquals(1, commands.size)
        val cmd = commands[0] as PathCommand.MoveTo
        assertEquals(0.5f, cmd.x)
        assertEquals(0.5f, cmd.y)
    }

    @Test
    fun testEmptyPath() {
        val commands = parsePathCommands("")
        assertEquals(0, commands.size)
    }

    @Test
    fun testWhitespaceOnly() {
        val commands = parsePathCommands("   \n\t  ")
        assertEquals(0, commands.size)
    }

    // ===========================================
    // Error Case Tests
    // ===========================================

    @Test
    fun testInvalidStartThrows() {
        assertFailsWith<SvgPathParseException> {
            parsePathCommands("10 20")
        }
    }

    @Test
    fun testUnknownCommandThrows() {
        assertFailsWith<SvgPathParseException> {
            parsePathCommands("M0 0 X10 20")
        }
    }

    @Test
    fun testInsufficientParametersThrows() {
        assertFailsWith<SvgPathParseException> {
            parsePathCommands("M10")
        }
    }

    @Test
    fun testInvalidNumberThrows() {
        assertFailsWith<SvgPathParseException> {
            parsePathCommands("M10 abc")
        }
    }

    // ===========================================
    // toPathString Tests
    // ===========================================

    @Test
    fun toPathStringMoveTo() {
        val commands = listOf(PathCommand.MoveTo(10f, 20f))
        assertEquals("M10.0 20.0", commands.toPathString())
    }

    @Test
    fun toPathStringMoveToRelative() {
        val commands = listOf(PathCommand.MoveToRelative(5f, 10f))
        assertEquals("m5.0 10.0", commands.toPathString())
    }

    @Test
    fun toPathStringLineTo() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.LineTo(30f, 40f)
        )
        assertEquals("M0.0 0.0 L30.0 40.0", commands.toPathString())
    }

    @Test
    fun toPathStringLineToRelative() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.LineToRelative(15f, 25f)
        )
        assertEquals("M0.0 0.0 l15.0 25.0", commands.toPathString())
    }

    @Test
    fun toPathStringHorizontalLineTo() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.HorizontalLineTo(50f)
        )
        assertEquals("M0.0 0.0 H50.0", commands.toPathString())
    }

    @Test
    fun toPathStringHorizontalLineToRelative() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.HorizontalLineToRelative(25f)
        )
        assertEquals("M0.0 0.0 h25.0", commands.toPathString())
    }

    @Test
    fun toPathStringVerticalLineTo() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.VerticalLineTo(60f)
        )
        assertEquals("M0.0 0.0 V60.0", commands.toPathString())
    }

    @Test
    fun toPathStringVerticalLineToRelative() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.VerticalLineToRelative(30f)
        )
        assertEquals("M0.0 0.0 v30.0", commands.toPathString())
    }

    @Test
    fun toPathStringCubicTo() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.CubicTo(1f, 2f, 3f, 4f, 5f, 6f)
        )
        assertEquals("M0.0 0.0 C1.0 2.0 3.0 4.0 5.0 6.0", commands.toPathString())
    }

    @Test
    fun toPathStringCubicToRelative() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.CubicToRelative(1f, 2f, 3f, 4f, 5f, 6f)
        )
        assertEquals("M0.0 0.0 c1.0 2.0 3.0 4.0 5.0 6.0", commands.toPathString())
    }

    @Test
    fun toPathStringSmoothCubicTo() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.SmoothCubicTo(3f, 4f, 5f, 6f)
        )
        assertEquals("M0.0 0.0 S3.0 4.0 5.0 6.0", commands.toPathString())
    }

    @Test
    fun toPathStringSmoothCubicToRelative() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.SmoothCubicToRelative(3f, 4f, 5f, 6f)
        )
        assertEquals("M0.0 0.0 s3.0 4.0 5.0 6.0", commands.toPathString())
    }

    @Test
    fun toPathStringQuadTo() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.QuadTo(1f, 2f, 3f, 4f)
        )
        assertEquals("M0.0 0.0 Q1.0 2.0 3.0 4.0", commands.toPathString())
    }

    @Test
    fun toPathStringQuadToRelative() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.QuadToRelative(1f, 2f, 3f, 4f)
        )
        assertEquals("M0.0 0.0 q1.0 2.0 3.0 4.0", commands.toPathString())
    }

    @Test
    fun toPathStringSmoothQuadTo() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.SmoothQuadTo(5f, 6f)
        )
        assertEquals("M0.0 0.0 T5.0 6.0", commands.toPathString())
    }

    @Test
    fun toPathStringSmoothQuadToRelative() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.SmoothQuadToRelative(5f, 6f)
        )
        assertEquals("M0.0 0.0 t5.0 6.0", commands.toPathString())
    }

    @Test
    fun toPathStringArcTo() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.ArcTo(5f, 5f, 0f, true, false, 10f, 10f)
        )
        assertEquals("M0.0 0.0 A5.0 5.0 0.0 1 0 10.0 10.0", commands.toPathString())
    }

    @Test
    fun toPathStringArcToRelative() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.ArcToRelative(5f, 5f, 0f, false, true, 10f, 10f)
        )
        assertEquals("M0.0 0.0 a5.0 5.0 0.0 0 1 10.0 10.0", commands.toPathString())
    }

    @Test
    fun toPathStringClose() {
        val commands = listOf(
            PathCommand.MoveTo(0f, 0f),
            PathCommand.LineTo(10f, 10f),
            PathCommand.Close
        )
        assertEquals("M0.0 0.0 L10.0 10.0 Z", commands.toPathString())
    }

    @Test
    fun toPathStringEmpty() {
        val commands = emptyList<PathCommand>()
        assertEquals("", commands.toPathString())
    }

    // ===========================================
    // SvgPathParseException Tests
    // ===========================================

    @Test
    fun svgPathParseExceptionWithMessage() {
        val exception = SvgPathParseException("test error")
        assertEquals("test error", exception.message)
    }

    @Test
    fun svgPathParseExceptionWithCause() {
        val cause = RuntimeException("cause")
        val exception = SvgPathParseException("test error", cause)
        assertEquals("test error", exception.message)
        assertEquals(cause, exception.cause)
    }
}
