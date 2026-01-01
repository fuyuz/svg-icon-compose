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

Place your SVG files in `src/commonMain/composeResources/svg/`:

```
src/commonMain/composeResources/svg/
├── check.svg
├── arrow-right.svg
└── menu.svg
```

### 2. Configure code generation (optional)

The plugin works with sensible defaults. Just apply the plugin and you're done:

```kotlin
plugins {
    id("io.github.fuyuz.svgicon") version "0.1.0"
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/compose/resourceGenerator/kotlin/svgicons"))
        }
    }
}
```

For custom configuration:

```kotlin
import io.github.fuyuz.svgicon.gradle.IconVisibility

svgIcon {
    svgDir = file("src/commonMain/composeResources/svg")  // default
    packageName = "com.example.icons"                      // default: "${project.group}.icons"
    visibility = IconVisibility.PUBLIC                     // default: PUBLIC
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

### Runtime SVG Parsing

For dynamic SVG content, use `parseSvg` to parse SVG strings at runtime:

```kotlin
import io.github.fuyuz.svgicon.SvgIcon
import io.github.fuyuz.svgicon.core.parseSvg

@Composable
fun DynamicIcon() {
    val svg = remember {
        parseSvg("""
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
            </svg>
        """)
    }

    SvgIcon(
        svg = svg,
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
