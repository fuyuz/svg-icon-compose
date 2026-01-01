# SVG DSL Usage Guide

This document explains how to use the Kotlin DSL to define SVG icons programmatically.

## Table of Contents

- [Basic Structure](#basic-structure)
- [SVG Elements](#svg-elements)
- [Styling](#styling)
- [Transforms](#transforms)
- [Animations](#animations)
- [DSL Builder](#dsl-builder)
- [Complete Examples](#complete-examples)

## Basic Structure

### Svg Root Element

The `Svg` class represents the root `<svg>` element:

```kotlin
Svg(
    width = 24,           // Width of the SVG (default: 24)
    height = 24,          // Height of the SVG (default: 24)
    viewBox = "0 0 24 24", // ViewBox string (default: "0 0 24 24")
    fill = "none",        // Default fill (default: "none")
    stroke = "currentColor", // Default stroke (default: "currentColor")
    strokeWidth = 2f,     // Default stroke width (default: 2)
    strokeLinecap = LineCap.ROUND,  // Line cap style (default: ROUND)
    strokeLinejoin = LineJoin.ROUND, // Line join style (default: ROUND)
    children = listOf(/* SVG elements */)
)
```

### Creating an Icon

Implement the `SvgIcon` interface:

```kotlin
object CheckIcon : SvgIcon {
    override val svg = Svg(
        children = listOf(
            SvgPath("M20 6L9 17l-5-5")
        )
    )
}

// Usage
SvgIcon(icon = CheckIcon, contentDescription = "Check")
```

## SVG Elements

### SvgPath

Paths are the most versatile element. You can create them from a path data string:

```kotlin
// From path data string (auto-parsed)
SvgPath("M20 6L9 17l-5-5")

// Explicitly with parsed commands
SvgPath(commands = listOf(
    PathCommand.MoveTo(20f, 6f),
    PathCommand.LineTo(9f, 17f),
    PathCommand.LineToRelative(-5f, -5f)
))
```

Supported path commands:
- `M/m` - MoveTo (absolute/relative)
- `L/l` - LineTo (absolute/relative)
- `H/h` - Horizontal line (absolute/relative)
- `V/v` - Vertical line (absolute/relative)
- `C/c` - Cubic bezier (absolute/relative)
- `S/s` - Smooth cubic bezier (absolute/relative)
- `Q/q` - Quadratic bezier (absolute/relative)
- `T/t` - Smooth quadratic bezier (absolute/relative)
- `A/a` - Arc (absolute/relative)
- `Z/z` - Close path

### SvgCircle

```kotlin
SvgCircle(
    cx = 12f,  // Center X
    cy = 12f,  // Center Y
    r = 10f    // Radius
)
```

### SvgEllipse

```kotlin
SvgEllipse(
    cx = 12f,  // Center X
    cy = 12f,  // Center Y
    rx = 10f,  // Radius X
    ry = 5f    // Radius Y
)
```

### SvgRect

```kotlin
SvgRect(
    x = 4f,       // Top-left X (default: 0)
    y = 4f,       // Top-left Y (default: 0)
    width = 16f,  // Width
    height = 16f, // Height
    rx = 2f,      // Corner radius X (default: 0)
    ry = 2f       // Corner radius Y (default: rx)
)
```

### SvgLine

```kotlin
SvgLine(
    x1 = 5f,   // Start X
    y1 = 12f,  // Start Y
    x2 = 19f,  // End X
    y2 = 12f   // End Y
)
```

### SvgPolyline

```kotlin
import androidx.compose.ui.geometry.Offset

SvgPolyline(
    points = listOf(
        Offset(5f, 12f),
        Offset(12f, 5f),
        Offset(19f, 12f)
    )
)
```

### SvgPolygon

```kotlin
import androidx.compose.ui.geometry.Offset

SvgPolygon(
    points = listOf(
        Offset(12f, 2f),
        Offset(22f, 22f),
        Offset(2f, 22f)
    )
)
```

### SvgGroup

Groups multiple elements together:

```kotlin
SvgGroup(
    children = listOf(
        SvgCircle(12f, 12f, 10f),
        SvgPath("M8 12l3 3 5-6")
    )
)
```

## Styling

### SvgStyle

Apply custom styles to elements using `SvgStyled`:

```kotlin
SvgStyled(
    element = SvgCircle(12f, 12f, 10f),
    style = SvgStyle(
        // Stroke properties
        stroke = "#ff0000",        // Stroke color
        strokeWidth = 3f,          // Stroke width
        strokeOpacity = 0.8f,      // Stroke opacity (0.0 - 1.0)
        strokeLinecap = LineCap.ROUND,   // butt, round, square
        strokeLinejoin = LineJoin.ROUND, // miter, round, bevel
        strokeDasharray = listOf(5f, 3f), // Dash pattern
        strokeDashoffset = 2f,     // Dash offset
        strokeMiterlimit = 4f,     // Miter limit

        // Fill properties
        fill = "#00ff00",          // Fill color
        fillOpacity = 0.5f,        // Fill opacity (0.0 - 1.0)
        fillRule = FillRule.EVENODD, // nonzero, evenodd

        // General properties
        opacity = 1f,              // Overall opacity (0.0 - 1.0)
        transform = SvgTransform.Rotate(45f), // Transform
        paintOrder = PaintOrder.STROKE_FILL,  // Paint order
        vectorEffect = VectorEffect.NON_SCALING_STROKE // Vector effect
    )
)
```

### Color Formats

Supported color formats:
- `"currentColor"` - Uses the tint color passed to SvgIcon
- `"#rgb"` - Short hex (e.g., `"#f00"`)
- `"#rrggbb"` - Full hex (e.g., `"#ff0000"`)
- `"#rrggbbaa"` - Hex with alpha (e.g., `"#ff0000ff"`)
- `"rgb(r, g, b)"` - RGB function
- `"rgba(r, g, b, a)"` - RGBA function
- Named colors: `"black"`, `"white"`, `"red"`, `"green"`, `"blue"`, etc.

### Stroke Width at Different Levels

You can set stroke width at multiple levels:

```kotlin
// 1. At Svg root level (default for all children)
Svg(
    strokeWidth = 2f,
    children = listOf(...)
)

// 2. At element level using SvgStyled
SvgStyled(
    element = SvgPath("M5 12h14"),
    style = SvgStyle(strokeWidth = 4f)
)

// 3. At composable level (overrides all)
SvgIcon(
    icon = myIcon,
    contentDescription = "Icon",
    strokeWidth = 3f  // Overrides SVG strokeWidth
)
```

## Transforms

### Transform Types

```kotlin
// Translate
SvgTransform.Translate(x = 10f, y = 5f)

// Scale
SvgTransform.Scale(sx = 1.5f, sy = 1.5f)

// Rotate (angle in degrees, optional pivot point)
SvgTransform.Rotate(angle = 45f, cx = 12f, cy = 12f)

// Skew
SvgTransform.SkewX(angle = 15f)
SvgTransform.SkewY(angle = 15f)

// Matrix (a, b, c, d, e, f)
SvgTransform.Matrix(
    a = 1f, b = 0f,  // Scale X, Skew Y
    c = 0f, d = 1f,  // Skew X, Scale Y
    e = 0f, f = 0f   // Translate X, Y
)

// Combined transforms (applied in order)
SvgTransform.Combined(listOf(
    SvgTransform.Translate(5f, 5f),
    SvgTransform.Rotate(45f),
    SvgTransform.Scale(1.2f)
))
```

### Applying Transforms

```kotlin
SvgStyled(
    element = SvgRect(4f, 4f, 16f, 16f),
    style = SvgStyle(
        transform = SvgTransform.Rotate(45f, 12f, 12f)
    )
)
```

## Animations

### SvgAnimated

Wrap elements with `SvgAnimated` to add animations:

```kotlin
import kotlin.time.Duration.Companion.milliseconds

SvgAnimated(
    element = SvgPath("M5 12h14"),
    animations = listOf(
        SvgAnimate.StrokeDraw(dur = 500.milliseconds)
    )
)
```

### Animation Types

All animation types use `kotlin.time.Duration` for timing:

```kotlin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
```

#### Stroke Animations

```kotlin
// Stroke drawing (line appears from start to end)
SvgAnimate.StrokeDraw(
    dur = 500.milliseconds,       // Duration
    delay = Duration.ZERO,        // Start delay (default: Duration.ZERO)
    reverse = false               // true = end to start
)

// Stroke width animation
SvgAnimate.StrokeWidth(from = 1f, to = 4f, dur = 500.milliseconds)

// Stroke opacity animation
SvgAnimate.StrokeOpacity(from = 0f, to = 1f, dur = 500.milliseconds)

// Stroke dash array animation
SvgAnimate.StrokeDasharray(
    from = listOf(0f, 100f),
    to = listOf(100f, 0f),
    dur = 500.milliseconds
)

// Stroke dash offset animation
SvgAnimate.StrokeDashoffset(from = 100f, to = 0f, dur = 500.milliseconds)
```

#### Fill Animations

```kotlin
SvgAnimate.FillOpacity(from = 0f, to = 1f, dur = 500.milliseconds)
```

#### Opacity Animation

```kotlin
SvgAnimate.Opacity(from = 0f, to = 1f, dur = 500.milliseconds)
```

#### Transform Animations

```kotlin
SvgAnimate.Transform(
    type = TransformType.ROTATE,  // ROTATE, SCALE, TRANSLATE_X, TRANSLATE_Y, etc.
    from = 0f,
    to = 360f,
    dur = 1.seconds,
    delay = Duration.ZERO
)
```

Available transform types:
- `TransformType.TRANSLATE` / `TRANSLATE_X` / `TRANSLATE_Y`
- `TransformType.SCALE` / `SCALE_X` / `SCALE_Y`
- `TransformType.ROTATE`
- `TransformType.SKEW_X` / `SKEW_Y`

#### Geometric Animations

```kotlin
// Circle/Ellipse center
SvgAnimate.Cx(from = 6f, to = 18f, dur = 500.milliseconds)
SvgAnimate.Cy(from = 6f, to = 18f, dur = 500.milliseconds)

// Circle radius
SvgAnimate.R(from = 2f, to = 10f, dur = 500.milliseconds)

// Ellipse radii
SvgAnimate.Rx(from = 2f, to = 10f, dur = 500.milliseconds)
SvgAnimate.Ry(from = 2f, to = 10f, dur = 500.milliseconds)

// Rectangle position
SvgAnimate.X(from = 0f, to = 10f, dur = 500.milliseconds)
SvgAnimate.Y(from = 0f, to = 10f, dur = 500.milliseconds)

// Rectangle size
SvgAnimate.Width(from = 10f, to = 20f, dur = 500.milliseconds)
SvgAnimate.Height(from = 10f, to = 20f, dur = 500.milliseconds)

// Line endpoints
SvgAnimate.X1(from = 0f, to = 5f, dur = 500.milliseconds)
SvgAnimate.Y1(from = 0f, to = 5f, dur = 500.milliseconds)
SvgAnimate.X2(from = 24f, to = 19f, dur = 500.milliseconds)
SvgAnimate.Y2(from = 24f, to = 19f, dur = 500.milliseconds)

// Path morphing
SvgAnimate.D(from = "M5 12h14", to = "M5 5h14v14", dur = 500.milliseconds)

// Polygon/Polyline points (uses Compose Offset)
import androidx.compose.ui.geometry.Offset

SvgAnimate.Points(
    from = listOf(Offset(5f, 5f), Offset(19f, 5f), Offset(19f, 19f)),
    to = listOf(Offset(12f, 2f), Offset(22f, 22f), Offset(2f, 22f)),
    dur = 500.milliseconds
)
```

#### Motion Animation

```kotlin
SvgAnimate.Motion(
    path = "M0,0 C10,20 30,20 40,0",  // Motion path
    dur = 1.seconds,
    delay = Duration.ZERO,
    rotate = MotionRotate.AUTO  // NONE, AUTO, AUTO_REVERSE
)
```

### Using AnimatedSvgIcon

```kotlin
AnimatedSvgIcon(
    icon = myAnimatedIcon,
    contentDescription = "Animated icon",
    tint = Color.White,
    size = 48.dp,
    animate = true,              // Enable/disable animation
    iterations = Int.MAX_VALUE,  // Number of iterations (MAX_VALUE = infinite)
    onAnimationEnd = { }         // Callback when animation ends
)
```

## DSL Builder

### Building Complete Svg Objects

Use `svg()` with parameters to create a complete `Svg` object:

```kotlin
object MyIcon : SvgIcon {
    override val svg = svg(
        strokeWidth = 3f,
        stroke = Color.Red,
        fill = Color.Blue.copy(alpha = 0.3f)
    ) {
        path("M20 6L9 17l-5-5")
        circle(12, 12, 10)
    }
}
```

### Color Handling

Colors use Compose's `Color` type with special semantics:

| Value | Meaning |
|-------|---------|
| `Color.Unspecified` | "currentColor" - uses tint from SvgIcon composable |
| `null` | "none" - no color (transparent / no fill/stroke) |
| `Color.Red`, etc. | Use that specific color |

```kotlin
// Use tint color (currentColor)
stroke = Color.Unspecified

// No stroke
stroke = null

// Specific color
stroke = Color.Red
stroke = Color(0xFF3B82F6)
stroke = Color.Blue.copy(alpha = 0.5f)
```

### Example with Colors

```kotlin
object ColorfulIcon : SvgIcon {
    override val svg = svg(
        strokeWidth = 2f,
        stroke = Color.Unspecified,  // Uses tint color
        fill = Color(0xFF3B82F6).copy(alpha = 0.3f)  // Semi-transparent blue
    ) {
        circle(12, 12, 10)
        path("M8 12l3 3 5-6")
    }
}
```

### ViewBox

Use the type-safe `ViewBox` class:

```kotlin
// Default 24x24
svg(width = 24, height = 24) { ... }

// Custom viewBox
svg(viewBox = ViewBox(0f, 0f, 100f, 100f)) { ... }

// Predefined sizes
svg(viewBox = ViewBox.Size16) { ... }
svg(viewBox = ViewBox.Size32) { ... }
svg(viewBox = ViewBox.square(48f)) { ... }
```

### Building Element Lists

Use the simpler `svg {}` to build element lists:

```kotlin
val elements = svg {
    // Basic elements
    path("M20 6L9 17l-5-5")
    circle(12, 12, 10)
    ellipse(12, 12, 8, 4)
    rect(x = 4, y = 4, width = 16, height = 16, rx = 2)
    line(5, 12, 19, 12)
    polyline(5 to 12, 12 to 5, 19 to 12)
    polygon(12 to 2, 22 to 22, 2 to 22)

    // Groups
    group {
        circle(6, 6, 3)
        circle(18, 6, 3)
    }

    // Animated elements (dur/delay use kotlin.time.Duration)
    animatedPath("M5 12h14", dur = 300.milliseconds)
    animatedCircle(12, 12, 10, dur = 500.milliseconds)
    animatedLine(5, 12, 19, 12, dur = 400.milliseconds)

    // Path with custom animations
    path("M12 2v10") {
        strokeDraw(dur = 300.milliseconds)
        opacity(from = 0f, to = 1f, dur = 200.milliseconds)
    }

    // Circle with animations
    circle(12, 12, 5) {
        scale(from = 0.5f, to = 1f, dur = 300.milliseconds)
        opacity(from = 0f, to = 1f, dur = 200.milliseconds)
    }

    // Clip paths
    defs {
        clipPath("myClip") {
            circle(12, 12, 10)
        }
    }
}

// Create icon from builder
object MyIcon : SvgIcon {
    override val svg = Svg(children = elements)
}
```

### AnimationBuilder Functions

Within an animation block, you can use Duration values:

```kotlin
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

path("M...") {
    // Stroke animations
    strokeDraw(dur = 500.milliseconds, delay = Duration.ZERO, reverse = false)
    strokeWidth(from = 1f, to = 4f, dur = 500.milliseconds)
    strokeOpacity(from = 0f, to = 1f, dur = 500.milliseconds)
    strokeDasharray(from = listOf(0f, 10f), to = listOf(10f, 0f), dur = 500.milliseconds)
    strokeDashoffset(from = 100f, to = 0f, dur = 500.milliseconds)

    // Fill animations
    fillOpacity(from = 0f, to = 1f, dur = 500.milliseconds)

    // General
    opacity(from = 0f, to = 1f, dur = 500.milliseconds)

    // Transforms
    translateX(from = 0f, to = 10f, dur = 500.milliseconds)
    translateY(from = 0f, to = 10f, dur = 500.milliseconds)
    scale(from = 0.5f, to = 1f, dur = 500.milliseconds)
    rotate(from = 0f, to = 360f, dur = 1.seconds)
    skewX(from = 0f, to = 15f, dur = 500.milliseconds)
    skewY(from = 0f, to = 15f, dur = 500.milliseconds)

    // Motion
    motion(path = "M0,0 L10,10", dur = 500.milliseconds, rotate = MotionRotate.AUTO)

    // Geometric
    cx(from = 6f, to = 18f, dur = 500.milliseconds)
    cy(from = 6f, to = 18f, dur = 500.milliseconds)
    r(from = 2f, to = 10f, dur = 500.milliseconds)
    // ... and more
}
```

## Complete Examples

### Simple Check Icon

```kotlin
object CheckIcon : SvgIcon {
    override val svg = Svg(
        strokeWidth = 2f,
        children = listOf(
            SvgPath("M20 6L9 17l-5-5")
        )
    )
}
```

### Colored Circle with Border

```kotlin
object FilledCircle : SvgIcon {
    override val svg = Svg(
        children = listOf(
            SvgStyled(
                element = SvgCircle(12f, 12f, 10f),
                style = SvgStyle(
                    fill = "#3B82F6",
                    fillOpacity = 0.3f,
                    stroke = "#3B82F6",
                    strokeWidth = 2f
                )
            )
        )
    )
}
```

### Animated Loading Spinner

```kotlin
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object LoadingSpinner : SvgIcon {
    override val svg = Svg(
        children = svg {
            // Outer circle (static)
            circle(12, 12, 10) {
                strokeOpacity(from = 0.3f, to = 0.3f, dur = 1.milliseconds)
            }

            // Arc that rotates
            path("M12 2a10 10 0 0 1 10 10") {
                rotate(from = 0f, to = 360f, dur = 1.seconds)
            }
        }
    )
}
```

### Multi-Element Icon with Staggered Animation

```kotlin
import kotlin.time.Duration.Companion.milliseconds

object StaggeredCheck : SvgIcon {
    override val svg = Svg(
        children = listOf(
            // Circle appears first
            SvgAnimated(
                element = SvgCircle(12f, 12f, 10f),
                animations = listOf(
                    SvgAnimate.StrokeDraw(dur = 400.milliseconds)
                )
            ),
            // Checkmark appears after circle
            SvgAnimated(
                element = SvgPath("M8 12l3 3 5-6"),
                animations = listOf(
                    SvgAnimate.StrokeDraw(dur = 300.milliseconds, delay = 400.milliseconds)
                )
            )
        )
    )
}
```

### Icon with Custom Stroke Styles

```kotlin
object DashedRect : SvgIcon {
    override val svg = Svg(
        children = listOf(
            SvgStyled(
                element = SvgRect(4f, 4f, 16f, 16f, rx = 2f),
                style = SvgStyle(
                    strokeWidth = 2f,
                    strokeDasharray = listOf(4f, 2f),
                    strokeLinecap = LineCap.ROUND
                )
            )
        )
    )
}
```

### Icon with Transform

```kotlin
object RotatedSquare : SvgIcon {
    override val svg = Svg(
        children = listOf(
            SvgStyled(
                element = SvgRect(6f, 6f, 12f, 12f),
                style = SvgStyle(
                    transform = SvgTransform.Rotate(45f, 12f, 12f),
                    fill = "#22C55E",
                    stroke = "none"
                )
            )
        )
    )
}
```
