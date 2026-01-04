package io.github.fuyuz.svgicon

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import io.github.fuyuz.svgicon.core.*

// ============================================
// External Animation State Control API
// ============================================

/**
 * Read-only animation state for SvgIcon.
 * Use [SvgIconAnimatable] for mutable state.
 */
@Stable
interface SvgIconAnimationState {
    /** Current animation progress (0.0 to 1.0) */
    val progress: Float

    /** Whether the animation is currently playing */
    val isPlaying: Boolean

    /** Whether the animation is at the end (progress >= 1.0) */
    val isAtEnd: Boolean get() = progress >= 1f

    /** Whether the animation is at the start (progress <= 0.0) */
    val isAtStart: Boolean get() = progress <= 0f
}

/**
 * Mutable animation state for SvgIcon with control methods.
 */
@Stable
interface SvgIconAnimatable : SvgIconAnimationState {
    suspend fun snapTo(progress: Float)
    suspend fun animateTo(progress: Float, durationMillis: Int? = null)
    fun stop()
}

internal class SvgIconAnimatableImpl(
    initialProgress: Float = 0f
) : SvgIconAnimatable {
    private val animatable = Animatable(initialProgress)
    private var _isPlaying by mutableStateOf(false)
    private val mutatorMutex = MutatorMutex()

    override val progress: Float get() = animatable.value
    override val isPlaying: Boolean get() = _isPlaying

    override suspend fun snapTo(progress: Float) {
        mutatorMutex.mutate {
            _isPlaying = false
            animatable.snapTo(progress)
        }
    }

    override suspend fun animateTo(progress: Float, durationMillis: Int?) {
        mutatorMutex.mutate {
            _isPlaying = true
            try {
                val duration = durationMillis ?: calculateDefaultDuration(animatable.value, progress)
                animatable.animateTo(
                    targetValue = progress,
                    animationSpec = tween(duration, easing = LinearEasing)
                )
            } finally {
                _isPlaying = false
            }
        }
    }

    override fun stop() {
        _isPlaying = false
    }

    private fun calculateDefaultDuration(from: Float, to: Float): Int {
        val distance = kotlin.math.abs(to - from)
        return (distance * 1000).toInt()
    }
}

@Composable
fun rememberSvgIconAnimationState(): SvgIconAnimatable {
    return remember { SvgIconAnimatableImpl() }
}

@Composable
fun SvgIcon(
    svg: Svg,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    strokeWidth: Float? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    Canvas(modifier.then(semantics).fillMaxSize()) {
        drawSvg(svg, tint, strokeWidth, textMeasurer)
    }
}

@Composable
fun AnimatedSvgIcon(
    svg: Svg,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    strokeWidth: Float? = null,
    animate: Boolean = true,
    iterations: Int = Int.MAX_VALUE,
    onAnimationEnd: (() -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val animations = remember(svg) { collectAllAnimations(svg.children) }
    val pathCache = remember(svg) { buildPathCache(svg.children) }

    if (animations.isEmpty()) {
        SvgIcon(svg, contentDescription, modifier, tint, strokeWidth)
        return
    }

    val totalAnimationMs = svg.duration
    var timeInAnimation by remember { mutableFloatStateOf(0f) }
    var iteration by remember { mutableIntStateOf(0) }

    if (animate && (iterations == Int.MAX_VALUE || iteration < iterations)) {
        LaunchedEffect(svg, animate) {
            val startTime = withFrameMillis { it }
            while (iterations == Int.MAX_VALUE || iteration < iterations) {
                withFrameMillis { frameTime ->
                    val elapsed = (frameTime - startTime).toFloat()
                    timeInAnimation = elapsed % totalAnimationMs
                    val newIteration = (elapsed / totalAnimationMs).toInt()
                    if (newIteration > iteration) {
                        iteration = newIteration
                        if (iterations != Int.MAX_VALUE && iteration >= iterations) {
                            onAnimationEnd?.invoke()
                        }
                    }
                }
            }
        }
    }

    val progressMap = animations.associate { entry ->
        val progress = remember(timeInAnimation, iteration) {
            val fillProgress = getFillProgress(
                timeInAnimation,
                totalAnimationMs,
                entry.animation.fillMode,
                entry.animation.direction,
                iteration
            ) ?: 0f
            mutableStateOf(fillProgress)
        }
        AnimationKey(entry.element, entry.animation) to progress
    }

    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    Canvas(modifier.then(semantics).fillMaxSize()) {
        drawAnimatedSvg(svg, tint, strokeWidth, progressMap, pathCache, textMeasurer)
    }
}
