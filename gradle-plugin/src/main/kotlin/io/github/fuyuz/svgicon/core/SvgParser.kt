package io.github.fuyuz.svgicon.core

/**
 * Represents a parsed SVG document with viewBox and elements.
 *
 * @param viewBox The viewBox of the SVG (minX, minY, width, height), null if not specified
 * @param elements The parsed SVG elements
 */
data class ParsedSvg(
    val viewBox: ViewBox?,
    val elements: List<SvgElement>
)

/**
 * SVG viewBox attribute.
 *
 * @param minX The minimum x coordinate
 * @param minY The minimum y coordinate
 * @param width The width of the viewBox
 * @param height The height of the viewBox
 */
data class ViewBox(
    val minX: Float,
    val minY: Float,
    val width: Float,
    val height: Float
)

/**
 * Parses raw SVG string into a ParsedSvg with viewBox and elements.
 *
 * This function is marked as "unsafe" because it parses arbitrary SVG content
 * at runtime without compile-time guarantees. Use the DSL builder for type-safe
 * SVG construction when possible.
 *
 * Supported elements:
 * - svg (viewBox)
 * - path (d attribute)
 * - circle (cx, cy, r)
 * - ellipse (cx, cy, rx, ry)
 * - rect (x, y, width, height, rx, ry)
 * - line (x1, y1, x2, y2)
 * - polyline (points)
 * - polygon (points)
 * - g (group)
 *
 * @param svg Raw SVG content string (can be full SVG or just inner elements)
 * @return ParsedSvg containing viewBox and elements
 */
fun unsafeSvg(svg: String): ParsedSvg {
    return SvgXmlParser.parseFull(svg)
}

/**
 * Parses raw SVG string into a Svg object.
 *
 * This is the preferred method for parsing SVG content as it returns the
 * complete Svg structure including all SVG root attributes.
 *
 * @param svg Raw SVG content string
 * @return Svg object with parsed attributes and children
 */
fun parseSvg(svg: String): Svg {
    return SvgXmlParser.parseToSvg(svg)
}

internal object SvgXmlParser {
    private val tagPattern = Regex("""<(\w+)([^>]*)(/?>)""")
    private val attrPattern = Regex("""([\w-]+)=["']([^"']*)["']""")
    private val closingTagPattern = Regex("""</(\w+)>""")
    private val animatePattern = Regex("""<animate\s+([^>]*)/?>(</animate>)?""", RegexOption.IGNORE_CASE)
    private val animateTransformPattern = Regex("""<animateTransform\s+([^>]*)/?>(</animateTransform>)?""", RegexOption.IGNORE_CASE)

    /**
     * Parses SVG string to a complete Svg object with all root attributes.
     */
    fun parseToSvg(svgStr: String): Svg {
        var remaining = svgStr.trim()

        // Skip XML declaration and DOCTYPE
        while (remaining.startsWith("<?") || remaining.startsWith("<!")) {
            val endIdx = remaining.indexOf('>') + 1
            if (endIdx > 0) {
                remaining = remaining.substring(endIdx).trim()
            } else {
                break
            }
        }

        val tagMatch = tagPattern.find(remaining) ?: return Svg()
        val tagName = tagMatch.groupValues[1].lowercase()

        if (tagName != "svg") {
            // No svg root element, parse as children only
            val elements = parseElements(remaining)
            return Svg(children = elements)
        }

        val attrsStr = tagMatch.groupValues[2]
        val selfClosing = tagMatch.groupValues[3] == "/>"
        val attrs = parseAttributes(attrsStr)

        // Parse SVG root attributes
        val width = attrs["width"]?.replace("px", "")?.toIntOrNull() ?: 24
        val height = attrs["height"]?.replace("px", "")?.toIntOrNull() ?: 24
        val viewBox = attrs["viewBox"] ?: "0 0 $width $height"
        val fill = attrs["fill"] ?: "none"
        val stroke = attrs["stroke"] ?: "currentColor"
        val strokeWidth = attrs["stroke-width"]?.toFloatOrNull() ?: 2f
        val strokeLinecap = when (attrs["stroke-linecap"]?.lowercase()) {
            "butt" -> LineCap.BUTT
            "square" -> LineCap.SQUARE
            else -> LineCap.ROUND
        }
        val strokeLinejoin = when (attrs["stroke-linejoin"]?.lowercase()) {
            "miter" -> LineJoin.MITER
            "bevel" -> LineJoin.BEVEL
            else -> LineJoin.ROUND
        }

        // Parse children
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

        return Svg(
            width = width,
            height = height,
            viewBox = viewBox,
            fill = fill,
            stroke = stroke,
            strokeWidth = strokeWidth,
            strokeLinecap = strokeLinecap,
            strokeLinejoin = strokeLinejoin,
            children = children
        )
    }

