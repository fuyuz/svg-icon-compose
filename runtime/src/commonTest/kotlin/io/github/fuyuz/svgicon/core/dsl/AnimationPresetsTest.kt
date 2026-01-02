package io.github.fuyuz.svgicon.core.dsl

import io.github.fuyuz.svgicon.core.AnimationDirection
import io.github.fuyuz.svgicon.core.SvgAnimate
import io.github.fuyuz.svgicon.core.TransformType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AnimationPresetsTest {

    // ===========================================
    // Opacity Animations
    // ===========================================

    @Test
    fun fadeInPreset() {
        val animations = Animations.fadeIn
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(0f, anim.from)
        assertEquals(1f, anim.to)
        assertEquals(300.milliseconds, anim.dur)
    }

    @Test
    fun fadeOutPreset() {
        val animations = Animations.fadeOut
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(1f, anim.from)
        assertEquals(0f, anim.to)
        assertEquals(300.milliseconds, anim.dur)
    }

    @Test
    fun fadeInWithCustomDuration() {
        val animations = Animations.fadeIn(500.milliseconds)
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(0f, anim.from)
        assertEquals(1f, anim.to)
        assertEquals(500.milliseconds, anim.dur)
    }

    @Test
    fun fadeOutWithCustomDuration() {
        val animations = Animations.fadeOut(1.seconds)
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Opacity>(anim)
        assertEquals(1f, anim.from)
        assertEquals(0f, anim.to)
        assertEquals(1.seconds, anim.dur)
    }

    // ===========================================
    // Stroke Draw Animations
    // ===========================================

    @Test
    fun strokeDrawDefault() {
        val animations = Animations.strokeDraw()
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.StrokeDraw>(anim)
        assertEquals(1.seconds, anim.dur)
        assertEquals(Duration.ZERO, anim.delay)
    }

    @Test
    fun strokeDrawWithCustomDuration() {
        val animations = Animations.strokeDraw(dur = 2.seconds)
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.StrokeDraw>(anim)
        assertEquals(2.seconds, anim.dur)
    }

    @Test
    fun strokeDrawWithDelay() {
        val animations = Animations.strokeDraw(dur = 1.seconds, delay = 500.milliseconds)
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.StrokeDraw>(anim)
        assertEquals(1.seconds, anim.dur)
        assertEquals(500.milliseconds, anim.delay)
    }

    // ===========================================
    // Transform Animations
    // ===========================================

    @Test
    fun spinPreset() {
        val animations = Animations.spin
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.ROTATE, anim.type)
        assertEquals(0f, anim.from)
        assertEquals(360f, anim.to)
        assertEquals(1.seconds, anim.dur)
    }

    @Test
    fun spinWithCustomDuration() {
        val animations = Animations.spin(2.seconds)
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.ROTATE, anim.type)
        assertEquals(2.seconds, anim.dur)
    }

    @Test
    fun pulsePreset() {
        val animations = Animations.pulse
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.SCALE, anim.type)
        assertEquals(1f, anim.from)
        assertEquals(1.1f, anim.to)
        assertEquals(500.milliseconds, anim.dur)
        assertEquals(AnimationDirection.ALTERNATE, anim.direction)
    }

    @Test
    fun pulseWithCustomScale() {
        val animations = Animations.pulse(scale = 1.5f)
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(1.5f, anim.to)
    }

    @Test
    fun pulseWithCustomDuration() {
        val animations = Animations.pulse(dur = 1.seconds)
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(1.seconds, anim.dur)
    }

    @Test
    fun shakePreset() {
        val animations = Animations.shake
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.ROTATE, anim.type)
        assertEquals(-5f, anim.from)
        assertEquals(5f, anim.to)
        assertEquals(100.milliseconds, anim.dur)
        assertEquals(AnimationDirection.ALTERNATE, anim.direction)
    }

    @Test
    fun bouncePreset() {
        val animations = Animations.bounce
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.TRANSLATE_Y, anim.type)
        assertEquals(0f, anim.from)
        assertEquals(-4f, anim.to)
        assertEquals(300.milliseconds, anim.dur)
        assertEquals(AnimationDirection.ALTERNATE, anim.direction)
    }

    // ===========================================
    // Scale Animations
    // ===========================================

    @Test
    fun scaleInPreset() {
        val animations = Animations.scaleIn
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.SCALE, anim.type)
        assertEquals(0f, anim.from)
        assertEquals(1f, anim.to)
        assertEquals(300.milliseconds, anim.dur)
    }

    @Test
    fun scaleOutPreset() {
        val animations = Animations.scaleOut
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.SCALE, anim.type)
        assertEquals(1f, anim.from)
        assertEquals(0f, anim.to)
        assertEquals(300.milliseconds, anim.dur)
    }

    @Test
    fun scaleInWithCustomDuration() {
        val animations = Animations.scaleIn(500.milliseconds)
        assertEquals(1, animations.size)
        val anim = animations[0]
        assertIs<SvgAnimate.Transform>(anim)
        assertEquals(TransformType.SCALE, anim.type)
        assertEquals(0f, anim.from)
        assertEquals(1f, anim.to)
        assertEquals(500.milliseconds, anim.dur)
    }
}
