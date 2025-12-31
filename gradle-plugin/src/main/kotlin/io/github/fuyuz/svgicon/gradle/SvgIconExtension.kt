package io.github.fuyuz.svgicon.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Extension for configuring SVG icon generation.
 *
 * Usage in build.gradle.kts:
 * ```kotlin
 * svgIcon {
 *     svgDir.set(file("src/commonMain/composeResources/svg"))
 *     packageName.set("com.example.icons")
 *     visibility.set("public") // or "internal"
 * }
 * ```
 */
abstract class SvgIconExtension {
    /**
     * Directory containing SVG files to process.
     */
    abstract val svgDir: DirectoryProperty

    /**
     * Package name for generated icon classes.
     */
    abstract val packageName: Property<String>

    /**
     * Visibility modifier for generated classes ("public" or "internal").
     * Defaults to "public".
     */
    abstract val visibility: Property<String>
}
