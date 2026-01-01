package io.github.fuyuz.svgicon.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin for generating SvgIcon implementations from SVG files.
 *
 * Apply this plugin in your build.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("io.github.fuyuz.svgicon")
 * }
 * ```
 *
 * Optional configuration:
 * ```kotlin
 * svgIcon {
 *     svgDir.set(file("src/commonMain/svgicons"))  // default
 *     packageName.set("${project.group}.icons")     // default
 *     visibility.set(IconVisibility.PUBLIC)         // default
 * }
 * ```
 */
class SvgIconPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Create extension
        val extension = project.extensions.create("svgIcon", SvgIconExtension::class.java)

        // Set default values
        extension.svgDir.convention(project.layout.projectDirectory.dir("src/commonMain/svgicons"))
        extension.packageName.convention(project.provider { "${project.group}.icons" })
        extension.visibility.convention(IconVisibility.PUBLIC)

        // Default output directory
        val outputDir = project.layout.buildDirectory.dir("generated/compose/resourceGenerator/kotlin/svgicons")

        // Register the generation task
        val generateTask = project.tasks.register("generateSvgIcons", GenerateSvgIconsTask::class.java) { task ->
            task.svgDir.set(extension.svgDir)
            task.outputDir.set(outputDir)
            task.packageName.set(extension.packageName)
            task.visibility.set(extension.visibility)
        }

        // Hook into Kotlin compilation
        project.afterEvaluate {
            project.tasks.matching { it.name.startsWith("compileKotlin") }.configureEach { compileTask ->
                compileTask.dependsOn(generateTask)
            }

            // Add generated sources to source sets
            project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                val kotlinExtension = project.extensions.findByName("kotlin")
                if (kotlinExtension != null) {
                    try {
                        val sourceSets = kotlinExtension.javaClass.getMethod("getSourceSets").invoke(kotlinExtension)
                        val commonMain = sourceSets.javaClass.getMethod("getByName", String::class.java).invoke(sourceSets, "commonMain")
                        val kotlin = commonMain.javaClass.getMethod("getKotlin").invoke(commonMain)
                        kotlin.javaClass.getMethod("srcDir", Any::class.java).invoke(kotlin, outputDir)
                    } catch (e: Exception) {
                        project.logger.warn("Failed to add generated sources to commonMain: ${e.message}")
                    }
                }
            }

            project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                val sourceSets = project.extensions.findByName("sourceSets")
                if (sourceSets != null) {
                    try {
                        val main = sourceSets.javaClass.getMethod("getByName", String::class.java).invoke(sourceSets, "main")
                        val kotlin = main.javaClass.getMethod("getKotlin").invoke(main)
                        kotlin.javaClass.getMethod("srcDir", Any::class.java).invoke(kotlin, outputDir)
                    } catch (e: Exception) {
                        project.logger.warn("Failed to add generated sources to main: ${e.message}")
                    }
                }
            }
        }
    }
}
