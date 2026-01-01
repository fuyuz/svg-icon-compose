package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Base interface for all SVG elements.
 */
sealed interface SvgElement

/**
 * SVG root element.
 * Represents the <svg> tag with its attributes.
 *
 * Color handling:
 * - Color.Unspecified = "currentColor" (uses tint from SvgIcon composable)
 * - null = "none" (no color / transparent)
 * - Any other Color = that specific color
 *
 * Size determination:
 * - If width/height are specified, they define the viewport size
 * - If only viewBox is specified, viewBox dimensions are used
 * - preserveAspectRatio controls how viewBox maps to viewport
 *
 * @param width Viewport width (null = use viewBox width)
 * @param height Viewport height (null = use viewBox height)
 * @param viewBox ViewBox defining the coordinate system (default: 24x24)
 * @param preserveAspectRatio How to scale/align viewBox within viewport
 * @param fill Default fill color (default: null = no fill)
 * @param stroke Default stroke color (default: Unspecified = uses tint color)
 * @param strokeWidth Default stroke width (default: 2)
 * @param strokeLinecap Default stroke linecap (default: Round)
 * @param strokeLinejoin Default stroke linejoin (default: Round)
 * @param children Child SVG elements
 */
data class Svg(
    val width: Float? = null,
    val height: Float? = null,
    val viewBox: ViewBox? = null,
    val preserveAspectRatio: PreserveAspectRatio = PreserveAspectRatio.Default,
    val fill: Color? = null,
    val stroke: Color? = Color.Unspecified,
    val strokeWidth: Float = 2f,
    val strokeLinecap: LineCap = LineCap.ROUND,
    val strokeLinejoin: LineJoin = LineJoin.ROUND,
    val children: List<SvgElement> = emptyList()
) {
    /**
     * Effective width for rendering.
     * Priority: explicit width > viewBox width > default (24)
     */
    val effectiveWidth: Float get() = width ?: viewBox?.width ?: 24f

    /**
     * Effective height for rendering.
     * Priority: explicit height > viewBox height > default (24)
     */
    val effectiveHeight: Float get() = height ?: viewBox?.height ?: 24f

    /**
     * Effective viewBox for rendering.
     * If viewBox is not specified, creates one from width/height or defaults.
     */
    val effectiveViewBox: ViewBox get() = viewBox ?: ViewBox(0f, 0f, effectiveWidth, effectiveHeight)
}

/**
 * SVG element with style attributes.
 */
data class SvgStyled(
    val element: SvgElement,
    val style: SvgStyle
) : SvgElement

/**
 * SVG element with animation.
 */
data class SvgAnimated(
    val element: SvgElement,
    val animations: List<SvgAnimate>
) : SvgElement

/**
 * SVG path element with parsed commands.
 * @param commands List of path commands
 */
data class SvgPath(val commands: List<PathCommand>) : SvgElement {
    companion object {
        /**
         * Creates SvgPath from path data string.
         */
        operator fun invoke(d: String): SvgPath = SvgPath(parsePathCommands(d))
    }
}

/**
 * SVG circle element.
 * @param cx Center x coordinate
 * @param cy Center y coordinate
 * @param r Radius
 */
data class SvgCircle(
    val cx: Float,
    val cy: Float,
    val r: Float
) : SvgElement

/**
 * SVG ellipse element.
 * @param cx Center x coordinate
 * @param cy Center y coordinate
 * @param rx Radius in x direction
 * @param ry Radius in y direction
 */
data class SvgEllipse(
    val cx: Float,
    val cy: Float,
    val rx: Float,
    val ry: Float
) : SvgElement

/**
 * SVG rectangle element.
 * @param x X coordinate of top-left corner
 * @param y Y coordinate of top-left corner
 * @param width Width of the rectangle
 * @param height Height of the rectangle
 * @param rx Horizontal corner radius (optional)
 * @param ry Vertical corner radius (optional, defaults to rx)
 */
