package io.github.fuyuz.svgicon.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SvgParserTest {

    // ===========================================
    // ViewBox Parsing Tests
    // ===========================================

    @Test
    fun parseViewBoxWithSpaces() {
        val svg = parseSvg("""<svg viewBox="0 0 48 48"></svg>""")
        assertNotNull(svg.viewBox)
        assertEquals(0f, svg.viewBox!!.minX)
        assertEquals(0f, svg.viewBox!!.minY)
        assertEquals(48f, svg.viewBox!!.width)
        assertEquals(48f, svg.viewBox!!.height)
    }

    @Test
    fun parseViewBoxWithCommas() {
        val svg = parseSvg("""<svg viewBox="0,0,24,24"></svg>""")
        assertNotNull(svg.viewBox)
        assertEquals(24f, svg.viewBox!!.width)
        assertEquals(24f, svg.viewBox!!.height)
    }

    @Test
    fun parseViewBoxWithOffset() {
        val svg = parseSvg("""<svg viewBox="10 20 100 200"></svg>""")
        assertNotNull(svg.viewBox)
        assertEquals(10f, svg.viewBox!!.minX)
        assertEquals(20f, svg.viewBox!!.minY)
        assertEquals(100f, svg.viewBox!!.width)
        assertEquals(200f, svg.viewBox!!.height)
    }

    @Test
    fun parseSvgWithoutViewBox() {
        val svg = parseSvg("""<svg><circle cx="12" cy="12" r="10"/></svg>""")
        assertNull(svg.viewBox)
    }

    // ===========================================
    // Width/Height Parsing Tests
    // ===========================================

    @Test
    fun parseWidthHeight() {
        val svg = parseSvg("""<svg width="48" height="48"></svg>""")
        assertEquals(48f, svg.width)
        assertEquals(48f, svg.height)
    }

    @Test
    fun parseWidthHeightWithPx() {
        val svg = parseSvg("""<svg width="48px" height="48px"></svg>""")
        assertEquals(48f, svg.width)
        assertEquals(48f, svg.height)
    }

    @Test
    fun parseWidthHeightWithPt() {
        val svg = parseSvg("""<svg width="24pt" height="24pt"></svg>""")
        assertEquals(24f, svg.width)
        assertEquals(24f, svg.height)
    }

    @Test
    fun parseSvgWithoutWidthHeight() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24"></svg>""")
        assertNull(svg.width)
        assertNull(svg.height)
    }

    @Test
    fun parseWidthHeightWithViewBox() {
        val svg = parseSvg("""<svg width="100" height="100" viewBox="0 0 24 24"></svg>""")
        assertEquals(100f, svg.width)
        assertEquals(100f, svg.height)
        assertNotNull(svg.viewBox)
        assertEquals(24f, svg.viewBox!!.width)
        assertEquals(24f, svg.viewBox!!.height)
    }

    // ===========================================
    // PreserveAspectRatio Parsing Tests
    // ===========================================

    @Test
    fun parsePreserveAspectRatioDefault() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24"></svg>""")
        assertEquals(AspectRatioAlign.X_MID_Y_MID, svg.preserveAspectRatio.align)
        assertEquals(MeetOrSlice.MEET, svg.preserveAspectRatio.meetOrSlice)
    }

    @Test
    fun parsePreserveAspectRatioNone() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24" preserveAspectRatio="none"></svg>""")
        assertEquals(AspectRatioAlign.NONE, svg.preserveAspectRatio.align)
    }

    @Test
    fun parsePreserveAspectRatioXMinYMin() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24" preserveAspectRatio="xMinYMin"></svg>""")
        assertEquals(AspectRatioAlign.X_MIN_Y_MIN, svg.preserveAspectRatio.align)
        assertEquals(MeetOrSlice.MEET, svg.preserveAspectRatio.meetOrSlice)
    }

    @Test
    fun parsePreserveAspectRatioXMidYMid() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24" preserveAspectRatio="xMidYMid"></svg>""")
        assertEquals(AspectRatioAlign.X_MID_Y_MID, svg.preserveAspectRatio.align)
    }

    @Test
    fun parsePreserveAspectRatioXMaxYMax() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24" preserveAspectRatio="xMaxYMax"></svg>""")
        assertEquals(AspectRatioAlign.X_MAX_Y_MAX, svg.preserveAspectRatio.align)
    }

    @Test
    fun parsePreserveAspectRatioWithMeet() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24" preserveAspectRatio="xMidYMid meet"></svg>""")
        assertEquals(AspectRatioAlign.X_MID_Y_MID, svg.preserveAspectRatio.align)
        assertEquals(MeetOrSlice.MEET, svg.preserveAspectRatio.meetOrSlice)
    }

    @Test
    fun parsePreserveAspectRatioWithSlice() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24" preserveAspectRatio="xMidYMid slice"></svg>""")
        assertEquals(AspectRatioAlign.X_MID_Y_MID, svg.preserveAspectRatio.align)
        assertEquals(MeetOrSlice.SLICE, svg.preserveAspectRatio.meetOrSlice)
    }

    @Test
    fun parsePreserveAspectRatioXMinYMaxSlice() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24" preserveAspectRatio="xMinYMax slice"></svg>""")
        assertEquals(AspectRatioAlign.X_MIN_Y_MAX, svg.preserveAspectRatio.align)
        assertEquals(MeetOrSlice.SLICE, svg.preserveAspectRatio.meetOrSlice)
    }

    // ===========================================
    // Combination Tests
    // ===========================================

    @Test
    fun parseFullSvgWithAllViewportAttributes() {
        val svg = parseSvg("""<svg width="100" height="100" viewBox="0 0 24 24" preserveAspectRatio="xMinYMin meet"></svg>""")
        assertEquals(100f, svg.width)
        assertEquals(100f, svg.height)
        assertNotNull(svg.viewBox)
        assertEquals(24f, svg.viewBox!!.width)
        assertEquals(AspectRatioAlign.X_MIN_Y_MIN, svg.preserveAspectRatio.align)
        assertEquals(MeetOrSlice.MEET, svg.preserveAspectRatio.meetOrSlice)
    }

    @Test
    fun parseSvgWithChildren() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgCircle>(svg.children[0])
    }

    @Test
    fun parseSvgWithPath() {
        val svg = parseSvg("""<svg viewBox="0 0 24 24"><path d="M12 12"/></svg>""")
        assertEquals(1, svg.children.size)
        assertIs<SvgPath>(svg.children[0])
    }

    // ===========================================
    // unsafeSvg Tests
    // ===========================================

    @Test
    fun unsafeSvgParsesViewBox() {
        val result = unsafeSvg("""<svg viewBox="0 0 48 48"><circle cx="24" cy="24" r="20"/></svg>""")
        assertNotNull(result.viewBox)
        assertEquals(48f, result.viewBox!!.width)
        assertEquals(48f, result.viewBox!!.height)
    }

    @Test
    fun unsafeSvgWithoutViewBox() {
        val result = unsafeSvg("""<svg><circle cx="12" cy="12" r="10"/></svg>""")
        assertNull(result.viewBox)
    }

    // ===========================================
    // ViewBox.parse Tests
    // ===========================================

    @Test
    fun viewBoxParseWithSpaces() {
        val viewBox = ViewBox.parse("0 0 24 24")
        assertEquals(0f, viewBox.minX)
        assertEquals(0f, viewBox.minY)
        assertEquals(24f, viewBox.width)
        assertEquals(24f, viewBox.height)
    }

    @Test
    fun viewBoxParseWithCommas() {
        val viewBox = ViewBox.parse("0,0,48,48")
        assertEquals(48f, viewBox.width)
        assertEquals(48f, viewBox.height)
    }

    @Test
    fun viewBoxParseInvalid() {
        val viewBox = ViewBox.parse("invalid")
        assertEquals(ViewBox.Default, viewBox)
    }

    // ===========================================
    // PreserveAspectRatio.parse Tests
    // ===========================================

    @Test
    fun preserveAspectRatioParseNone() {
        val par = PreserveAspectRatio.parse("none")
        assertEquals(AspectRatioAlign.NONE, par.align)
    }

    @Test
    fun preserveAspectRatioParseXMidYMid() {
        val par = PreserveAspectRatio.parse("xMidYMid")
        assertEquals(AspectRatioAlign.X_MID_Y_MID, par.align)
        assertEquals(MeetOrSlice.MEET, par.meetOrSlice)
    }

    @Test
    fun preserveAspectRatioParseWithSlice() {
        val par = PreserveAspectRatio.parse("xMaxYMin slice")
        assertEquals(AspectRatioAlign.X_MAX_Y_MIN, par.align)
        assertEquals(MeetOrSlice.SLICE, par.meetOrSlice)
    }

    // ===========================================
    // AspectRatioAlign.parse Tests
    // ===========================================

    @Test
    fun aspectRatioAlignParseAllValues() {
        assertEquals(AspectRatioAlign.NONE, AspectRatioAlign.parse("none"))
        assertEquals(AspectRatioAlign.X_MIN_Y_MIN, AspectRatioAlign.parse("xMinYMin"))
        assertEquals(AspectRatioAlign.X_MID_Y_MIN, AspectRatioAlign.parse("xMidYMin"))
        assertEquals(AspectRatioAlign.X_MAX_Y_MIN, AspectRatioAlign.parse("xMaxYMin"))
        assertEquals(AspectRatioAlign.X_MIN_Y_MID, AspectRatioAlign.parse("xMinYMid"))
        assertEquals(AspectRatioAlign.X_MID_Y_MID, AspectRatioAlign.parse("xMidYMid"))
        assertEquals(AspectRatioAlign.X_MAX_Y_MID, AspectRatioAlign.parse("xMaxYMid"))
        assertEquals(AspectRatioAlign.X_MIN_Y_MAX, AspectRatioAlign.parse("xMinYMax"))
        assertEquals(AspectRatioAlign.X_MID_Y_MAX, AspectRatioAlign.parse("xMidYMax"))
        assertEquals(AspectRatioAlign.X_MAX_Y_MAX, AspectRatioAlign.parse("xMaxYMax"))
    }

    @Test
    fun aspectRatioAlignParseCaseInsensitive() {
        assertEquals(AspectRatioAlign.X_MID_Y_MID, AspectRatioAlign.parse("XMIDYMID"))
        assertEquals(AspectRatioAlign.X_MIN_Y_MIN, AspectRatioAlign.parse("xminymin"))
    }

    @Test
    fun aspectRatioAlignParseUnknownReturnsDefault() {
        assertEquals(AspectRatioAlign.X_MID_Y_MID, AspectRatioAlign.parse("unknown"))
    }

    // ===========================================
    // MeetOrSlice.parse Tests
    // ===========================================

    @Test
    fun meetOrSliceParseMeet() {
        assertEquals(MeetOrSlice.MEET, MeetOrSlice.parse("meet"))
    }

    @Test
    fun meetOrSliceParseSlice() {
        assertEquals(MeetOrSlice.SLICE, MeetOrSlice.parse("slice"))
    }

    @Test
    fun meetOrSliceParseCaseInsensitive() {
        assertEquals(MeetOrSlice.SLICE, MeetOrSlice.parse("SLICE"))
    }

    @Test
    fun meetOrSliceParseUnknownReturnsMeet() {
        assertEquals(MeetOrSlice.MEET, MeetOrSlice.parse("unknown"))
    }
}
