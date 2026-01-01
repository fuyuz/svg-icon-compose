package io.github.fuyuz.svgicon.sample

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.fuyuz.svgicon.AnimatedSvgIcon
import io.github.fuyuz.svgicon.SvgIcon
import io.github.fuyuz.svgicon.rememberSvgIconAnimationState
import io.github.fuyuz.svgicon.core.AnimationDirection
import io.github.fuyuz.svgicon.core.LineCap
import io.github.fuyuz.svgicon.core.MotionRotate
import io.github.fuyuz.svgicon.core.Svg
import io.github.fuyuz.svgicon.core.SvgAnimate
import io.github.fuyuz.svgicon.core.SvgAnimated
import io.github.fuyuz.svgicon.core.SvgCircle
import io.github.fuyuz.svgicon.core.SvgLine
import io.github.fuyuz.svgicon.core.SvgPath
import io.github.fuyuz.svgicon.core.SvgPolygon
import io.github.fuyuz.svgicon.core.SvgRect
import io.github.fuyuz.svgicon.core.SvgStyle
import io.github.fuyuz.svgicon.core.SvgStyled
import io.github.fuyuz.svgicon.core.SvgTransform
import io.github.fuyuz.svgicon.core.TransformType
import io.github.fuyuz.svgicon.core.parseSvg
import io.github.fuyuz.svgicon.core.svg
import io.github.fuyuz.svgicon.sample.generated.icons.AllIcons
import io.github.fuyuz.svgicon.sample.generated.icons.Icons
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// ============================================
// DSL-defined Icons Examples
// ============================================

/**
 * Simple check icon using DSL.
 */
private val DslCheckIcon = Svg(
    children = listOf(
        SvgPath("M20 6L9 17l-5-5")
    )
)

/**
 * Filled circle with border using SvgStyled.
 */
private val DslFilledCircle = Svg(
    children = listOf(
        SvgStyled(
            element = SvgCircle(12f, 12f, 10f),
            style = SvgStyle(
                fill = Color(0xFF3B82F6).copy(alpha = 0.3f),
                stroke = Color(0xFF3B82F6),
                strokeWidth = 2f
            )
        )
    )
)

/**
 * Icon with custom stroke width and dashed line.
 */