data class SvgRect(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float,
    val height: Float,
    val rx: Float = 0f,
    val ry: Float = rx
) : SvgElement

/**
 * SVG line element.
 * @param x1 Start x coordinate
 * @param y1 Start y coordinate
 * @param x2 End x coordinate
 * @param y2 End y coordinate
 */
data class SvgLine(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
) : SvgElement

/**
 * Text anchor alignment.
 */
enum class TextAnchor {
    START,
    MIDDLE,
    END
}

/**
 * Dominant baseline alignment.
 */
enum class DominantBaseline {
    AUTO,
    TEXT_BOTTOM,
    ALPHABETIC,
    IDEOGRAPHIC,
    MIDDLE,
    CENTRAL,
    MATHEMATICAL,
    HANGING,
    TEXT_TOP
}

/**
 * SVG text element.
 * @param text The text content to display
 * @param x X coordinate of the text position
 * @param y Y coordinate of the text position
 * @param textAnchor Horizontal alignment (start, middle, end)
 * @param dominantBaseline Vertical alignment
 * @param fontSize Font size
 * @param fontFamily Font family name
 * @param fontWeight Font weight (100-900, "normal", "bold")
 * @param letterSpacing Letter spacing
 * @param dx Relative X offset
 * @param dy Relative Y offset
 */
data class SvgText(
    val text: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val textAnchor: TextAnchor? = null,
    val dominantBaseline: DominantBaseline? = null,
    val fontSize: Float? = null,
    val fontFamily: String? = null,
    val fontWeight: String? = null,
    val letterSpacing: Float? = null,
    val dx: Float? = null,
    val dy: Float? = null
) : SvgElement

/**
 * SVG polyline element (open shape).
 * @param points List of coordinate points
 */
data class SvgPolyline(val points: List<Offset>) : SvgElement

/**
 * SVG polygon element (closed shape).
 * @param points List of coordinate points
 */
data class SvgPolygon(val points: List<Offset>) : SvgElement

/**
 * SVG group element (g tag).
 * @param children Child elements
 * @param style Optional style applied to all children
 */
data class SvgGroup(
    val children: List<SvgElement>,
    val style: SvgStyle? = null
) : SvgElement

/**
 * SVG clipPath element.
 * Defines a clipping region that restricts the visible area of elements.
 * @param id Unique identifier for referencing this clip path
 * @param children Child elements that define the clipping shape
 * @param clipPathUnits Coordinate system: "userSpaceOnUse" or "objectBoundingBox"
 */
data class SvgClipPath(
    val id: String,
    val children: List<SvgElement>,
    val clipPathUnits: ClipPathUnits = ClipPathUnits.USER_SPACE_ON_USE
) : SvgElement

/**
 * SVG mask element.
 * Defines a mask that controls the transparency of elements.
 * @param id Unique identifier for referencing this mask
 * @param children Child elements that define the mask
 * @param maskUnits Coordinate system for mask positioning
 * @param maskContentUnits Coordinate system for mask content
 */
data class SvgMask(
    val id: String,
    val children: List<SvgElement>,
    val maskUnits: MaskUnits = MaskUnits.OBJECT_BOUNDING_BOX,
    val maskContentUnits: MaskUnits = MaskUnits.USER_SPACE_ON_USE,
    val x: Float = -0.1f,
    val y: Float = -0.1f,
    val width: Float = 1.2f,
    val height: Float = 1.2f
) : SvgElement

/**
 * SVG defs element.
 * Container for reusable elements like clipPath, mask, gradients, etc.
 * @param children Child definition elements
 */
data class SvgDefs(val children: List<SvgElement>) : SvgElement

// ============================================
// Gradient Elements
// ============================================

/**
 * Gradient spread method.
 */
enum class SpreadMethod {
    PAD,
    REFLECT,
    REPEAT
}

/**
 * Gradient units.
 */
