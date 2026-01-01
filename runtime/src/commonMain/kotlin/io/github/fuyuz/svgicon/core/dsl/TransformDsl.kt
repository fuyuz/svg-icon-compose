package io.github.fuyuz.svgicon.core.dsl

import io.github.fuyuz.svgicon.core.SvgTransform

/**
 * Transform DSL extensions for fluent transform construction.
 */

// ============================================
// Transform Builders
// ============================================

/** Create a translate transform. */
fun translate(x: Number, y: Number = 0): SvgTransform = SvgTransform.Translate(x.toFloat(), y.toFloat())

/** Create a translate transform (x only). */
fun translateX(x: Number): SvgTransform = SvgTransform.Translate(x.toFloat(), 0f)

/** Create a translate transform (y only). */
fun translateY(y: Number): SvgTransform = SvgTransform.Translate(0f, y.toFloat())

/** Create a scale transform. */
fun scale(s: Number): SvgTransform = SvgTransform.Scale(s.toFloat())

/** Create a scale transform with separate x and y factors. */
fun scale(sx: Number, sy: Number): SvgTransform = SvgTransform.Scale(sx.toFloat(), sy.toFloat())

/** Create a scaleX transform. */
fun scaleX(sx: Number): SvgTransform = SvgTransform.Scale(sx.toFloat(), 1f)

/** Create a scaleY transform. */
fun scaleY(sy: Number): SvgTransform = SvgTransform.Scale(1f, sy.toFloat())

/** Create a rotate transform around the origin. */
fun rotate(angle: Number): SvgTransform = SvgTransform.Rotate(angle.toFloat())

/** Create a rotate transform around a specific point. */
fun rotate(angle: Number, cx: Number, cy: Number): SvgTransform =
    SvgTransform.Rotate(angle.toFloat(), cx.toFloat(), cy.toFloat())

/** Create a rotate transform around a specific point. */
fun rotate(angle: Number, pivot: Pair<Number, Number>): SvgTransform =
    SvgTransform.Rotate(angle.toFloat(), pivot.first.toFloat(), pivot.second.toFloat())

/** Create a skewX transform. */
fun skewX(angle: Number): SvgTransform = SvgTransform.SkewX(angle.toFloat())

/** Create a skewY transform. */
fun skewY(angle: Number): SvgTransform = SvgTransform.SkewY(angle.toFloat())

/** Create a matrix transform. */
fun matrix(a: Number, b: Number, c: Number, d: Number, e: Number, f: Number): SvgTransform =
    SvgTransform.Matrix(a.toFloat(), b.toFloat(), c.toFloat(), d.toFloat(), e.toFloat(), f.toFloat())

// ============================================
// Transform Operators
// ============================================

/**
 * Combine two transforms.
 *
 * Example:
 * ```kotlin
 * val t = rotate(45f) + scale(1.5f) + translate(10, 5)
 * ```
 */
operator fun SvgTransform.plus(other: SvgTransform): SvgTransform = when {
    this is SvgTransform.Combined && other is SvgTransform.Combined ->
        SvgTransform.Combined(this.transforms + other.transforms)
    this is SvgTransform.Combined ->
        SvgTransform.Combined(this.transforms + other)
    other is SvgTransform.Combined ->
        SvgTransform.Combined(listOf(this) + other.transforms)
    else ->
        SvgTransform.Combined(listOf(this, other))
}

// ============================================
// Transform Builder DSL
// ============================================

/**
 * Builder for constructing multiple transforms.
 */
@SvgDslMarker
class TransformBuilder {
    private val transforms = mutableListOf<SvgTransform>()

    fun translate(x: Number, y: Number = 0) {
        transforms.add(SvgTransform.Translate(x.toFloat(), y.toFloat()))
    }

    fun scale(s: Number) {
        transforms.add(SvgTransform.Scale(s.toFloat()))
    }

    fun scale(sx: Number, sy: Number) {
        transforms.add(SvgTransform.Scale(sx.toFloat(), sy.toFloat()))
    }

    fun rotate(angle: Number) {
        transforms.add(SvgTransform.Rotate(angle.toFloat()))
    }

    fun rotate(angle: Number, cx: Number, cy: Number) {
        transforms.add(SvgTransform.Rotate(angle.toFloat(), cx.toFloat(), cy.toFloat()))
    }

    fun rotate(angle: Number, pivot: Pair<Number, Number>) {
        transforms.add(SvgTransform.Rotate(angle.toFloat(), pivot.first.toFloat(), pivot.second.toFloat()))
    }

    fun skewX(angle: Number) {
        transforms.add(SvgTransform.SkewX(angle.toFloat()))
    }

    fun skewY(angle: Number) {
        transforms.add(SvgTransform.SkewY(angle.toFloat()))
    }

    fun build(): SvgTransform? = when (transforms.size) {
        0 -> null
        1 -> transforms.first()
        else -> SvgTransform.Combined(transforms.toList())
    }
}

/**
 * Build a transform using DSL syntax.
 *
 * Example:
 * ```kotlin
 * val t = transform {
 *     rotate(45f, pivot = 12 to 12)
 *     scale(1.5f)
 *     translate(10, 5)
 * }
 * ```
 */
inline fun transform(block: TransformBuilder.() -> Unit): SvgTransform? =
    TransformBuilder().apply(block).build()