private val DslDashedRect = Svg(
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

/**
 * Icon with transform (rotated square).
 */
private val DslRotatedSquare = Svg(
    stroke = null,  // no stroke
    children = listOf(
        SvgStyled(
            element = SvgRect(6f, 6f, 12f, 12f),
            style = SvgStyle(
                transform = SvgTransform.Rotate(45f, 12f, 12f),
                fill = Color(0xFF22C55E)
            )
        )
    )
)

/**
 * Animated check icon with staggered animation using DSL.
 */
private val DslAnimatedCheck = Svg(
    children = listOf(
        // Circle appears first
        SvgAnimated(
            element = SvgCircle(12f, 12f, 10f),
            animations = listOf(
                SvgAnimate.StrokeDraw(dur = 1.seconds)
            )
        ),
        // Checkmark appears after circle
        SvgAnimated(
            element = SvgPath("M8 12l3 3 5-6"),
            animations = listOf(
                SvgAnimate.StrokeDraw(dur = 500.milliseconds, delay = 1.seconds)
            )
        )
    )
)

/**
 * Icon with inline styles using stroke/fill parameters.
 */
private val DslStyledIcon = Svg(
    children = svg {
        // Circle with custom fill and stroke
        circle(12, 12, 10, fill = Color(0xFF3B82F6).copy(alpha = 0.2f), stroke = Color(0xFF3B82F6))
        // Path with custom stroke color
        path("M8 12l3 3 5-6", stroke = Color(0xFF22C55E), strokeWidth = 3f)
    }
)

/**
 * Icon built with svg {} builder.
 */
private val DslBuilderIcon = Svg(
    children = svg {
        circle(12, 12, 10)
        path("M8 12l3 3 5-6")
    }
)

/**
 * Animated icon using svg {} builder with animation blocks.
 */
private val DslBuilderAnimatedIcon = Svg(
    children = svg {
        // Circle with stroke draw animation
        circle(12, 12, 10) {
            strokeDraw(dur = 1.seconds)
        }
        // Path with delayed stroke draw
        path("M8 12l3 3 5-6") {
            strokeDraw(dur = 500.milliseconds, delay = 1.seconds)
        }
    }
)

/**
 * Rotating element animation.
 */
private val DslRotatingIcon = Svg(
    children = svg {
        // Static outer circle
        circle(12, 12, 10)
        // Rotating inner element
        path("M12 6v6l4 2") {
            rotate(from = 0f, to = 360f, dur = 2.seconds)
        }
    }
)

/**
 * Path morphing animation - triangle to circle.
 * Uses cubic Bézier curves with matching command structure for smooth morphing.
 * Triangle: 3 straight edges (control points on the line)
 * Circle: 3 curved segments (control points creating arc)
 */
private val DslPathMorphIcon = Svg(
    children = listOf(
        SvgAnimated(
            element = SvgPath("M12 2 C12 2 22 20 22 20 C22 20 2 20 2 20 C2 20 12 2 12 2 Z"),
            animations = listOf(
                SvgAnimate.D(
                    // Triangle: top → bottom-right → bottom-left → top (using cubic curves)
                    from = "M12 2 C12 2 22 20 22 20 C22 20 2 20 2 20 C2 20 12 2 12 2 Z",
                    // Circle approximation with 3 segments
                    to = "M12 2 C18 2 22 7 22 12 C22 17 18 22 12 22 C6 22 2 12 12 2 Z",
                    dur = 1.seconds,
                    direction = AnimationDirection.ALTERNATE
                )
            )
        )
    )
)

/**
 * Circle radius animation.
 */
private val DslCircleGrowIcon = Svg(
    children = listOf(
        SvgAnimated(
            element = SvgCircle(12f, 12f, 2f),
            animations = listOf(
                SvgAnimate.R(from = 2f, to = 10f, dur = 800.milliseconds)
            )
        )
    )
)

/**
 * Line animation - X1/Y1/X2/Y2.
 */
private val DslLineAnimIcon = Svg(
    children = listOf(
        SvgAnimated(
            element = SvgLine(4f, 12f, 12f, 12f),
            animations = listOf(
                SvgAnimate.X2(from = 12f, to = 20f, dur = 500.milliseconds),
                SvgAnimate.Y1(from = 12f, to = 4f, dur = 500.milliseconds),
                SvgAnimate.Y2(from = 12f, to = 20f, dur = 500.milliseconds)
            )
        )
    )
)

/**
 * Rect size animation.
 */
private val DslRectGrowIcon = Svg(
    children = listOf(
        SvgAnimated(
            element = SvgRect(10f, 10f, 4f, 4f),
            animations = listOf(
                SvgAnimate.X(from = 10f, to = 4f, dur = 600.milliseconds),
                SvgAnimate.Y(from = 10f, to = 4f, dur = 600.milliseconds),
                SvgAnimate.Width(from = 4f, to = 16f, dur = 600.milliseconds),
                SvgAnimate.Height(from = 4f, to = 16f, dur = 600.milliseconds)
            )
        )
    )
)

/**
 * Motion path animation - element moving along a path.
 */
private val DslMotionPathIcon = Svg(
    children = listOf(
        // Static guide path
        SvgStyled(
            element = SvgPath("M4 12Q12 4 20 12"),
            style = SvgStyle(strokeWidth = 1f, stroke = Color.Gray.copy(alpha = 0.3f))
        ),
        // Moving dot
        SvgAnimated(
            element = SvgCircle(0f, 0f, 2f),
            animations = listOf(
                SvgAnimate.Motion(
                    path = "M4 12Q12 4 20 12",
                    dur = 1.5.seconds,
                    rotate = MotionRotate.AUTO
                )
            )
        )
    )
)

/**
 * Fill opacity animation.
 */
private val DslFillOpacityIcon = Svg(
    children = listOf(
        SvgAnimated(
            element = SvgStyled(
                element = SvgCircle(12f, 12f, 10f),
                style = SvgStyle(fill = Color(0xFF3B82F6))
            ),
            animations = listOf(
                SvgAnimate.FillOpacity(from = 0.2f, to = 1f, dur = 800.milliseconds)
            )
        )
    )
)

/**
 * Stroke dashoffset animation (marching ants).
 */
private val DslDashOffsetIcon = Svg(
    children = listOf(
        SvgAnimated(
            element = SvgStyled(
                element = SvgCircle(12f, 12f, 8f),
                style = SvgStyle(strokeDasharray = listOf(4f, 4f))
            ),
            animations = listOf(
                SvgAnimate.StrokeDashoffset(from = 0f, to = 16f, dur = 1.seconds)
            )
        )
    )
)

/**
 * Skew animation.
 */
private val DslSkewAnimIcon = Svg(
    children = listOf(
        SvgAnimated(
            element = SvgRect(6f, 6f, 12f, 12f),
            animations = listOf(
                SvgAnimate.Transform(
                    type = TransformType.SKEW_X,
                    from = 0f,
                    to = 15f,
                    dur = 500.milliseconds
                )
            )
        )
    )
)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SVG Icon Sample"
    ) {
        App()
    }
}

