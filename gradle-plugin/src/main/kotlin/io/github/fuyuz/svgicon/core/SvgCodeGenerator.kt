package io.github.fuyuz.svgicon.core

import com.squareup.kotlinpoet.*

/**
 * Parses SVG XML and generates KotlinPoet CodeBlocks that reference runtime types.
 * This eliminates the need for duplicate SvgDsl types in the gradle-plugin.
 */
object SvgCodeGenerator {
    // Runtime type references
    // Data structures are in core package
    private val corePackage = "io.github.fuyuz.svgicon.core"
    // DSL builders are in core.dsl package
    private val dslPackage = "io.github.fuyuz.svgicon.core.dsl"

    // Data structures (core package)
    private val svgClass = ClassName(corePackage, "Svg")
    private val svgPathClass = ClassName(corePackage, "SvgPath")
    private val svgCircleClass = ClassName(corePackage, "SvgCircle")
    private val svgEllipseClass = ClassName(corePackage, "SvgEllipse")
    private val svgRectClass = ClassName(corePackage, "SvgRect")
    private val svgLineClass = ClassName(corePackage, "SvgLine")
    private val svgPolylineClass = ClassName(corePackage, "SvgPolyline")
    private val svgPolygonClass = ClassName(corePackage, "SvgPolygon")
    private val svgGroupClass = ClassName(corePackage, "SvgGroup")
    private val svgStyledClass = ClassName(corePackage, "SvgStyled")
    private val svgStyleClass = ClassName(corePackage, "SvgStyle")
    private val svgClipPathClass = ClassName(corePackage, "SvgClipPath")
    private val svgMaskClass = ClassName(corePackage, "SvgMask")
    private val svgDefsClass = ClassName(corePackage, "SvgDefs")
    private val svgAnimatedClass = ClassName(corePackage, "SvgAnimated")
    private val svgAnimateClass = ClassName(corePackage, "SvgAnimate")
    private val transformTypeClass = ClassName(corePackage, "TransformType")
    private val calcModeClass = ClassName(corePackage, "CalcMode")
    private val keySplinesClass = ClassName(corePackage, "KeySplines")
    private val animationDirectionClass = ClassName(corePackage, "AnimationDirection")
    private val animationFillModeClass = ClassName(corePackage, "AnimationFillMode")
    private val pathCommandClass = ClassName(corePackage, "PathCommand")
    private val lineCapClass = ClassName(corePackage, "LineCap")
    private val lineJoinClass = ClassName(corePackage, "LineJoin")
    private val fillRuleClass = ClassName(corePackage, "FillRule")
    private val paintOrderClass = ClassName(corePackage, "PaintOrder")
    private val clipPathUnitsClass = ClassName(corePackage, "ClipPathUnits")
    private val maskUnitsClass = ClassName(corePackage, "MaskUnits")
    private val svgTransformClass = ClassName(corePackage, "SvgTransform")
    private val viewBoxClass = ClassName(corePackage, "ViewBox")
    private val preserveAspectRatioClass = ClassName(corePackage, "PreserveAspectRatio")
    private val aspectRatioAlignClass = ClassName(corePackage, "AspectRatioAlign")
    private val meetOrSliceClass = ClassName(corePackage, "MeetOrSlice")
    private val millisecondsProperty = MemberName("kotlin.time.Duration.Companion", "milliseconds")
    private val colorClass = ClassName("androidx.compose.ui.graphics", "Color")
    private val offsetClass = ClassName("androidx.compose.ui.geometry", "Offset")

    // Regex patterns
    private val tagPattern = Regex("""<(\w+)([^>]*)(/?>)""")
    private val attrPattern = Regex("""([\w-]+)=["']([^"']*)["']""")
    private val animatePattern = Regex("""<animate\s+([^>]*)/?>(</animate>)?""", RegexOption.IGNORE_CASE)
    private val animateTransformPattern = Regex("""<animateTransform\s+([^>]*)/?>(</animateTransform>)?""", RegexOption.IGNORE_CASE)

    /**
     * Parse SVG content and generate a CodeBlock for the Svg object.
     */
    fun generateSvgCodeBlock(svgContent: String): CodeBlock {
        // Extract stylesheet first
        val (stylesheet, cleanedSvg) = extractStylesheet(svgContent)

        var remaining = cleanedSvg.trim()

        // Skip XML declaration and DOCTYPE
        while (remaining.startsWith("<?") || remaining.startsWith("<!")) {
            val endIdx = remaining.indexOf('>') + 1
            if (endIdx > 0) {
                remaining = remaining.substring(endIdx).trim()
            } else {
                break
            }
        }

        val tagMatch = tagPattern.find(remaining) ?: return generateEmptySvg()
        val tagName = tagMatch.groupValues[1].lowercase()

        if (tagName != "svg") {
            val children = parseElementsWithStylesheet(remaining, stylesheet)
            return generateSvgWithChildren(null, children)
        }

        val attrsStr = tagMatch.groupValues[2]
        val selfClosing = tagMatch.groupValues[3] == "/>"
        val attrs = parseAttributes(attrsStr)

        val children = if (selfClosing) {
            emptyList()
        } else {
            val closeTag = "</svg>"
            val closeIdx = remaining.indexOf(closeTag)
            if (closeIdx > 0) {
                val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                parseElementsWithStylesheet(innerContent, stylesheet)
            } else {
                emptyList()
            }
        }

        return generateSvgWithAttrs(attrs, children)
    }

    private fun generateEmptySvg(): CodeBlock {
        return CodeBlock.of("%T()", svgClass)
    }

