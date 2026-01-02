# SVG Icon Compose

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.21-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.9.3-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)

A Kotlin Multiplatform library for rendering SVG icons in Compose Multiplatform applications with compile-time code generation.

## Supported Platforms

| Platform | Support |
|----------|---------|
| Android  | ✅      |
| Desktop (JVM) | ✅ |
| iOS      | ✅      |
| Web (JS) | ✅      |
| Web (Wasm) | ✅    |

## Installation

### Release Version

In your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

In your `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.fuyuz.svgicon") version "0.1.0"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.fuyuz.svgicon:runtime:0.1.0")
        }
    }
}
```

### Snapshot Version

For the latest development version, add the snapshot repository:

In your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}
```

In your `build.gradle.kts`:

```kotlin
plugins {
    id("io.github.fuyuz.svgicon") version "0.1.1-SNAPSHOT"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.fuyuz.svgicon:runtime:0.1.1-SNAPSHOT")
        }
    }
}
```

## Quick Start

### 1. Add SVG files

Place your SVG files in `src/commonMain/svgicons/`:

```
src/commonMain/svgicons/
├── check.svg
├── arrow-right.svg
└── menu.svg
```

### 2. Configure code generation (optional)

The plugin works with sensible defaults and automatically configures source sets. For custom configuration:

```kotlin
import io.github.fuyuz.svgicon.gradle.IconVisibility

svgIcon {
    svgDir = file("src/commonMain/svgicons")  // default
    packageName = "com.example.icons"          // default: "${project.group}.icons"
    visibility = IconVisibility.PUBLIC         // default: PUBLIC
}
```

The plugin automatically generates icon classes before Kotlin compilation.

### 3. Use in Compose

```kotlin
import io.github.fuyuz.svgicon.SvgIcon
import your.package.generated.icons.Icons

@Composable
fun MyScreen() {
    SvgIcon(
        icon = Icons.Check,
        contentDescription = "Check",
        tint = Color.Green,
        size = 24.dp
    )
}
```

### DSL Builder

Create SVG icons directly in Kotlin with the type-safe DSL:

```kotlin
import io.github.fuyuz.svgicon.core.*
import io.github.fuyuz.svgicon.core.dsl.*

// Simple icon
val checkIcon = svg {
    path("M20 6L9 17l-5-5")
}

// Infix styled syntax
val styledIcon = svg {
    circle(12, 12, 10) styled { fill = Color.Blue.withAlpha(0.3f) }
    path("M8 12l3 3 5-6") styled {
        stroke = Color.Green
        strokeWidth = 3f
    }
}

// Shared styles with withStyle
val multiElementIcon = svg {
    withStyle(stroke = Color.Blue, strokeWidth = 2f, strokeLinecap = LineCap.ROUND) {
        path("M12 4v4")
        path("M12 16v4")
        path("M4 12h4")
        path("M16 12h4")
    }
}

// Transform DSL with operators
val transformedIcon = svg {
    rect(4, 4, 8, 8) styled {
        fill = SvgColors.Success
        transform = rotate(45, 8 to 8) + scale(1.2)
    }
}

// Color extensions
val colorfulIcon = svg {
    circle(8, 8, 5) styled { fill = "#3B82F6".toSvgColor() }
    circle(16, 16, 5) styled { fill = SvgColors.Error.withAlpha(0.6f) }
}

// ViewBox convenience
val customViewBox = svg(viewBox = "0 0 100 100".toViewBox()) {
    circle(50, 50, 40)
}
```

### Animated Icons with Presets

```kotlin
import io.github.fuyuz.svgicon.core.dsl.Animations

// Using animation presets
val fadeInIcon = svg {
    circle(12, 12, 10) with Animations.fadeIn
}

// Custom animations with infix syntax
val animatedCheck = svg {
    circle(12, 12, 10) animated { strokeDraw(dur = 1.seconds) }
    path("M8 12l3 3 5-6") animated {
        strokeDraw(dur = 500.milliseconds, delay = 1.seconds)
    }
}