@Composable
fun App() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "SVG Icon Library Sample",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Text(
                    "Icons are auto-generated from composeResources/svg/:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AllIcons.entries.forEach { (name, svg) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SvgIcon(
                                svg = svg,
                                contentDescription = name,
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                            Text(
                                name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Text(
                    "Access via Icons.xxx:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = Icons.Check,
                            contentDescription = "Check",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF22C55E)
                        )
                        Text("Icons.Check", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = Icons.ArrowRight,
                            contentDescription = "ArrowRight",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Text("Icons.ArrowRight", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = Icons.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFF59E0B)
                        )
                        Text("Icons.Search", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "Different sizes:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    listOf(16.dp, 24.dp, 32.dp, 48.dp, 64.dp).forEach { iconSize ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SvgIcon(
                                svg = Icons.Check,
                                contentDescription = "Check",
                                modifier = Modifier.size(iconSize),
                                tint = Color(0xFF22C55E)
                            )
                            Text(
                                "${iconSize.value.toInt()}dp",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Text(
                    "Different colors:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        Color.White,
                        Color(0xFFEF4444),
                        Color(0xFFF59E0B),
                        Color(0xFF22C55E),
                        Color(0xFF3B82F6),
                        Color(0xFF8B5CF6)
                    ).forEach { color ->
                        SvgIcon(
                            svg = Icons.ArrowRight,
                            contentDescription = "Arrow Right",
                            modifier = Modifier.size(32.dp),
                            tint = color
                        )
                    }
                }

                Text(
                    "Animated icons (infinite loop):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Loader - rotation animation (infinite)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = Icons.Loader,
                            contentDescription = "Loader",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Text("Loader", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Heart Pulse - stroke animation (infinite)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = Icons.HeartPulse,
                            contentDescription = "Heart Pulse",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Text("HeartPulse", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Bell Ring - shake animation (infinite)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = Icons.BellRing,
                            contentDescription = "Bell Ring",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFF59E0B)
                        )
                        Text("BellRing", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "Click to animate (single iteration with callback):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Click-to-animate Bell
                    var bellAnimating by remember { mutableStateOf(false) }
                    var bellKey by remember { mutableStateOf(0) }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            bellAnimating = true
                            bellKey++
                        }
                    ) {
                        key(bellKey) {
                            AnimatedSvgIcon(
                                svg = Icons.BellRing,
                                contentDescription = "Click to ring",
                                modifier = Modifier.size(48.dp),
                                tint = if (bellAnimating) Color(0xFFF59E0B) else Color.Gray,
                                animate = bellAnimating,
                                iterations = 3,
                                onAnimationEnd = { bellAnimating = false }
                            )
                        }
                        Text(
                            if (bellAnimating) "Ringing..." else "Click me!",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (bellAnimating) Color(0xFFF59E0B) else Color.Gray
                        )
                    }

                    // Click-to-animate Loader
                    var loaderAnimating by remember { mutableStateOf(false) }
                    var loaderKey by remember { mutableStateOf(0) }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            loaderAnimating = true
                            loaderKey++
                        }
                    ) {
                        key(loaderKey) {
                            AnimatedSvgIcon(
                                svg = Icons.Loader,
                                contentDescription = "Click to load",
                                modifier = Modifier.size(48.dp),
                                tint = if (loaderAnimating) Color(0xFF3B82F6) else Color.Gray,
                                animate = loaderAnimating,
                                iterations = 2,
                                onAnimationEnd = { loaderAnimating = false }
                            )
                        }
                        Text(
                            if (loaderAnimating) "Loading..." else "Click me!",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (loaderAnimating) Color(0xFF3B82F6) else Color.Gray
                        )
                    }
                }

                Text(
                    "Press and hold (external animation state):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reverse animation on release
                    val reverseAnimationState = rememberSvgIconAnimationState()
                    val reverseScope = rememberCoroutineScope()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    reverseScope.launch {
                                        reverseAnimationState.animateTo(1f, durationMillis = 300)
                                    }
                                    tryAwaitRelease()
                                    reverseScope.launch {
                                        reverseAnimationState.animateTo(0f, durationMillis = 300)
                                    }
                                }
                            )
                        }
                    ) {
                        AnimatedSvgIcon(
                            svg = DslAnimatedCheck,
                            animationState = reverseAnimationState,
                            contentDescription = "Press to animate",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF22C55E)
                        )
                        Text(
                            "Reverse on release",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    // Snap back on release
                    val snapAnimationState = rememberSvgIconAnimationState()
                    val snapScope = rememberCoroutineScope()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    snapScope.launch {
                                        snapAnimationState.animateTo(1f, durationMillis = 300)
                                    }
                                    tryAwaitRelease()
                                    snapScope.launch {
                                        snapAnimationState.snapTo(0f)
                                    }
                                }
                            )
                        }
                    ) {
                        AnimatedSvgIcon(
                            svg = DslAnimatedCheck,
                            animationState = snapAnimationState,
                            contentDescription = "Press to animate",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Text(
                            "Snap on release",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    "Transform examples (skewX, skewY, matrix):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Original (no transform)
                    val originalSvg = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <rect x="4" y="4" width="16" height="16" rx="2"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = originalSvg,
                            contentDescription = "Original",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        Text("Original", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // skewX transform
                    val skewXSvg = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <g transform="skewX(15)">
                                    <rect x="4" y="4" width="16" height="16" rx="2"/>
                                </g>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = skewXSvg,
                            contentDescription = "SkewX",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Text("skewX(15)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // skewY transform
                    val skewYSvg = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <g transform="skewY(15)">
                                    <rect x="4" y="4" width="16" height="16" rx="2"/>
                                </g>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = skewYSvg,
                            contentDescription = "SkewY",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF22C55E)
                        )
                        Text("skewY(15)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // matrix transform (combined scale + skew)
                    val matrixSvg = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <g transform="matrix(1, 0.2, -0.2, 1, 0, 0)">
                                    <rect x="4" y="4" width="16" height="16" rx="2"/>
                                </g>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = matrixSvg,
                            contentDescription = "Matrix",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFF59E0B)
                        )
                        Text("matrix()", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "DSL-defined icons (Kotlin code):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simple DSL check
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = DslCheckIcon,
                            contentDescription = "DSL Check",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF22C55E)
                        )
                        Text("SvgPath", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Filled circle with border
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = DslFilledCircle,
                            contentDescription = "Filled Circle",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        Text("SvgStyled", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Dashed rectangle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = DslDashedRect,
                            contentDescription = "Dashed Rect",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFF59E0B)
                        )
                        Text("Dashed", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Rotated square
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = DslRotatedSquare,
                            contentDescription = "Rotated Square",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        Text("Transform", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // svg {} builder icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = DslBuilderIcon,
                            contentDescription = "Builder Icon",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF8B5CF6)
                        )
                        Text("svg {}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Styled icon with stroke/fill params
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = DslStyledIcon,
                            contentDescription = "Styled Icon",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        Text("stroke/fill", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "DSL animated icons:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Animated check (staggered)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslAnimatedCheck,
                            contentDescription = "Animated Check",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF22C55E)
                        )
                        Text("Staggered", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Builder animated
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslBuilderAnimatedIcon,
                            contentDescription = "Builder Animated",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Text("Builder", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Rotating icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslRotatingIcon,
                            contentDescription = "Rotating",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFF59E0B)
                        )
                        Text("Rotate", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "Advanced animations (path morph, geometry, motion):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Path morphing
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslPathMorphIcon,
                            contentDescription = "Path Morph",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF8B5CF6)
                        )
                        Text("Path Morph", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Circle radius
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslCircleGrowIcon,
                            contentDescription = "Circle Grow",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF22C55E)
                        )
                        Text("Circle R", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Rect grow
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslRectGrowIcon,
                            contentDescription = "Rect Grow",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Text("Rect XY/WH", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Line animation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslLineAnimIcon,
                            contentDescription = "Line Anim",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFF59E0B)
                        )
                        Text("Line X1Y1X2Y2", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Motion path
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslMotionPathIcon,
                            contentDescription = "Motion Path",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Text("Motion Path", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "Stroke & opacity animations:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fill opacity
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslFillOpacityIcon,
                            contentDescription = "Fill Opacity",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("FillOpacity", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Dash offset (marching ants)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslDashOffsetIcon,
                            contentDescription = "Dash Offset",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Text("DashOffset", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Skew animation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = DslSkewAnimIcon,
                            contentDescription = "Skew",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF22C55E)
                        )
                        Text("SkewX", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "Runtime SVG parsing (parseSvg):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Star icon parsed from SVG string
                    val starSvg = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = starSvg,
                            contentDescription = "Star",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFF59E0B)
                        )
                        Text("Star (string)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Heart icon parsed from SVG string
                    val heartSvg = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = heartSvg,
                            contentDescription = "Heart",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Text("Heart (string)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Circle icon parsed from SVG string
                    val circleSvg = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="10"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = circleSvg,
                            contentDescription = "Circle",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF22C55E)
                        )
                        Text("Circle (string)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "Inline CSS style attribute:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Inline style: fill color
                    val inlineStyleFill = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <circle cx="12" cy="12" r="10" style="fill:#3B82F6; stroke:#1E40AF; stroke-width:2"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = inlineStyleFill,
                            contentDescription = "Inline Fill",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("style=\"fill:...\"", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Inline style: multiple properties
                    val inlineStyleMultiple = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <rect x="4" y="4" width="16" height="16" rx="2" style="fill:#22C55E; stroke:#166534; stroke-width:2; opacity:0.8"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = inlineStyleMultiple,
                            contentDescription = "Multiple Styles",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("Multiple props", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Inline style: stroke-dasharray
                    val inlineStyleDashed = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <circle cx="12" cy="12" r="9" style="fill:none; stroke:#F59E0B; stroke-width:2; stroke-dasharray:4,2"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = inlineStyleDashed,
                            contentDescription = "Dashed",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("stroke-dasharray", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // CSS overrides XML attribute
                    val cssOverride = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <path d="M20 6L9 17l-5-5" stroke="blue" style="stroke:#EF4444; stroke-width:3; stroke-linecap:round"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = cssOverride,
                            contentDescription = "CSS Override",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("CSS > XML", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "Internal stylesheet (<style> tag):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Class selector
                    val classSelector = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <style>.blue-fill { fill: #3B82F6; } .red-stroke { stroke: #EF4444; stroke-width: 2; }</style>
                                <circle cx="12" cy="12" r="10" class="blue-fill red-stroke"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = classSelector,
                            contentDescription = "Class Selector",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text(".class", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // ID selector
                    val idSelector = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <style>#my-rect { fill: #22C55E; stroke: #166534; stroke-width: 2; }</style>
                                <rect x="4" y="4" width="16" height="16" rx="2" id="my-rect"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = idSelector,
                            contentDescription = "ID Selector",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("#id", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Tag selector
                    val tagSelector = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <style>path { stroke: #F59E0B; stroke-width: 3; fill: none; }</style>
                                <path d="M20 6L9 17l-5-5"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = tagSelector,
                            contentDescription = "Tag Selector",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("tag", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Universal selector
                    val universalSelector = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <style>* { stroke: #8B5CF6; stroke-width: 2; fill: none; }</style>
                                <circle cx="12" cy="12" r="8"/>
                                <path d="M12 4v4M12 16v4M4 12h4M16 12h4"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            svg = universalSelector,
                            contentDescription = "Universal Selector",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("*", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Text(
                    "CSS @keyframes animation:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Spin animation
                    val spinAnimation = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <style>
                                    @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
                                    .spinner { animation: spin 1s linear; stroke: #3B82F6; stroke-width: 2; fill: none; }
                                </style>
                                <circle cx="12" cy="12" r="10" class="spinner"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = spinAnimation,
                            contentDescription = "Spin",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("rotate", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Fade animation
                    val fadeAnimation = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <style>
                                    @keyframes fade { from { opacity: 0; } to { opacity: 1; } }
                                    .fading { animation: fade 1s ease; fill: #22C55E; }
                                </style>
                                <circle cx="12" cy="12" r="10" class="fading"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = fadeAnimation,
                            contentDescription = "Fade",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("opacity", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Scale animation
                    val scaleAnimation = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <style>
                                    @keyframes grow { from { transform: scale(0.5); } to { transform: scale(1); } }
                                    .growing { animation: grow 1s ease-out; stroke: #F59E0B; stroke-width: 2; fill: none; }
                                </style>
                                <rect x="4" y="4" width="16" height="16" rx="2" class="growing"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = scaleAnimation,
                            contentDescription = "Scale",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("scale", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Stroke width animation
                    val strokeAnimation = remember {
                        parseSvg("""
                            <svg viewBox="0 0 24 24">
                                <style>
                                    @keyframes pulse { from { stroke-width: 1; } to { stroke-width: 4; } }
                                    .pulsing { animation: pulse 500ms ease-in-out; stroke: #EF4444; fill: none; }
                                </style>
                                <circle cx="12" cy="12" r="10" class="pulsing"/>
                            </svg>
                        """.trimIndent())
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            svg = strokeAnimation,
                            contentDescription = "Stroke Width",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Text("stroke-width", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