    fun parseFull(svg: String): ParsedSvg {
        var viewBox: ViewBox? = null
        val elements = mutableListOf<SvgElement>()
        var remaining = svg.trim()

        while (remaining.isNotEmpty()) {
            // Skip XML declaration and DOCTYPE
            if (remaining.startsWith("<?") || remaining.startsWith("<!")) {
                val endIdx = remaining.indexOf('>') + 1
                if (endIdx > 0) {
                    remaining = remaining.substring(endIdx).trim()
                    continue
                }
            }

            val tagMatch = tagPattern.find(remaining)
            if (tagMatch == null) {
                break
            }

            val tagName = tagMatch.groupValues[1].lowercase()
            val attrsStr = tagMatch.groupValues[2]
            val selfClosing = tagMatch.groupValues[3] == "/>"

            val attrs = parseAttributes(attrsStr)

            when (tagName) {
                "svg" -> {
                    // Extract viewBox from svg element
                    viewBox = parseViewBox(attrs["viewBox"])

                    // Parse children of svg element
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeTag = "</svg>"
                        val closeIdx = remaining.indexOf(closeTag)
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            elements.addAll(parseElements(innerContent))
                            remaining = remaining.substring(closeIdx + closeTag.length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                "path" -> {
                    val d = attrs["d"] ?: ""
                    if (d.isNotEmpty()) {
                        val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                        val animations = parseAnimations(innerContent)
                        val element = wrapWithStyle(SvgPath(d), attrs)
                        elements.add(wrapWithAnimations(element, animations))
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
                    val animations = parseAnimations(innerContent)
                    val element = wrapWithStyle(SvgCircle(cx, cy, r), attrs)
                    elements.add(wrapWithAnimations(element, animations))
                    remaining = newRemaining
                }
                "ellipse" -> {
                    val cx = attrs["cx"]?.toFloatOrNull() ?: 0f
                    val cy = attrs["cy"]?.toFloatOrNull() ?: 0f
                    val rx = attrs["rx"]?.toFloatOrNull() ?: 0f
                    val ry = attrs["ry"]?.toFloatOrNull() ?: 0f
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent)
                    val element = wrapWithStyle(SvgEllipse(cx, cy, rx, ry), attrs)
                    elements.add(wrapWithAnimations(element, animations))
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
                    val animations = parseAnimations(innerContent)
                    val element = wrapWithStyle(SvgRect(x, y, width, height, rx, ry), attrs)
                    elements.add(wrapWithAnimations(element, animations))
                    remaining = newRemaining
                }
                "line" -> {
                    val x1 = attrs["x1"]?.toFloatOrNull() ?: 0f
                    val y1 = attrs["y1"]?.toFloatOrNull() ?: 0f
                    val x2 = attrs["x2"]?.toFloatOrNull() ?: 0f
                    val y2 = attrs["y2"]?.toFloatOrNull() ?: 0f
                    val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                    val animations = parseAnimations(innerContent)
                    val element = wrapWithStyle(SvgLine(x1, y1, x2, y2), attrs)
                    elements.add(wrapWithAnimations(element, animations))
                    remaining = newRemaining
                }
                "polyline" -> {
                    val points = parsePoints(attrs["points"] ?: "")
                    if (points.isNotEmpty()) {
                        val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                        val animations = parseAnimations(innerContent)
                        val element = wrapWithStyle(SvgPolyline(points), attrs)
                        elements.add(wrapWithAnimations(element, animations))
                        remaining = newRemaining
                    } else {
                        remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                    }
                }
                "polygon" -> {
                    val points = parsePoints(attrs["points"] ?: "")
                    if (points.isNotEmpty()) {
                        val (innerContent, newRemaining) = extractElementContent(remaining, tagMatch, selfClosing, tagName)
                        val animations = parseAnimations(innerContent)
                        val element = wrapWithStyle(SvgPolygon(points), attrs)
                        elements.add(wrapWithAnimations(element, animations))
                        remaining = newRemaining
                    } else {
                        remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                    }
                }
                "g" -> {
                    if (selfClosing) {
                        remaining = remaining.substring(tagMatch.range.last + 1).trim()
                    } else {
                        val closeTag = "</g>"
                        val closeIdx = findMatchingClose(remaining, tagMatch.range.last + 1, "g")
                        if (closeIdx > 0) {
                            val innerContent = remaining.substring(tagMatch.range.last + 1, closeIdx)
                            val children = parseElements(innerContent)
                            elements.add(wrapWithStyle(SvgGroup(children), attrs))
                            remaining = remaining.substring(closeIdx + closeTag.length).trim()
                        } else {
                            remaining = remaining.substring(tagMatch.range.last + 1).trim()
                        }
                    }
                }
                else -> {
                    // Skip unknown elements
                    remaining = advancePastElement(remaining, tagMatch, selfClosing, tagName)
                }
            }
        }

        return ParsedSvg(viewBox, elements)
    }

    /**
     * Parse elements only (without viewBox extraction).
     * Used for parsing inner content.
     */
    fun parseElements(svg: String): List<SvgElement> {
        val result = parseFull(svg)
        return result.elements
    }

    private fun parseViewBox(viewBoxStr: String?): ViewBox? {
        if (viewBoxStr.isNullOrBlank()) return null
        val parts = viewBoxStr.trim().split(Regex("[,\\s]+")).mapNotNull { it.toFloatOrNull() }
        if (parts.size != 4) return null
        return ViewBox(parts[0], parts[1], parts[2], parts[3])
    }

    private fun parseAttributes(attrsStr: String): Map<String, String> {
        val attrs = mutableMapOf<String, String>()
        attrPattern.findAll(attrsStr).forEach { match ->
            attrs[match.groupValues[1]] = match.groupValues[2]
        }
        return attrs
    }

    private fun parsePoints(pointsStr: String): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        val numbers = pointsStr.trim()
            .split(Regex("[,\\s]+"))
            .mapNotNull { it.toFloatOrNull() }

        for (i in numbers.indices step 2) {
            if (i + 1 < numbers.size) {
                points.add(numbers[i] to numbers[i + 1])
            }
        }
        return points
    }

    private fun advancePastElement(
        remaining: String,
        tagMatch: MatchResult,
        selfClosing: Boolean,
        tagName: String
    ): String {
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

    /**
     * Extracts the inner content of an element and advances past it.
     * Returns the inner content and the remaining string.
     */
    private fun extractElementContent(
        remaining: String,
        tagMatch: MatchResult,
        selfClosing: Boolean,
        tagName: String
    ): Pair<String, String> {
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

    /**
     * Parse SMIL animation elements from inner content.
     * Returns a list of SvgAnimate objects.
     */
    private fun parseAnimations(innerContent: String): List<SvgAnimate> {
        val animations = mutableListOf<SvgAnimate>()

        // Parse <animate> elements
        animatePattern.findAll(innerContent).forEach { match ->
            val attrs = parseAttributes(match.groupValues[1])
            val anim = parseAnimateElement(attrs)
            if (anim != null) {
                animations.add(anim)
            }
        }

        // Parse <animateTransform> elements
        animateTransformPattern.findAll(innerContent).forEach { match ->
            val attrs = parseAttributes(match.groupValues[1])
            val anim = parseAnimateTransformElement(attrs)
            if (anim != null) {
                animations.add(anim)
            }
        }

        return animations
    }

    /**
     * Parse an <animate> element.
     */
    private fun parseAnimateElement(attrs: Map<String, String>): SvgAnimate? {
        val attributeName = attrs["attributeName"] ?: return null
        val dur = parseDuration(attrs["dur"] ?: "0s")
        val delay = parseDuration(attrs["begin"] ?: "0s")
        val repeatCount = attrs["repeatCount"]
        val isInfinite = repeatCount == "indefinite"

        // Parse from/to values
        val from = attrs["from"]?.toFloatOrNull()
        val to = attrs["to"]?.toFloatOrNull()

        // Parse values attribute (for keyframe animations like "2;3;2")
        val values = attrs["values"]?.split(";")?.mapNotNull { it.trim().toFloatOrNull() }

        // Determine from and to for value-based animations
        // For oscillating animations (like "1;0.5;1"), use min and max values
        val effectiveFrom: Float?
        val effectiveTo: Float?
        if (from != null && to != null) {
            effectiveFrom = from
            effectiveTo = to
        } else if (values != null && values.size >= 2) {
            // Use min/max for oscillating animations to capture the full range
            effectiveFrom = values.minOrNull()
            effectiveTo = values.maxOrNull()
        } else {
            effectiveFrom = from ?: values?.firstOrNull()
            effectiveTo = to ?: values?.lastOrNull()
        }

        return when (attributeName.lowercase()) {
            "opacity" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.Opacity(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "stroke-width" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.StrokeWidth(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "stroke-opacity" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.StrokeOpacity(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "fill-opacity" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.FillOpacity(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "stroke-dashoffset" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    // If it's a dashoffset animation, use StrokeDraw for drawing effect
                    SvgAnimate.StrokeDraw(dur, delay, reverse = effectiveFrom > effectiveTo)
                } else null
            }
            "cx" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.Cx(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "cy" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.Cy(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "r" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.R(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "x" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.X(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "y" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.Y(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "width" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.Width(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            "height" -> {
                if (effectiveFrom != null && effectiveTo != null) {
                    SvgAnimate.Height(effectiveFrom, effectiveTo, dur, delay)
                } else null
            }
            else -> null
        }
    }

    /**
     * Parse an <animateTransform> element.
     */
    private fun parseAnimateTransformElement(attrs: Map<String, String>): SvgAnimate? {
        val type = attrs["type"]?.lowercase() ?: return null
        val dur = parseDuration(attrs["dur"] ?: "0s")
        val delay = parseDuration(attrs["begin"] ?: "0s")

        // Parse from/to values
        val fromStr = attrs["from"]
        val toStr = attrs["to"]

        // Parse values attribute (for keyframe animations like "-5 12 2;5 12 2;-5 12 2")
        val valuesStr = attrs["values"]

        val transformType = when (type) {
            "translate" -> TransformType.TRANSLATE
            "scale" -> TransformType.SCALE
            "rotate" -> TransformType.ROTATE
            "skewx" -> TransformType.SKEW_X
            "skewy" -> TransformType.SKEW_Y
            else -> return null
        }

        // Extract numeric values - for rotate, the first number is the angle
        val from: Float?
        val to: Float?

        if (fromStr != null && toStr != null) {
            from = parseTransformValue(fromStr, type)
            to = parseTransformValue(toStr, type)
        } else if (valuesStr != null) {
            // Parse all keyframe values and use min/max for oscillating animations
            val keyframes = valuesStr.split(";").mapNotNull { parseTransformValue(it.trim(), type) }
            if (keyframes.size >= 2) {
                from = keyframes.minOrNull()
                to = keyframes.maxOrNull()
            } else {
                from = keyframes.firstOrNull()
                to = keyframes.lastOrNull()
            }
        } else {
            from = parseTransformValue(fromStr, type)
            to = parseTransformValue(toStr, type)
        }

        return if (from != null && to != null) {
            SvgAnimate.Transform(transformType, from, to, dur, delay)
        } else null
    }

    /**
     * Parse a transform value string and extract the primary value.
     */
    private fun parseTransformValue(valueStr: String?, type: String): Float? {
        if (valueStr == null) return null
        val parts = valueStr.trim().split(Regex("[\\s,]+")).mapNotNull { it.toFloatOrNull() }
        return parts.firstOrNull()
    }

    /**
     * Parse duration string (e.g., "1s", "500ms", "0.5s") to milliseconds.
     */
    private fun parseDuration(durStr: String): Int {
        val trimmed = durStr.trim().lowercase()
        return when {
            trimmed.endsWith("ms") -> trimmed.dropLast(2).toFloatOrNull()?.toInt() ?: 0
            trimmed.endsWith("s") -> ((trimmed.dropLast(1).toFloatOrNull() ?: 0f) * 1000).toInt()
            else -> trimmed.toFloatOrNull()?.toInt() ?: 0
        }
    }

    /**
     * Wrap element with animations if any are found.
     */
    private fun wrapWithAnimations(element: SvgElement, animations: List<SvgAnimate>): SvgElement {
        return if (animations.isNotEmpty()) {
            SvgAnimated(element, animations)
        } else {
            element
        }
    }

    private fun findMatchingClose(str: String, startIdx: Int, tagName: String): Int {
        var depth = 1
        var idx = startIdx
        val openPattern = Regex("""<$tagName\b[^>]*>""")
        val closePattern = Regex("""</$tagName>""")

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

    private fun wrapWithStyle(element: SvgElement, attrs: Map<String, String>): SvgElement {
        val style = parseStyle(attrs)
        return if (style != null) {
            SvgStyled(element, style)
        } else {
            element
        }
    }

    private fun parseStyle(attrs: Map<String, String>): SvgStyle? {
        val fill = attrs["fill"]
        val fillOpacity = attrs["fill-opacity"]?.toFloatOrNull()
        val fillRule = when (attrs["fill-rule"]?.lowercase()) {
            "evenodd" -> FillRule.EVENODD
            "nonzero" -> FillRule.NONZERO
            else -> null
        }
        val stroke = attrs["stroke"]
        val strokeWidth = attrs["stroke-width"]?.toFloatOrNull()
        val strokeOpacity = attrs["stroke-opacity"]?.toFloatOrNull()
        val strokeLinecap = when (attrs["stroke-linecap"]?.lowercase()) {
            "butt" -> LineCap.BUTT
            "round" -> LineCap.ROUND
            "square" -> LineCap.SQUARE
            else -> null
        }
        val strokeLinejoin = when (attrs["stroke-linejoin"]?.lowercase()) {
            "miter" -> LineJoin.MITER
            "round" -> LineJoin.ROUND
            "bevel" -> LineJoin.BEVEL
            else -> null
        }
        val strokeDasharray = attrs["stroke-dasharray"]?.let { parseDashArray(it) }
        val strokeDashoffset = attrs["stroke-dashoffset"]?.toFloatOrNull()
        val strokeMiterlimit = attrs["stroke-miterlimit"]?.toFloatOrNull()
        val opacity = attrs["opacity"]?.toFloatOrNull()
        val transform = attrs["transform"]?.let { parseTransform(it) }

        // Only create style if at least one attribute is present
        if (fill == null && fillOpacity == null && fillRule == null &&
            stroke == null && strokeWidth == null && strokeOpacity == null &&
            strokeLinecap == null && strokeLinejoin == null &&
            strokeDasharray == null && strokeDashoffset == null &&
            strokeMiterlimit == null && opacity == null && transform == null) {
            return null
        }

        return SvgStyle(
            fill = fill,
            fillOpacity = fillOpacity,
            fillRule = fillRule,
            stroke = stroke,
            strokeWidth = strokeWidth,
            strokeOpacity = strokeOpacity,
            strokeLinecap = strokeLinecap,
            strokeLinejoin = strokeLinejoin,
            strokeDasharray = strokeDasharray,
            strokeDashoffset = strokeDashoffset,
            strokeMiterlimit = strokeMiterlimit,
            opacity = opacity,
            transform = transform
        )
    }

    private fun parseDashArray(dashStr: String): List<Float>? {
        if (dashStr.isBlank() || dashStr.lowercase() == "none") return null
        return dashStr.split(Regex("[,\\s]+")).mapNotNull { it.toFloatOrNull() }.takeIf { it.isNotEmpty() }
    }

    private fun parseTransform(transformStr: String): SvgTransform? {
        val transforms = mutableListOf<SvgTransform>()
        val pattern = Regex("""(\w+)\s*\(([^)]*)\)""")

        pattern.findAll(transformStr).forEach { match ->
            val type = match.groupValues[1].lowercase()
            val params = match.groupValues[2].split(Regex("[,\\s]+")).mapNotNull { it.toFloatOrNull() }

            when (type) {
                "translate" -> {
                    if (params.isNotEmpty()) {
                        transforms.add(SvgTransform.Translate(params[0], params.getOrElse(1) { 0f }))
                    }
                }
                "scale" -> {
                    if (params.isNotEmpty()) {
                        transforms.add(SvgTransform.Scale(params[0], params.getOrElse(1) { params[0] }))
                    }
                }
                "rotate" -> {
                    if (params.isNotEmpty()) {
                        transforms.add(SvgTransform.Rotate(
                            params[0],
                            params.getOrElse(1) { 0f },
                            params.getOrElse(2) { 0f }
                        ))
                    }
                }
                "skewx" -> {
                    if (params.isNotEmpty()) {
                        transforms.add(SvgTransform.SkewX(params[0]))
                    }
                }
                "skewy" -> {
                    if (params.isNotEmpty()) {
                        transforms.add(SvgTransform.SkewY(params[0]))
                    }
                }
                "matrix" -> {
                    if (params.size >= 6) {
                        transforms.add(SvgTransform.Matrix(
                            params[0], params[1], params[2],
                            params[3], params[4], params[5]
                        ))
                    }
                }
            }
        }

        return when {
            transforms.isEmpty() -> null
            transforms.size == 1 -> transforms[0]
            else -> SvgTransform.Combined(transforms)
        }
    }
}
