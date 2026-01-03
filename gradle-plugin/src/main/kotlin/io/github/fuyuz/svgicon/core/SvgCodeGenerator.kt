package io.github.fuyuz.svgicon.core

import com.squareup.kotlinpoet.CodeBlock

/**
 * Parses SVG XML and generates KotlinPoet CodeBlocks.
 * Uses SvgParser from runtime for parsing and DslToCodeGenerator for code generation.
 */
object SvgCodeGenerator {
    /**
     * Parse SVG content and generate a CodeBlock for the Svg object.
     * This is the main public API that remains unchanged.
     *
     * @param svgContent Raw SVG XML content
     * @return CodeBlock that constructs the equivalent Svg object
     */
    fun generateSvgCodeBlock(svgContent: String): CodeBlock {
        // Step 1: Parse SVG using runtime's parser
        val svgDsl = svg(svgContent)

        // Step 2: Convert DSL to CodeBlock
        return DslToCodeGenerator.generateCodeBlock(svgDsl)
    }
}
