package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SvgAnimationTest {

    // ===========================================
    // DefaultAnimationDuration Tests
    // ===========================================

    @Test
    fun defaultAnimationDurationIs500ms() {
        assertEquals(500.milliseconds, DefaultAnimationDuration)
    }

    // ===========================================
    // CalcMode Tests
    // ===========================================

    @Test
    fun calcModeValues() {
        assertEquals(4, CalcMode.entries.size)
        assertTrue(CalcMode.entries.contains(CalcMode.LINEAR))
        assertTrue(CalcMode.entries.contains(CalcMode.DISCRETE))
        assertTrue(CalcMode.entries.contains(CalcMode.PACED))
        assertTrue(CalcMode.entries.contains(CalcMode.SPLINE))
    }

    // ===========================================
    // KeySplines Tests
    // ===========================================

    @Test
    fun keySplinesProperties() {
        val splines = KeySplines(0.1f, 0.2f, 0.3f, 0.4f)
        assertEquals(0.1f, splines.x1)
        assertEquals(0.2f, splines.y1)
        assertEquals(0.3f, splines.x2)
        assertEquals(0.4f, splines.y2)
    }

    @Test
    fun keySplinesEase() {
        val ease = KeySplines.EASE
        assertEquals(0.25f, ease.x1)
        assertEquals(0.1f, ease.y1)
        assertEquals(0.25f, ease.x2)
        assertEquals(1f, ease.y2)
    }

    @Test
    fun keySplinesEaseIn() {
        val easeIn = KeySplines.EASE_IN
        assertEquals(0.42f, easeIn.x1)
        assertEquals(0f, easeIn.y1)
        assertEquals(1f, easeIn.x2)
        assertEquals(1f, easeIn.y2)
    }

    @Test
    fun keySplinesEaseOut() {
        val easeOut = KeySplines.EASE_OUT
        assertEquals(0f, easeOut.x1)
        assertEquals(0f, easeOut.y1)
        assertEquals(0.58f, easeOut.x2)
        assertEquals(1f, easeOut.y2)
    }

    @Test
    fun keySplinesEaseInOut() {
        val easeInOut = KeySplines.EASE_IN_OUT
        assertEquals(0.42f, easeInOut.x1)
        assertEquals(0f, easeInOut.y1)
        assertEquals(0.58f, easeInOut.x2)
        assertEquals(1f, easeInOut.y2)
    }

    // ===========================================
    // TransformType Tests
    // ===========================================

    @Test
    fun transformTypeValues() {
        assertEquals(9, TransformType.entries.size)
        assertTrue(TransformType.entries.contains(TransformType.TRANSLATE))
        assertTrue(TransformType.entries.contains(TransformType.TRANSLATE_X))
        assertTrue(TransformType.entries.contains(TransformType.TRANSLATE_Y))
        assertTrue(TransformType.entries.contains(TransformType.SCALE))
        assertTrue(TransformType.entries.contains(TransformType.SCALE_X))
        assertTrue(TransformType.entries.contains(TransformType.SCALE_Y))
        assertTrue(TransformType.entries.contains(TransformType.ROTATE))
        assertTrue(TransformType.entries.contains(TransformType.SKEW_X))
        assertTrue(TransformType.entries.contains(TransformType.SKEW_Y))
    }

    // ===========================================
    // MotionRotate Tests
    // ===========================================

    @Test
    fun motionRotateValues() {
        assertEquals(3, MotionRotate.entries.size)
        assertTrue(MotionRotate.entries.contains(MotionRotate.NONE))
        assertTrue(MotionRotate.entries.contains(MotionRotate.AUTO))
        assertTrue(MotionRotate.entries.contains(MotionRotate.AUTO_REVERSE))
    }

    // ===========================================
    // SvgAnimate.isInfinite Tests
    // ===========================================

    @Test
    fun isInfiniteReturnsTrueForNegativeIterations() {
        val anim = SvgAnimate.Opacity(iterations = -1)
        assertTrue(anim.isInfinite)
    }

    @Test
    fun isInfiniteReturnsTrueForInfiniteConstant() {
        val anim = SvgAnimate.Opacity(iterations = SvgAnimate.INFINITE)
        assertTrue(anim.isInfinite)
    }

    @Test
    fun isInfiniteReturnsFalseForPositiveIterations() {
        val anim = SvgAnimate.Opacity(iterations = 3)
        assertFalse(anim.isInfinite)
    }

    @Test
    fun isInfiniteReturnsFalseForOneIteration() {
        val anim = SvgAnimate.Opacity(iterations = 1)
        assertFalse(anim.isInfinite)
    }

    // ===========================================
    // SvgAnimate.StrokeDraw Tests
    // ===========================================

    @Test
    fun strokeDrawDefaults() {
        val anim = SvgAnimate.StrokeDraw()
        assertEquals(DefaultAnimationDuration, anim.dur)
        assertEquals(Duration.ZERO, anim.delay)
        assertFalse(anim.reverse)
        assertEquals(CalcMode.LINEAR, anim.calcMode)
        assertNull(anim.keySplines)
        assertEquals(SvgAnimate.INFINITE, anim.iterations)
        assertEquals(AnimationDirection.NORMAL, anim.direction)
        assertEquals(AnimationFillMode.NONE, anim.fillMode)
    }

    @Test
    fun strokeDrawCustomValues() {
        val anim = SvgAnimate.StrokeDraw(
            dur = 1.seconds,
            delay = 500.milliseconds,
            reverse = true,
            calcMode = CalcMode.SPLINE,
            keySplines = KeySplines.EASE,
            iterations = 3,
            direction = AnimationDirection.ALTERNATE,
            fillMode = AnimationFillMode.FORWARDS
        )
        assertEquals(1.seconds, anim.dur)
        assertEquals(500.milliseconds, anim.delay)
        assertTrue(anim.reverse)
        assertEquals(CalcMode.SPLINE, anim.calcMode)
        assertEquals(KeySplines.EASE, anim.keySplines)
        assertEquals(3, anim.iterations)
        assertEquals(AnimationDirection.ALTERNATE, anim.direction)
        assertEquals(AnimationFillMode.FORWARDS, anim.fillMode)
    }

    // ===========================================
    // SvgAnimate.StrokeWidth Tests
    // ===========================================

    @Test
    fun strokeWidthAnimation() {
        val anim = SvgAnimate.StrokeWidth(from = 1f, to = 5f, dur = 300.milliseconds)
        assertEquals(1f, anim.from)
        assertEquals(5f, anim.to)
        assertEquals(300.milliseconds, anim.dur)
    }

    // ===========================================
    // SvgAnimate.StrokeOpacity Tests
    // ===========================================

    @Test
    fun strokeOpacityDefaults() {
        val anim = SvgAnimate.StrokeOpacity()
        assertEquals(0f, anim.from)
        assertEquals(1f, anim.to)
    }

    // ===========================================
    // SvgAnimate.StrokeDasharray Tests
    // ===========================================

    @Test
    fun strokeDasharrayAnimation() {
        val anim = SvgAnimate.StrokeDasharray(
            from = listOf(5f, 5f),
            to = listOf(10f, 2f)
        )
        assertEquals(listOf(5f, 5f), anim.from)
        assertEquals(listOf(10f, 2f), anim.to)
    }

    // ===========================================
    // SvgAnimate.StrokeDashoffset Tests
    // ===========================================

    @Test
    fun strokeDashoffsetAnimation() {
        val anim = SvgAnimate.StrokeDashoffset(from = 0f, to = 100f)
        assertEquals(0f, anim.from)
        assertEquals(100f, anim.to)
    }

    // ===========================================
    // SvgAnimate.FillOpacity Tests
    // ===========================================

    @Test
    fun fillOpacityDefaults() {
        val anim = SvgAnimate.FillOpacity()
        assertEquals(0f, anim.from)
        assertEquals(1f, anim.to)
    }

    // ===========================================
    // SvgAnimate.Opacity Tests
    // ===========================================

    @Test
    fun opacityDefaults() {
        val anim = SvgAnimate.Opacity()
        assertEquals(0f, anim.from)
        assertEquals(1f, anim.to)
    }

    @Test
    fun opacityCustomValues() {
        val anim = SvgAnimate.Opacity(from = 1f, to = 0f, dur = 250.milliseconds)
        assertEquals(1f, anim.from)
        assertEquals(0f, anim.to)
        assertEquals(250.milliseconds, anim.dur)
    }

    // ===========================================
    // Geometric Property Animation Tests
    // ===========================================

    @Test
    fun cxAnimation() {
        val anim = SvgAnimate.Cx(from = 10f, to = 20f)
        assertEquals(10f, anim.from)
        assertEquals(20f, anim.to)
    }

    @Test
    fun cyAnimation() {
        val anim = SvgAnimate.Cy(from = 15f, to = 25f)
        assertEquals(15f, anim.from)
        assertEquals(25f, anim.to)
    }

    @Test
    fun rAnimation() {
        val anim = SvgAnimate.R(from = 5f, to = 15f)
        assertEquals(5f, anim.from)
        assertEquals(15f, anim.to)
    }

    @Test
    fun rxAnimation() {
        val anim = SvgAnimate.Rx(from = 2f, to = 8f)
        assertEquals(2f, anim.from)
        assertEquals(8f, anim.to)
    }

    @Test
    fun ryAnimation() {
        val anim = SvgAnimate.Ry(from = 3f, to = 6f)
        assertEquals(3f, anim.from)
        assertEquals(6f, anim.to)
    }

    @Test
    fun xAnimation() {
        val anim = SvgAnimate.X(from = 0f, to = 100f)
        assertEquals(0f, anim.from)
        assertEquals(100f, anim.to)
    }

    @Test
    fun yAnimation() {
        val anim = SvgAnimate.Y(from = 0f, to = 50f)
        assertEquals(0f, anim.from)
        assertEquals(50f, anim.to)
    }

    @Test
    fun widthAnimation() {
        val anim = SvgAnimate.Width(from = 24f, to = 48f)
        assertEquals(24f, anim.from)
        assertEquals(48f, anim.to)
    }

    @Test
    fun heightAnimation() {
        val anim = SvgAnimate.Height(from = 24f, to = 36f)
        assertEquals(24f, anim.from)
        assertEquals(36f, anim.to)
    }

    // ===========================================
    // Line Animation Tests
    // ===========================================

    @Test
    fun x1Animation() {
        val anim = SvgAnimate.X1(from = 0f, to = 10f)
        assertEquals(0f, anim.from)
        assertEquals(10f, anim.to)
    }

    @Test
    fun y1Animation() {
        val anim = SvgAnimate.Y1(from = 0f, to = 10f)
        assertEquals(0f, anim.from)
        assertEquals(10f, anim.to)
    }

    @Test
    fun x2Animation() {
        val anim = SvgAnimate.X2(from = 24f, to = 48f)
        assertEquals(24f, anim.from)
        assertEquals(48f, anim.to)
    }

    @Test
    fun y2Animation() {
        val anim = SvgAnimate.Y2(from = 24f, to = 48f)
        assertEquals(24f, anim.from)
        assertEquals(48f, anim.to)
    }

    // ===========================================
    // SvgAnimate.D (Path Morphing) Tests
    // ===========================================

    @Test
    fun dAnimationPathMorphing() {
        val anim = SvgAnimate.D(
            from = "M10 10 L20 20",
            to = "M5 15 L25 15"
        )
        assertEquals("M10 10 L20 20", anim.from)
        assertEquals("M5 15 L25 15", anim.to)
    }

    // ===========================================
    // SvgAnimate.Points Tests
    // ===========================================

    @Test
    fun pointsAnimation() {
        val fromPoints = listOf(Offset(0f, 0f), Offset(10f, 10f))
        val toPoints = listOf(Offset(5f, 5f), Offset(15f, 15f))
        val anim = SvgAnimate.Points(from = fromPoints, to = toPoints)
        assertEquals(fromPoints, anim.from)
        assertEquals(toPoints, anim.to)
    }

    // ===========================================
    // SvgAnimate.Transform Tests
    // ===========================================

    @Test
    fun transformAnimation() {
        val anim = SvgAnimate.Transform(
            type = TransformType.ROTATE,
            from = 0f,
            to = 360f,
            dur = 1.seconds
        )
        assertEquals(TransformType.ROTATE, anim.type)
        assertEquals(0f, anim.from)
        assertEquals(360f, anim.to)
        assertEquals(1.seconds, anim.dur)
    }

    @Test
    fun scaleTransformAnimation() {
        val anim = SvgAnimate.Transform(
            type = TransformType.SCALE,
            from = 1f,
            to = 2f
        )
        assertEquals(TransformType.SCALE, anim.type)
        assertEquals(1f, anim.from)
        assertEquals(2f, anim.to)
    }

    @Test
    fun translateTransformAnimation() {
        val anim = SvgAnimate.Transform(
            type = TransformType.TRANSLATE,
            from = 0f,
            to = 100f
        )
        assertEquals(TransformType.TRANSLATE, anim.type)
    }

    // ===========================================
    // SvgAnimate.Motion Tests
    // ===========================================

    @Test
    fun motionAnimationDefaults() {
        val anim = SvgAnimate.Motion(path = "M0 0 L100 100")
        assertEquals("M0 0 L100 100", anim.path)
        assertEquals(DefaultAnimationDuration, anim.dur)
        assertEquals(Duration.ZERO, anim.delay)
        assertEquals(MotionRotate.NONE, anim.rotate)
        assertEquals(CalcMode.LINEAR, anim.calcMode)
    }

    @Test
    fun motionAnimationWithAutoRotate() {
        val anim = SvgAnimate.Motion(
            path = "M0 0 C50 0 50 100 100 100",
            rotate = MotionRotate.AUTO,
            dur = 2.seconds
        )
        assertEquals(MotionRotate.AUTO, anim.rotate)
        assertEquals(2.seconds, anim.dur)
    }

    @Test
    fun motionAnimationWithAutoReverseRotate() {
        val anim = SvgAnimate.Motion(
            path = "M0 0 L100 100",
            rotate = MotionRotate.AUTO_REVERSE
        )
        assertEquals(MotionRotate.AUTO_REVERSE, anim.rotate)
    }

    // ===========================================
    // SvgAnimate.INFINITE Constant Tests
    // ===========================================

    @Test
    fun infiniteConstantIsNegative() {
        assertTrue(SvgAnimate.INFINITE < 0)
        assertEquals(-1, SvgAnimate.INFINITE)
    }
}
