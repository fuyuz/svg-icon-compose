package io.github.fuyuz.svgicon.core

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SvgStyleTest {

    // ===========================================
    // SvgStyle Default Tests
    // ===========================================

    @Test
    fun svgStyleDefaultValues() {
        val style = SvgStyle()
        assertNull(style.fill)
        assertNull(style.fillOpacity)
        assertNull(style.fillRule)
        assertNull(style.stroke)
        assertNull(style.strokeWidth)
        assertNull(style.strokeOpacity)
        assertNull(style.strokeLinecap)
        assertNull(style.strokeLinejoin)
        assertNull(style.strokeDasharray)
        assertNull(style.strokeDashoffset)
        assertNull(style.strokeMiterlimit)
        assertNull(style.opacity)
        assertNull(style.transform)
        assertNull(style.paintOrder)
        assertNull(style.vectorEffect)
        assertNull(style.clipPathId)
        assertNull(style.maskId)
        assertNull(style.markerStart)
        assertNull(style.markerMid)
        assertNull(style.markerEnd)
    }

    @Test
    fun svgStyleEmptyConstant() {
        val empty = SvgStyle.Empty
        assertEquals(SvgStyle(), empty)
    }

    // ===========================================
    // SvgStyle With Values Tests
    // ===========================================

    @Test
    fun svgStyleWithFill() {
        val style = SvgStyle(fill = Color.Red)
        assertEquals(Color.Red, style.fill)
    }

    @Test
    fun svgStyleWithStroke() {
        val style = SvgStyle(stroke = Color.Blue, strokeWidth = 3f)
        assertEquals(Color.Blue, style.stroke)
        assertEquals(3f, style.strokeWidth)
    }

    @Test
    fun svgStyleWithOpacity() {
        val style = SvgStyle(opacity = 0.5f, fillOpacity = 0.8f, strokeOpacity = 0.3f)
        assertEquals(0.5f, style.opacity)
        assertEquals(0.8f, style.fillOpacity)
        assertEquals(0.3f, style.strokeOpacity)
    }

    @Test
    fun svgStyleWithStrokeDetails() {
        val style = SvgStyle(
            strokeLinecap = LineCap.SQUARE,
            strokeLinejoin = LineJoin.BEVEL,
            strokeDasharray = listOf(5f, 3f, 2f),
            strokeDashoffset = 10f,
            strokeMiterlimit = 8f
        )
        assertEquals(LineCap.SQUARE, style.strokeLinecap)
        assertEquals(LineJoin.BEVEL, style.strokeLinejoin)
        assertEquals(listOf(5f, 3f, 2f), style.strokeDasharray)
        assertEquals(10f, style.strokeDashoffset)
        assertEquals(8f, style.strokeMiterlimit)
    }

    @Test
    fun svgStyleWithTransform() {
        val transform = SvgTransform.Rotate(45f)
        val style = SvgStyle(transform = transform)
        assertEquals(transform, style.transform)
    }

    @Test
    fun svgStyleWithPaintOrder() {
        val style = SvgStyle(paintOrder = PaintOrder.STROKE_FILL)
        assertEquals(PaintOrder.STROKE_FILL, style.paintOrder)
    }

    @Test
    fun svgStyleWithVectorEffect() {
        val style = SvgStyle(vectorEffect = VectorEffect.NON_SCALING_STROKE)
        assertEquals(VectorEffect.NON_SCALING_STROKE, style.vectorEffect)
    }

    @Test
    fun svgStyleWithClipPathAndMask() {
        val style = SvgStyle(clipPathId = "clip1", maskId = "mask1")
        assertEquals("clip1", style.clipPathId)
        assertEquals("mask1", style.maskId)
    }

    @Test
    fun svgStyleWithMarkers() {
        val style = SvgStyle(
            markerStart = "url(#arrow-start)",
            markerMid = "url(#arrow-mid)",
            markerEnd = "url(#arrow-end)"
        )
        assertEquals("url(#arrow-start)", style.markerStart)
        assertEquals("url(#arrow-mid)", style.markerMid)
        assertEquals("url(#arrow-end)", style.markerEnd)
    }

    @Test
    fun svgStyleWithFillRule() {
        val style = SvgStyle(fillRule = FillRule.EVENODD)
        assertEquals(FillRule.EVENODD, style.fillRule)
    }

    // ===========================================
    // FillRule Tests
    // ===========================================

    @Test
    fun fillRuleValues() {
        assertEquals(2, FillRule.entries.size)
        assertTrue(FillRule.entries.contains(FillRule.NONZERO))
        assertTrue(FillRule.entries.contains(FillRule.EVENODD))
    }

    // ===========================================
    // LineCap Tests
    // ===========================================

    @Test
    fun lineCapValues() {
        assertEquals(3, LineCap.entries.size)
        assertTrue(LineCap.entries.contains(LineCap.BUTT))
        assertTrue(LineCap.entries.contains(LineCap.ROUND))
        assertTrue(LineCap.entries.contains(LineCap.SQUARE))
    }

    // ===========================================
    // LineJoin Tests
    // ===========================================

    @Test
    fun lineJoinValues() {
        assertEquals(3, LineJoin.entries.size)
        assertTrue(LineJoin.entries.contains(LineJoin.MITER))
        assertTrue(LineJoin.entries.contains(LineJoin.ROUND))
        assertTrue(LineJoin.entries.contains(LineJoin.BEVEL))
    }

    // ===========================================
    // PaintOrder Tests
    // ===========================================

    @Test
    fun paintOrderValues() {
        assertEquals(2, PaintOrder.entries.size)
        assertTrue(PaintOrder.entries.contains(PaintOrder.FILL_STROKE))
        assertTrue(PaintOrder.entries.contains(PaintOrder.STROKE_FILL))
    }

    // ===========================================
    // VectorEffect Tests
    // ===========================================

    @Test
    fun vectorEffectValues() {
        assertEquals(2, VectorEffect.entries.size)
        assertTrue(VectorEffect.entries.contains(VectorEffect.NONE))
        assertTrue(VectorEffect.entries.contains(VectorEffect.NON_SCALING_STROKE))
    }

    // ===========================================
    // ClipPathUnits Tests
    // ===========================================

    @Test
    fun clipPathUnitsValues() {
        assertEquals(2, ClipPathUnits.entries.size)
        assertTrue(ClipPathUnits.entries.contains(ClipPathUnits.USER_SPACE_ON_USE))
        assertTrue(ClipPathUnits.entries.contains(ClipPathUnits.OBJECT_BOUNDING_BOX))
    }

    // ===========================================
    // MaskUnits Tests
    // ===========================================

    @Test
    fun maskUnitsValues() {
        assertEquals(2, MaskUnits.entries.size)
        assertTrue(MaskUnits.entries.contains(MaskUnits.USER_SPACE_ON_USE))
        assertTrue(MaskUnits.entries.contains(MaskUnits.OBJECT_BOUNDING_BOX))
    }

    // ===========================================
    // SvgTransform Tests
    // ===========================================

    @Test
    fun svgTransformTranslate() {
        val t = SvgTransform.Translate(10f, 20f)
        assertEquals(10f, t.x)
        assertEquals(20f, t.y)
    }

    @Test
    fun svgTransformTranslateDefaultY() {
        val t = SvgTransform.Translate(10f)
        assertEquals(10f, t.x)
        assertEquals(0f, t.y)
    }

    @Test
    fun svgTransformScale() {
        val t = SvgTransform.Scale(2f, 3f)
        assertEquals(2f, t.sx)
        assertEquals(3f, t.sy)
    }

    @Test
    fun svgTransformScaleUniform() {
        val t = SvgTransform.Scale(2f)
        assertEquals(2f, t.sx)
        assertEquals(2f, t.sy)
    }

    @Test
    fun svgTransformRotate() {
        val t = SvgTransform.Rotate(45f, 12f, 12f)
        assertEquals(45f, t.angle)
        assertEquals(12f, t.cx)
        assertEquals(12f, t.cy)
    }

    @Test
    fun svgTransformRotateDefault() {
        val t = SvgTransform.Rotate(90f)
        assertEquals(90f, t.angle)
        assertEquals(0f, t.cx)
        assertEquals(0f, t.cy)
    }

    @Test
    fun svgTransformSkewX() {
        val t = SvgTransform.SkewX(15f)
        assertEquals(15f, t.angle)
    }

    @Test
    fun svgTransformSkewY() {
        val t = SvgTransform.SkewY(20f)
        assertEquals(20f, t.angle)
    }

    @Test
    fun svgTransformMatrix() {
        val t = SvgTransform.Matrix(1f, 0f, 0f, 1f, 10f, 20f)
        assertEquals(1f, t.a)
        assertEquals(0f, t.b)
        assertEquals(0f, t.c)
        assertEquals(1f, t.d)
        assertEquals(10f, t.e)
        assertEquals(20f, t.f)
    }

    @Test
    fun svgTransformCombined() {
        val transforms = listOf(
            SvgTransform.Translate(10f, 20f),
            SvgTransform.Rotate(45f)
        )
        val t = SvgTransform.Combined(transforms)
        assertEquals(2, t.transforms.size)
    }

    // ===========================================
    // normalizeDashArray Tests
    // ===========================================

    @Test
    fun normalizeDashArrayEvenLength() {
        val result = normalizeDashArray(listOf(5f, 3f))
        assertEquals(2, result.size)
        assertEquals(5f, result[0])
        assertEquals(3f, result[1])
    }

    @Test
    fun normalizeDashArrayOddLengthRepeats() {
        // SVG spec: odd-length arrays are repeated to make them even
        val result = normalizeDashArray(listOf(5f, 3f, 2f))
        assertEquals(6, result.size)
        assertEquals(5f, result[0])
        assertEquals(3f, result[1])
        assertEquals(2f, result[2])
        assertEquals(5f, result[3])
        assertEquals(3f, result[4])
        assertEquals(2f, result[5])
    }

    @Test
    fun normalizeDashArraySingleValue() {
        // Single value becomes [5, 5]
        val result = normalizeDashArray(listOf(5f))
        assertEquals(2, result.size)
        assertEquals(5f, result[0])
        assertEquals(5f, result[1])
    }
}
