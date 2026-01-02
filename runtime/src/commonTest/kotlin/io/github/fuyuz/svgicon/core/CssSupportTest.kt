package io.github.fuyuz.svgicon.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class CssSupportTest {

    // ===========================================
    // CssSelector.specificity Tests
    // ===========================================

    @Test
    fun universalSelectorHasLowestSpecificity() {
        assertEquals(0, CssSelector.Universal.specificity)
    }

    @Test
    fun tagSelectorHasSpecificity1() {
        assertEquals(1, CssSelector.Tag("circle").specificity)
    }

    @Test
    fun classSelectorHasSpecificity2() {
        assertEquals(2, CssSelector.Class("my-class").specificity)
    }

    @Test
    fun idSelectorHasHighestSpecificity() {
        assertEquals(3, CssSelector.Id("my-id").specificity)
    }

    @Test
    fun specificityOrderIsCorrect() {
        val universal = CssSelector.Universal.specificity
        val tag = CssSelector.Tag("path").specificity
        val classSelector = CssSelector.Class("styled").specificity
        val id = CssSelector.Id("header").specificity

        kotlin.test.assertTrue(universal < tag)
        kotlin.test.assertTrue(tag < classSelector)
        kotlin.test.assertTrue(classSelector < id)
    }

    // ===========================================
    // CssTimingFunction.parse Tests
    // ===========================================

    @Test
    fun parseLinear() {
        val result = CssTimingFunction.parse("linear")
        assertIs<CssTimingFunction.Linear>(result)
    }

    @Test
    fun parseEase() {
        val result = CssTimingFunction.parse("ease")
        assertIs<CssTimingFunction.Ease>(result)
    }

    @Test
    fun parseEaseIn() {
        val result = CssTimingFunction.parse("ease-in")
        assertIs<CssTimingFunction.EaseIn>(result)
    }

    @Test
    fun parseEaseOut() {
        val result = CssTimingFunction.parse("ease-out")
        assertIs<CssTimingFunction.EaseOut>(result)
    }

    @Test
    fun parseEaseInOut() {
        val result = CssTimingFunction.parse("ease-in-out")
        assertIs<CssTimingFunction.EaseInOut>(result)
    }

    @Test
    fun parseCubicBezier() {
        val result = CssTimingFunction.parse("cubic-bezier(0.1, 0.2, 0.3, 0.4)")
        assertIs<CssTimingFunction.CubicBezier>(result)
        assertEquals(0.1f, result.x1)
        assertEquals(0.2f, result.y1)
        assertEquals(0.3f, result.x2)
        assertEquals(0.4f, result.y2)
    }

    @Test
    fun parseCubicBezierWithSpaces() {
        val result = CssTimingFunction.parse("cubic-bezier( 0.25 , 0.1 , 0.25 , 1.0 )")
        assertIs<CssTimingFunction.CubicBezier>(result)
        assertEquals(0.25f, result.x1)
        assertEquals(0.1f, result.y1)
        assertEquals(0.25f, result.x2)
        assertEquals(1.0f, result.y2)
    }

    @Test
    fun parseCubicBezierInvalidFallsBackToEase() {
        val result = CssTimingFunction.parse("cubic-bezier(invalid)")
        assertIs<CssTimingFunction.Ease>(result)
    }

    @Test
    fun parseStepsDefault() {
        val result = CssTimingFunction.parse("steps(4)")
        assertIs<CssTimingFunction.Steps>(result)
        assertEquals(4, result.count)
        assertEquals(StepPosition.END, result.position)
    }

    @Test
    fun parseStepsWithStart() {
        val result = CssTimingFunction.parse("steps(3, start)")
        assertIs<CssTimingFunction.Steps>(result)
        assertEquals(3, result.count)
        assertEquals(StepPosition.START, result.position)
    }

    @Test
    fun parseStepsWithJumpStart() {
        val result = CssTimingFunction.parse("steps(5, jump-start)")
        assertIs<CssTimingFunction.Steps>(result)
        assertEquals(5, result.count)
        assertEquals(StepPosition.START, result.position)
    }

    @Test
    fun parseStepsWithEnd() {
        val result = CssTimingFunction.parse("steps(2, end)")
        assertIs<CssTimingFunction.Steps>(result)
        assertEquals(2, result.count)
        assertEquals(StepPosition.END, result.position)
    }

    @Test
    fun parseStepsWithBoth() {
        val result = CssTimingFunction.parse("steps(4, both)")
        assertIs<CssTimingFunction.Steps>(result)
        assertEquals(4, result.count)
        assertEquals(StepPosition.BOTH, result.position)
    }

    @Test
    fun parseStepsWithJumpBoth() {
        val result = CssTimingFunction.parse("steps(6, jump-both)")
        assertIs<CssTimingFunction.Steps>(result)
        assertEquals(6, result.count)
        assertEquals(StepPosition.BOTH, result.position)
    }

    @Test
    fun parseStepsWithNone() {
        val result = CssTimingFunction.parse("steps(8, none)")
        assertIs<CssTimingFunction.Steps>(result)
        assertEquals(8, result.count)
        assertEquals(StepPosition.NONE, result.position)
    }

    @Test
    fun parseStepsWithJumpNone() {
        val result = CssTimingFunction.parse("steps(10, jump-none)")
        assertIs<CssTimingFunction.Steps>(result)
        assertEquals(10, result.count)
        assertEquals(StepPosition.NONE, result.position)
    }

    @Test
    fun parseCaseInsensitive() {
        assertIs<CssTimingFunction.Linear>(CssTimingFunction.parse("LINEAR"))
        assertIs<CssTimingFunction.Ease>(CssTimingFunction.parse("EASE"))
        assertIs<CssTimingFunction.EaseIn>(CssTimingFunction.parse("Ease-In"))
    }

    @Test
    fun parseWithWhitespace() {
        assertIs<CssTimingFunction.Linear>(CssTimingFunction.parse("  linear  "))
        assertIs<CssTimingFunction.Ease>(CssTimingFunction.parse("\tease\n"))
    }

    @Test
    fun parseUnknownDefaultsToEase() {
        val result = CssTimingFunction.parse("unknown-function")
        assertIs<CssTimingFunction.Ease>(result)
    }

    // ===========================================
    // CssTimingFunction.toKeySplines Tests
    // ===========================================

    @Test
    fun linearToKeySplinesReturnsNull() {
        assertNull(CssTimingFunction.Linear.toKeySplines())
    }

    @Test
    fun easeToKeySplinesReturnsCorrectValues() {
        val splines = CssTimingFunction.Ease.toKeySplines()
        assertEquals(KeySplines.EASE, splines)
    }

    @Test
    fun easeInToKeySplinesReturnsCorrectValues() {
        val splines = CssTimingFunction.EaseIn.toKeySplines()
        assertEquals(KeySplines.EASE_IN, splines)
    }

    @Test
    fun easeOutToKeySplinesReturnsCorrectValues() {
        val splines = CssTimingFunction.EaseOut.toKeySplines()
        assertEquals(KeySplines.EASE_OUT, splines)
    }

    @Test
    fun easeInOutToKeySplinesReturnsCorrectValues() {
        val splines = CssTimingFunction.EaseInOut.toKeySplines()
        assertEquals(KeySplines.EASE_IN_OUT, splines)
    }

    @Test
    fun cubicBezierToKeySplinesReturnsCustomValues() {
        val timing = CssTimingFunction.CubicBezier(0.1f, 0.2f, 0.3f, 0.4f)
        val splines = timing.toKeySplines()
        assertEquals(0.1f, splines?.x1)
        assertEquals(0.2f, splines?.y1)
        assertEquals(0.3f, splines?.x2)
        assertEquals(0.4f, splines?.y2)
    }

    @Test
    fun stepsToKeySplinesReturnsNull() {
        val timing = CssTimingFunction.Steps(4)
        assertNull(timing.toKeySplines())
    }

    // ===========================================
    // CssTimingFunction.toCalcMode Tests
    // ===========================================

    @Test
    fun linearToCalcModeReturnsLinear() {
        assertEquals(CalcMode.LINEAR, CssTimingFunction.Linear.toCalcMode())
    }

    @Test
    fun stepsToCalcModeReturnsDiscrete() {
        assertEquals(CalcMode.DISCRETE, CssTimingFunction.Steps(4).toCalcMode())
    }

    @Test
    fun easeToCalcModeReturnsSpline() {
        assertEquals(CalcMode.SPLINE, CssTimingFunction.Ease.toCalcMode())
    }

    @Test
    fun easeInToCalcModeReturnsSpline() {
        assertEquals(CalcMode.SPLINE, CssTimingFunction.EaseIn.toCalcMode())
    }

    @Test
    fun easeOutToCalcModeReturnsSpline() {
        assertEquals(CalcMode.SPLINE, CssTimingFunction.EaseOut.toCalcMode())
    }

    @Test
    fun easeInOutToCalcModeReturnsSpline() {
        assertEquals(CalcMode.SPLINE, CssTimingFunction.EaseInOut.toCalcMode())
    }

    @Test
    fun cubicBezierToCalcModeReturnsSpline() {
        assertEquals(CalcMode.SPLINE, CssTimingFunction.CubicBezier(0.1f, 0.2f, 0.3f, 0.4f).toCalcMode())
    }

    // ===========================================
    // CssStylesheet Tests
    // ===========================================

    @Test
    fun cssStylesheetDefaultValues() {
        val stylesheet = CssStylesheet()
        assertEquals(emptyList(), stylesheet.rules)
        assertEquals(emptyList(), stylesheet.keyframes)
    }

    @Test
    fun cssStylesheetWithRules() {
        val rule = CssRule(
            selector = CssSelector.Class("test"),
            declarations = mapOf("fill" to "red")
        )
        val stylesheet = CssStylesheet(rules = listOf(rule))
        assertEquals(1, stylesheet.rules.size)
        assertEquals(rule, stylesheet.rules[0])
    }

    @Test
    fun cssStylesheetWithKeyframes() {
        val keyframe = CssKeyframe(0f, mapOf("opacity" to "0"))
        val keyframes = CssKeyframes("fadeIn", listOf(keyframe))
        val stylesheet = CssStylesheet(keyframes = listOf(keyframes))
        assertEquals(1, stylesheet.keyframes.size)
        assertEquals(keyframes, stylesheet.keyframes[0])
    }

    // ===========================================
    // CssKeyframe Tests
    // ===========================================

    @Test
    fun cssKeyframeStoresOffsetAndProperties() {
        val keyframe = CssKeyframe(
            offset = 0.5f,
            properties = mapOf("opacity" to "0.5", "transform" to "scale(1.5)")
        )
        assertEquals(0.5f, keyframe.offset)
        assertEquals(2, keyframe.properties.size)
        assertEquals("0.5", keyframe.properties["opacity"])
        assertEquals("scale(1.5)", keyframe.properties["transform"])
    }

    // ===========================================
    // CssKeyframes Tests
    // ===========================================

    @Test
    fun cssKeyframesStoresNameAndKeyframes() {
        val keyframes = CssKeyframes(
            name = "spin",
            keyframes = listOf(
                CssKeyframe(0f, mapOf("transform" to "rotate(0deg)")),
                CssKeyframe(1f, mapOf("transform" to "rotate(360deg)"))
            )
        )
        assertEquals("spin", keyframes.name)
        assertEquals(2, keyframes.keyframes.size)
    }

    // ===========================================
    // CssAnimation Tests
    // ===========================================

    @Test
    fun cssAnimationDefaultValues() {
        val animation = CssAnimation(name = "test")
        assertEquals("test", animation.name)
        assertEquals(kotlin.time.Duration.ZERO, animation.duration)
        assertIs<CssTimingFunction.Ease>(animation.timingFunction)
        assertEquals(kotlin.time.Duration.ZERO, animation.delay)
        assertEquals(1, animation.iterationCount)
        assertEquals(AnimationDirection.NORMAL, animation.direction)
        assertEquals(AnimationFillMode.NONE, animation.fillMode)
    }

    // ===========================================
    // AnimationDirection Tests
    // ===========================================

    @Test
    fun animationDirectionValues() {
        assertEquals(4, AnimationDirection.entries.size)
        kotlin.test.assertTrue(AnimationDirection.entries.contains(AnimationDirection.NORMAL))
        kotlin.test.assertTrue(AnimationDirection.entries.contains(AnimationDirection.REVERSE))
        kotlin.test.assertTrue(AnimationDirection.entries.contains(AnimationDirection.ALTERNATE))
        kotlin.test.assertTrue(AnimationDirection.entries.contains(AnimationDirection.ALTERNATE_REVERSE))
    }

    // ===========================================
    // AnimationFillMode Tests
    // ===========================================

    @Test
    fun animationFillModeValues() {
        assertEquals(4, AnimationFillMode.entries.size)
        kotlin.test.assertTrue(AnimationFillMode.entries.contains(AnimationFillMode.NONE))
        kotlin.test.assertTrue(AnimationFillMode.entries.contains(AnimationFillMode.FORWARDS))
        kotlin.test.assertTrue(AnimationFillMode.entries.contains(AnimationFillMode.BACKWARDS))
        kotlin.test.assertTrue(AnimationFillMode.entries.contains(AnimationFillMode.BOTH))
    }

    // ===========================================
    // StepPosition Tests
    // ===========================================

    @Test
    fun stepPositionValues() {
        assertEquals(4, StepPosition.entries.size)
        kotlin.test.assertTrue(StepPosition.entries.contains(StepPosition.START))
        kotlin.test.assertTrue(StepPosition.entries.contains(StepPosition.END))
        kotlin.test.assertTrue(StepPosition.entries.contains(StepPosition.BOTH))
        kotlin.test.assertTrue(StepPosition.entries.contains(StepPosition.NONE))
    }
}
