package io.github.fuyuz.svgicon.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ViewBoxTest {

    // ===========================================
    // ViewBox Constructor Tests
    // ===========================================

    @Test
    fun viewBoxDefaultValues() {
        val viewBox = ViewBox()
        assertEquals(0f, viewBox.minX)
        assertEquals(0f, viewBox.minY)
        assertEquals(24f, viewBox.width)
        assertEquals(24f, viewBox.height)
    }

    @Test
    fun viewBoxCustomValues() {
        val viewBox = ViewBox(10f, 20f, 100f, 50f)
        assertEquals(10f, viewBox.minX)
        assertEquals(20f, viewBox.minY)
        assertEquals(100f, viewBox.width)
        assertEquals(50f, viewBox.height)
    }

    // ===========================================
    // ViewBox.parse Tests
    // ===========================================

    @Test
    fun parseViewBoxWithSpaces() {
        val viewBox = ViewBox.parse("0 0 48 48")
        assertEquals(0f, viewBox.minX)
        assertEquals(0f, viewBox.minY)
        assertEquals(48f, viewBox.width)
        assertEquals(48f, viewBox.height)
    }

    @Test
    fun parseViewBoxWithCommas() {
        val viewBox = ViewBox.parse("0,0,100,50")
        assertEquals(0f, viewBox.minX)
        assertEquals(0f, viewBox.minY)
        assertEquals(100f, viewBox.width)
        assertEquals(50f, viewBox.height)
    }

    @Test
    fun parseViewBoxWithMixedDelimiters() {
        val viewBox = ViewBox.parse("10, 20 30,40")
        assertEquals(10f, viewBox.minX)
        assertEquals(20f, viewBox.minY)
        assertEquals(30f, viewBox.width)
        assertEquals(40f, viewBox.height)
    }

    @Test
    fun parseViewBoxWithTwoValues() {
        val viewBox = ViewBox.parse("100 50")
        assertEquals(0f, viewBox.minX)
        assertEquals(0f, viewBox.minY)
        assertEquals(100f, viewBox.width)
        assertEquals(50f, viewBox.height)
    }

    @Test
    fun parseViewBoxWithInvalidInputReturnsDefault() {
        val viewBox = ViewBox.parse("invalid")
        assertEquals(ViewBox.Default, viewBox)
    }

    @Test
    fun parseViewBoxWithEmptyStringReturnsDefault() {
        val viewBox = ViewBox.parse("")
        assertEquals(ViewBox.Default, viewBox)
    }

    @Test
    fun parseViewBoxWithNegativeValues() {
        val viewBox = ViewBox.parse("-10 -20 100 50")
        assertEquals(-10f, viewBox.minX)
        assertEquals(-20f, viewBox.minY)
        assertEquals(100f, viewBox.width)
        assertEquals(50f, viewBox.height)
    }

    @Test
    fun parseViewBoxWithDecimalValues() {
        val viewBox = ViewBox.parse("0.5 1.5 24.5 48.5")
        assertEquals(0.5f, viewBox.minX)
        assertEquals(1.5f, viewBox.minY)
        assertEquals(24.5f, viewBox.width)
        assertEquals(48.5f, viewBox.height)
    }

    // ===========================================
    // ViewBox.toSvgString Tests
    // ===========================================

    @Test
    fun toSvgStringDefault() {
        val viewBox = ViewBox.Default
        assertEquals("0.0 0.0 24.0 24.0", viewBox.toSvgString())
    }

    @Test
    fun toSvgStringCustom() {
        val viewBox = ViewBox(10f, 20f, 100f, 50f)
        assertEquals("10.0 20.0 100.0 50.0", viewBox.toSvgString())
    }

    // ===========================================
    // ViewBox Companion Object Tests
    // ===========================================

    @Test
    fun viewBoxDefaultIs24x24() {
        assertEquals(24f, ViewBox.Default.width)
        assertEquals(24f, ViewBox.Default.height)
    }

    @Test
    fun viewBoxSize16Is16x16() {
        assertEquals(16f, ViewBox.Size16.width)
        assertEquals(16f, ViewBox.Size16.height)
        assertEquals(0f, ViewBox.Size16.minX)
        assertEquals(0f, ViewBox.Size16.minY)
    }

    @Test
    fun viewBoxSize32Is32x32() {
        assertEquals(32f, ViewBox.Size32.width)
        assertEquals(32f, ViewBox.Size32.height)
    }

    @Test
    fun viewBoxSize48Is48x48() {
        assertEquals(48f, ViewBox.Size48.width)
        assertEquals(48f, ViewBox.Size48.height)
    }

    @Test
    fun viewBoxSquareCreatesSquareViewBox() {
        val viewBox = ViewBox.square(64f)
        assertEquals(0f, viewBox.minX)
        assertEquals(0f, viewBox.minY)
        assertEquals(64f, viewBox.width)
        assertEquals(64f, viewBox.height)
    }

    @Test
    fun viewBoxOfCreatesViewBoxWithWidthAndHeight() {
        val viewBox = ViewBox.of(100, 50)
        assertEquals(0f, viewBox.minX)
        assertEquals(0f, viewBox.minY)
        assertEquals(100f, viewBox.width)
        assertEquals(50f, viewBox.height)
    }

    @Test
    fun viewBoxOfWithFloats() {
        val viewBox = ViewBox.of(100.5, 50.5)
        assertEquals(100.5f, viewBox.width)
        assertEquals(50.5f, viewBox.height)
    }

    // ===========================================
    // String.toViewBox Extension Tests
    // ===========================================

    @Test
    fun toViewBoxExtension() {
        val viewBox = "0 0 48 48".toViewBox()
        assertEquals(0f, viewBox.minX)
        assertEquals(0f, viewBox.minY)
        assertEquals(48f, viewBox.width)
        assertEquals(48f, viewBox.height)
    }

    @Test
    fun toViewBoxExtensionWithCommas() {
        val viewBox = "10,20,30,40".toViewBox()
        assertEquals(10f, viewBox.minX)
        assertEquals(20f, viewBox.minY)
        assertEquals(30f, viewBox.width)
        assertEquals(40f, viewBox.height)
    }
}

