package io.github.fuyuz.svgicon.core

import kotlin.time.Duration

/**
 * CSS selector for matching elements.
 * Supports basic selectors: .class, #id, tag, *
 */
sealed interface CssSelector {
    /** Class selector: .my-class */
    data class Class(val className: String) : CssSelector

    /** ID selector: #my-id */
    data class Id(val id: String) : CssSelector

    /** Tag selector: path, circle, etc. */
    data class Tag(val tagName: String) : CssSelector

    /** Universal selector: * */
    data object Universal : CssSelector

    /**
     * CSS specificity for this selector.
     * Higher values take precedence.
     * Order: Universal(0) < Tag(1) < Class(2) < Id(3)
     */
    val specificity: Int
        get() = when (this) {
            is Universal -> 0
            is Tag -> 1
            is Class -> 2
            is Id -> 3
        }
}

/**
 * A single CSS rule consisting of a selector and style declarations.
 */
data class CssRule(
    val selector: CssSelector,
    val declarations: Map<String, String>
)

/**
 * Parsed CSS stylesheet containing rules and keyframes.
 */
data class CssStylesheet(
    val rules: List<CssRule> = emptyList(),
    val keyframes: List<CssKeyframes> = emptyList()
)

/**
 * Step position for steps() timing function.
 */
enum class StepPosition {
    START, END, BOTH, NONE
}

/**
 * CSS timing function for animation easing.
 * Maps to CSS animation-timing-function values.
 */
sealed interface CssTimingFunction {
    /** Linear interpolation */
    data object Linear : CssTimingFunction

    /** CSS ease: cubic-bezier(0.25, 0.1, 0.25, 1) */
    data object Ease : CssTimingFunction

    /** CSS ease-in: cubic-bezier(0.42, 0, 1, 1) */
    data object EaseIn : CssTimingFunction

    /** CSS ease-out: cubic-bezier(0, 0, 0.58, 1) */
    data object EaseOut : CssTimingFunction

    /** CSS ease-in-out: cubic-bezier(0.42, 0, 0.58, 1) */
    data object EaseInOut : CssTimingFunction

    /** Custom cubic bezier timing function */
    data class CubicBezier(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : CssTimingFunction

    /** Step function for discrete animation */
    data class Steps(val count: Int, val position: StepPosition = StepPosition.END) : CssTimingFunction

    /**
     * Convert to KeySplines for SMIL animation.
     * Returns null for non-cubic-bezier functions.
     */
    fun toKeySplines(): KeySplines? = when (this) {
        is Linear -> null // Linear doesn't need keySplines
        is Ease -> KeySplines.EASE
        is EaseIn -> KeySplines.EASE_IN
        is EaseOut -> KeySplines.EASE_OUT
        is EaseInOut -> KeySplines.EASE_IN_OUT
        is CubicBezier -> KeySplines(x1, y1, x2, y2)
        is Steps -> null // Steps not supported as keySplines
    }

    /**
     * Convert to CalcMode for SMIL animation.
     */
    fun toCalcMode(): CalcMode = when (this) {
        is Linear -> CalcMode.LINEAR
        is Steps -> CalcMode.DISCRETE
        else -> CalcMode.SPLINE
    }

    companion object {
        /**
         * Parse CSS timing function string.
         */
        fun parse(value: String): CssTimingFunction {
            val trimmed = value.trim().lowercase()
            return when {
                trimmed == "linear" -> Linear
                trimmed == "ease" -> Ease
                trimmed == "ease-in" -> EaseIn
                trimmed == "ease-out" -> EaseOut
                trimmed == "ease-in-out" -> EaseInOut
                trimmed.startsWith("cubic-bezier(") -> {
                    val params = trimmed
                        .removePrefix("cubic-bezier(")
                        .removeSuffix(")")
                        .split(",")
                        .mapNotNull { it.trim().toFloatOrNull() }
                    if (params.size == 4) {
                        CubicBezier(params[0], params[1], params[2], params[3])
                    } else {
                        Ease // Default fallback
                    }
                }
                trimmed.startsWith("steps(") -> {
                    val params = trimmed
                        .removePrefix("steps(")
                        .removeSuffix(")")
                        .split(",")
                        .map { it.trim() }
                    val count = params.getOrNull(0)?.toIntOrNull() ?: 1
                    val position = when (params.getOrNull(1)?.lowercase()) {
                        "start", "jump-start" -> StepPosition.START
                        "both", "jump-both" -> StepPosition.BOTH
                        "none", "jump-none" -> StepPosition.NONE
                        else -> StepPosition.END
                    }
                    Steps(count, position)
                }
                else -> Ease // Default
            }
        }
    }
}

/**
 * A single keyframe in a CSS @keyframes animation.
 *
 * @param offset Position in the animation (0.0 = 0%, 1.0 = 100%)
 * @param properties CSS properties at this keyframe
 */
data class CssKeyframe(
    val offset: Float,
    val properties: Map<String, String>
)

/**
 * CSS @keyframes animation definition.
 *
 * @param name Animation name (e.g., "spin", "fadeIn")
 * @param keyframes List of keyframes
 */
data class CssKeyframes(
    val name: String,
    val keyframes: List<CssKeyframe>
)

/**
 * CSS animation-direction values.
 */
enum class AnimationDirection {
    NORMAL,
    REVERSE,
    ALTERNATE,
    ALTERNATE_REVERSE
}

/**
 * CSS animation-fill-mode values.
 */
enum class AnimationFillMode {
    NONE,
    FORWARDS,
    BACKWARDS,
    BOTH
}

/**
 * Parsed CSS animation property.
 *
 * @param name Reference to @keyframes name
 * @param duration Animation duration
 * @param timingFunction Easing function
 * @param delay Animation delay
 * @param iterationCount Number of iterations (Int.MAX_VALUE for infinite)
 * @param direction Animation direction
 * @param fillMode How styles are applied before/after animation
 */
data class CssAnimation(
    val name: String,
    val duration: Duration = Duration.ZERO,
    val timingFunction: CssTimingFunction = CssTimingFunction.Ease,
    val delay: Duration = Duration.ZERO,
    val iterationCount: Int = 1,
    val direction: AnimationDirection = AnimationDirection.NORMAL,
    val fillMode: AnimationFillMode = AnimationFillMode.NONE
)
