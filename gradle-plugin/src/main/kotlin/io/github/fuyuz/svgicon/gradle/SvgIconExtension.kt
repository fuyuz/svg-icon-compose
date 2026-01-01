package io.github.fuyuz.svgicon.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Visibility modifier for generated icon classes.
 */
enum class IconVisibility {
    PUBLIC,
    INTERNAL
}

/**
 * Extension for configuring SVG icon generation.
 *
 * All properties have sensible defaults, so minimal configuration is needed:
 * ```kotlin
 * plugins {
 *     id("io.github.fuyuz.svgicon")
 * }
 * // That's it! Uses default svgDir and packageName
 * ```
 *
 * Custom configuration:
 * ```kotlin
 * svgIcon {
 *     svgDir.set(file("src/commonMain/composeResources/svg"))
 *     packageName.set("com.example.icons")
 *     visibility.set(IconVisibility.INTERNAL)
 * }
 * ```
 */
abstract class SvgIconExtension {
    /**
     * Directory containing SVG files to process.
     * Defaults to "src/commonMain/composeResources/svg".
     */
    abstract val svgDir: DirectoryProperty

    /**
     * Package name for generated icon classes.
     * Defaults to "${project.group}.icons".
     */
    abstract val packageName: Property<String>

    /**
     * Visibility modifier for generated classes.
     * Defaults to [IconVisibility.PUBLIC].
     */
    abstract val visibility: Property<IconVisibility>
}
