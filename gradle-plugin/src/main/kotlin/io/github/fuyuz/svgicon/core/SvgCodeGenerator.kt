package io.github.fuyuz.svgicon.core

import com.squareup.kotlinpoet.*

/**
 * Parses SVG XML and generates KotlinPoet CodeBlocks that reference runtime types.
 * This eliminates the need for duplicate SvgDsl types in the gradle-plugin.
 */
object SvgCodeGenerator {
    // Runtime type references
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
    private val calcModeClass = ClassName("io.github.fuyuz.svgicon.core", "CalcMode")
    private val keySplinesClass = ClassName("io.github.fuyuz.svgicon.core", "KeySplines")
    private val pathCommandClass = ClassName("io.github.fuyuz.svgicon.core", "PathCommand")
    private val lineCapClass = ClassName("io.github.fuyuz.svgicon.core", "LineCap")
    private val lineJoinClass = ClassName("io.github.fuyuz.svgicon.core", "LineJoin")
    private val fillRuleClass = ClassName("io.github.fuyuz.svgicon.core", "FillRule")
    private val clipPathUnitsClass = ClassName("io.github.fuyuz.svgicon.core", "ClipPathUnits")
    private val maskUnitsClass = ClassName("io.github.fuyuz.svgicon.core", "MaskUnits")
    private val svgTransformClass = ClassName("io.github.fuyuz.svgicon.core", "SvgTransform")
    private val viewBoxClass = ClassName("io.github.fuyuz.svgicon.core", "ViewBox")
    private val preserveAspectRatioClass = ClassName("io.github.fuyuz.svgicon.core", "PreserveAspectRatio")
    private val aspectRatioAlignClass = ClassName("io.github.fuyuz.svgicon.core", "AspectRatioAlign")
    private val meetOrSliceClass = ClassName("io.github.fuyuz.svgicon.core", "MeetOrSlice")
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
        var remaining = svgContent.trim()

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
            val children = parseElements(remaining)
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
                parseElements(innerContent)
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

        // fill
        val fill = attrs["fill"] ?: "none"
        if (fill != "none") {
            builder.add(generateColorCodeBlock("fill", fill))
            builder.add(",\n")
        }

        // stroke
        val stroke = attrs["stroke"] ?: "currentColor"
        if (stroke != "currentColor") {
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
    // Style Wrapping
    // ============================================

    private fun wrapWithStyle(element: CodeBlock, attrs: Map<String, String>): CodeBlock {
        val styleParts = mutableListOf<CodeBlock>()

        attrs["fill"]?.let { styleParts.add(generateColorCodeBlock("fill", it)) }
        attrs["stroke"]?.let { styleParts.add(generateColorCodeBlock("stroke", it)) }
        attrs["stroke-width"]?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("strokeWidth = %Lf", it)) }
        attrs["opacity"]?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("opacity = %Lf", it)) }
        attrs["fill-opacity"]?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("fillOpacity = %Lf", it)) }
        attrs["stroke-opacity"]?.toFloatOrNull()?.let { styleParts.add(CodeBlock.of("strokeOpacity = %Lf", it)) }

        attrs["stroke-linecap"]?.lowercase()?.let { cap ->
            val capName = when (cap) {
                "butt" -> "BUTT"
                "square" -> "SQUARE"
                "round" -> "ROUND"
                else -> null
            }
            capName?.let { styleParts.add(CodeBlock.of("strokeLinecap = %T.%L", lineCapClass, it)) }
        }

        attrs["stroke-linejoin"]?.lowercase()?.let { join ->
            val joinName = when (join) {
                "miter" -> "MITER"
                "bevel" -> "BEVEL"
                "round" -> "ROUND"
                else -> null
            }
            joinName?.let { styleParts.add(CodeBlock.of("strokeLinejoin = %T.%L", lineJoinClass, it)) }
        }

        attrs["fill-rule"]?.lowercase()?.let { rule ->
            val ruleName = when (rule) {
                "evenodd" -> "EVENODD"
                "nonzero" -> "NONZERO"
                else -> null
            }
            ruleName?.let { styleParts.add(CodeBlock.of("fillRule = %T.%L", fillRuleClass, it)) }
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
                val reverse = (effectiveFrom ?: 0f) > (effectiveTo ?: 0f)
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
