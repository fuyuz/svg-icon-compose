package io.github.fuyuz.svgicon.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

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

        // Hook into Kotlin compilation - use KotlinCompilationTask to match all Kotlin compile tasks
        // including Android variants (compileDebugKotlin, compileReleaseKotlin, etc.)
        // No afterEvaluate needed - withType().configureEach is lazy
        project.tasks.withType(KotlinCompilationTask::class.java).configureEach { compileTask ->
            compileTask.dependsOn(generateTask)
        }

        // Add generated sources to source sets for Kotlin Multiplatform
        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kotlinExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
            kotlinExtension.sourceSets.named("commonMain") { sourceSet ->
                // Passing task provider automatically sets up task dependency
                sourceSet.kotlin.srcDir(generateTask.map { it.outputDir })
            }
        }

        // Add generated sources to source sets for Kotlin JVM
        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            val kotlinExtension = project.extensions.getByType(KotlinProjectExtension::class.java)
            kotlinExtension.sourceSets.named("main") { sourceSet ->
                sourceSet.kotlin.srcDir(generateTask.map { it.outputDir })
            }
        }
    }
}
