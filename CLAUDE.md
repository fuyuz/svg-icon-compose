# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin Multiplatform library for SVG icons in Jetpack Compose. Supports Android, Desktop/JVM, iOS, JavaScript, and WebAssembly targets. Icons placed in `src/commonMain/svgicons/` are automatically converted to `Icons.xxx` accessors at build time.

## Build Commands

```bash
./gradlew build                       # Full build for all platforms
./gradlew publishToMavenLocal         # Publish to local Maven repository
./gradlew :runtime:compileKotlinDesktop  # Desktop-only compilation (fast iteration)
./gradlew :sample:run                 # Run sample desktop app
```

## Architecture

### Module Structure

- **runtime/** - Core SVG DSL, parser, and Compose rendering
- **processor/** - KSP processor and Gradle plugin for icon generation
- **sample/** - Demo application

### Core Components

- **SvgDsl.kt** - Kotlin DSL representing SVG structure (Svg, SvgPath, SvgCircle, etc.)
- **SvgParser.kt** - Parses SVG XML into DSL structures
- **PathRenderer.kt** - Renders SVG elements to Compose Canvas
- **SvgIcon.kt** - Composable for rendering static icons
- **AnimatedSvgIcon.kt** - Composable for rendering animated icons

### Icon Generation

Icons in `src/commonMain/svgicons/` are processed by the Gradle plugin:
1. SVG files are parsed at build time
2. Kotlin code is generated as `Icons.IconName`
3. Generated code is added to the compilation

## Adding Icons

### Method 1: svgicons directory (Recommended)

Place SVG files in `src/commonMain/svgicons/`:
```
src/commonMain/svgicons/
├── check.svg
├── arrow-right.svg
└── menu.svg
```

Access via generated code:
```kotlin
SvgIcon(icon = Icons.Check, contentDescription = "Check")
```

### Method 2: Inline Definition

```kotlin
object MyIcon : SvgIcon {
    override val svg = Svg(
        children = listOf(
            SvgPath("M20 6L9 17l-5-5"),
            SvgCircle(12f, 12f, 10f)
        )
    )
}
```

## Key Conventions

- Package: `io.github.fuyuz.svgicon`
- All source code in `commonMain/` (no platform-specific code)
- Default SVG canvas is 24x24
- Default stroke width is 2dp
- Use `@Preview` composables for visual testing

## Development Guidelines

- **Update tests when changing implementation** - Any changes to runtime or processor modules must include corresponding test updates
- **Add sample icons for new features** - When adding new SVG element support or animation features, add a simple sample icon to `sample/src/commonMain/svgicons/` to demonstrate the functionality

## Dependencies

Managed via `/gradle/libs.versions.toml`:
- Kotlin 2.1.21
- Compose Multiplatform 1.9.3
- KSP for code generation
- Android: minSdk 24, compileSdk 35

## Versioning

Version is defined in two places (both must be updated together):
- `gradle.properties` - VERSION_NAME for runtime module
- `gradle-plugin/gradle.properties` - VERSION_NAME for gradle-plugin module

## Changelog Management

This project uses [Keep a Changelog](https://keepachangelog.com/) format.

### Format

```markdown
# Changelog

## [Unreleased]
### Added
- New features

### Changed
- Changes in existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Removed features

### Fixed
- Bug fixes

### Security
- Vulnerability fixes

## [X.Y.Z] - YYYY-MM-DD
...
```

### Guidelines

1. **Update on every PR** - Add entries to `[Unreleased]` section when merging features/fixes
2. **Use past tense** - "Added support for..." not "Add support for..."
3. **Link to PRs/issues** - Include references like `(#123)` when relevant
4. **Group by type** - Use the standard categories (Added, Changed, Fixed, etc.)
5. **Release process** - When releasing, move `[Unreleased]` entries to a new version section with date