class AspectRatioAlignTest {

    @Test
    fun parseNone() {
        assertEquals(AspectRatioAlign.NONE, AspectRatioAlign.parse("none"))
    }

    @Test
    fun parseXMinYMin() {
        assertEquals(AspectRatioAlign.X_MIN_Y_MIN, AspectRatioAlign.parse("xMinYMin"))
    }

    @Test
    fun parseXMidYMin() {
        assertEquals(AspectRatioAlign.X_MID_Y_MIN, AspectRatioAlign.parse("xMidYMin"))
    }

    @Test
    fun parseXMaxYMin() {
        assertEquals(AspectRatioAlign.X_MAX_Y_MIN, AspectRatioAlign.parse("xMaxYMin"))
    }

    @Test
    fun parseXMinYMid() {
        assertEquals(AspectRatioAlign.X_MIN_Y_MID, AspectRatioAlign.parse("xMinYMid"))
    }

    @Test
    fun parseXMidYMid() {
        assertEquals(AspectRatioAlign.X_MID_Y_MID, AspectRatioAlign.parse("xMidYMid"))
    }

    @Test
    fun parseXMaxYMid() {
        assertEquals(AspectRatioAlign.X_MAX_Y_MID, AspectRatioAlign.parse("xMaxYMid"))
    }

    @Test
    fun parseXMinYMax() {
        assertEquals(AspectRatioAlign.X_MIN_Y_MAX, AspectRatioAlign.parse("xMinYMax"))
    }

    @Test
    fun parseXMidYMax() {
        assertEquals(AspectRatioAlign.X_MID_Y_MAX, AspectRatioAlign.parse("xMidYMax"))
    }