// Available presets
Animations.fadeIn          // Fade from transparent to opaque
Animations.fadeOut         // Fade from opaque to transparent
Animations.spin            // Continuous rotation
Animations.pulse           // Scale up and down
Animations.shake           // Rotate back and forth
Animations.bounce          // Translate up and down
Animations.scaleIn         // Scale from 0 to 1
Animations.strokeDraw()    // Draw stroke progressively
```

### Path Morphing (Type-Safe)

```kotlin
// morphTo with string - only available for path elements (compile-time checked)
val morphIcon = svg {
    path("M10 10 L20 20") animated {
        morphTo("M5 15 L25 15", dur = 500.milliseconds)
    }
}

// morphTo with path builder - type-safe target path
val morphBuilderIcon = svg {
    path("M10 10 L20 20") animated {
        morphTo(dur = 500.milliseconds) {
            moveTo(5f, 15f)
            lineTo(25f, 15f)
        }
    }
}

// morphPointsTo for polygon/polyline
val morphPolygon = svg {
    polygon(12 to 2, 22 to 22, 2 to 22) animated {
        morphPointsTo(
            to = listOf(Offset(6f, 12f), Offset(18f, 12f), Offset(12f, 22f)),
            dur = 500.milliseconds
        )
    }
}
```

### Symbol and Use Elements

```kotlin
val iconWithSymbol = svg {
    defs {
        symbol(id = "checkIcon") {
            circle(12, 12, 10)
            path("M8 12l3 3 5-6")
        }
    }
    use(href = "#checkIcon", x = 0, y = 0)
    use(href = "#checkIcon", x = 30, y = 0)
}
```

### Animated Icons

Use `AnimatedSvgIcon` for icons with SMIL animations:

```kotlin
@Composable
fun AnimatedIcon() {
    AnimatedSvgIcon(
        svg = animatedCheck,
        contentDescription = "Animated Check",
        modifier = Modifier.size(48.dp),
        iterations = 1,  // or Int.MAX_VALUE for infinite
        onAnimationEnd = { /* callback */ }
    )
}
```

### Runtime SVG Parsing

For dynamic SVG content, use `svg` to parse SVG strings at runtime:

```kotlin
import io.github.fuyuz.svgicon.SvgIcon
import io.github.fuyuz.svgicon.core.svg

@Composable
fun DynamicIcon() {
    val icon = remember {
        svg("""
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """)
    }

    SvgIcon(
        svg = icon,
        contentDescription = "Circle",
        tint = Color.Blue,
        size = 48.dp
    )
}
```

## SVG Support

### Elements

| Element | Supported |
|---------|-----------|
| `<path>` | ✅ All commands (M, L, H, V, C, S, Q, T, A, Z) |
| `<circle>` | ✅ |
| `<ellipse>` | ✅ |
| `<rect>` | ✅ (including rx, ry) |
| `<line>` | ✅ |
| `<polyline>` | ✅ |
| `<polygon>` | ✅ |
| `<text>` | ✅ (fontSize, fontWeight, fontFamily, textAnchor, dominantBaseline) |
| `<g>` | ✅ (groups) |

### Attributes

| Attribute | Supported |
|-----------|-----------|
| `fill`, `stroke` | ✅ |
| `stroke-width` | ✅ |
| `stroke-linecap`, `stroke-linejoin` | ✅ |
| `opacity`, `fill-opacity`, `stroke-opacity` | ✅ |
| `transform` | ✅ (translate, scale, rotate, skewX, skewY, matrix) |
| `stroke-dasharray`, `stroke-dashoffset` | ✅ |
| `fill-rule` | ✅ |

## Modules

| Module | Description |
|--------|-------------|
| `runtime` | Core library with `SvgIcon` composable and SVG rendering |
| `gradle-plugin` | Gradle plugin for SVG to Kotlin code generation |

## Building from Source

```bash
# Build all modules
./gradlew build

# Run tests
./gradlew check

# Run sample application
./gradlew :sample:run

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

## Requirements

- Kotlin 2.1.21+
- Compose Multiplatform 1.9.3+
- Java 17+

## License

MIT License
