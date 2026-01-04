package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer

/**
 * Context for SVG drawing operations.
 */
data class DrawContext(
    val strokeColor: Color,
    val fillColor: Color?,
    val stroke: Stroke,
    val opacity: Float = 1f,
    val fillRule: PathFillType = PathFillType.NonZero,
    val hasStroke: Boolean = true,
    val paintOrder: PaintOrder = PaintOrder.FILL_STROKE,
    val vectorEffect: VectorEffect = VectorEffect.NONE,
    val scaleFactor: Float = 1f,
    val clipPathId: String? = null,
    val maskId: String? = null
)

/**
 * Registry for SVG definitions (clip paths, masks, etc.).
 */
data class DefsRegistry(
    val clipPaths: Map<String, SvgClipPath> = emptyMap(),
    val masks: Map<String, SvgMask> = emptyMap(),
    val symbols: Map<String, SvgSymbol> = emptyMap(),
    val markers: Map<String, SvgMarker> = emptyMap(),
    val patterns: Map<String, SvgPattern> = emptyMap(),
    val textMeasurer: TextMeasurer? = null
)

/**
 * Combined context for SVG drawing.
 */
data class SvgDrawingContext(
    val ctx: DrawContext,
    val registry: DefsRegistry
) {
    fun withCtx(newCtx: DrawContext) = copy(ctx = newCtx)
}

/**
 * Transformation for SVG viewBox.
 */
data class ViewBoxTransform(
    val scaleX: Float,
    val scaleY: Float,
    val translateX: Float,
    val translateY: Float
)
