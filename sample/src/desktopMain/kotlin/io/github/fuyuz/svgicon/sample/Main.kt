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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.fuyuz.svgicon.AnimatedSvgIcon
import io.github.fuyuz.svgicon.SvgIcon
import io.github.fuyuz.svgicon.core.parseSvg
import io.github.fuyuz.svgicon.sample.generated.icons.AllIcons
import io.github.fuyuz.svgicon.sample.generated.icons.Icons

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
                    AllIcons.entries.forEach { (name, icon) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SvgIcon(
                                icon = icon,
                                contentDescription = name,
                                tint = Color.White,
                                size = 48.dp
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
                            icon = Icons.Check,
                            contentDescription = "Check",
                            tint = Color(0xFF22C55E),
                            size = 48.dp
                        )
                        Text("Icons.Check", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            icon = Icons.ArrowRight,
                            contentDescription = "ArrowRight",
                            tint = Color(0xFF3B82F6),
                            size = 48.dp
                        )
                        Text("Icons.ArrowRight", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SvgIcon(
                            icon = Icons.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFF59E0B),
                            size = 48.dp
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
                    listOf(16.dp, 24.dp, 32.dp, 48.dp, 64.dp).forEach { size ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SvgIcon(
                                icon = Icons.Check,
                                contentDescription = "Check",
                                tint = Color(0xFF22C55E),
                                size = size
                            )
                            Text(
                                "${size.value.toInt()}dp",
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
                            icon = Icons.ArrowRight,
                            contentDescription = "Arrow Right",
                            tint = color,
                            size = 32.dp
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
                            icon = Icons.Loader,
                            contentDescription = "Loader",
                            tint = Color(0xFF3B82F6),
                            size = 48.dp
                        )
                        Text("Loader", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Heart Pulse - stroke animation (infinite)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            icon = Icons.HeartPulse,
                            contentDescription = "Heart Pulse",
                            tint = Color(0xFFEF4444),
                            size = 48.dp
                        )
                        Text("HeartPulse", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Bell Ring - shake animation (infinite)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedSvgIcon(
                            icon = Icons.BellRing,
                            contentDescription = "Bell Ring",
                            tint = Color(0xFFF59E0B),
                            size = 48.dp
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
                                icon = Icons.BellRing,
                                contentDescription = "Click to ring",
                                tint = if (bellAnimating) Color(0xFFF59E0B) else Color.Gray,
                                size = 48.dp,
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
                                icon = Icons.Loader,
                                contentDescription = "Click to load",
                                tint = if (loaderAnimating) Color(0xFF3B82F6) else Color.Gray,
                                size = 48.dp,
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
                            tint = Color.White,
                            size = 48.dp
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
                            tint = Color(0xFF3B82F6),
                            size = 48.dp
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
                            tint = Color(0xFF22C55E),
                            size = 48.dp
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
                            tint = Color(0xFFF59E0B),
                            size = 48.dp
                        )
                        Text("matrix()", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                            tint = Color(0xFFF59E0B),
                            size = 48.dp
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
                            tint = Color(0xFFEF4444),
                            size = 48.dp
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
                            tint = Color(0xFF22C55E),
                            size = 48.dp
                        )
                        Text("Circle (string)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
