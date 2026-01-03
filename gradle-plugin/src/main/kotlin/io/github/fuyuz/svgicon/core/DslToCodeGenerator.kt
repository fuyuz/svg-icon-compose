package io.github.fuyuz.svgicon.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.squareup.kotlinpoet.*
import kotlin.time.Duration

/**
 * Converts runtime DSL objects into KotlinPoet CodeBlocks.
 * This enables the gradle-plugin to use SvgParser from runtime and then
 * serialize the result to generated Kotlin code.
 */
object DslToCodeGenerator {
    // Package references
    private val corePackage = "io.github.fuyuz.svgicon.core"

    // Type references
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
    private val svgTextClass = ClassName(corePackage, "SvgText")
    private val svgLinearGradientClass = ClassName(corePackage, "SvgLinearGradient")
    private val svgRadialGradientClass = ClassName(corePackage, "SvgRadialGradient")
    private val svgMarkerClass = ClassName(corePackage, "SvgMarker")
    private val svgSymbolClass = ClassName(corePackage, "SvgSymbol")
    private val svgUseClass = ClassName(corePackage, "SvgUse")
    private val svgPatternClass = ClassName(corePackage, "SvgPattern")
    private val gradientStopClass = ClassName(corePackage, "GradientStop")
    private val pathCommandClass = ClassName(corePackage, "PathCommand")
    private val lineCapClass = ClassName(corePackage, "LineCap")
    private val lineJoinClass = ClassName(corePackage, "LineJoin")
    private val fillRuleClass = ClassName(corePackage, "FillRule")
    private val clipPathUnitsClass = ClassName(corePackage, "ClipPathUnits")
    private val maskUnitsClass = ClassName(corePackage, "MaskUnits")
    private val svgTransformClass = ClassName(corePackage, "SvgTransform")
    private val viewBoxClass = ClassName(corePackage, "ViewBox")
    private val preserveAspectRatioClass = ClassName(corePackage, "PreserveAspectRatio")
    private val aspectRatioAlignClass = ClassName(corePackage, "AspectRatioAlign")
    private val meetOrSliceClass = ClassName(corePackage, "MeetOrSlice")
    private val transformTypeClass = ClassName(corePackage, "TransformType")
    private val calcModeClass = ClassName(corePackage, "CalcMode")
    private val keySplinesClass = ClassName(corePackage, "KeySplines")
    private val animationDirectionClass = ClassName(corePackage, "AnimationDirection")
    private val animationFillModeClass = ClassName(corePackage, "AnimationFillMode")
    private val motionRotateClass = ClassName(corePackage, "MotionRotate")
    private val textAnchorClass = ClassName(corePackage, "TextAnchor")
    private val dominantBaselineClass = ClassName(corePackage, "DominantBaseline")
    private val markerOrientClass = ClassName(corePackage, "MarkerOrient")
    private val paintOrderClass = ClassName(corePackage, "PaintOrder")
    private val vectorEffectClass = ClassName(corePackage, "VectorEffect")
    private val spreadMethodClass = ClassName(corePackage, "SpreadMethod")
    private val gradientUnitsClass = ClassName(corePackage, "GradientUnits")
    private val patternUnitsClass = ClassName(corePackage, "PatternUnits")
    private val colorClass = ClassName("androidx.compose.ui.graphics", "Color")
    private val offsetClass = ClassName("androidx.compose.ui.geometry", "Offset")
    private val millisecondsProperty = MemberName("kotlin.time.Duration.Companion", "milliseconds")