    @Test
    fun parseXMaxYMax() {
        assertEquals(AspectRatioAlign.X_MAX_Y_MAX, AspectRatioAlign.parse("xMaxYMax"))
    }

    @Test
    fun parseUnknownDefaultsToXMidYMid() {
        assertEquals(AspectRatioAlign.X_MID_Y_MID, AspectRatioAlign.parse("unknown"))
    }

    @Test
    fun parseCaseInsensitive() {
        assertEquals(AspectRatioAlign.NONE, AspectRatioAlign.parse("NONE"))
        assertEquals(AspectRatioAlign.X_MID_Y_MID, AspectRatioAlign.parse("XMIDYMID"))
    }
}

class MeetOrSliceTest {

    @Test
    fun parseMeet() {
        assertEquals(MeetOrSlice.MEET, MeetOrSlice.parse("meet"))
    }

    @Test
    fun parseSlice() {
        assertEquals(MeetOrSlice.SLICE, MeetOrSlice.parse("slice"))
    }

    @Test
    fun parseUnknownDefaultsToMeet() {
        assertEquals(MeetOrSlice.MEET, MeetOrSlice.parse("unknown"))
    }

    @Test
    fun parseCaseInsensitive() {
        assertEquals(MeetOrSlice.MEET, MeetOrSlice.parse("MEET"))
        assertEquals(MeetOrSlice.SLICE, MeetOrSlice.parse("SLICE"))
    }
}

class PreserveAspectRatioTest {

    @Test
    fun defaultValues() {
        val par = PreserveAspectRatio()
        assertEquals(AspectRatioAlign.X_MID_Y_MID, par.align)
        assertEquals(MeetOrSlice.MEET, par.meetOrSlice)
    }

    @Test
    fun parseWithAlignOnly() {
        val par = PreserveAspectRatio.parse("xMinYMin")
        assertEquals(AspectRatioAlign.X_MIN_Y_MIN, par.align)
        assertEquals(MeetOrSlice.MEET, par.meetOrSlice)
    }

    @Test
    fun parseWithAlignAndMeet() {
        val par = PreserveAspectRatio.parse("xMidYMid meet")
        assertEquals(AspectRatioAlign.X_MID_Y_MID, par.align)
        assertEquals(MeetOrSlice.MEET, par.meetOrSlice)
    }

    @Test
    fun parseWithAlignAndSlice() {
        val par = PreserveAspectRatio.parse("xMaxYMax slice")
        assertEquals(AspectRatioAlign.X_MAX_Y_MAX, par.align)
        assertEquals(MeetOrSlice.SLICE, par.meetOrSlice)
    }

    @Test
    fun parseNone() {
        val par = PreserveAspectRatio.parse("none")
        assertEquals(AspectRatioAlign.NONE, par.align)
        assertEquals(MeetOrSlice.MEET, par.meetOrSlice)
    }

    @Test
    fun parseWithMultipleSpaces() {
        val par = PreserveAspectRatio.parse("xMinYMax   slice")
        assertEquals(AspectRatioAlign.X_MIN_Y_MAX, par.align)
        assertEquals(MeetOrSlice.SLICE, par.meetOrSlice)
    }

    @Test
    fun parseWithExtraWhitespace() {
        val par = PreserveAspectRatio.parse("  xMidYMin  meet  ")
        assertEquals(AspectRatioAlign.X_MID_Y_MIN, par.align)
        assertEquals(MeetOrSlice.MEET, par.meetOrSlice)
    }

    @Test
    fun defaultConstant() {
        assertEquals(AspectRatioAlign.X_MID_Y_MID, PreserveAspectRatio.Default.align)
        assertEquals(MeetOrSlice.MEET, PreserveAspectRatio.Default.meetOrSlice)
    }

    @Test
    fun noneConstant() {
        assertEquals(AspectRatioAlign.NONE, PreserveAspectRatio.None.align)
    }
}