    private fun generateSvgWithChildren(attrs: Map<String, String>?, children: List<CodeBlock>): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgClass)
        builder.indent()
        builder.add("children = listOf(\n")
        builder.indent()
        children.forEachIndexed { index, child ->
            builder.add(child)
            if (index < children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateSvgWithAttrs(attrs: Map<String, String>, children: List<CodeBlock>): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgClass)
        builder.indent()

        // width/height
        parseLengthAttribute(attrs["width"])?.let { builder.add("width = %Lf,\n", it) }
        parseLengthAttribute(attrs["height"])?.let { builder.add("height = %Lf,\n", it) }

        // viewBox
        parseViewBox(attrs["viewBox"])?.let { (minX, minY, width, height) ->
            val isDefault = minX == 0f && minY == 0f && width == 24f && height == 24f
            if (!isDefault) {
                builder.add("viewBox = %T(%Lf, %Lf, %Lf, %Lf),\n", viewBoxClass, minX, minY, width, height)
            }
        }

        // preserveAspectRatio
        attrs["preserveAspectRatio"]?.let { par ->
            val parts = par.trim().split(Regex("\\s+"))
            val align = if (parts.isNotEmpty()) parseAspectRatioAlign(parts[0]) else "X_MID_Y_MID"
            val meetOrSlice = if (parts.size > 1 && parts[1].lowercase() == "slice") "SLICE" else "MEET"
            if (align != "X_MID_Y_MID" || meetOrSlice != "MEET") {
                builder.add("preserveAspectRatio = %T(%T.%L, %T.%L),\n",
                    preserveAspectRatioClass, aspectRatioAlignClass, align, meetOrSliceClass, meetOrSlice)
            }
        }

        // fill - only generate if explicitly specified and not "inherit"
        val fill = attrs["fill"]
        if (fill != null && fill != "inherit") {
            builder.add(generateColorCodeBlock("fill", fill))
            builder.add(",\n")
        }

        // stroke - only generate if explicitly specified and not "inherit"
        val stroke = attrs["stroke"]
        if (stroke != null && stroke != "inherit") {
            builder.add(generateColorCodeBlock("stroke", stroke))
            builder.add(",\n")
        }

        // strokeWidth
        val strokeWidth = attrs["stroke-width"]?.toFloatOrNull() ?: 2f
        if (strokeWidth != 2f) builder.add("strokeWidth = %Lf,\n", strokeWidth)

        // strokeLinecap
        val linecap = attrs["stroke-linecap"]?.lowercase()
        if (linecap != null && linecap != "round") {
            val capName = when (linecap) {
                "butt" -> "BUTT"
                "square" -> "SQUARE"
                else -> "ROUND"
            }
            builder.add("strokeLinecap = %T.%L,\n", lineCapClass, capName)
        }

        // strokeLinejoin
        val linejoin = attrs["stroke-linejoin"]?.lowercase()
        if (linejoin != null && linejoin != "round") {
            val joinName = when (linejoin) {
                "miter" -> "MITER"
                "bevel" -> "BEVEL"
                else -> "ROUND"
            }
            builder.add("strokeLinejoin = %T.%L,\n", lineJoinClass, joinName)
        }

        // children
        builder.add("children = listOf(\n")
        builder.indent()
        children.forEachIndexed { index, child ->
            builder.add(child)
            if (index < children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")

        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun parseElements(content: String): List<CodeBlock> {
        val elements = mutableListOf<CodeBlock>()
        var remaining = content.trim()

        while (remaining.isNotEmpty()) {
            // Skip XML declaration and DOCTYPE
            if (remaining.startsWith("<?") || remaining.startsWith("<!")) {
                val endIdx = remaining.indexOf('>') + 1
                if (endIdx > 0) {
                    remaining = remaining.substring(endIdx).trim()
                    continue
                }
            }

            val tagMatch = tagPattern.find(remaining) ?: break
            val tagName = tagMatch.groupValues[1].lowercase()
            val attrsStr = tagMatch.groupValues[2]
            val selfClosing = tagMatch.groupValues[3] == "/>"
            val attrs = parseAttributes(attrsStr)

            when (tagName) {
                "svg" -> {
                    // Nested svg - skip for now
                    remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                }
                "path" -> {
                    val d = attrs["d"] ?: ""
                    if (d.isNotEmpty()) {
                        val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                        val animations = parseAnimations(innerContent, attrs)
                        val pathCode = generatePathCodeBlock(d)
                        val styledCode = wrapWithStyle(pathCode, attrs)
                        elements.add(wrapWithAnimations(styledCode, animations))
                        remaining = newRemaining
                    } else {
                        remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                    }
                }
                "circle" -> {
                    val cx = attrs["cx"]?.toFloatOrNull() ?: 0f
                    val cy = attrs["cy"]?.toFloatOrNull() ?: 0f
                    val r = attrs["r"]?.toFloatOrNull() ?: 0f
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent, attrs)
                    val circleCode = CodeBlock.of("%T(%Lf, %Lf, %Lf)", svgCircleClass, cx, cy, r)
                    val styledCode = wrapWithStyle(circleCode, attrs)
                    elements.add(wrapWithAnimations(styledCode, animations))
                    remaining = newRemaining
                }
                "ellipse" -> {
                    val cx = attrs["cx"]?.toFloatOrNull() ?: 0f
                    val cy = attrs["cy"]?.toFloatOrNull() ?: 0f
                    val rx = attrs["rx"]?.toFloatOrNull() ?: 0f
                    val ry = attrs["ry"]?.toFloatOrNull() ?: 0f
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent, attrs)
                    val ellipseCode = CodeBlock.of("%T(%Lf, %Lf, %Lf, %Lf)", svgEllipseClass, cx, cy, rx, ry)
                    val styledCode = wrapWithStyle(ellipseCode, attrs)
                    elements.add(wrapWithAnimations(styledCode, animations))
                    remaining = newRemaining
                }
                "rect" -> {
                    val x = attrs["x"]?.toFloatOrNull() ?: 0f
                    val y = attrs["y"]?.toFloatOrNull() ?: 0f
                    val width = attrs["width"]?.toFloatOrNull() ?: 0f
                    val height = attrs["height"]?.toFloatOrNull() ?: 0f
                    val rx = attrs["rx"]?.toFloatOrNull() ?: 0f
                    val ry = attrs["ry"]?.toFloatOrNull() ?: rx
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent, attrs)
                    val rectCode = generateRectCodeBlock(x, y, width, height, rx, ry)
                    val styledCode = wrapWithStyle(rectCode, attrs)
                    elements.add(wrapWithAnimations(styledCode, animations))
                    remaining = newRemaining
                }
                "line" -> {
                    val x1 = attrs["x1"]?.toFloatOrNull() ?: 0f
                    val y1 = attrs["y1"]?.toFloatOrNull() ?: 0f
                    val x2 = attrs["x2"]?.toFloatOrNull() ?: 0f
                    val y2 = attrs["y2"]?.toFloatOrNull() ?: 0f
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent, attrs)
                    val lineCode = CodeBlock.of("%T(%Lf, %Lf, %Lf, %Lf)", svgLineClass, x1, y1, x2, y2)
                    val styledCode = wrapWithStyle(lineCode, attrs)
                    elements.add(wrapWithAnimations(styledCode, animations))
                    remaining = newRemaining
                }
                "polyline" -> {
                    val points = parsePoints(attrs["points"] ?: "")
                    if (points.isNotEmpty()) {
                        val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                        val animations = parseAnimations(innerContent, attrs)
                        val polylineCode = generatePolylineCodeBlock(points)
                        val styledCode = wrapWithStyle(polylineCode, attrs)
                        elements.add(wrapWithAnimations(styledCode, animations))
                        remaining = newRemaining
                    } else {
                        remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                    }
                }
                "polygon" -> {
                    val points = parsePoints(attrs["points"] ?: "")
                    if (points.isNotEmpty()) {
                        val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                        val animations = parseAnimations(innerContent, attrs)
                        val polygonCode = generatePolygonCodeBlock(points)
                        val styledCode = wrapWithStyle(polygonCode, attrs)
                        elements.add(wrapWithAnimations(styledCode, animations))
                        remaining = newRemaining
                    } else {
                        remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                    }
                }
                "g" -> {
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeIdx = findMatchingClose(remaining, tagMatch.range.last + 1, "g")
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            val children = parseElements(innerContent)
                            val groupCode = generateGroupCodeBlock(children)
                            elements.add(wrapWithStyle(groupCode, attrs))
                            remaining = remaining.substring(closeIdx + "</g>".length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                "defs" -> {
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeIdx = findMatchingClose(remaining, tagMatch.range.last + 1, "defs")
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            val children = parseElements(innerContent)
                            elements.add(generateDefsCodeBlock(children))
                            remaining = remaining.substring(closeIdx + "</defs>".length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                "clippath" -> {
                    val id = attrs["id"] ?: ""
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeIdx = findMatchingClose(remaining, tagMatch.range.last + 1, "clipPath")
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            val children = parseElements(innerContent)
                            val unitsStr = attrs["clipPathUnits"]?.lowercase()
                            val units = if (unitsStr == "objectboundingbox") "OBJECT_BOUNDING_BOX" else "USER_SPACE_ON_USE"
                            elements.add(generateClipPathCodeBlock(id, children, units))
                            remaining = remaining.substring(closeIdx + "</clipPath>".length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                "mask" -> {
                    val id = attrs["id"] ?: ""
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeIdx = findMatchingClose(remaining, tagMatch.range.last + 1, "mask")
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            val children = parseElements(innerContent)
                            elements.add(generateMaskCodeBlock(id, children))
                            remaining = remaining.substring(closeIdx + "</mask>".length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                else -> {
                    remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                }
            }
        }

        return elements
    }

    private fun parseElementsWithStylesheet(content: String, stylesheet: CssStylesheet): List<CodeBlock> {
        val elements = mutableListOf<CodeBlock>()
        var remaining = content.trim()

        while (remaining.isNotEmpty()) {
            // Skip XML declaration and DOCTYPE
            if (remaining.startsWith("<?") || remaining.startsWith("<!")) {
                val endIdx = remaining.indexOf('>') + 1
                if (endIdx > 0) {
                    remaining = remaining.substring(endIdx).trim()
                    continue
                }
            }

            val tagMatch = tagPattern.find(remaining) ?: break
            val tagName = tagMatch.groupValues[1].lowercase()
            val attrsStr = tagMatch.groupValues[2]
            val selfClosing = tagMatch.groupValues[3] == "/>"
            val attrs = parseAttributes(attrsStr)

            when (tagName) {
                "svg" -> {
                    // Nested svg - skip for now
                    remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                }
                "style" -> {
                    // Skip style tags (already processed)
                    remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                }
                "path" -> {
                    val d = attrs["d"] ?: ""
                    if (d.isNotEmpty()) {
                        val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                        val animations = parseAnimations(innerContent, attrs)
                        val pathCode = generatePathCodeBlock(d)
                        val styledCode = wrapWithStyleFromStylesheet(pathCode, tagName, attrs, stylesheet)
                        elements.add(wrapWithAnimations(styledCode, animations))
                        remaining = newRemaining
                    } else {
                        remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                    }
                }
                "circle" -> {
                    val cx = attrs["cx"]?.toFloatOrNull() ?: 0f
                    val cy = attrs["cy"]?.toFloatOrNull() ?: 0f
                    val r = attrs["r"]?.toFloatOrNull() ?: 0f
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent, attrs)
                    val circleCode = CodeBlock.of("%T(%Lf, %Lf, %Lf)", svgCircleClass, cx, cy, r)
                    val styledCode = wrapWithStyleFromStylesheet(circleCode, tagName, attrs, stylesheet)
                    elements.add(wrapWithAnimations(styledCode, animations))
                    remaining = newRemaining
                }
                "ellipse" -> {
                    val cx = attrs["cx"]?.toFloatOrNull() ?: 0f
                    val cy = attrs["cy"]?.toFloatOrNull() ?: 0f
                    val rx = attrs["rx"]?.toFloatOrNull() ?: 0f
                    val ry = attrs["ry"]?.toFloatOrNull() ?: 0f
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent, attrs)
                    val ellipseCode = CodeBlock.of("%T(%Lf, %Lf, %Lf, %Lf)", svgEllipseClass, cx, cy, rx, ry)
                    val styledCode = wrapWithStyleFromStylesheet(ellipseCode, tagName, attrs, stylesheet)
                    elements.add(wrapWithAnimations(styledCode, animations))
                    remaining = newRemaining
                }
                "rect" -> {
                    val x = attrs["x"]?.toFloatOrNull() ?: 0f
                    val y = attrs["y"]?.toFloatOrNull() ?: 0f
                    val width = attrs["width"]?.toFloatOrNull() ?: 0f
                    val height = attrs["height"]?.toFloatOrNull() ?: 0f
                    val rx = attrs["rx"]?.toFloatOrNull() ?: 0f
                    val ry = attrs["ry"]?.toFloatOrNull() ?: rx
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent, attrs)
                    val rectCode = generateRectCodeBlock(x, y, width, height, rx, ry)
                    val styledCode = wrapWithStyleFromStylesheet(rectCode, tagName, attrs, stylesheet)
                    elements.add(wrapWithAnimations(styledCode, animations))
                    remaining = newRemaining
                }
                "line" -> {
                    val x1 = attrs["x1"]?.toFloatOrNull() ?: 0f
                    val y1 = attrs["y1"]?.toFloatOrNull() ?: 0f
                    val x2 = attrs["x2"]?.toFloatOrNull() ?: 0f
                    val y2 = attrs["y2"]?.toFloatOrNull() ?: 0f
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent, attrs)
                    val lineCode = CodeBlock.of("%T(%Lf, %Lf, %Lf, %Lf)", svgLineClass, x1, y1, x2, y2)
                    val styledCode = wrapWithStyleFromStylesheet(lineCode, tagName, attrs, stylesheet)
                    elements.add(wrapWithAnimations(styledCode, animations))
                    remaining = newRemaining
                }
                "polyline" -> {
                    val points = parsePoints(attrs["points"] ?: "")
                    if (points.isNotEmpty()) {
                        val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                        val animations = parseAnimations(innerContent, attrs)
                        val polylineCode = generatePolylineCodeBlock(points)
                        val styledCode = wrapWithStyleFromStylesheet(polylineCode, tagName, attrs, stylesheet)
                        elements.add(wrapWithAnimations(styledCode, animations))
                        remaining = newRemaining
                    } else {
                        remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                    }
                }
                "polygon" -> {
                    val points = parsePoints(attrs["points"] ?: "")
                    if (points.isNotEmpty()) {
                        val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                        val animations = parseAnimations(innerContent, attrs)
                        val polygonCode = generatePolygonCodeBlock(points)
                        val styledCode = wrapWithStyleFromStylesheet(polygonCode, tagName, attrs, stylesheet)
                        elements.add(wrapWithAnimations(styledCode, animations))
                        remaining = newRemaining
                    } else {
                        remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                    }
                }
                "g" -> {
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeIdx = findMatchingClose(remaining, tagMatch.range.last + 1, "g")
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            val children = parseElementsWithStylesheet(innerContent, stylesheet)
                            val groupCode = generateGroupCodeBlock(children)
                            elements.add(wrapWithStyleFromStylesheet(groupCode, tagName, attrs, stylesheet))
                            remaining = remaining.substring(closeIdx + "</g>".length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                "defs" -> {
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeIdx = findMatchingClose(remaining, tagMatch.range.last + 1, "defs")
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            val children = parseElementsWithStylesheet(innerContent, stylesheet)
                            elements.add(generateDefsCodeBlock(children))
                            remaining = remaining.substring(closeIdx + "</defs>".length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                "clippath" -> {
                    val id = attrs["id"] ?: ""
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeIdx = findMatchingClose(remaining, tagMatch.range.last + 1, "clipPath")
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            val children = parseElementsWithStylesheet(innerContent, stylesheet)
                            val unitsStr = attrs["clipPathUnits"]?.lowercase()
                            val units = if (unitsStr == "objectboundingbox") "OBJECT_BOUNDING_BOX" else "USER_SPACE_ON_USE"
                            elements.add(generateClipPathCodeBlock(id, children, units))
                            remaining = remaining.substring(closeIdx + "</clipPath>".length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                "mask" -> {
                    val id = attrs["id"] ?: ""
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeIdx = findMatchingClose(remaining, tagMatch.range.last + 1, "mask")
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            val children = parseElementsWithStylesheet(innerContent, stylesheet)
                            elements.add(generateMaskCodeBlock(id, children))
                            remaining = remaining.substring(closeIdx + "</mask>".length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                else -> {
                    remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                }
            }
        }

        return elements
    }

    // ============================================
    // Element Code Generation
    // ============================================

    private fun generatePathCodeBlock(d: String): CodeBlock {
        val commands = parsePathCommands(d)
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(\n", svgPathClass)
        builder.indent()
        commands.forEachIndexed { index, cmd ->
            builder.add(cmd)
            if (index < commands.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n))")
        return builder.build()
    }

    private fun generateRectCodeBlock(x: Float, y: Float, width: Float, height: Float, rx: Float, ry: Float): CodeBlock {
        return if (rx == 0f && ry == 0f && x == 0f && y == 0f) {
            CodeBlock.of("%T(width = %Lf, height = %Lf)", svgRectClass, width, height)
        } else if (rx == 0f && ry == 0f) {
            CodeBlock.of("%T(x = %Lf, y = %Lf, width = %Lf, height = %Lf)", svgRectClass, x, y, width, height)
        } else {
            CodeBlock.of("%T(x = %Lf, y = %Lf, width = %Lf, height = %Lf, rx = %Lf, ry = %Lf)",
                svgRectClass, x, y, width, height, rx, ry)
        }
    }

    private fun generatePolylineCodeBlock(points: List<Pair<Float, Float>>): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(", svgPolylineClass)
        points.forEachIndexed { index, (x, y) ->
            builder.add("%T(%Lf, %Lf)", offsetClass, x, y)
            if (index < points.size - 1) builder.add(", ")
        }
        builder.add("))")
        return builder.build()
    }

    private fun generatePolygonCodeBlock(points: List<Pair<Float, Float>>): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(", svgPolygonClass)
        points.forEachIndexed { index, (x, y) ->
            builder.add("%T(%Lf, %Lf)", offsetClass, x, y)
            if (index < points.size - 1) builder.add(", ")
        }
        builder.add("))")
        return builder.build()
    }

    private fun generateGroupCodeBlock(children: List<CodeBlock>): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(\n", svgGroupClass)
        builder.indent()
        children.forEachIndexed { index, child ->
            builder.add(child)
            if (index < children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n))")
        return builder.build()
    }

    private fun generateDefsCodeBlock(children: List<CodeBlock>): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(\n", svgDefsClass)
        builder.indent()
        children.forEachIndexed { index, child ->
            builder.add(child)
            if (index < children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n))")
        return builder.build()
    }

    private fun generateClipPathCodeBlock(id: String, children: List<CodeBlock>, units: String): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgClipPathClass)
        builder.indent()
        builder.add("id = %S,\n", id)
        builder.add("children = listOf(\n")
        builder.indent()
        children.forEachIndexed { index, child ->
            builder.add(child)
            if (index < children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")
        if (units != "USER_SPACE_ON_USE") {
            builder.add(",\nclipPathUnits = %T.%L", clipPathUnitsClass, units)
        }
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateMaskCodeBlock(id: String, children: List<CodeBlock>): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgMaskClass)
        builder.indent()
        builder.add("id = %S,\n", id)
        builder.add("children = listOf(\n")
        builder.indent()
        children.forEachIndexed { index, child ->
            builder.add(child)
            if (index < children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    // ============================================
    // CSS Stylesheet Parsing
    // ============================================

    /**
     * Simple CSS selector representation for code generation.
     */
    private sealed interface CssSelector {
        data class Class(val className: String) : CssSelector
        data class Id(val id: String) : CssSelector
        data class Tag(val tagName: String) : CssSelector
        data object Universal : CssSelector

        val specificity: Int
            get() = when (this) {
                is Universal -> 0
                is Tag -> 1
                is Class -> 2
                is Id -> 3
            }
    }

    private data class CssRule(
        val selector: CssSelector,
        val declarations: Map<String, String>
    )

    private data class CssStylesheet(
        val rules: List<CssRule> = emptyList(),
        val keyframes: List<CssKeyframes> = emptyList()
    )

    // CSS Animation data structures
    private data class CssKeyframe(
        val offset: Float,
        val properties: Map<String, String>
    )

    private data class CssKeyframes(
        val name: String,
        val keyframes: List<CssKeyframe>
    )

    private data class CssAnimation(
        val name: String,
        val duration: Long, // milliseconds
        val timingFunction: String,
        val delay: Long, // milliseconds
        val iterationCount: Int,
        val direction: String = "NORMAL",  // AnimationDirection enum name
        val fillMode: String = "NONE"      // AnimationFillMode enum name
    )

    private val styleTagPattern = Regex("""<style[^>]*>([\s\S]*?)</style>""", RegexOption.IGNORE_CASE)
    private val cssRulePattern = Regex("""([^{@]+)\{([^}]*)\}""")
    private val keyframesPattern = Regex("""@keyframes\s+(\w[\w-]*)\s*\{((?:[^{}]|\{[^{}]*\})*)\}""", RegexOption.IGNORE_CASE)
    private val keyframePattern = Regex("""([\d.]+%|from|to)\s*\{([^}]*)\}""")

    private fun extractStylesheet(svgContent: String): Pair<CssStylesheet, String> {
        val matches = styleTagPattern.findAll(svgContent).toList()
        if (matches.isEmpty()) {
            return CssStylesheet() to svgContent
        }

        val rules = mutableListOf<CssRule>()
        val keyframes = mutableListOf<CssKeyframes>()

        for (match in matches) {
            val cssContent = match.groupValues[1]
            // Parse @keyframes first
            keyframes.addAll(parseCssKeyframes(cssContent))
            // Parse regular CSS rules (excluding @keyframes content)
            val cssWithoutKeyframes = cssContent.replace(keyframesPattern, "")
            rules.addAll(parseCssRules(cssWithoutKeyframes))
        }

        val cleanedContent = svgContent.replace(styleTagPattern, "")
        return CssStylesheet(rules, keyframes) to cleanedContent
    }

    private fun parseCssKeyframes(cssContent: String): List<CssKeyframes> {
        val result = mutableListOf<CssKeyframes>()

        keyframesPattern.findAll(cssContent).forEach { match ->
            val name = match.groupValues[1]
            val keyframesContent = match.groupValues[2]
            val keyframes = parseKeyframeList(keyframesContent)
            if (keyframes.isNotEmpty()) {
                result.add(CssKeyframes(name, keyframes))
            }
        }

        return result
    }

    private fun parseKeyframeList(content: String): List<CssKeyframe> {
        val keyframes = mutableListOf<CssKeyframe>()

        keyframePattern.findAll(content).forEach { match ->
            val offsetStr = match.groupValues[1].trim().lowercase()
            val properties = parseCssStyleAttribute(match.groupValues[2])

            val offset = when (offsetStr) {
                "from" -> 0f
                "to" -> 1f
                else -> offsetStr.removeSuffix("%").toFloatOrNull()?.div(100f)
            }

            if (offset != null && properties.isNotEmpty()) {
                keyframes.add(CssKeyframe(offset, properties))
            }
        }

        return keyframes.sortedBy { it.offset }
    }

    private fun parseCssRules(cssContent: String): List<CssRule> {
        val rules = mutableListOf<CssRule>()

        cssRulePattern.findAll(cssContent).forEach { match ->
            val selectorStr = match.groupValues[1].trim()
            val declarationsStr = match.groupValues[2]

            val selector = parseCssSelector(selectorStr)
            if (selector != null) {
                val declarations = parseCssStyleAttribute(declarationsStr)
                if (declarations.isNotEmpty()) {
                    rules.add(CssRule(selector, declarations))
                }
            }
        }

        return rules
    }

    private fun parseCssSelector(selectorStr: String): CssSelector? {
        val trimmed = selectorStr.trim()
        return when {
            trimmed.startsWith(".") -> CssSelector.Class(trimmed.drop(1))
            trimmed.startsWith("#") -> CssSelector.Id(trimmed.drop(1))
            trimmed == "*" -> CssSelector.Universal
            trimmed.matches(Regex("[a-zA-Z][a-zA-Z0-9-]*")) -> CssSelector.Tag(trimmed.lowercase())
            else -> null
        }
    }

    private fun resolveStylesForElement(
        tagName: String,
        attrs: Map<String, String>,
        stylesheet: CssStylesheet
    ): Map<String, String> {
        val elementId = attrs["id"]
        val elementClasses = attrs["class"]?.split(Regex("\\s+"))?.filter { it.isNotEmpty() } ?: emptyList()

        val matchingRules = stylesheet.rules
            .filter { rule ->
                when (rule.selector) {
                    is CssSelector.Universal -> true
                    is CssSelector.Tag -> rule.selector.tagName == tagName.lowercase()
                    is CssSelector.Class -> elementClasses.contains(rule.selector.className)
                    is CssSelector.Id -> elementId == rule.selector.id
                }
            }
            .sortedBy { it.selector.specificity }

        val mergedStyles = mutableMapOf<String, String>()
        for (rule in matchingRules) {
            mergedStyles.putAll(rule.declarations)
        }

        val inlineStyle = parseCssStyleAttribute(attrs["style"])
        mergedStyles.putAll(inlineStyle)

        return mergedStyles
    }

    // ============================================
    // Style Wrapping
    // ============================================

    /**
     * Parses CSS inline style string into a Map.
     * Example: "fill:red; stroke:blue; stroke-width:2" -> {fill=red, stroke=blue, stroke-width=2}
     */
    private fun parseCssStyleAttribute(styleStr: String?): Map<String, String> {
        if (styleStr.isNullOrBlank()) return emptyMap()

        val cssProperties = mutableMapOf<String, String>()
        val declarations = styleStr.split(";")

        for (declaration in declarations) {
            val colonIndex = declaration.indexOf(':')
            if (colonIndex > 0) {
                val property = declaration.substring(0, colonIndex).trim().lowercase()
                val value = declaration.substring(colonIndex + 1).trim()
                if (property.isNotEmpty() && value.isNotEmpty()) {
                    cssProperties[property] = value
                }
            }
        }

        return cssProperties
    }

    private fun wrapWithStyle(element: CodeBlock, attrs: Map<String, String>): CodeBlock {
        // Parse inline style attribute if present and merge with XML attributes
        // CSS style takes precedence over XML attributes
        val cssStyles = parseCssStyleAttribute(attrs["style"])
        val mergedAttrs = if (cssStyles.isNotEmpty()) {
            attrs.toMutableMap().apply {
                putAll(cssStyles)
                remove("style")
            }
        } else {
            attrs
        }

        val styleParts = mutableListOf<CodeBlock>()

        // fill/stroke: skip "inherit" (use parent's value)
        mergedAttrs["fill"]?.takeIf { it != "inherit" }?.let { styleParts.add(generateColorCodeBlock("fill", it)) }
        mergedAttrs["stroke"]?.takeIf { it != "inherit" }?.let { styleParts.add(generateColorCodeBlock("stroke", it)) }
        mergedAttrs["stroke-width"]?.takeIf { it != "inherit" }?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("strokeWidth = %Lf", it)) }
        mergedAttrs["opacity"]?.takeIf { it != "inherit" }?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("opacity = %Lf", it)) }
        mergedAttrs["fill-opacity"]?.takeIf { it != "inherit" }?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("fillOpacity = %Lf", it)) }
        mergedAttrs["stroke-opacity"]?.takeIf { it != "inherit" }?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("strokeOpacity = %Lf", it)) }

        mergedAttrs["stroke-linecap"]?.takeIf { it != "inherit" }?.lowercase()?.let { cap ->
            val capName = when (cap) {
                "butt" -> "BUTT"
                "square" -> "SQUARE"
                "round" -> "ROUND"
                else -> null
            }
            capName?.let { styleParts.add(CodeBlock.of("strokeLinecap = %T.%L", lineCapClass, it)) }
        }

        mergedAttrs["stroke-linejoin"]?.takeIf { it != "inherit" }?.lowercase()?.let { join ->
            val joinName = when (join) {
                "miter" -> "MITER"
                "bevel" -> "BEVEL"
                "round" -> "ROUND"
                else -> null
            }
            joinName?.let { styleParts.add(CodeBlock.of("strokeLinejoin = %T.%L", lineJoinClass, it)) }
        }

        mergedAttrs["fill-rule"]?.takeIf { it != "inherit" }?.lowercase()?.let { rule ->
            val ruleName = when (rule) {
                "evenodd" -> "EVENODD"
                "nonzero" -> "NONZERO"
                else -> null
            }
            ruleName?.let { styleParts.add(CodeBlock.of("fillRule = %T.%L", fillRuleClass, it)) }
        }

        mergedAttrs["paint-order"]?.takeIf { it != "inherit" }?.let { paintOrder ->
            val orderName = parsePaintOrder(paintOrder)
            orderName?.let { styleParts.add(CodeBlock.of("paintOrder = %T.%L", paintOrderClass, it)) }
        }

        if (styleParts.isEmpty()) {
            return element
        }

        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgStyledClass)
        builder.indent()
        builder.add(element)
        builder.add(",\n%T(", svgStyleClass)
        styleParts.forEachIndexed { index, part ->
            builder.add(part)
            if (index < styleParts.size - 1) builder.add(", ")
        }
        builder.add(")")
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    /**
     * Wraps element with style, resolving styles from stylesheet + inline styles.
     */
    private fun wrapWithStyleFromStylesheet(
        element: CodeBlock,
        tagName: String,
        attrs: Map<String, String>,
        stylesheet: CssStylesheet
    ): CodeBlock {
        val resolvedStyles = resolveStylesForElement(tagName, attrs, stylesheet)
        val mergedAttrs = attrs.toMutableMap()
        mergedAttrs.putAll(resolvedStyles)
        mergedAttrs.remove("style")

        // Check for CSS animation property
        val animationValue = mergedAttrs["animation"]
        val animations = mutableListOf<CodeBlock>()

        if (animationValue != null) {
            val cssAnimation = parseCssAnimation(animationValue)
            if (cssAnimation != null) {
                val keyframes = stylesheet.keyframes.find { it.name == cssAnimation.name }
                if (keyframes != null) {
                    animations.addAll(cssAnimationToCodeBlocks(cssAnimation, keyframes))
                }
            }
            mergedAttrs.remove("animation")
        }

        var result = wrapWithStyleFromMergedAttrs(element, mergedAttrs)

        // Wrap with animations if any
        if (animations.isNotEmpty()) {
            result = wrapWithAnimations(result, animations)
        }

        return result
    }

    private fun parseCssAnimation(value: String): CssAnimation? {
        var processedValue = value.trim()

        var name: String? = null
        var duration: Long = 0
        var timingFunction = "ease"
        var delay: Long = 0
        var iterationCount = 1
        var direction = "NORMAL"
        var fillMode = "NONE"

        // Extract cubic-bezier() or steps() functions before splitting on whitespace
        val cubicBezierRegex = Regex("""cubic-bezier\s*\([^)]+\)""", RegexOption.IGNORE_CASE)
        val stepsRegex = Regex("""steps\s*\([^)]+\)""", RegexOption.IGNORE_CASE)

        cubicBezierRegex.find(processedValue)?.let { match ->
            timingFunction = match.value.replace(Regex("\\s+"), "").lowercase()
            processedValue = processedValue.replace(match.value, "")
        }

        stepsRegex.find(processedValue)?.let { match ->
            timingFunction = match.value.replace(Regex("\\s+"), "").lowercase()
            processedValue = processedValue.replace(match.value, "")
        }

        val parts = processedValue.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (parts.isEmpty()) return null

        for (part in parts) {
            val lower = part.lowercase()
            when {
                // Direction (check before duration since some end with 's')
                lower == "normal" -> direction = "NORMAL"
                lower == "reverse" -> direction = "REVERSE"
                lower == "alternate" -> direction = "ALTERNATE"
                lower == "alternate-reverse" -> direction = "ALTERNATE_REVERSE"
                // Fill mode (check before duration since 'forwards'/'backwards' end with 's')
                lower == "forwards" -> fillMode = "FORWARDS"
                lower == "backwards" -> fillMode = "BACKWARDS"
                lower == "both" -> fillMode = "BOTH"
                // Timing function (check before duration)
                lower in listOf("linear", "ease", "ease-in", "ease-out", "ease-in-out") -> {
                    timingFunction = lower
                }
                // Duration/delay (e.g., "1s", "500ms")
                lower.endsWith("ms") -> {
                    val ms = lower.removeSuffix("ms").toFloatOrNull()?.toLong()
                    if (ms != null) {
                        if (duration == 0L) duration = ms else delay = ms
                    }
                }
                lower.endsWith("s") && !lower.endsWith("ms") -> {
                    val sec = lower.removeSuffix("s").toFloatOrNull()
                    if (sec != null) {
                        val ms = (sec * 1000).toLong()
                        if (duration == 0L) duration = ms else delay = ms
                    }
                }
                // Iteration count
                lower == "infinite" -> iterationCount = -1  // INFINITE
                lower.toIntOrNull() != null -> iterationCount = lower.toInt()
                // Animation name
                name == null && !lower.matches(Regex("^[0-9].*")) -> name = part
            }
        }

        return name?.let { CssAnimation(it, duration, timingFunction, delay, iterationCount, direction, fillMode) }
    }

    private fun cssAnimationToCodeBlocks(animation: CssAnimation, keyframes: CssKeyframes): List<CodeBlock> {
        val result = mutableListOf<CodeBlock>()

        // Generate calcMode and keySplines based on timing function
        val (calcModeStr, keySplinesCode) = when {
            animation.timingFunction == "linear" -> "LINEAR" to null
            animation.timingFunction == "ease" -> "SPLINE" to CodeBlock.of("%T.EASE", keySplinesClass)
            animation.timingFunction == "ease-in" -> "SPLINE" to CodeBlock.of("%T.EASE_IN", keySplinesClass)
            animation.timingFunction == "ease-out" -> "SPLINE" to CodeBlock.of("%T.EASE_OUT", keySplinesClass)
            animation.timingFunction == "ease-in-out" -> "SPLINE" to CodeBlock.of("%T.EASE_IN_OUT", keySplinesClass)
            animation.timingFunction.startsWith("cubic-bezier(") -> {
                val params = animation.timingFunction
                    .removePrefix("cubic-bezier(")
                    .removeSuffix(")")
                    .split(",")
                    .mapNotNull { it.trim().toFloatOrNull() }
                if (params.size == 4) {
                    "SPLINE" to CodeBlock.of("%T(%Lf, %Lf, %Lf, %Lf)",
                        keySplinesClass, params[0], params[1], params[2], params[3])
                } else {
                    "LINEAR" to null
                }
            }
            else -> "LINEAR" to null
        }

        // Group keyframe properties
        val propertyKeyframes = mutableMapOf<String, MutableList<Pair<Float, String>>>()
        for (kf in keyframes.keyframes) {
            for ((prop, value) in kf.properties) {
                propertyKeyframes.getOrPut(prop) { mutableListOf() }.add(kf.offset to value)
            }
        }

        // Generate animation CodeBlocks for each property
        for ((prop, frames) in propertyKeyframes) {
            if (frames.size < 2) continue

            val fromValue = frames.first().second
            val toValue = frames.last().second

            val animCode = when (prop.lowercase()) {
                "opacity" -> {
                    val from = fromValue.toFloatOrNull() ?: 1f
                    val to = toValue.toFloatOrNull() ?: 1f
                    generateOpacityAnimation(from, to, animation.duration, animation.delay, calcModeStr, keySplinesCode,
                        animation.iterationCount, animation.direction, animation.fillMode)
                }
                "transform" -> {
                    val fromTransform = parseTransformAnimation(fromValue)
                    val toTransform = parseTransformAnimation(toValue)
                    if (fromTransform != null && toTransform != null && fromTransform.first == toTransform.first) {
                        generateTransformAnimation(fromTransform.first, fromTransform.second, toTransform.second,
                            animation.duration, animation.delay, calcModeStr, keySplinesCode,
                            animation.iterationCount, animation.direction, animation.fillMode)
                    } else null
                }
                "stroke-width" -> {
                    val from = fromValue.toFloatOrNull() ?: 2f
                    val to = toValue.toFloatOrNull() ?: 2f
                    generateStrokeWidthAnimation(from, to, animation.duration, animation.delay, calcModeStr, keySplinesCode,
                        animation.iterationCount, animation.direction, animation.fillMode)
                }
                else -> null
            }

            if (animCode != null) result.add(animCode)
        }

        return result
    }

    private fun parseTransformAnimation(value: String): Pair<String, Float>? {
        val trimmed = value.trim().lowercase()
        return when {
            trimmed.startsWith("rotate(") -> {
                val angle = trimmed.removePrefix("rotate(").removeSuffix(")").removeSuffix("deg").trim().toFloatOrNull()
                angle?.let { "ROTATE" to it }
            }
            trimmed.startsWith("scale(") -> {
                val scale = trimmed.removePrefix("scale(").removeSuffix(")").trim().toFloatOrNull()
                scale?.let { "SCALE" to it }
            }
            trimmed.startsWith("translatex(") -> {
                val v = trimmed.removePrefix("translatex(").removeSuffix(")").removeSuffix("px").trim().toFloatOrNull()
                v?.let { "TRANSLATE_X" to it }
            }
            trimmed.startsWith("translatey(") -> {
                val v = trimmed.removePrefix("translatey(").removeSuffix(")").removeSuffix("px").trim().toFloatOrNull()
                v?.let { "TRANSLATE_Y" to it }
            }
            else -> null
        }
    }

    private fun generateOpacityAnimation(from: Float, to: Float, durMs: Long, delayMs: Long, calcMode: String, keySplines: CodeBlock?, iterations: Int, direction: String, fillMode: String): CodeBlock {
        return if (keySplines != null) {
            CodeBlock.of("%T.Opacity(%Lf, %Lf, %L.milliseconds, %L.milliseconds, %T.%L, %L, %L, %T.%L, %T.%L)",
                svgAnimateClass, from, to, durMs, delayMs, calcModeClass, calcMode, keySplines, iterations,
                animationDirectionClass, direction, animationFillModeClass, fillMode)
        } else {
            CodeBlock.of("%T.Opacity(%Lf, %Lf, %L.milliseconds, %L.milliseconds, %T.%L, null, %L, %T.%L, %T.%L)",
                svgAnimateClass, from, to, durMs, delayMs, calcModeClass, calcMode, iterations,
                animationDirectionClass, direction, animationFillModeClass, fillMode)
        }
    }

    private fun generateTransformAnimation(type: String, from: Float, to: Float, durMs: Long, delayMs: Long, calcMode: String, keySplines: CodeBlock?, iterations: Int, direction: String, fillMode: String): CodeBlock {
        return if (keySplines != null) {
            CodeBlock.of("%T.Transform(%T.%L, %Lf, %Lf, %L.milliseconds, %L.milliseconds, %T.%L, %L, %L, %T.%L, %T.%L)",
                svgAnimateClass, transformTypeClass, type, from, to, durMs, delayMs, calcModeClass, calcMode, keySplines, iterations,
                animationDirectionClass, direction, animationFillModeClass, fillMode)
        } else {
            CodeBlock.of("%T.Transform(%T.%L, %Lf, %Lf, %L.milliseconds, %L.milliseconds, %T.%L, null, %L, %T.%L, %T.%L)",
                svgAnimateClass, transformTypeClass, type, from, to, durMs, delayMs, calcModeClass, calcMode, iterations,
                animationDirectionClass, direction, animationFillModeClass, fillMode)
        }
    }

    private fun generateStrokeWidthAnimation(from: Float, to: Float, durMs: Long, delayMs: Long, calcMode: String, keySplines: CodeBlock?, iterations: Int, direction: String, fillMode: String): CodeBlock {
        return if (keySplines != null) {
            CodeBlock.of("%T.StrokeWidth(%Lf, %Lf, %L.milliseconds, %L.milliseconds, %T.%L, %L, %L, %T.%L, %T.%L)",
                svgAnimateClass, from, to, durMs, delayMs, calcModeClass, calcMode, keySplines, iterations,
                animationDirectionClass, direction, animationFillModeClass, fillMode)
        } else {
            CodeBlock.of("%T.StrokeWidth(%Lf, %Lf, %L.milliseconds, %L.milliseconds, %T.%L, null, %L, %T.%L, %T.%L)",
                svgAnimateClass, from, to, durMs, delayMs, calcModeClass, calcMode, iterations,
                animationDirectionClass, direction, animationFillModeClass, fillMode)
        }
    }

    /**
     * Wraps element with style from pre-merged attributes.
     */
    private fun wrapWithStyleFromMergedAttrs(element: CodeBlock, mergedAttrs: Map<String, String>): CodeBlock {
        val styleParts = mutableListOf<CodeBlock>()

        // fill/stroke: skip "inherit" (use parent's value)
        mergedAttrs["fill"]?.takeIf { it != "inherit" }?.let { styleParts.add(generateColorCodeBlock("fill", it)) }
        mergedAttrs["stroke"]?.takeIf { it != "inherit" }?.let { styleParts.add(generateColorCodeBlock("stroke", it)) }
        mergedAttrs["stroke-width"]?.takeIf { it != "inherit" }?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("strokeWidth = %Lf", it)) }
        mergedAttrs["opacity"]?.takeIf { it != "inherit" }?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("opacity = %Lf", it)) }
        mergedAttrs["fill-opacity"]?.takeIf { it != "inherit" }?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("fillOpacity = %Lf", it)) }
        mergedAttrs["stroke-opacity"]?.takeIf { it != "inherit" }?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("strokeOpacity = %Lf", it)) }

        mergedAttrs["stroke-linecap"]?.takeIf { it != "inherit" }?.lowercase()?.let { cap ->
            val capName = when (cap) {
                "butt" -> "BUTT"
                "square" -> "SQUARE"
                "round" -> "ROUND"
                else -> null
            }
            capName?.let { styleParts.add(CodeBlock.of("strokeLinecap = %T.%L", lineCapClass, it)) }
        }

        mergedAttrs["stroke-linejoin"]?.takeIf { it != "inherit" }?.lowercase()?.let { join ->
            val joinName = when (join) {
                "miter" -> "MITER"
                "bevel" -> "BEVEL"
                "round" -> "ROUND"
                else -> null
            }
            joinName?.let { styleParts.add(CodeBlock.of("strokeLinejoin = %T.%L", lineJoinClass, it)) }
        }

        mergedAttrs["fill-rule"]?.takeIf { it != "inherit" }?.lowercase()?.let { rule ->
            val ruleName = when (rule) {
                "evenodd" -> "EVENODD"
                "nonzero" -> "NONZERO"
                else -> null
            }
            ruleName?.let { styleParts.add(CodeBlock.of("fillRule = %T.%L", fillRuleClass, it)) }
        }

        mergedAttrs["paint-order"]?.takeIf { it != "inherit" }?.let { paintOrder ->
            val orderName = parsePaintOrder(paintOrder)
            orderName?.let { styleParts.add(CodeBlock.of("paintOrder = %T.%L", paintOrderClass, it)) }
        }

        if (styleParts.isEmpty()) {
            return element
        }

        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgStyledClass)
        builder.indent()
        builder.add(element)
        builder.add(",\n%T(", svgStyleClass)
        styleParts.forEachIndexed { index, part ->
            builder.add(part)
            if (index < styleParts.size - 1) builder.add(", ")
        }
        builder.add(")")
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    /**
     * Parses SVG paint-order attribute.
     * Returns PaintOrder enum name or null.
     */
    private fun parsePaintOrder(value: String): String? {
        val trimmed = value.trim().lowercase()
        return when {
            trimmed == "normal" -> "FILL_STROKE"
            trimmed.startsWith("stroke") -> "STROKE_FILL"
            trimmed.startsWith("fill") -> "FILL_STROKE"
            else -> null
        }
    }

    // ============================================
    // Animation Parsing and Generation
    // ============================================

    private fun parseAnimations(innerContent: String, attrs: Map<String, String>): List<CodeBlock> {
        val animations = mutableListOf<CodeBlock>()

        // Parse <animate> elements
        animatePattern.findAll(innerContent).forEach { match ->
            val animAttrs = parseAttributes(match.groupValues[1])
            val anim = generateAnimateCodeBlock(animAttrs)
            if (anim != null) {
                animations.add(anim)
            }
        }

        // Parse <animateTransform> elements
        animateTransformPattern.findAll(innerContent).forEach { match ->
            val animAttrs = parseAttributes(match.groupValues[1])
            val anim = generateAnimateTransformCodeBlock(animAttrs)
            if (anim != null) {
                animations.add(anim)
            }
        }

        return animations
    }

    private fun generateAnimateCodeBlock(attrs: Map<String, String>): CodeBlock? {
        val attributeName = attrs["attributeName"] ?: return null
        val dur = parseDuration(attrs["dur"] ?: "0s")
        val delay = parseDuration(attrs["begin"] ?: "0s")

        // Parse calcMode and keySplines
        val calcMode = attrs["calcMode"]?.uppercase() ?: "LINEAR"
        val keySplines = parseKeySplines(attrs["keySplines"])

        // Parse from/to values
        val from = attrs["from"]?.toFloatOrNull()
        val to = attrs["to"]?.toFloatOrNull()
        val values = attrs["values"]?.split(";")?.mapNotNull { it.trim().toFloatOrNull() }

        val effectiveFrom: Float?
        val effectiveTo: Float?
        if (from != null && to != null) {
            effectiveFrom = from
            effectiveTo = to
        } else if (values != null && values.size >= 2) {
            effectiveFrom = values.minOrNull()
            effectiveTo = values.maxOrNull()
        } else {
            effectiveFrom = from ?: values?.firstOrNull()
            effectiveTo = to ?: values?.lastOrNull()
        }

        return when (attributeName.lowercase()) {
            "opacity" -> if (effectiveFrom != null && effectiveTo != null) {
                generateAnimateWithEasing("Opacity", dur, delay, calcMode, keySplines,
                    "from" to effectiveFrom, "to" to effectiveTo)
            } else null
            "stroke-width" -> if (effectiveFrom != null && effectiveTo != null) {
                generateAnimateWithEasing("StrokeWidth", dur, delay, calcMode, keySplines,
                    "from" to effectiveFrom, "to" to effectiveTo)
            } else null
            "stroke-opacity" -> if (effectiveFrom != null && effectiveTo != null) {
                generateAnimateWithEasing("StrokeOpacity", dur, delay, calcMode, keySplines,
                    "from" to effectiveFrom, "to" to effectiveTo)
            } else null
            "fill-opacity" -> if (effectiveFrom != null && effectiveTo != null) {
                generateAnimateWithEasing("FillOpacity", dur, delay, calcMode, keySplines,
                    "from" to effectiveFrom, "to" to effectiveTo)
            } else null
            "stroke-dashoffset" -> {
                // stroke-dashoffset from high to low (e.g., 50 to 0) = stroke appearing (normal)
                // stroke-dashoffset from low to high (e.g., 0 to 50) = stroke disappearing (reverse)
                val reverse = (effectiveFrom ?: 0f) < (effectiveTo ?: 0f)
                generateStrokeDrawCodeBlock(dur, delay, reverse, calcMode, keySplines)
            }
            else -> null
        }
    }

    private fun generateAnimateTransformCodeBlock(attrs: Map<String, String>): CodeBlock? {
        val type = attrs["type"]?.lowercase() ?: return null
        val dur = parseDuration(attrs["dur"] ?: "0s")
        val delay = parseDuration(attrs["begin"] ?: "0s")

        val calcMode = attrs["calcMode"]?.uppercase() ?: "LINEAR"
        val keySplines = parseKeySplines(attrs["keySplines"])

        val fromStr = attrs["from"]
        val toStr = attrs["to"]
        val valuesStr = attrs["values"]

        val transformType = when (type) {
            "translate" -> "TRANSLATE"
            "scale" -> "SCALE"
            "rotate" -> "ROTATE"
            "skewx" -> "SKEW_X"
            "skewy" -> "SKEW_Y"
            else -> return null
        }

        val from: Float?
        val to: Float?

        if (fromStr != null && toStr != null) {
            from = parseTransformValue(fromStr)
            to = parseTransformValue(toStr)
        } else if (valuesStr != null) {
            val keyframes = valuesStr.split(";").mapNotNull { parseTransformValue(it.trim()) }
            if (keyframes.size >= 2) {
                from = keyframes.minOrNull()
                to = keyframes.maxOrNull()
            } else {
                from = keyframes.firstOrNull()
                to = keyframes.lastOrNull()
            }
        } else {
            from = parseTransformValue(fromStr)
            to = parseTransformValue(toStr)
        }

        if (from == null || to == null) return null

        return generateTransformCodeBlock(transformType, from, to, dur, delay, calcMode, keySplines)
    }

    private fun generateAnimateWithEasing(
        animType: String,
        dur: Int,
        delay: Int,
        calcMode: String,
        keySplines: List<Float>?,
        vararg params: Pair<String, Float>
    ): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T.%L(", svgAnimateClass, animType)

        params.forEach { (name, value) ->
            builder.add("%L = %Lf, ", name, value)
        }

        builder.add("dur = %L.%M, delay = %L.%M", dur, millisecondsProperty, delay, millisecondsProperty)

        if (calcMode != "LINEAR") {
            builder.add(", calcMode = %T.%L", calcModeClass, calcMode)
        }

        if (keySplines != null && keySplines.size >= 4) {
            builder.add(", keySplines = %T(%Lf, %Lf, %Lf, %Lf)",
                keySplinesClass, keySplines[0], keySplines[1], keySplines[2], keySplines[3])
        }

        builder.add(")")
        return builder.build()
    }

    private fun generateStrokeDrawCodeBlock(dur: Int, delay: Int, reverse: Boolean, calcMode: String, keySplines: List<Float>?): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T.StrokeDraw(dur = %L.%M, delay = %L.%M",
            svgAnimateClass, dur, millisecondsProperty, delay, millisecondsProperty)

        if (reverse) {
            builder.add(", reverse = true")
        }

        if (calcMode != "LINEAR") {
            builder.add(", calcMode = %T.%L", calcModeClass, calcMode)
        }

        if (keySplines != null && keySplines.size >= 4) {
            builder.add(", keySplines = %T(%Lf, %Lf, %Lf, %Lf)",
                keySplinesClass, keySplines[0], keySplines[1], keySplines[2], keySplines[3])
        }

        builder.add(")")
        return builder.build()
    }

    private fun generateTransformCodeBlock(
        transformType: String,
        from: Float,
        to: Float,
        dur: Int,
        delay: Int,
        calcMode: String,
        keySplines: List<Float>?
    ): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T.Transform(type = %T.%L, from = %Lf, to = %Lf, dur = %L.%M, delay = %L.%M",
            svgAnimateClass, transformTypeClass, transformType, from, to, dur, millisecondsProperty, delay, millisecondsProperty)

        if (calcMode != "LINEAR") {
            builder.add(", calcMode = %T.%L", calcModeClass, calcMode)
        }

        if (keySplines != null && keySplines.size >= 4) {
            builder.add(", keySplines = %T(%Lf, %Lf, %Lf, %Lf)",
                keySplinesClass, keySplines[0], keySplines[1], keySplines[2], keySplines[3])
        }

        builder.add(")")
        return builder.build()
    }

    private fun wrapWithAnimations(element: CodeBlock, animations: List<CodeBlock>): CodeBlock {
        if (animations.isEmpty()) return element

        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgAnimatedClass)
        builder.indent()
        builder.add("element = ")
        builder.add(element)
        builder.add(",\n")
        builder.add("animations = listOf(\n")
        builder.indent()
        animations.forEachIndexed { index, anim ->
            builder.add(anim)
            if (index < animations.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    // ============================================
    // Path Command Parsing
    // ============================================

    private fun parsePathCommands(pathData: String): List<CodeBlock> {
        val commands = mutableListOf<CodeBlock>()
        val tokens = tokenizePathData(pathData)
        var i = 0
        var lastCommand = ' '

        try {
            while (i < tokens.size) {
                val token = tokens[i]
                val command = if (token.length == 1 && token[0].isLetter()) {
                    i++
                    token[0]
                } else {
                    when (lastCommand) {
                        'M' -> 'L'
                        'm' -> 'l'
                        ' ' -> break
                        else -> lastCommand
                    }
                }

                when (command) {
                    'M' -> {
                        val x = tokens[i++].toFloat()
                        val y = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.MoveTo(%Lf, %Lf)", pathCommandClass, x, y))
                    }
                    'm' -> {
                        val dx = tokens[i++].toFloat()
                        val dy = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.MoveToRelative(%Lf, %Lf)", pathCommandClass, dx, dy))
                    }
                    'L' -> {
                        val x = tokens[i++].toFloat()
                        val y = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.LineTo(%Lf, %Lf)", pathCommandClass, x, y))
                    }
                    'l' -> {
                        val dx = tokens[i++].toFloat()
                        val dy = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.LineToRelative(%Lf, %Lf)", pathCommandClass, dx, dy))
                    }
                    'H' -> {
                        val x = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.HorizontalLineTo(%Lf)", pathCommandClass, x))
                    }
                    'h' -> {
                        val dx = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.HorizontalLineToRelative(%Lf)", pathCommandClass, dx))
                    }
                    'V' -> {
                        val y = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.VerticalLineTo(%Lf)", pathCommandClass, y))
                    }
                    'v' -> {
                        val dy = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.VerticalLineToRelative(%Lf)", pathCommandClass, dy))
                    }
                    'C' -> {
                        val x1 = tokens[i++].toFloat()
                        val y1 = tokens[i++].toFloat()
                        val x2 = tokens[i++].toFloat()
                        val y2 = tokens[i++].toFloat()
                        val x = tokens[i++].toFloat()
                        val y = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.CubicTo(%Lf, %Lf, %Lf, %Lf, %Lf, %Lf)",
                            pathCommandClass, x1, y1, x2, y2, x, y))
                    }
                    'c' -> {
                        val dx1 = tokens[i++].toFloat()
                        val dy1 = tokens[i++].toFloat()
                        val dx2 = tokens[i++].toFloat()
                        val dy2 = tokens[i++].toFloat()
                        val dx = tokens[i++].toFloat()
                        val dy = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.CubicToRelative(%Lf, %Lf, %Lf, %Lf, %Lf, %Lf)",
                            pathCommandClass, dx1, dy1, dx2, dy2, dx, dy))
                    }
                    'S' -> {
                        val x2 = tokens[i++].toFloat()
                        val y2 = tokens[i++].toFloat()
                        val x = tokens[i++].toFloat()
                        val y = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.SmoothCubicTo(%Lf, %Lf, %Lf, %Lf)",
                            pathCommandClass, x2, y2, x, y))
                    }
                    's' -> {
                        val dx2 = tokens[i++].toFloat()
                        val dy2 = tokens[i++].toFloat()
                        val dx = tokens[i++].toFloat()
                        val dy = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.SmoothCubicToRelative(%Lf, %Lf, %Lf, %Lf)",
                            pathCommandClass, dx2, dy2, dx, dy))
                    }
                    'Q' -> {
                        val x1 = tokens[i++].toFloat()
                        val y1 = tokens[i++].toFloat()
                        val x = tokens[i++].toFloat()
                        val y = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.QuadTo(%Lf, %Lf, %Lf, %Lf)",
                            pathCommandClass, x1, y1, x, y))
                    }
                    'q' -> {
                        val dx1 = tokens[i++].toFloat()
                        val dy1 = tokens[i++].toFloat()
                        val dx = tokens[i++].toFloat()
                        val dy = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.QuadToRelative(%Lf, %Lf, %Lf, %Lf)",
                            pathCommandClass, dx1, dy1, dx, dy))
                    }
                    'T' -> {
                        val x = tokens[i++].toFloat()
                        val y = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.SmoothQuadTo(%Lf, %Lf)", pathCommandClass, x, y))
                    }
                    't' -> {
                        val dx = tokens[i++].toFloat()
                        val dy = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.SmoothQuadToRelative(%Lf, %Lf)", pathCommandClass, dx, dy))
                    }
                    'A' -> {
                        val rx = tokens[i++].toFloat()
                        val ry = tokens[i++].toFloat()
                        val xAxisRotation = tokens[i++].toFloat()
                        val largeArcFlag = tokens[i++].toFloat() != 0f
                        val sweepFlag = tokens[i++].toFloat() != 0f
                        val x = tokens[i++].toFloat()
                        val y = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.ArcTo(%Lf, %Lf, %Lf, %L, %L, %Lf, %Lf)",
                            pathCommandClass, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y))
                    }
                    'a' -> {
                        val rx = tokens[i++].toFloat()
                        val ry = tokens[i++].toFloat()
                        val xAxisRotation = tokens[i++].toFloat()
                        val largeArcFlag = tokens[i++].toFloat() != 0f
                        val sweepFlag = tokens[i++].toFloat() != 0f
                        val dx = tokens[i++].toFloat()
                        val dy = tokens[i++].toFloat()
                        commands.add(CodeBlock.of("%T.ArcToRelative(%Lf, %Lf, %Lf, %L, %L, %Lf, %Lf)",
                            pathCommandClass, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, dx, dy))
                    }
                    'Z', 'z' -> {
                        commands.add(CodeBlock.of("%T.Close", pathCommandClass))
                    }
                }
                lastCommand = command
            }
        } catch (e: Exception) {
            // Return what we have so far
        }

        return commands
    }

    private fun tokenizePathData(pathData: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()

        for (char in pathData) {
            when {
                char.isLetter() -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                    tokens.add(char.toString())
                }
                char == ',' || char == ' ' || char == '\n' || char == '\t' -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                }
                char == '-' -> {
                    if (current.isNotEmpty() && !current.endsWith("e") && !current.endsWith("E")) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                    current.append(char)
                }
                char == '.' -> {
                    if (current.contains('.')) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                    current.append(char)
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            tokens.add(current.toString())
        }

        return tokens
    }

    // ============================================
    // Helper Functions
    // ============================================

    private fun parseAttributes(attrsStr: String): Map<String, String> {
        val attrs = mutableMapOf<String, String>()
        attrPattern.findAll(attrsStr).forEach { match ->
            attrs[match.groupValues[1]] = match.groupValues[2]
        }
        return attrs
    }

    private fun parseViewBox(viewBoxStr: String?): List<Float>? {
        if (viewBoxStr.isNullOrBlank()) return null
        val parts = viewBoxStr.trim().split(Regex("[,\\s]+")).mapNotNull { it.toFloatOrNull() }
        if (parts.size != 4) return null
        return parts
    }

    private fun parseLengthAttribute(value: String?): Float? {
        if (value == null) return null
        val trimmed = value.trim().lowercase()
            .removeSuffix("px")
            .removeSuffix("pt")
            .removeSuffix("em")
            .removeSuffix("%")
            .trim()
        return trimmed.toFloatOrNull()
    }

    private fun parsePoints(pointsStr: String): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        val numbers = pointsStr.trim().split(Regex("[,\\s]+")).mapNotNull { it.toFloatOrNull() }
        for (i in numbers.indices step 2) {
            if (i + 1 < numbers.size) {
                points.add(numbers[i] to numbers[i + 1])
            }
        }
        return points
    }

    private fun parseDuration(durStr: String): Int {
        val trimmed = durStr.trim().lowercase()
        return when {
            trimmed.endsWith("ms") -> trimmed.dropLast(2).toFloatOrNull()?.toInt() ?: 0
            trimmed.endsWith("s") -> ((trimmed.dropLast(1).toFloatOrNull() ?: 0f) * 1000).toInt()
            else -> trimmed.toFloatOrNull()?.toInt() ?: 0
        }
    }

    private fun parseKeySplines(keySplines: String?): List<Float>? {
        if (keySplines.isNullOrBlank()) return null
        val values = keySplines.trim().split(Regex("[\\s,;]+")).mapNotNull { it.toFloatOrNull() }
        return if (values.size >= 4) values.take(4) else null
    }

    private fun parseTransformValue(valueStr: String?): Float? {
        if (valueStr == null) return null
        return valueStr.trim().split(Regex("[\\s,]+")).firstOrNull()?.toFloatOrNull()
    }

    private fun parseAspectRatioAlign(value: String): String {
        return when (value.lowercase()) {
            "none" -> "NONE"
            "xminymin" -> "X_MIN_Y_MIN"
            "xmidymin" -> "X_MID_Y_MIN"
            "xmaxymin" -> "X_MAX_Y_MIN"
            "xminymid" -> "X_MIN_Y_MID"
            "xmidymid" -> "X_MID_Y_MID"
            "xmaxymid" -> "X_MAX_Y_MID"
            "xminymax" -> "X_MIN_Y_MAX"
            "xmidymax" -> "X_MID_Y_MAX"
            "xmaxymax" -> "X_MAX_Y_MAX"
            else -> "X_MID_Y_MID"
        }
    }

    private fun generateColorCodeBlock(name: String, colorStr: String): CodeBlock {
        return when {
            colorStr == "currentColor" -> CodeBlock.of("%L = %T.Unspecified", name, colorClass)
            colorStr == "none" -> CodeBlock.of("%L = %T.Transparent", name, colorClass)
            colorStr == "inherit" -> CodeBlock.of("%L = null", name)
            colorStr.startsWith("#") -> {
                val hex = colorStr.removePrefix("#")
                val colorValue = when (hex.length) {
                    3 -> "FF${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}".uppercase()
                    6 -> "FF${hex}".uppercase()
                    8 -> hex.uppercase()
                    else -> "FF000000"
                }
                // Use Int constructor which properly interprets ARGB values
                CodeBlock.of("%L = %T(0x${colorValue}.toInt())", name, colorClass)
            }
            else -> CodeBlock.of("%L = %T.Unspecified", name, colorClass)
        }
    }

    private fun advancePastElement(remaining: String, tagMatch: MatchResult, selfClosing: Boolean, tagName: String): String {
        return if (selfClosing) {
            remaining.substring(tagMatch.range.last + 1).trim()
        } else {
            val closeTag = "</$tagName>"
            val closeIdx = remaining.indexOf(closeTag, tagMatch.range.last + 1)
            if (closeIdx > 0) {
                remaining.substring(closeIdx + closeTag.length).trim()
            } else {
                remaining.substring(tagMatch.range.last + 1).trim()
            }
        }
    }

    private fun extractElementContent(remaining: String, tagMatch: MatchResult, selfClosing: Boolean, tagName: String): Pair<String, String> {
        return if (selfClosing) {
            "" to remaining.substring(tagMatch.range.last + 1).trim()
        } else {
            val closeTag = "</$tagName>"
            val closeIdx = remaining.indexOf(closeTag, tagMatch.range.last + 1)
            if (closeIdx > 0) {
                val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                innerContent to remaining.substring(closeIdx + closeTag.length).trim()
            } else {
                "" to remaining.substring(tagMatch.range.last + 1).trim()
            }
        }
    }

    private fun findMatchingClose(str: String, startIdx: Int, tagName: String): Int {
        var depth = 1
        var idx = startIdx
        val openPattern = Regex("""<$tagName\b[^>]*>""", RegexOption.IGNORE_CASE)
        val closePattern = Regex("""</$tagName>""", RegexOption.IGNORE_CASE)

        while (idx < str.length && depth > 0) {
            val openMatch = openPattern.find(str, idx)
            val closeMatch = closePattern.find(str, idx)

            if (closeMatch == null) break

            if (openMatch != null && openMatch.range.first < closeMatch.range.first) {
                depth++
                idx = openMatch.range.last + 1
            } else {
                depth--
                if (depth == 0) {
                    return closeMatch.range.first
                }
                idx = closeMatch.range.last + 1
            }
        }

        return -1
    }
}
