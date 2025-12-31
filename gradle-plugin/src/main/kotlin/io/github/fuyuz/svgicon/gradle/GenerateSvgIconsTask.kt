package io.github.fuyuz.svgicon.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task that generates Kotlin icon classes from SVG files.
 */
abstract class GenerateSvgIconsTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val svgDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val visibility: Property<String>

    init {
        group = "svgicon"
        description = "Generates SvgIcon implementations from SVG files"
    }

    @TaskAction
    fun generate() {
        val svgDirectory = svgDir.get().asFile
        val outputDirectory = outputDir.get().asFile
        val pkg = packageName.get()
        val vis = visibility.getOrElse("public")

        if (!svgDirectory.exists()) {
            logger.warn("SVG directory does not exist: ${svgDirectory.absolutePath}")
            return
        }

        // Clean output directory
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()

        val svgFiles = svgDirectory.listFiles { file -> file.extension == "svg" } ?: emptyArray()

        if (svgFiles.isEmpty()) {
            logger.warn("No SVG files found in: ${svgDirectory.absolutePath}")
            return
        }

        logger.lifecycle("Generating icons from ${svgDirectory.absolutePath}")

        val iconVisibility = when (vis.lowercase()) {
            "public" -> IconVisibility.PUBLIC
            else -> IconVisibility.INTERNAL
        }

        val generator = IconGenerator(visibility = iconVisibility)
        val iconNames = generator.generateIcons(svgDirectory, pkg, outputDirectory)

        logger.lifecycle("Generated ${iconNames.size} icons")
    }
}
