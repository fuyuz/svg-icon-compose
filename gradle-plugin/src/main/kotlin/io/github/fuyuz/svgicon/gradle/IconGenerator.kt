package io.github.fuyuz.svgicon.gradle

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.fuyuz.svgicon.core.*
import java.io.File

/**
 * Generates Kotlin icon files from SVG files using KotlinPoet.
 */
class IconGenerator(
    private val visibility: IconVisibility = IconVisibility.INTERNAL
) {

    private val svgIconClass = ClassName("io.github.fuyuz.svgicon", "SvgIcon")
    private val svgClass = ClassName("io.github.fuyuz.svgicon.core", "Svg")
    private val svgPathClass = ClassName("io.github.fuyuz.svgicon.core", "SvgPath")
    private val svgCircleClass = ClassName("io.github.fuyuz.svgicon.core", "SvgCircle")
    private val svgEllipseClass = ClassName("io.github.fuyuz.svgicon.core", "SvgEllipse")
    private val svgRectClass = ClassName("io.github.fuyuz.svgicon.core", "SvgRect")
    private val svgLineClass = ClassName("io.github.fuyuz.svgicon.core", "SvgLine")
    private val svgPolylineClass = ClassName("io.github.fuyuz.svgicon.core", "SvgPolyline")
    private val svgPolygonClass = ClassName("io.github.fuyuz.svgicon.core", "SvgPolygon")
    private val svgGroupClass = ClassName("io.github.fuyuz.svgicon.core", "SvgGroup")
    private val svgStyledClass = ClassName("io.github.fuyuz.svgicon.core", "SvgStyled")
    private val svgStyleClass = ClassName("io.github.fuyuz.svgicon.core", "SvgStyle")
    private val svgClipPathClass = ClassName("io.github.fuyuz.svgicon.core", "SvgClipPath")
    private val svgMaskClass = ClassName("io.github.fuyuz.svgicon.core", "SvgMask")
    private val svgDefsClass = ClassName("io.github.fuyuz.svgicon.core", "SvgDefs")
    private val svgAnimatedClass = ClassName("io.github.fuyuz.svgicon.core", "SvgAnimated")
    private val svgAnimateClass = ClassName("io.github.fuyuz.svgicon.core", "SvgAnimate")
    private val transformTypeClass = ClassName("io.github.fuyuz.svgicon.core", "TransformType")
    private val pathCommandClass = ClassName("io.github.fuyuz.svgicon.core", "PathCommand")
    private val lineCapClass = ClassName("io.github.fuyuz.svgicon.core", "LineCap")
    private val lineJoinClass = ClassName("io.github.fuyuz.svgicon.core", "LineJoin")
    private val fillRuleClass = ClassName("io.github.fuyuz.svgicon.core", "FillRule")
    private val clipPathUnitsClass = ClassName("io.github.fuyuz.svgicon.core", "ClipPathUnits")
    private val maskUnitsClass = ClassName("io.github.fuyuz.svgicon.core", "MaskUnits")
    private val paintOrderClass = ClassName("io.github.fuyuz.svgicon.core", "PaintOrder")
    private val vectorEffectClass = ClassName("io.github.fuyuz.svgicon.core", "VectorEffect")
    private val svgTransformClass = ClassName("io.github.fuyuz.svgicon.core", "SvgTransform")
    private val viewBoxClass = ClassName("io.github.fuyuz.svgicon.core", "ViewBox")
    private val preserveAspectRatioClass = ClassName("io.github.fuyuz.svgicon.core", "PreserveAspectRatio")
    private val aspectRatioAlignClass = ClassName("io.github.fuyuz.svgicon.core", "AspectRatioAlign")
    private val meetOrSliceClass = ClassName("io.github.fuyuz.svgicon.core", "MeetOrSlice")
    private val millisecondsProperty = MemberName("kotlin.time.Duration.Companion", "milliseconds")
    private val colorClass = ClassName("androidx.compose.ui.graphics", "Color")
    private val offsetClass = ClassName("androidx.compose.ui.geometry", "Offset")

    fun generateIcons(svgDir: File, packageName: String, outputDir: File): List<String> {
        val iconNames = mutableListOf<String>()

        svgDir.listFiles { file -> file.extension == "svg" }?.forEach { svgFile ->
            try {
                val iconName = svgFile.nameWithoutExtension.toPascalCase().sanitizeIconName()
                val svgContent = svgFile.readText()
                val svg = parseSvg(svgContent)

                val fileSpec = generateIconFile(iconName, packageName, svg)
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

    private fun generateIconFile(iconName: String, packageName: String, svg: Svg): FileSpec {
        val svgProperty = PropertySpec.builder("svg", svgClass)
            .addModifiers(KModifier.OVERRIDE)
            .initializer(generateSvgCodeBlock(svg))
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

    private fun generateSvgCodeBlock(svg: Svg): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgClass)
        builder.indent()

        // Generate width if specified
        svg.width?.let {
            builder.add("width = %Lf,\n", it)
        }

        // Generate height if specified
        svg.height?.let {
            builder.add("height = %Lf,\n", it)
        }

        // Generate viewBox if specified and not default
        svg.viewBox?.let { viewBox ->
            val isDefaultViewBox = viewBox.minX == 0f && viewBox.minY == 0f &&
                viewBox.width == 24f && viewBox.height == 24f
            if (!isDefaultViewBox) {
                builder.add("viewBox = %T(%Lf, %Lf, %Lf, %Lf),\n",
                    viewBoxClass, viewBox.minX, viewBox.minY, viewBox.width, viewBox.height)
            }
        }

        // Generate preserveAspectRatio if not default
        val par = svg.preserveAspectRatio
        if (par.align != AspectRatioAlign.X_MID_Y_MID || par.meetOrSlice != MeetOrSlice.MEET) {
            builder.add("preserveAspectRatio = %T(%T.%L, %T.%L),\n",
                preserveAspectRatioClass,
                aspectRatioAlignClass, par.align.name,
                meetOrSliceClass, par.meetOrSlice.name)
        }

        // Generate fill as Color type (if not default "none")
        if (svg.fill != "none") {
            builder.add(generateSvgColorCodeBlock("fill", svg.fill))
            builder.add(",\n")
        }

        // Generate stroke as Color type (only if not default currentColor)
        if (svg.stroke != "currentColor") {
            builder.add(generateSvgColorCodeBlock("stroke", svg.stroke))
            builder.add(",\n")
        }

        if (svg.strokeWidth != 2f) builder.add("strokeWidth = %Lf,\n", svg.strokeWidth)
        if (svg.strokeLinecap != LineCap.ROUND) {
            builder.add("strokeLinecap = %T.%L,\n", lineCapClass, svg.strokeLinecap.name)
        }
        if (svg.strokeLinejoin != LineJoin.ROUND) {
            builder.add("strokeLinejoin = %T.%L,\n", lineJoinClass, svg.strokeLinejoin.name)
        }

        builder.add("children = listOf(\n")
        builder.indent()
        svg.children.forEachIndexed { index, element ->
            builder.add(generateElementCodeBlock(element))
            if (index < svg.children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")

        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    /**
     * Generate color code block for Svg root element attributes.
     * - "none" -> null
     * - "currentColor" -> Color.Unspecified
     * - "#RRGGBB" -> Color(0xFFRRGGBB)
     */
    private fun generateSvgColorCodeBlock(name: String, colorStr: String): CodeBlock {
        return when {
            colorStr == "none" -> CodeBlock.of("%L = null", name)
            colorStr == "currentColor" -> CodeBlock.of("%L = %T.Unspecified", name, colorClass)
            colorStr.startsWith("#") -> {
                val hex = colorStr.removePrefix("#")
                val colorValue = when (hex.length) {
                    3 -> "FF${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}".uppercase()
                    6 -> "FF${hex}".uppercase()
                    8 -> hex.uppercase()
                    else -> "FF000000"
                }
                CodeBlock.of("%L = %T(0x${colorValue})", name, colorClass)
            }
            else -> CodeBlock.of("%L = %T.Unspecified", name, colorClass)
        }
    }

    private fun generateElementCodeBlock(element: SvgElement): CodeBlock {
        return when (element) {
            is SvgPath -> generatePathCodeBlock(element)
            is SvgCircle -> CodeBlock.of("%T(%Lf, %Lf, %Lf)", svgCircleClass, element.cx, element.cy, element.r)
            is SvgEllipse -> CodeBlock.of("%T(%Lf, %Lf, %Lf, %Lf)", svgEllipseClass, element.cx, element.cy, element.rx, element.ry)
            is SvgRect -> generateRectCodeBlock(element)
            is SvgLine -> CodeBlock.of("%T(%Lf, %Lf, %Lf, %Lf)", svgLineClass, element.x1, element.y1, element.x2, element.y2)
            is SvgPolyline -> generatePolylineCodeBlock(element)
            is SvgPolygon -> generatePolygonCodeBlock(element)
            is SvgGroup -> generateGroupCodeBlock(element)
            is SvgStyled -> generateStyledCodeBlock(element)
            is SvgAnimated -> generateAnimatedCodeBlock(element)
            is SvgClipPath -> generateClipPathCodeBlock(element)
            is SvgMask -> generateMaskCodeBlock(element)
            is SvgDefs -> generateDefsCodeBlock(element)
        }
    }

    private fun generatePathCodeBlock(path: SvgPath): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(\n", svgPathClass)
        builder.indent()
        path.commands.forEachIndexed { index, cmd ->
            builder.add(generateCommandCodeBlock(cmd))
            if (index < path.commands.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n))")
        return builder.build()
    }

    private fun generateCommandCodeBlock(cmd: PathCommand): CodeBlock {
        return when (cmd) {
            is PathCommand.MoveTo -> CodeBlock.of("%T.MoveTo(%Lf, %Lf)", pathCommandClass, cmd.x, cmd.y)
            is PathCommand.MoveToRelative -> CodeBlock.of("%T.MoveToRelative(%Lf, %Lf)", pathCommandClass, cmd.dx, cmd.dy)
            is PathCommand.LineTo -> CodeBlock.of("%T.LineTo(%Lf, %Lf)", pathCommandClass, cmd.x, cmd.y)
            is PathCommand.LineToRelative -> CodeBlock.of("%T.LineToRelative(%Lf, %Lf)", pathCommandClass, cmd.dx, cmd.dy)
            is PathCommand.HorizontalLineTo -> CodeBlock.of("%T.HorizontalLineTo(%Lf)", pathCommandClass, cmd.x)
            is PathCommand.HorizontalLineToRelative -> CodeBlock.of("%T.HorizontalLineToRelative(%Lf)", pathCommandClass, cmd.dx)
            is PathCommand.VerticalLineTo -> CodeBlock.of("%T.VerticalLineTo(%Lf)", pathCommandClass, cmd.y)
            is PathCommand.VerticalLineToRelative -> CodeBlock.of("%T.VerticalLineToRelative(%Lf)", pathCommandClass, cmd.dy)
            is PathCommand.CubicTo -> CodeBlock.of("%T.CubicTo(%Lf, %Lf, %Lf, %Lf, %Lf, %Lf)", pathCommandClass, cmd.x1, cmd.y1, cmd.x2, cmd.y2, cmd.x, cmd.y)
            is PathCommand.CubicToRelative -> CodeBlock.of("%T.CubicToRelative(%Lf, %Lf, %Lf, %Lf, %Lf, %Lf)", pathCommandClass, cmd.dx1, cmd.dy1, cmd.dx2, cmd.dy2, cmd.dx, cmd.dy)
            is PathCommand.SmoothCubicTo -> CodeBlock.of("%T.SmoothCubicTo(%Lf, %Lf, %Lf, %Lf)", pathCommandClass, cmd.x2, cmd.y2, cmd.x, cmd.y)
            is PathCommand.SmoothCubicToRelative -> CodeBlock.of("%T.SmoothCubicToRelative(%Lf, %Lf, %Lf, %Lf)", pathCommandClass, cmd.dx2, cmd.dy2, cmd.dx, cmd.dy)
            is PathCommand.QuadTo -> CodeBlock.of("%T.QuadTo(%Lf, %Lf, %Lf, %Lf)", pathCommandClass, cmd.x1, cmd.y1, cmd.x, cmd.y)
            is PathCommand.QuadToRelative -> CodeBlock.of("%T.QuadToRelative(%Lf, %Lf, %Lf, %Lf)", pathCommandClass, cmd.dx1, cmd.dy1, cmd.dx, cmd.dy)
            is PathCommand.SmoothQuadTo -> CodeBlock.of("%T.SmoothQuadTo(%Lf, %Lf)", pathCommandClass, cmd.x, cmd.y)
            is PathCommand.SmoothQuadToRelative -> CodeBlock.of("%T.SmoothQuadToRelative(%Lf, %Lf)", pathCommandClass, cmd.dx, cmd.dy)
            is PathCommand.ArcTo -> CodeBlock.of("%T.ArcTo(%Lf, %Lf, %Lf, %L, %L, %Lf, %Lf)", pathCommandClass, cmd.rx, cmd.ry, cmd.xAxisRotation, cmd.largeArcFlag, cmd.sweepFlag, cmd.x, cmd.y)
            is PathCommand.ArcToRelative -> CodeBlock.of("%T.ArcToRelative(%Lf, %Lf, %Lf, %L, %L, %Lf, %Lf)", pathCommandClass, cmd.rx, cmd.ry, cmd.xAxisRotation, cmd.largeArcFlag, cmd.sweepFlag, cmd.dx, cmd.dy)
            PathCommand.Close -> CodeBlock.of("%T.Close", pathCommandClass)
        }
    }

    private fun generateRectCodeBlock(rect: SvgRect): CodeBlock {
        return if (rect.rx == 0f && rect.ry == 0f && rect.x == 0f && rect.y == 0f) {
            CodeBlock.of("%T(width = %Lf, height = %Lf)", svgRectClass, rect.width, rect.height)
        } else if (rect.rx == 0f && rect.ry == 0f) {
            CodeBlock.of("%T(x = %Lf, y = %Lf, width = %Lf, height = %Lf)", svgRectClass, rect.x, rect.y, rect.width, rect.height)
        } else {
            CodeBlock.of("%T(x = %Lf, y = %Lf, width = %Lf, height = %Lf, rx = %Lf, ry = %Lf)",
                svgRectClass, rect.x, rect.y, rect.width, rect.height, rect.rx, rect.ry)
        }
    }

    private fun generatePolylineCodeBlock(polyline: SvgPolyline): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(", svgPolylineClass)
        polyline.points.forEachIndexed { index, (x, y) ->
            builder.add("%T(%Lf, %Lf)", offsetClass, x, y)
            if (index < polyline.points.size - 1) builder.add(", ")
        }
        builder.add("))")
        return builder.build()
    }

    private fun generatePolygonCodeBlock(polygon: SvgPolygon): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(", svgPolygonClass)
        polygon.points.forEachIndexed { index, (x, y) ->
            builder.add("%T(%Lf, %Lf)", offsetClass, x, y)
            if (index < polygon.points.size - 1) builder.add(", ")
        }
        builder.add("))")
        return builder.build()
    }

    private fun generateGroupCodeBlock(group: SvgGroup): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(\n", svgGroupClass)
        builder.indent()
        group.children.forEachIndexed { index, child ->
            builder.add(generateElementCodeBlock(child))
            if (index < group.children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n))")
        return builder.build()
    }

    private fun generateAnimatedCodeBlock(animated: SvgAnimated): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgAnimatedClass)
        builder.indent()
        builder.add("element = ")
        builder.add(generateElementCodeBlock(animated.element))
        builder.add(",\n")
        builder.add("animations = listOf(\n")
        builder.indent()
        animated.animations.forEachIndexed { index, anim ->
            builder.add(generateAnimateCodeBlock(anim))
            if (index < animated.animations.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateAnimateCodeBlock(anim: SvgAnimate): CodeBlock {
        // Note: Internal types use Int for dur/delay, we generate Duration for runtime
        return when (anim) {
            is SvgAnimate.Transform -> CodeBlock.of(
                "%T.Transform(type = %T.%L, from = %Lf, to = %Lf, dur = %L.%M, delay = %L.%M)",
                svgAnimateClass, transformTypeClass, anim.type.name, anim.from, anim.to,
                anim.dur, millisecondsProperty, anim.delay, millisecondsProperty
            )
            is SvgAnimate.Opacity -> CodeBlock.of(
                "%T.Opacity(from = %Lf, to = %Lf, dur = %L.%M, delay = %L.%M)",
                svgAnimateClass, anim.from, anim.to,
                anim.dur, millisecondsProperty, anim.delay, millisecondsProperty
            )
            is SvgAnimate.StrokeWidth -> CodeBlock.of(
                "%T.StrokeWidth(from = %Lf, to = %Lf, dur = %L.%M, delay = %L.%M)",
                svgAnimateClass, anim.from, anim.to,
                anim.dur, millisecondsProperty, anim.delay, millisecondsProperty
            )
            is SvgAnimate.StrokeDashoffset -> CodeBlock.of(
                "%T.StrokeDashoffset(from = %Lf, to = %Lf, dur = %L.%M, delay = %L.%M)",
                svgAnimateClass, anim.from, anim.to,
                anim.dur, millisecondsProperty, anim.delay, millisecondsProperty
            )
            else -> CodeBlock.of("/* Unsupported animation type */")
        }
    }

    private fun generateStyledCodeBlock(styled: SvgStyled): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgStyledClass)
        builder.indent()
        builder.add(generateElementCodeBlock(styled.element))
        builder.add(",\n")
        builder.add(generateStyleCodeBlock(styled.style))
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateClipPathCodeBlock(clipPath: SvgClipPath): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgClipPathClass)
        builder.indent()
        builder.add("id = %S,\n", clipPath.id)
        builder.add("children = listOf(\n")
        builder.indent()
        clipPath.children.forEachIndexed { index, child ->
            builder.add(generateElementCodeBlock(child))
            if (index < clipPath.children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")
        if (clipPath.clipPathUnits != ClipPathUnits.USER_SPACE_ON_USE) {
            builder.add(",\nclipPathUnits = %T.%L", clipPathUnitsClass, clipPath.clipPathUnits.name)
        }
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateMaskCodeBlock(mask: SvgMask): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgMaskClass)
        builder.indent()
        builder.add("id = %S,\n", mask.id)
        builder.add("children = listOf(\n")
        builder.indent()
        mask.children.forEachIndexed { index, child ->
            builder.add(generateElementCodeBlock(child))
            if (index < mask.children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateDefsCodeBlock(defs: SvgDefs): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(\n", svgDefsClass)
        builder.indent()
        defs.children.forEachIndexed { index, child ->
            builder.add(generateElementCodeBlock(child))
            if (index < defs.children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n))")
        return builder.build()
    }

    private fun generateStyleCodeBlock(style: SvgStyle): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(", svgStyleClass)

        val parts = mutableListOf<CodeBlock>()
        style.fill?.let { colorStr -> parts.add(generateColorCodeBlock("fill", colorStr)) }
        style.stroke?.let { colorStr -> parts.add(generateColorCodeBlock("stroke", colorStr)) }
        style.strokeWidth?.let { parts.add(CodeBlock.of("strokeWidth = %Lf", it)) }
        style.opacity?.let { parts.add(CodeBlock.of("opacity = %Lf", it)) }

        parts.forEachIndexed { index, part ->
            builder.add(part)
            if (index < parts.size - 1) builder.add(", ")
        }

        builder.add(")")
        return builder.build()
    }

    private fun generateColorCodeBlock(name: String, colorStr: String): CodeBlock {
        return when {
            colorStr == "currentColor" -> CodeBlock.of("%L = %T.Unspecified", name, colorClass)
            colorStr == "none" -> CodeBlock.of("%L = null", name)
            colorStr.startsWith("#") -> {
                val hex = colorStr.removePrefix("#")
                val colorValue = when (hex.length) {
                    3 -> "FF${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}".uppercase()
                    6 -> "FF${hex}".uppercase()
                    8 -> hex.uppercase()
                    else -> "FF000000"
                }
                CodeBlock.of("%L = %T(0x${colorValue}UL)", name, colorClass)
            }
            else -> CodeBlock.of("%L = %T.Unspecified", name, colorClass)
        }
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