enum class GradientUnits {
    USER_SPACE_ON_USE,
    OBJECT_BOUNDING_BOX
}

/**
 * Gradient stop.
 * @param offset Position in the gradient (0.0 - 1.0)
 * @param color Color at this position
 * @param opacity Opacity at this position (0.0 - 1.0)
 */
data class GradientStop(
    val offset: Float,
    val color: Color,
    val opacity: Float = 1f
)

/**
 * SVG linearGradient element.
 * @param id Unique identifier for referencing this gradient
 * @param x1 Start x coordinate (0.0 - 1.0 for objectBoundingBox)
 * @param y1 Start y coordinate
 * @param x2 End x coordinate
 * @param y2 End y coordinate
 * @param stops Gradient color stops
 * @param gradientUnits Coordinate system for gradient
 * @param spreadMethod How gradient extends beyond bounds
 * @param gradientTransform Transform applied to gradient
 */
data class SvgLinearGradient(
    val id: String,
    val x1: Float = 0f,
    val y1: Float = 0f,
    val x2: Float = 1f,
    val y2: Float = 0f,
    val stops: List<GradientStop> = emptyList(),
    val gradientUnits: GradientUnits = GradientUnits.OBJECT_BOUNDING_BOX,
    val spreadMethod: SpreadMethod = SpreadMethod.PAD,
    val gradientTransform: SvgTransform? = null
) : SvgElement

/**
 * SVG radialGradient element.
 * @param id Unique identifier for referencing this gradient
 * @param cx Center x coordinate
 * @param cy Center y coordinate
 * @param r Radius
 * @param fx Focal point x coordinate
 * @param fy Focal point y coordinate
 * @param stops Gradient color stops
 * @param gradientUnits Coordinate system for gradient
 * @param spreadMethod How gradient extends beyond bounds
 * @param gradientTransform Transform applied to gradient
 */
data class SvgRadialGradient(
    val id: String,
    val cx: Float = 0.5f,
    val cy: Float = 0.5f,
    val r: Float = 0.5f,
    val fx: Float? = null,
    val fy: Float? = null,
    val stops: List<GradientStop> = emptyList(),
    val gradientUnits: GradientUnits = GradientUnits.OBJECT_BOUNDING_BOX,
    val spreadMethod: SpreadMethod = SpreadMethod.PAD,
    val gradientTransform: SvgTransform? = null
) : SvgElement

// ============================================
// Symbol and Use Elements
// ============================================

/**
 * SVG symbol element.
 * Defines a reusable graphic template that can be instantiated with <use>.
 * Symbols are not rendered directly - they must be referenced by <use>.
 *
 * @param id Unique identifier for referencing this symbol
 * @param viewBox ViewBox for the symbol's coordinate system
 * @param preserveAspectRatio How to scale/align viewBox within use bounds
 * @param children Child elements of the symbol
 */
data class SvgSymbol(
    val id: String,
    val viewBox: ViewBox? = null,
    val preserveAspectRatio: PreserveAspectRatio = PreserveAspectRatio.Default,
    val children: List<SvgElement> = emptyList()
) : SvgElement

/**
 * SVG use element.
 * References and instantiates another element (typically a symbol).
 *
 * @param href ID reference to the element to use (e.g., "#mySymbol")
 * @param x X position for the use instance
 * @param y Y position for the use instance
 * @param width Width of the use instance (for symbols)
 * @param height Height of the use instance (for symbols)
 */
data class SvgUse(
    val href: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float? = null,
    val height: Float? = null
) : SvgElement

/**
 * SVG image element.
 * Embeds an external image in the SVG.
 *
 * @param href URL or data URI of the image
 * @param x X position
 * @param y Y position
 * @param width Width of the image
 * @param height Height of the image
 * @param preserveAspectRatio How to scale/align image
 */
data class SvgImage(
    val href: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float,
    val height: Float,
    val preserveAspectRatio: PreserveAspectRatio = PreserveAspectRatio.Default
) : SvgElement
