package io.github.fuyuz.svgicon.gradle

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.fuyuz.svgicon.core.SvgCodeGenerator
import java.io.File

/**
 * Generates Kotlin icon files from SVG files using KotlinPoet.
 *
 * Generated structure:
 * ```kotlin
 * // Icons.kt
 * object Icons {
 *     val Check: Svg get() = _Check
 *     val Menu: Svg get() = _Menu
 * }
 *
 * private val _Check: Svg = Svg(...)
 * private val _Menu: Svg = Svg(...)
 * ```
 */
class IconGenerator(
    private val visibility: IconVisibility = IconVisibility.INTERNAL
) {
    private val svgClass = ClassName("io.github.fuyuz.svgicon.core", "Svg")

    fun generateIcons(svgDir: File, packageName: String, outputDir: File): List<String> {
        val iconNames = mutableListOf<String>()
        val svgCodeBlocks = mutableMapOf<String, CodeBlock>()

        svgDir.listFiles { file -> file.extension == "svg" }?.forEach { svgFile ->
            try {
                val iconName = svgFile.nameWithoutExtension.toPascalCase().sanitizeIconName()
                val svgContent = svgFile.readText()
                val svgCodeBlock = SvgCodeGenerator.generateSvgCodeBlock(svgContent)

                iconNames.add(iconName)
                svgCodeBlocks[iconName] = svgCodeBlock
            } catch (e: Exception) {
                System.err.println("Error processing ${svgFile.name}: ${e.message}")
            }
        }

        if (iconNames.isNotEmpty()) {
            generateIconsFile(iconNames, svgCodeBlocks, packageName, outputDir)
            generateAllIconsFile(iconNames, packageName, outputDir)
        }

        return iconNames
    }

    /**
     * Generates a single Icons.kt file containing:
     * - Icons object with public accessors
     * - Private val declarations for each icon
     */
    private fun generateIconsFile(
        iconNames: List<String>,
        svgCodeBlocks: Map<String, CodeBlock>,
        packageName: String,
        outputDir: File
    ) {
        val visibilityModifier = when (visibility) {
            IconVisibility.PUBLIC -> KModifier.PUBLIC
            IconVisibility.INTERNAL -> KModifier.INTERNAL
        }

        val iconsObjectBuilder = TypeSpec.objectBuilder("Icons")
            .addModifiers(visibilityModifier)
            .addKdoc("Container for all icons.\nUsage: Icons.Check, Icons.Menu, etc.")

        // Add public properties that delegate to private vals
        iconNames.sorted().forEach { name ->
            val property = PropertySpec.builder(name, svgClass)
                .getter(FunSpec.getterBuilder().addStatement("return _$name").build())
                .build()
            iconsObjectBuilder.addProperty(property)
        }

        val fileSpecBuilder = FileSpec.builder(packageName, "Icons")
            .addType(iconsObjectBuilder.build())

        // Add private val declarations for each icon
        iconNames.sorted().forEach { name ->
            val svgCodeBlock = svgCodeBlocks[name] ?: return@forEach
            val privateProperty = PropertySpec.builder("_$name", svgClass)
                .addModifiers(KModifier.PRIVATE)
                .initializer(svgCodeBlock)
                .addKdoc("$name icon.\nAuto-generated from SVG file.")
                .build()
            fileSpecBuilder.addProperty(privateProperty)
        }

        fileSpecBuilder.build().writeTo(outputDir)
    }

    private fun generateAllIconsFile(iconNames: List<String>, packageName: String, outputDir: File) {
        val pairType = ClassName("kotlin", "Pair").parameterizedBy(
            String::class.asClassName(),
            svgClass
        )
        val listType = ClassName("kotlin.collections", "List").parameterizedBy(pairType)

        val entriesBuilder = CodeBlock.builder()
            .add("listOf(\n")
            .indent()

        iconNames.sorted().forEachIndexed { index, name ->
            entriesBuilder.add("%S to Icons.%L", name, name)
            if (index < iconNames.size - 1) entriesBuilder.add(",\n")
        }

        entriesBuilder.unindent().add("\n)")

        val visibilityModifier = when (visibility) {
            IconVisibility.PUBLIC -> KModifier.PUBLIC
            IconVisibility.INTERNAL -> KModifier.INTERNAL
        }

        val entriesProperty = PropertySpec.builder("entries", listType)
            .initializer(entriesBuilder.build())
            .build()

        val allIconsObject = TypeSpec.objectBuilder("AllIcons")
            .addModifiers(visibilityModifier)
            .addKdoc("Registry of all available icons.\nAuto-generated.")
            .addProperty(entriesProperty)
            .build()

        FileSpec.builder(packageName, "AllIcons")
            .addType(allIconsObject)
            .build()
            .writeTo(outputDir)
    }

    private fun String.toPascalCase(): String {
        return split("-", "_", " ")
            .joinToString("") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }
    }

    private fun String.sanitizeIconName(): String {
        return when (this) {
            "List", "Map", "Set", "Pair", "Any", "Unit", "Nothing", "Object", "Class", "Type" ->
                "${this}Icon"
            else -> this
        }
    }
}
