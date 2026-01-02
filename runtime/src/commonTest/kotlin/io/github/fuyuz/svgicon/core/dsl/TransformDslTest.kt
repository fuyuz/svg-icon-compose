package io.github.fuyuz.svgicon.core.dsl

import io.github.fuyuz.svgicon.core.SvgTransform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class TransformDslTest {

    // ===========================================
    // Transform Builder Functions
    // ===========================================

    @Test
    fun translateWithXAndY() {
        val t = translate(10, 20)
        assertIs<SvgTransform.Translate>(t)
        assertEquals(10f, t.x)
        assertEquals(20f, t.y)
    }

    @Test
    fun translateWithXOnly() {
        val t = translate(15)
        assertIs<SvgTransform.Translate>(t)
        assertEquals(15f, t.x)
        assertEquals(0f, t.y)
    }

    @Test
    fun translateWithFloats() {
        val t = translate(5.5, 10.5)
        assertIs<SvgTransform.Translate>(t)
        assertEquals(5.5f, t.x)
        assertEquals(10.5f, t.y)
    }

    @Test
    fun translateXFunction() {
        val t = translateX(25)
        assertIs<SvgTransform.Translate>(t)
        assertEquals(25f, t.x)
        assertEquals(0f, t.y)
    }

    @Test
    fun translateYFunction() {
        val t = translateY(30)
        assertIs<SvgTransform.Translate>(t)
        assertEquals(0f, t.x)
        assertEquals(30f, t.y)
    }

    @Test
    fun scaleUniform() {
        val t = scale(2)
        assertIs<SvgTransform.Scale>(t)
        assertEquals(2f, t.sx)
        assertEquals(2f, t.sy)
    }

    @Test
    fun scaleNonUniform() {
        val t = scale(2, 3)
        assertIs<SvgTransform.Scale>(t)
        assertEquals(2f, t.sx)
        assertEquals(3f, t.sy)
    }

    @Test
    fun scaleXFunction() {
        val t = scaleX(1.5)
        assertIs<SvgTransform.Scale>(t)
        assertEquals(1.5f, t.sx)
        assertEquals(1f, t.sy)
    }

    @Test
    fun scaleYFunction() {
        val t = scaleY(0.5)
        assertIs<SvgTransform.Scale>(t)
        assertEquals(1f, t.sx)
        assertEquals(0.5f, t.sy)
    }

    @Test
    fun rotateAngleOnly() {
        val t = rotate(45)
        assertIs<SvgTransform.Rotate>(t)
        assertEquals(45f, t.angle)
        assertEquals(0f, t.cx)
        assertEquals(0f, t.cy)
    }

    @Test
    fun rotateWithPivotPoint() {
        val t = rotate(90, 12, 12)
        assertIs<SvgTransform.Rotate>(t)
        assertEquals(90f, t.angle)
        assertEquals(12f, t.cx)
        assertEquals(12f, t.cy)
    }

    @Test
    fun rotateWithPivotPair() {
        val t = rotate(180, 24 to 24)
        assertIs<SvgTransform.Rotate>(t)
        assertEquals(180f, t.angle)
        assertEquals(24f, t.cx)
        assertEquals(24f, t.cy)
    }

    @Test
    fun skewXFunction() {
        val t = skewX(15)
        assertIs<SvgTransform.SkewX>(t)
        assertEquals(15f, t.angle)
    }

    @Test
    fun skewYFunction() {
        val t = skewY(20)
        assertIs<SvgTransform.SkewY>(t)
        assertEquals(20f, t.angle)
    }

    @Test
    fun matrixFunction() {
        val t = matrix(1, 0, 0, 1, 10, 20)
        assertIs<SvgTransform.Matrix>(t)
        assertEquals(1f, t.a)
        assertEquals(0f, t.b)
        assertEquals(0f, t.c)
        assertEquals(1f, t.d)
        assertEquals(10f, t.e)
        assertEquals(20f, t.f)
    }

    // ===========================================
    // Transform Plus Operator Tests
    // ===========================================

    @Test
    fun plusCombinesTwoSimpleTransforms() {
        val t = translate(10, 0) + rotate(45)
        assertIs<SvgTransform.Combined>(t)
        assertEquals(2, t.transforms.size)
        assertIs<SvgTransform.Translate>(t.transforms[0])
        assertIs<SvgTransform.Rotate>(t.transforms[1])
    }

    @Test
    fun plusCombinesMultipleTransforms() {
        val t = translate(10, 0) + rotate(45) + scale(2)
        assertIs<SvgTransform.Combined>(t)
        assertEquals(3, t.transforms.size)
    }

    @Test
    fun plusCombinesCombinedWithSimple() {
        val combined = translate(10, 0) + rotate(45)
        val t = combined + scale(2)
        assertIs<SvgTransform.Combined>(t)
        assertEquals(3, t.transforms.size)
    }

    @Test
    fun plusCombinesSimpleWithCombined() {
        val combined = rotate(45) + scale(2)
        val t = translate(10, 0) + combined
        assertIs<SvgTransform.Combined>(t)
        assertEquals(3, t.transforms.size)
        assertIs<SvgTransform.Translate>(t.transforms[0])
    }

    @Test
    fun plusCombinesTwoCombinedTransforms() {
        val combined1 = translate(10, 0) + rotate(45)
        val combined2 = scale(2) + skewX(15)
        val t = combined1 + combined2
        assertIs<SvgTransform.Combined>(t)
        assertEquals(4, t.transforms.size)
    }

    // ===========================================
    // TransformBuilder Tests
    // ===========================================

    @Test
    fun transformBuilderEmpty() {
        val t = transform { }
        assertNull(t)
    }

    @Test
    fun transformBuilderSingleTransform() {
        val t = transform {
            rotate(45)
        }
        assertIs<SvgTransform.Rotate>(t)
        assertEquals(45f, t.angle)
    }

    @Test
    fun transformBuilderMultipleTransforms() {
        val t = transform {
            translate(10, 20)
            rotate(45)
            scale(2)
        }
        assertIs<SvgTransform.Combined>(t)
        assertEquals(3, t.transforms.size)
    }

    @Test
    fun transformBuilderTranslate() {
        val t = transform {
            translate(5, 10)
        }
        assertIs<SvgTransform.Translate>(t)
        assertEquals(5f, t.x)
        assertEquals(10f, t.y)
    }

    @Test
    fun transformBuilderScaleUniform() {
        val t = transform {
            scale(1.5)
        }
        assertIs<SvgTransform.Scale>(t)
        assertEquals(1.5f, t.sx)
        assertEquals(1.5f, t.sy)
    }

    @Test
    fun transformBuilderScaleNonUniform() {
        val t = transform {
            scale(2, 3)
        }
        assertIs<SvgTransform.Scale>(t)
        assertEquals(2f, t.sx)
        assertEquals(3f, t.sy)
    }

    @Test
    fun transformBuilderRotateWithPivot() {
        val t = transform {
            rotate(90, 12, 12)
        }
        assertIs<SvgTransform.Rotate>(t)
        assertEquals(90f, t.angle)
        assertEquals(12f, t.cx)
        assertEquals(12f, t.cy)
    }

    @Test
    fun transformBuilderRotateWithPivotPair() {
        val t = transform {
            rotate(180, 24 to 24)
        }
        assertIs<SvgTransform.Rotate>(t)
        assertEquals(180f, t.angle)
        assertEquals(24f, t.cx)
        assertEquals(24f, t.cy)
    }

    @Test
    fun transformBuilderSkewX() {
        val t = transform {
            skewX(30)
        }
        assertIs<SvgTransform.SkewX>(t)
        assertEquals(30f, t.angle)
    }

    @Test
    fun transformBuilderSkewY() {
        val t = transform {
            skewY(45)
        }
        assertIs<SvgTransform.SkewY>(t)
        assertEquals(45f, t.angle)
    }

    @Test
    fun transformBuilderComplexTransform() {
        val t = transform {
            translate(10, 10)
            rotate(45, 12 to 12)
            scale(1.5)
            skewX(10)
            skewY(5)
        }
        assertIs<SvgTransform.Combined>(t)
        assertEquals(5, t.transforms.size)
        assertIs<SvgTransform.Translate>(t.transforms[0])
        assertIs<SvgTransform.Rotate>(t.transforms[1])
        assertIs<SvgTransform.Scale>(t.transforms[2])
        assertIs<SvgTransform.SkewX>(t.transforms[3])
        assertIs<SvgTransform.SkewY>(t.transforms[4])
    }
}
