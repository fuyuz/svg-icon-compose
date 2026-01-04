package io.github.fuyuz.svgicon.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.Incremental
import org.gradle.work.InputChanges

/**
 * Gradle task that generates Kotlin icon classes from SVG files.
 */
@CacheableTask
abstract class GenerateSvgIconsTask : DefaultTask() {

    @get:Incremental
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val svgDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val visibility: Property<IconVisibility>

    init {
        group = "svgicon"
        description = "Generates SvgIcon implementations from SVG files"
    }

    @TaskAction
    fun generate(inputChanges: InputChanges) {
        val svgDirectory = svgDir.get().asFile
        val outputDirectory = outputDir.get().asFile
        val pkg = packageName.get()
        val vis = visibility.get()

        if (!svgDirectory.exists()) {
            logger.warn("SVG directory does not exist: ${svgDirectory.absolutePath}")
            return
        }

        if (inputChanges.isIncremental) {
            val changes = inputChanges.getFileChanges(svgDir)
            val removedFiles = changes.filter { it.changeType == org.gradle.work.ChangeType.REMOVED }
            removedFiles.forEach { change ->
                val fileName = change.file.nameWithoutExtension
                val generatedFile = outputDirectory.resolve(pkg.replace('.', '/')).resolve("${fileName.capitalize()}.kt")
                if (generatedFile.exists()) {
                    generatedFile.delete()
                }
            }
        } else {
            // Clean output directory for non-incremental build
            outputDirectory.deleteRecursively()
            outputDirectory.mkdirs()
        }

        val svgFiles = svgDirectory.listFiles { file -> file.extension == "svg" } ?: emptyArray()

        if (svgFiles.isEmpty()) {
            logger.warn("No SVG files found in: ${svgDirectory.absolutePath}")
            return
        }

        logger.lifecycle("Generating icons from ${svgDirectory.absolutePath}")

        val generator = IconGenerator(visibility = vis)
        val iconNames = generator.generateIcons(svgDirectory, pkg, outputDirectory)

        logger.lifecycle("Generated ${iconNames.size} icons")
    }
}
