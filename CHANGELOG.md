# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Implemented `paint-order` attribute support for controlling fill/stroke drawing order (#40)
- Added support for `inherit` keyword in style attributes (fill, stroke, opacity, stroke-width, etc.) (#61)
- Added `strokeMiterlimit` property to `Svg` class with default value of 4 per SVG spec (#35)

### Changed

- Aligned default fill behavior with SVG spec: `fill` now defaults to `black` instead of `none` (#34)
- Aligned default stroke properties with SVG spec (#35):
  - `stroke` defaults to `none` (null) instead of `currentColor`
  - `stroke-width` defaults to `1` instead of `2`
  - `stroke-linecap` defaults to `butt` instead of `round`
  - `stroke-linejoin` defaults to `miter` instead of `round`
  - `stroke-miterlimit` defaults to `4`

### Fixed

- Fixed `fill="none"` and `stroke="none"` being treated the same as inherit; now correctly results in no painting (#61)
- Fixed generated color code using incorrect Color constructor (ULong instead of Int) (#61)
- Applied fill-rule to static path rendering to match animated rendering behavior (#47)
- Fixed stroke-dasharray odd-length handling to repeat array per SVG spec (#48)
- Applied clip-path to static rendering to match animated rendering behavior (#36)
- Fixed SvgAnimated elements not rendering in static SvgIcon

## [0.1.1] - 2026-01-10

### Added

- Added `AdditiveMode` enum for animateTransform additive attribute support (#31)
- Added `fromY` and `toY` properties to `SvgAnimate.Transform` for separate X/Y translate values

### Fixed

- Fixed animateTransform translate type only using X value, ignoring Y (#30)
- Fixed animateTransform additive="sum" not being supported (#31)

## [0.1.0] - 2025-01-04

Initial release of SVG Icon Compose library.

### Added

- **Core SVG rendering** - Render SVG icons in Jetpack Compose with `SvgIcon` composable
- **Animation support** - Animate SVG elements with `AnimatedSvgIcon` composable
- **Kotlin DSL** - Type-safe DSL for defining SVG icons programmatically
  - `svg { }` builder with path, circle, rect, ellipse, line, polyline, polygon, text elements
  - Style scopes and `styled` infix syntax for fluent element styling
  - Duration, Color, Offset, and PathBuilder support
- **Auto-generation** - Gradle plugin to generate `Icons.xxx` accessors from SVG files in `src/commonMain/svgicons/`
- **Animation features**
  - External animation state control with Lottie-style API (`rememberSvgIconAnimationState`)
  - Animation presets and type-safe animation extensions
  - Path morphing with `morphTo` animation
  - Stroke draw animation for progressive stroke reveal
  - Support for `animation-direction` and `animation-fill-mode`
  - CSS animation and internal stylesheet support
- **SVG parsing**
  - Inline CSS style attribute support
  - Symbol and Use element rendering
  - Viewport and `preserveAspectRatio` support
- **Multiplatform support** - Android, Desktop/JVM, iOS, JavaScript, and WebAssembly targets
- **Kotlin context parameters** - Modern Kotlin 2.1 context parameters for DrawScope functions

[0.1.1]: https://github.com/fuyuz/svg-icon-compose/releases/tag/v0.1.1
[0.1.0]: https://github.com/fuyuz/svg-icon-compose/releases/tag/v0.1.0