    /**
     * Converts an Svg object to a CodeBlock.
     */
    fun generateCodeBlock(svg: Svg): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgClass)
        builder.indent()

        // Generate non-default properties
        svg.width?.let { builder.add("width = %Lf,\n", it) }
        svg.height?.let { builder.add("height = %Lf,\n", it) }

        // viewBox
        svg.viewBox?.let { vb ->
            val isDefault = vb.minX == 0f && vb.minY == 0f && vb.width == 24f && vb.height == 24f
            if (!isDefault) {
                builder.add("viewBox = %T(%Lf, %Lf, %Lf, %Lf),\n", viewBoxClass, vb.minX, vb.minY, vb.width, vb.height)
            }
        }

        // preserveAspectRatio
        if (svg.preserveAspectRatio != PreserveAspectRatio.Default) {
            builder.add("preserveAspectRatio = %T(%T.%L, %T.%L),\n",
                preserveAspectRatioClass, aspectRatioAlignClass, svg.preserveAspectRatio.align.name,
                meetOrSliceClass, svg.preserveAspectRatio.meetOrSlice.name)
        }

        // fill (default is null)
        svg.fill?.let { fill ->
            builder.add(generateColorCodeBlock("fill", fill))
            builder.add(",\n")
        }

        // stroke (default is Color.Unspecified)
        if (svg.stroke != Color.Unspecified) {
            svg.stroke?.let { stroke ->
                builder.add(generateColorCodeBlock("stroke", stroke))
                builder.add(",\n")
            } ?: builder.add("stroke = null,\n")
        }

        // strokeWidth (default is 2f)
        if (svg.strokeWidth != 2f) {
            builder.add("strokeWidth = %Lf,\n", svg.strokeWidth)
        }

        // strokeLinecap (default is ROUND)
        if (svg.strokeLinecap != LineCap.ROUND) {
            builder.add("strokeLinecap = %T.%L,\n", lineCapClass, svg.strokeLinecap.name)
        }

        // strokeLinejoin (default is ROUND)
        if (svg.strokeLinejoin != LineJoin.ROUND) {
            builder.add("strokeLinejoin = %T.%L,\n", lineJoinClass, svg.strokeLinejoin.name)
        }

        // children
        if (svg.children.isNotEmpty()) {
            builder.add("children = listOf(\n")
            builder.indent()
            svg.children.forEachIndexed { index, child ->
                builder.add(generateElementCodeBlock(child))
                if (index < svg.children.size - 1) builder.add(",\n")
            }
            builder.unindent()
            builder.add("\n)")
        }

        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    /**
     * Converts an SvgElement to a CodeBlock.
     */
    fun generateElementCodeBlock(element: SvgElement): CodeBlock {
        return when (element) {
            is SvgPath -> generatePathCodeBlock(element)
            is SvgCircle -> generateCircleCodeBlock(element)
            is SvgEllipse -> generateEllipseCodeBlock(element)
            is SvgRect -> generateRectCodeBlock(element)
            is SvgLine -> generateLineCodeBlock(element)
            is SvgPolyline -> generatePolylineCodeBlock(element)
            is SvgPolygon -> generatePolygonCodeBlock(element)
            is SvgGroup -> generateGroupCodeBlock(element)
            is SvgStyled -> generateStyledCodeBlock(element)
            is SvgAnimated -> generateAnimatedCodeBlock(element)
            is SvgClipPath -> generateClipPathCodeBlock(element)
            is SvgMask -> generateMaskCodeBlock(element)
            is SvgDefs -> generateDefsCodeBlock(element)
            is SvgText -> generateTextCodeBlock(element)
            is SvgLinearGradient -> generateLinearGradientCodeBlock(element)
            is SvgRadialGradient -> generateRadialGradientCodeBlock(element)
            is SvgMarker -> generateMarkerCodeBlock(element)
            is SvgSymbol -> generateSymbolCodeBlock(element)
            is SvgUse -> generateUseCodeBlock(element)
            is SvgPattern -> generatePatternCodeBlock(element)
        }
    }

    // ============================================
    // Element Code Generation
    // ============================================

    private fun generatePathCodeBlock(path: SvgPath): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(\n", svgPathClass)
        builder.indent()
        path.commands.forEachIndexed { index, cmd ->
            builder.add(generatePathCommandCodeBlock(cmd))
            if (index < path.commands.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n))")
        return builder.build()
    }

    private fun generateCircleCodeBlock(circle: SvgCircle): CodeBlock {
        return CodeBlock.of("%T(%Lf, %Lf, %Lf)", svgCircleClass, circle.cx, circle.cy, circle.r)
    }

    private fun generateEllipseCodeBlock(ellipse: SvgEllipse): CodeBlock {
        return CodeBlock.of("%T(%Lf, %Lf, %Lf, %Lf)", svgEllipseClass, ellipse.cx, ellipse.cy, ellipse.rx, ellipse.ry)
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

    private fun generateLineCodeBlock(line: SvgLine): CodeBlock {
        return CodeBlock.of("%T(%Lf, %Lf, %Lf, %Lf)", svgLineClass, line.x1, line.y1, line.x2, line.y2)
    }

    private fun generatePolylineCodeBlock(polyline: SvgPolyline): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(", svgPolylineClass)
        polyline.points.forEachIndexed { index, point ->
            builder.add("%T(%Lf, %Lf)", offsetClass, point.x, point.y)
            if (index < polyline.points.size - 1) builder.add(", ")
        }
        builder.add("))")
        return builder.build()
    }

    private fun generatePolygonCodeBlock(polygon: SvgPolygon): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(listOf(", svgPolygonClass)
        polygon.points.forEachIndexed { index, point ->
            builder.add("%T(%Lf, %Lf)", offsetClass, point.x, point.y)
            if (index < polygon.points.size - 1) builder.add(", ")
        }
        builder.add("))")
        return builder.build()
    }

    private fun generateGroupCodeBlock(group: SvgGroup): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgGroupClass)
        builder.indent()
        builder.add("children = listOf(\n")
        builder.indent()
        group.children.forEachIndexed { index, child ->
            builder.add(generateElementCodeBlock(child))
            if (index < group.children.size - 1) builder.add(",\n")
        }
        builder.unindent()
        builder.add("\n)")
        group.style?.let { style ->
            if (style != SvgStyle.Empty) {
                builder.add(",\n")
                builder.add("style = ")
                builder.add(generateStyleCodeBlock(style))
            }
        }
        builder.unindent()
        builder.add("\n)")
        return builder.build()
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

    private fun generateTextCodeBlock(text: SvgText): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgTextClass)
        builder.indent()
        builder.add("text = %S", text.text)
        if (text.x != 0f) builder.add(",\nx = %Lf", text.x)
        if (text.y != 0f) builder.add(",\ny = %Lf", text.y)
        text.textAnchor?.let { builder.add(",\ntextAnchor = %T.%L", textAnchorClass, it.name) }
        text.dominantBaseline?.let { builder.add(",\ndominantBaseline = %T.%L", dominantBaselineClass, it.name) }
        text.fontSize?.let { builder.add(",\nfontSize = %Lf", it) }
        text.fontFamily?.let { builder.add(",\nfontFamily = %S", it) }
        text.fontWeight?.let { builder.add(",\nfontWeight = %S", it) }
        text.letterSpacing?.let { builder.add(",\nletterSpacing = %Lf", it) }
        text.dx?.let { builder.add(",\ndx = %Lf", it) }
        text.dy?.let { builder.add(",\ndy = %Lf", it) }
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateLinearGradientCodeBlock(gradient: SvgLinearGradient): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgLinearGradientClass)
        builder.indent()
        builder.add("id = %S", gradient.id)
        if (gradient.x1 != 0f) builder.add(",\nx1 = %Lf", gradient.x1)
        if (gradient.y1 != 0f) builder.add(",\ny1 = %Lf", gradient.y1)
        if (gradient.x2 != 1f) builder.add(",\nx2 = %Lf", gradient.x2)
        if (gradient.y2 != 0f) builder.add(",\ny2 = %Lf", gradient.y2)
        if (gradient.stops.isNotEmpty()) {
            builder.add(",\nstops = listOf(")
            gradient.stops.forEachIndexed { index, stop ->
                builder.add("%T(%Lf, ", gradientStopClass, stop.offset)
                builder.add(generateColorValueCodeBlock(stop.color))
                if (stop.opacity != 1f) builder.add(", %Lf", stop.opacity)
                builder.add(")")
                if (index < gradient.stops.size - 1) builder.add(", ")
            }
            builder.add(")")
        }
        if (gradient.gradientUnits != GradientUnits.OBJECT_BOUNDING_BOX) {
            builder.add(",\ngradientUnits = %T.%L", gradientUnitsClass, gradient.gradientUnits.name)
        }
        if (gradient.spreadMethod != SpreadMethod.PAD) {
            builder.add(",\nspreadMethod = %T.%L", spreadMethodClass, gradient.spreadMethod.name)
        }
        gradient.gradientTransform?.let {
            builder.add(",\ngradientTransform = ")
            builder.add(generateTransformCodeBlock(it))
        }
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateRadialGradientCodeBlock(gradient: SvgRadialGradient): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgRadialGradientClass)
        builder.indent()
        builder.add("id = %S", gradient.id)
        if (gradient.cx != 0.5f) builder.add(",\ncx = %Lf", gradient.cx)
        if (gradient.cy != 0.5f) builder.add(",\ncy = %Lf", gradient.cy)
        if (gradient.r != 0.5f) builder.add(",\nr = %Lf", gradient.r)
        gradient.fx?.let { builder.add(",\nfx = %Lf", it) }
        gradient.fy?.let { builder.add(",\nfy = %Lf", it) }
        if (gradient.stops.isNotEmpty()) {
            builder.add(",\nstops = listOf(")
            gradient.stops.forEachIndexed { index, stop ->
                builder.add("%T(%Lf, ", gradientStopClass, stop.offset)
                builder.add(generateColorValueCodeBlock(stop.color))
                if (stop.opacity != 1f) builder.add(", %Lf", stop.opacity)
                builder.add(")")
                if (index < gradient.stops.size - 1) builder.add(", ")
            }
            builder.add(")")
        }
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateMarkerCodeBlock(marker: SvgMarker): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgMarkerClass)
        builder.indent()
        builder.add("id = %S", marker.id)
        marker.viewBox?.let { vb ->
            builder.add(",\nviewBox = %T(%Lf, %Lf, %Lf, %Lf)", viewBoxClass, vb.minX, vb.minY, vb.width, vb.height)
        }
        if (marker.refX != 0f) builder.add(",\nrefX = %Lf", marker.refX)
        if (marker.refY != 0f) builder.add(",\nrefY = %Lf", marker.refY)
        if (marker.markerWidth != 3f) builder.add(",\nmarkerWidth = %Lf", marker.markerWidth)
        if (marker.markerHeight != 3f) builder.add(",\nmarkerHeight = %Lf", marker.markerHeight)
        if (marker.orient != MarkerOrient.Auto) {
            builder.add(",\norient = ")
            builder.add(generateMarkerOrientCodeBlock(marker.orient))
        }
        if (marker.children.isNotEmpty()) {
            builder.add(",\nchildren = listOf(\n")
            builder.indent()
            marker.children.forEachIndexed { index, child ->
                builder.add(generateElementCodeBlock(child))
                if (index < marker.children.size - 1) builder.add(",\n")
            }
            builder.unindent()
            builder.add("\n)")
        }
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateMarkerOrientCodeBlock(orient: MarkerOrient): CodeBlock {
        return when (orient) {
            is MarkerOrient.Auto -> CodeBlock.of("%T.Auto", markerOrientClass)
            is MarkerOrient.AutoStartReverse -> CodeBlock.of("%T.AutoStartReverse", markerOrientClass)
            is MarkerOrient.Angle -> CodeBlock.of("%T.Angle(%Lf)", markerOrientClass, orient.degrees)
        }
    }

    private fun generateSymbolCodeBlock(symbol: SvgSymbol): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgSymbolClass)
        builder.indent()
        builder.add("id = %S", symbol.id)
        symbol.viewBox?.let { vb ->
            builder.add(",\nviewBox = %T(%Lf, %Lf, %Lf, %Lf)", viewBoxClass, vb.minX, vb.minY, vb.width, vb.height)
        }
        if (symbol.children.isNotEmpty()) {
            builder.add(",\nchildren = listOf(\n")
            builder.indent()
            symbol.children.forEachIndexed { index, child ->
                builder.add(generateElementCodeBlock(child))
                if (index < symbol.children.size - 1) builder.add(",\n")
            }
            builder.unindent()
            builder.add("\n)")
        }
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateUseCodeBlock(use: SvgUse): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(", svgUseClass)
        builder.add("href = %S", use.href)
        if (use.x != 0f) builder.add(", x = %Lf", use.x)
        if (use.y != 0f) builder.add(", y = %Lf", use.y)
        use.width?.let { builder.add(", width = %Lf", it) }
        use.height?.let { builder.add(", height = %Lf", it) }
        builder.add(")")
        return builder.build()
    }

    private fun generatePatternCodeBlock(pattern: SvgPattern): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(\n", svgPatternClass)
        builder.indent()
        builder.add("id = %S,\n", pattern.id)
        builder.add("width = %Lf,\n", pattern.width)
        builder.add("height = %Lf", pattern.height)
        if (pattern.patternUnits != PatternUnits.USER_SPACE_ON_USE) {
            builder.add(",\npatternUnits = %T.%L", patternUnitsClass, pattern.patternUnits.name)
        }
        if (pattern.patternContentUnits != PatternUnits.USER_SPACE_ON_USE) {
            builder.add(",\npatternContentUnits = %T.%L", patternUnitsClass, pattern.patternContentUnits.name)
        }
        pattern.patternTransform?.let {
            builder.add(",\npatternTransform = ")
            builder.add(generateTransformCodeBlock(it))
        }
        if (pattern.children.isNotEmpty()) {
            builder.add(",\nchildren = listOf(\n")
            builder.indent()
            pattern.children.forEachIndexed { index, child ->
                builder.add(generateElementCodeBlock(child))
                if (index < pattern.children.size - 1) builder.add(",\n")
            }
            builder.unindent()
            builder.add("\n)")
        }
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    // ============================================
    // Style Code Generation
    // ============================================

    private fun generateStyleCodeBlock(style: SvgStyle): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T(", svgStyleClass)
        val parts = mutableListOf<CodeBlock>()

        style.fill?.let { parts.add(generateColorCodeBlock("fill", it)) }
        style.fillOpacity?.let { parts.add(CodeBlock.of("fillOpacity = %Lf", it)) }
        style.fillRule?.let { parts.add(CodeBlock.of("fillRule = %T.%L", fillRuleClass, it.name)) }
        style.stroke?.let { parts.add(generateColorCodeBlock("stroke", it)) }
        style.strokeWidth?.let { parts.add(CodeBlock.of("strokeWidth = %Lf", it)) }
        style.strokeOpacity?.let { parts.add(CodeBlock.of("strokeOpacity = %Lf", it)) }
        style.strokeLinecap?.let { parts.add(CodeBlock.of("strokeLinecap = %T.%L", lineCapClass, it.name)) }
        style.strokeLinejoin?.let { parts.add(CodeBlock.of("strokeLinejoin = %T.%L", lineJoinClass, it.name)) }
        style.strokeDasharray?.let { arr ->
            val arrCode = arr.joinToString(", ") { "${it}f" }
            parts.add(CodeBlock.of("strokeDasharray = listOf(%L)", arrCode))
        }
        style.strokeDashoffset?.let { parts.add(CodeBlock.of("strokeDashoffset = %Lf", it)) }
        style.strokeMiterlimit?.let { parts.add(CodeBlock.of("strokeMiterlimit = %Lf", it)) }
        style.opacity?.let { parts.add(CodeBlock.of("opacity = %Lf", it)) }
        style.transform?.let {
            parts.add(CodeBlock.builder().add("transform = ").add(generateTransformCodeBlock(it)).build())
        }
        style.paintOrder?.let { parts.add(CodeBlock.of("paintOrder = %T.%L", paintOrderClass, it.name)) }
        style.vectorEffect?.let { parts.add(CodeBlock.of("vectorEffect = %T.%L", vectorEffectClass, it.name)) }
        style.clipPathId?.let { parts.add(CodeBlock.of("clipPathId = %S", it)) }
        style.maskId?.let { parts.add(CodeBlock.of("maskId = %S", it)) }
        style.markerStart?.let { parts.add(CodeBlock.of("markerStart = %S", it)) }
        style.markerMid?.let { parts.add(CodeBlock.of("markerMid = %S", it)) }
        style.markerEnd?.let { parts.add(CodeBlock.of("markerEnd = %S", it)) }

        parts.forEachIndexed { index, part ->
            builder.add(part)
            if (index < parts.size - 1) builder.add(", ")
        }
        builder.add(")")
        return builder.build()
    }

    // ============================================
    // Transform Code Generation
    // ============================================

    private fun generateTransformCodeBlock(transform: SvgTransform): CodeBlock {
        return when (transform) {
            is SvgTransform.Translate -> CodeBlock.of("%T.Translate(%Lf, %Lf)", svgTransformClass, transform.x, transform.y)
            is SvgTransform.Scale -> CodeBlock.of("%T.Scale(%Lf, %Lf)", svgTransformClass, transform.sx, transform.sy)
            is SvgTransform.Rotate -> CodeBlock.of("%T.Rotate(%Lf, %Lf, %Lf)", svgTransformClass, transform.angle, transform.cx, transform.cy)
            is SvgTransform.SkewX -> CodeBlock.of("%T.SkewX(%Lf)", svgTransformClass, transform.angle)
            is SvgTransform.SkewY -> CodeBlock.of("%T.SkewY(%Lf)", svgTransformClass, transform.angle)
            is SvgTransform.Matrix -> CodeBlock.of("%T.Matrix(%Lf, %Lf, %Lf, %Lf, %Lf, %Lf)",
                svgTransformClass, transform.a, transform.b, transform.c, transform.d, transform.e, transform.f)
            is SvgTransform.Combined -> {
                val builder = CodeBlock.builder()
                builder.add("%T.Combined(listOf(", svgTransformClass)
                transform.transforms.forEachIndexed { index, t ->
                    builder.add(generateTransformCodeBlock(t))
                    if (index < transform.transforms.size - 1) builder.add(", ")
                }
                builder.add("))")
                builder.build()
            }
        }
    }

    // ============================================
    // Animation Code Generation
    // ============================================

    private fun generateAnimateCodeBlock(animate: SvgAnimate): CodeBlock {
        return when (animate) {
            is SvgAnimate.StrokeDraw -> generateStrokeDrawCodeBlock(animate)
            is SvgAnimate.StrokeWidth -> generatePropertyAnimateCodeBlock("StrokeWidth", animate.from, animate.to, animate)
            is SvgAnimate.StrokeOpacity -> generatePropertyAnimateCodeBlock("StrokeOpacity", animate.from, animate.to, animate)
            is SvgAnimate.StrokeDasharray -> generateDasharrayAnimateCodeBlock(animate)
            is SvgAnimate.StrokeDashoffset -> generatePropertyAnimateCodeBlock("StrokeDashoffset", animate.from, animate.to, animate)
            is SvgAnimate.FillOpacity -> generatePropertyAnimateCodeBlock("FillOpacity", animate.from, animate.to, animate)
            is SvgAnimate.Opacity -> generatePropertyAnimateCodeBlock("Opacity", animate.from, animate.to, animate)
            is SvgAnimate.Cx -> generatePropertyAnimateCodeBlock("Cx", animate.from, animate.to, animate)
            is SvgAnimate.Cy -> generatePropertyAnimateCodeBlock("Cy", animate.from, animate.to, animate)
            is SvgAnimate.R -> generatePropertyAnimateCodeBlock("R", animate.from, animate.to, animate)
            is SvgAnimate.Rx -> generatePropertyAnimateCodeBlock("Rx", animate.from, animate.to, animate)
            is SvgAnimate.Ry -> generatePropertyAnimateCodeBlock("Ry", animate.from, animate.to, animate)
            is SvgAnimate.X -> generatePropertyAnimateCodeBlock("X", animate.from, animate.to, animate)
            is SvgAnimate.Y -> generatePropertyAnimateCodeBlock("Y", animate.from, animate.to, animate)
            is SvgAnimate.Width -> generatePropertyAnimateCodeBlock("Width", animate.from, animate.to, animate)
            is SvgAnimate.Height -> generatePropertyAnimateCodeBlock("Height", animate.from, animate.to, animate)
            is SvgAnimate.X1 -> generatePropertyAnimateCodeBlock("X1", animate.from, animate.to, animate)
            is SvgAnimate.Y1 -> generatePropertyAnimateCodeBlock("Y1", animate.from, animate.to, animate)
            is SvgAnimate.X2 -> generatePropertyAnimateCodeBlock("X2", animate.from, animate.to, animate)
            is SvgAnimate.Y2 -> generatePropertyAnimateCodeBlock("Y2", animate.from, animate.to, animate)
            is SvgAnimate.D -> generateDAnimateCodeBlock(animate)
            is SvgAnimate.Points -> generatePointsAnimateCodeBlock(animate)
            is SvgAnimate.Transform -> generateTransformAnimateCodeBlock(animate)
            is SvgAnimate.Motion -> generateMotionAnimateCodeBlock(animate)
        }
    }

    private fun generateStrokeDrawCodeBlock(animate: SvgAnimate.StrokeDraw): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T.StrokeDraw(", svgAnimateClass)
        val parts = mutableListOf<String>()
        if (animate.dur != Duration.parse("500ms")) {
            parts.add("dur = ${animate.dur.inWholeMilliseconds}.milliseconds")
        }
        if (animate.delay != Duration.ZERO) {
            parts.add("delay = ${animate.delay.inWholeMilliseconds}.milliseconds")
        }
        if (animate.reverse) {
            parts.add("reverse = true")
        }
        if (animate.calcMode != CalcMode.LINEAR) {
            parts.add("calcMode = CalcMode.${animate.calcMode.name}")
        }
        animate.keySplines?.let { ks ->
            parts.add("keySplines = KeySplines(${ks.x1}f, ${ks.y1}f, ${ks.x2}f, ${ks.y2}f)")
        }
        if (animate.iterations != SvgAnimate.INFINITE) {
            parts.add("iterations = ${animate.iterations}")
        }
        if (animate.direction != AnimationDirection.NORMAL) {
            parts.add("direction = AnimationDirection.${animate.direction.name}")
        }
        if (animate.fillMode != AnimationFillMode.NONE) {
            parts.add("fillMode = AnimationFillMode.${animate.fillMode.name}")
        }
        builder.add(parts.joinToString(", "))
        builder.add(")")
        return builder.build()
    }

    private fun generatePropertyAnimateCodeBlock(type: String, from: Float, to: Float, animate: SvgAnimate): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T.%L(from = %Lf, to = %Lf", svgAnimateClass, type, from, to)
        appendAnimateCommonParams(builder, animate)
        builder.add(")")
        return builder.build()
    }

    private fun generateDasharrayAnimateCodeBlock(animate: SvgAnimate.StrokeDasharray): CodeBlock {
        val builder = CodeBlock.builder()
        val fromStr = animate.from.joinToString(", ") { "${it}f" }
        val toStr = animate.to.joinToString(", ") { "${it}f" }
        builder.add("%T.StrokeDasharray(from = listOf(%L), to = listOf(%L)", svgAnimateClass, fromStr, toStr)
        appendAnimateCommonParams(builder, animate)
        builder.add(")")
        return builder.build()
    }

    private fun generateDAnimateCodeBlock(animate: SvgAnimate.D): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T.D(from = %S, to = %S", svgAnimateClass, animate.from, animate.to)
        appendAnimateCommonParams(builder, animate)
        builder.add(")")
        return builder.build()
    }

    private fun generatePointsAnimateCodeBlock(animate: SvgAnimate.Points): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T.Points(\n", svgAnimateClass)
        builder.indent()
        builder.add("from = listOf(")
        animate.from.forEachIndexed { index, point ->
            builder.add("%T(%Lf, %Lf)", offsetClass, point.x, point.y)
            if (index < animate.from.size - 1) builder.add(", ")
        }
        builder.add("),\n")
        builder.add("to = listOf(")
        animate.to.forEachIndexed { index, point ->
            builder.add("%T(%Lf, %Lf)", offsetClass, point.x, point.y)
            if (index < animate.to.size - 1) builder.add(", ")
        }
        builder.add(")")
        appendAnimateCommonParams(builder, animate)
        builder.unindent()
        builder.add("\n)")
        return builder.build()
    }

    private fun generateTransformAnimateCodeBlock(animate: SvgAnimate.Transform): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T.Transform(type = %T.%L, from = %Lf, to = %Lf",
            svgAnimateClass, transformTypeClass, animate.type.name, animate.from, animate.to)
        appendAnimateCommonParams(builder, animate)
        builder.add(")")
        return builder.build()
    }

    private fun generateMotionAnimateCodeBlock(animate: SvgAnimate.Motion): CodeBlock {
        val builder = CodeBlock.builder()
        builder.add("%T.Motion(path = %S", svgAnimateClass, animate.path)
        if (animate.rotate != MotionRotate.NONE) {
            builder.add(", rotate = %T.%L", motionRotateClass, animate.rotate.name)
        }
        appendAnimateCommonParams(builder, animate)
        builder.add(")")
        return builder.build()
    }

    private fun appendAnimateCommonParams(builder: CodeBlock.Builder, animate: SvgAnimate) {
        if (animate.dur != Duration.parse("500ms")) {
            builder.add(", dur = %L.%M", animate.dur.inWholeMilliseconds, millisecondsProperty)
        }
        if (animate.delay != Duration.ZERO) {
            builder.add(", delay = %L.%M", animate.delay.inWholeMilliseconds, millisecondsProperty)
        }
        if (animate.calcMode != CalcMode.LINEAR) {
            builder.add(", calcMode = %T.%L", calcModeClass, animate.calcMode.name)
        }
        animate.keySplines?.let { ks ->
            builder.add(", keySplines = %T(%Lf, %Lf, %Lf, %Lf)", keySplinesClass, ks.x1, ks.y1, ks.x2, ks.y2)
        }
        if (animate.iterations != SvgAnimate.INFINITE) {
            builder.add(", iterations = %L", animate.iterations)
        }
        if (animate.direction != AnimationDirection.NORMAL) {
            builder.add(", direction = %T.%L", animationDirectionClass, animate.direction.name)
        }
        if (animate.fillMode != AnimationFillMode.NONE) {
            builder.add(", fillMode = %T.%L", animationFillModeClass, animate.fillMode.name)
        }
    }

    // ============================================
    // Path Command Code Generation
    // ============================================

    private fun generatePathCommandCodeBlock(cmd: PathCommand): CodeBlock {
        return when (cmd) {
            is PathCommand.MoveTo -> CodeBlock.of("%T.MoveTo(%Lf, %Lf)", pathCommandClass, cmd.x, cmd.y)
            is PathCommand.MoveToRelative -> CodeBlock.of("%T.MoveToRelative(%Lf, %Lf)", pathCommandClass, cmd.dx, cmd.dy)
            is PathCommand.LineTo -> CodeBlock.of("%T.LineTo(%Lf, %Lf)", pathCommandClass, cmd.x, cmd.y)
            is PathCommand.LineToRelative -> CodeBlock.of("%T.LineToRelative(%Lf, %Lf)", pathCommandClass, cmd.dx, cmd.dy)
            is PathCommand.HorizontalLineTo -> CodeBlock.of("%T.HorizontalLineTo(%Lf)", pathCommandClass, cmd.x)
            is PathCommand.HorizontalLineToRelative -> CodeBlock.of("%T.HorizontalLineToRelative(%Lf)", pathCommandClass, cmd.dx)
            is PathCommand.VerticalLineTo -> CodeBlock.of("%T.VerticalLineTo(%Lf)", pathCommandClass, cmd.y)
            is PathCommand.VerticalLineToRelative -> CodeBlock.of("%T.VerticalLineToRelative(%Lf)", pathCommandClass, cmd.dy)
            is PathCommand.CubicTo -> CodeBlock.of("%T.CubicTo(%Lf, %Lf, %Lf, %Lf, %Lf, %Lf)",
                pathCommandClass, cmd.x1, cmd.y1, cmd.x2, cmd.y2, cmd.x, cmd.y)
            is PathCommand.CubicToRelative -> CodeBlock.of("%T.CubicToRelative(%Lf, %Lf, %Lf, %Lf, %Lf, %Lf)",
                pathCommandClass, cmd.dx1, cmd.dy1, cmd.dx2, cmd.dy2, cmd.dx, cmd.dy)
            is PathCommand.SmoothCubicTo -> CodeBlock.of("%T.SmoothCubicTo(%Lf, %Lf, %Lf, %Lf)",
                pathCommandClass, cmd.x2, cmd.y2, cmd.x, cmd.y)
            is PathCommand.SmoothCubicToRelative -> CodeBlock.of("%T.SmoothCubicToRelative(%Lf, %Lf, %Lf, %Lf)",
                pathCommandClass, cmd.dx2, cmd.dy2, cmd.dx, cmd.dy)
            is PathCommand.QuadTo -> CodeBlock.of("%T.QuadTo(%Lf, %Lf, %Lf, %Lf)",
                pathCommandClass, cmd.x1, cmd.y1, cmd.x, cmd.y)
            is PathCommand.QuadToRelative -> CodeBlock.of("%T.QuadToRelative(%Lf, %Lf, %Lf, %Lf)",
                pathCommandClass, cmd.dx1, cmd.dy1, cmd.dx, cmd.dy)
            is PathCommand.SmoothQuadTo -> CodeBlock.of("%T.SmoothQuadTo(%Lf, %Lf)", pathCommandClass, cmd.x, cmd.y)
            is PathCommand.SmoothQuadToRelative -> CodeBlock.of("%T.SmoothQuadToRelative(%Lf, %Lf)", pathCommandClass, cmd.dx, cmd.dy)
            is PathCommand.ArcTo -> CodeBlock.of("%T.ArcTo(%Lf, %Lf, %Lf, %L, %L, %Lf, %Lf)",
                pathCommandClass, cmd.rx, cmd.ry, cmd.xAxisRotation, cmd.largeArcFlag, cmd.sweepFlag, cmd.x, cmd.y)
            is PathCommand.ArcToRelative -> CodeBlock.of("%T.ArcToRelative(%Lf, %Lf, %Lf, %L, %L, %Lf, %Lf)",
                pathCommandClass, cmd.rx, cmd.ry, cmd.xAxisRotation, cmd.largeArcFlag, cmd.sweepFlag, cmd.dx, cmd.dy)
            PathCommand.Close -> CodeBlock.of("%T.Close", pathCommandClass)
        }
    }

    // ============================================
    // Color Code Generation
    // ============================================

    private fun generateColorCodeBlock(name: String, color: Color): CodeBlock {
        return when {
            color == Color.Unspecified -> CodeBlock.of("%L = %T.Unspecified", name, colorClass)
            color.alpha == 0f -> CodeBlock.of("%L = null", name)
            else -> {
                val argb = (color.alpha * 255).toInt().shl(24) or
                        (color.red * 255).toInt().shl(16) or
                        (color.green * 255).toInt().shl(8) or
                        (color.blue * 255).toInt()
                CodeBlock.of("%L = %T(0x%08XUL)", name, colorClass, argb.toLong() and 0xFFFFFFFFL)
            }
        }
    }

    private fun generateColorValueCodeBlock(color: Color): CodeBlock {
        return when {
            color == Color.Unspecified -> CodeBlock.of("%T.Unspecified", colorClass)
            else -> {
                val argb = (color.alpha * 255).toInt().shl(24) or
                        (color.red * 255).toInt().shl(16) or
                        (color.green * 255).toInt().shl(8) or
                        (color.blue * 255).toInt()
                CodeBlock.of("%T(0x%08XUL)", colorClass, argb.toLong() and 0xFFFFFFFFL)
            }
        }
    }
}
