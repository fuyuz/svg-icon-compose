package io.github.fuyuz.svgicon.gradle

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.fuyuz.svgicon.core.SvgCodeGenerator
import java.io.File

/**
 * Generates Kotlin icon files from SVG files using KotlinPoet.
 */
class IconGenerator(
    private val visibility: IconVisibility = IconVisibility.INTERNAL
) {
    private val svgIconClass = ClassName("io.github.fuyuz.svgicon", "SvgIcon")
    private val svgClass = ClassName("io.github.fuyuz.svgicon.core", "Svg")

    fun generateIcons(svgDir: File, packageName: String, outputDir: File): List<String> {
        val iconNames = mutableListOf<String>()

        svgDir.listFiles { file -> file.extension == "svg" }?.forEach { svgFile ->
            try {
                val iconName = svgFile.nameWithoutExtension.toPascalCase().sanitizeIconName()
                val svgContent = svgFile.readText()
                val svgCodeBlock = SvgCodeGenerator.generateSvgCodeBlock(svgContent)

                val fileSpec = generateIconFile(iconName, packageName, svgCodeBlock)
                fileSpec.writeTo(outputDir)
                iconNames.add(iconName)
            } catch (e: Exception) {
                System.err.println("Error processing ${svgFile.name}: ${e.message}")
            }
        }

        if (iconNames.isNotEmpty()) {
            generateAllIconsFile(iconNames, packageName, outputDir)
        }

        return iconNames
    }

    private fun generateIconFile(iconName: String, packageName: String, svgCodeBlock: CodeBlock): FileSpec {
        val svgProperty = PropertySpec.builder("svg", svgClass)
            .addModifiers(KModifier.OVERRIDE)
            .initializer(svgCodeBlock)
            .build()

        val visibilityModifier = when (visibility) {
            IconVisibility.PUBLIC -> KModifier.PUBLIC
            IconVisibility.INTERNAL -> KModifier.INTERNAL
        }

        val iconObject = TypeSpec.objectBuilder(iconName)
            .addModifiers(visibilityModifier)
            .addSuperinterface(svgIconClass)
            .addKdoc("$iconName icon.\nAuto-generated from SVG file.")
            .addProperty(svgProperty)
            .build()

        return FileSpec.builder(packageName, iconName)
            .addType(iconObject)
            .build()
    }

    private fun generateAllIconsFile(iconNames: List<String>, packageName: String, outputDir: File) {
        val pairType = ClassName("kotlin", "Pair").parameterizedBy(
            String::class.asClassName(),
            svgIconClass
        )
        val listType = ClassName("kotlin.collections", "List").parameterizedBy(pairType)

        val entriesBuilder = CodeBlock.builder()
            .add("listOf(\n")
            .indent()

        iconNames.sorted().forEachIndexed { index, name ->
            entriesBuilder.add("%S to %L", name, name)
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

        val iconsObjectBuilder = TypeSpec.objectBuilder("Icons")
            .addModifiers(visibilityModifier)
            .addKdoc("Container for all icons.\nUsage: Icons.Check, Icons.Menu, etc.")

        iconNames.sorted().forEach { name ->
            val property = PropertySpec.builder(name, svgIconClass)
                .initializer("$packageName.$name")
                .build()
            iconsObjectBuilder.addProperty(property)
        }

        FileSpec.builder(packageName, "Icons")
            .addType(iconsObjectBuilder.build())
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
