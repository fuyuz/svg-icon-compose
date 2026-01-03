package io.github.fuyuz.svgicon.core

/**
 * SVG path commands.
 */
sealed interface PathCommand {
    /** Move to absolute position */
    data class MoveTo(val x: Float, val y: Float) : PathCommand
    /** Move to relative position */
    data class MoveToRelative(val dx: Float, val dy: Float) : PathCommand
    /** Line to absolute position */
    data class LineTo(val x: Float, val y: Float) : PathCommand
    /** Line to relative position */
    data class LineToRelative(val dx: Float, val dy: Float) : PathCommand
    /** Horizontal line to absolute x */
    data class HorizontalLineTo(val x: Float) : PathCommand
    /** Horizontal line to relative dx */
    data class HorizontalLineToRelative(val dx: Float) : PathCommand
    /** Vertical line to absolute y */
    data class VerticalLineTo(val y: Float) : PathCommand
    /** Vertical line to relative dy */
    data class VerticalLineToRelative(val dy: Float) : PathCommand
    /** Cubic bezier curve (absolute) */
    data class CubicTo(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val x: Float, val y: Float) : PathCommand
    /** Cubic bezier curve (relative) */
    data class CubicToRelative(val dx1: Float, val dy1: Float, val dx2: Float, val dy2: Float, val dx: Float, val dy: Float) : PathCommand
    /** Smooth cubic bezier curve (absolute) */
    data class SmoothCubicTo(val x2: Float, val y2: Float, val x: Float, val y: Float) : PathCommand
    /** Smooth cubic bezier curve (relative) */
    data class SmoothCubicToRelative(val dx2: Float, val dy2: Float, val dx: Float, val dy: Float) : PathCommand
    /** Quadratic bezier curve (absolute) */
    data class QuadTo(val x1: Float, val y1: Float, val x: Float, val y: Float) : PathCommand
    /** Quadratic bezier curve (relative) */
    data class QuadToRelative(val dx1: Float, val dy1: Float, val dx: Float, val dy: Float) : PathCommand
    /** Smooth quadratic bezier curve (absolute) */
    data class SmoothQuadTo(val x: Float, val y: Float) : PathCommand
    /** Smooth quadratic bezier curve (relative) */
    data class SmoothQuadToRelative(val dx: Float, val dy: Float) : PathCommand
    /** Arc (absolute) */
    data class ArcTo(val rx: Float, val ry: Float, val xAxisRotation: Float, val largeArcFlag: Boolean, val sweepFlag: Boolean, val x: Float, val y: Float) : PathCommand
    /** Arc (relative) */
    data class ArcToRelative(val rx: Float, val ry: Float, val xAxisRotation: Float, val largeArcFlag: Boolean, val sweepFlag: Boolean, val dx: Float, val dy: Float) : PathCommand
    /** Close path */
    data object Close : PathCommand
}

/**
 * Exception thrown when SVG path data parsing fails.
 */
class SvgPathParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Parses SVG path data string into a list of PathCommands.
 * @throws SvgPathParseException if the path data is invalid
 */
fun parsePathCommands(pathData: String): List<PathCommand> {
    val commands = mutableListOf<PathCommand>()
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
                // Repeat last command (except for M which becomes L)
                when (lastCommand) {
                    'M' -> 'L'
                    'm' -> 'l'
                    ' ' -> throw SvgPathParseException("Path must start with a move command (M or m), got: $token")
                    else -> lastCommand
                }
            }

            when (command) {
                'M' -> {
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.MoveTo(x, y))
                }
                'm' -> {
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.MoveToRelative(dx, dy))
                }
                'L' -> {
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.LineTo(x, y))
                }
                'l' -> {
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.LineToRelative(dx, dy))
                }
                'H' -> {
                    val x = tokens[i++].toFloat()
                    commands.add(PathCommand.HorizontalLineTo(x))
                }
                'h' -> {
                    val dx = tokens[i++].toFloat()
                    commands.add(PathCommand.HorizontalLineToRelative(dx))
                }
                'V' -> {
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.VerticalLineTo(y))
                }
                'v' -> {
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.VerticalLineToRelative(dy))
                }
                'C' -> {
                    val x1 = tokens[i++].toFloat()
                    val y1 = tokens[i++].toFloat()
                    val x2 = tokens[i++].toFloat()
                    val y2 = tokens[i++].toFloat()
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.CubicTo(x1, y1, x2, y2, x, y))
                }
                'c' -> {
                    val dx1 = tokens[i++].toFloat()
                    val dy1 = tokens[i++].toFloat()
                    val dx2 = tokens[i++].toFloat()
                    val dy2 = tokens[i++].toFloat()
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.CubicToRelative(dx1, dy1, dx2, dy2, dx, dy))
                }
                'S' -> {
                    val x2 = tokens[i++].toFloat()
                    val y2 = tokens[i++].toFloat()
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.SmoothCubicTo(x2, y2, x, y))
                }
                's' -> {
                    val dx2 = tokens[i++].toFloat()
                    val dy2 = tokens[i++].toFloat()
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.SmoothCubicToRelative(dx2, dy2, dx, dy))
                }
                'Q' -> {
                    val x1 = tokens[i++].toFloat()
                    val y1 = tokens[i++].toFloat()
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.QuadTo(x1, y1, x, y))
                }
                'q' -> {
                    val dx1 = tokens[i++].toFloat()
                    val dy1 = tokens[i++].toFloat()
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.QuadToRelative(dx1, dy1, dx, dy))
                }
                'T' -> {
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.SmoothQuadTo(x, y))
                }
                't' -> {
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.SmoothQuadToRelative(dx, dy))
                }
                'A' -> {
                    val rx = tokens[i++].toFloat()
                    val ry = tokens[i++].toFloat()
                    val xAxisRotation = tokens[i++].toFloat()
                    val largeArcFlag = tokens[i++].toFloat() != 0f
                    val sweepFlag = tokens[i++].toFloat() != 0f
                    val x = tokens[i++].toFloat()
                    val y = tokens[i++].toFloat()
                    commands.add(PathCommand.ArcTo(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y))
                }
                'a' -> {
                    val rx = tokens[i++].toFloat()
                    val ry = tokens[i++].toFloat()
                    val xAxisRotation = tokens[i++].toFloat()
                    val largeArcFlag = tokens[i++].toFloat() != 0f
                    val sweepFlag = tokens[i++].toFloat() != 0f
                    val dx = tokens[i++].toFloat()
                    val dy = tokens[i++].toFloat()
                    commands.add(PathCommand.ArcToRelative(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, dx, dy))
                }
                'Z', 'z' -> {
                    commands.add(PathCommand.Close)
                }
                else -> throw SvgPathParseException("Unknown path command: $command")
            }
            lastCommand = command
        }
    } catch (e: SvgPathParseException) {
        throw e
    } catch (e: IndexOutOfBoundsException) {
        throw SvgPathParseException("Unexpected end of path data: not enough parameters for command", e)
    } catch (e: NumberFormatException) {
        throw SvgPathParseException("Invalid number in path data: ${e.message}", e)
    }

    return commands
}

/**
 * Converts a list of PathCommands back to SVG path data string.
 */
fun List<PathCommand>.toPathString(): String = buildString {
    for (cmd in this@toPathString) {
        if (isNotEmpty()) append(' ')
        append(
            when (cmd) {
                is PathCommand.MoveTo -> "M${cmd.x} ${cmd.y}"
                is PathCommand.MoveToRelative -> "m${cmd.dx} ${cmd.dy}"
                is PathCommand.LineTo -> "L${cmd.x} ${cmd.y}"
                is PathCommand.LineToRelative -> "l${cmd.dx} ${cmd.dy}"
                is PathCommand.HorizontalLineTo -> "H${cmd.x}"
                is PathCommand.HorizontalLineToRelative -> "h${cmd.dx}"
                is PathCommand.VerticalLineTo -> "V${cmd.y}"
                is PathCommand.VerticalLineToRelative -> "v${cmd.dy}"
                is PathCommand.CubicTo -> "C${cmd.x1} ${cmd.y1} ${cmd.x2} ${cmd.y2} ${cmd.x} ${cmd.y}"
                is PathCommand.CubicToRelative -> "c${cmd.dx1} ${cmd.dy1} ${cmd.dx2} ${cmd.dy2} ${cmd.dx} ${cmd.dy}"
                is PathCommand.SmoothCubicTo -> "S${cmd.x2} ${cmd.y2} ${cmd.x} ${cmd.y}"
                is PathCommand.SmoothCubicToRelative -> "s${cmd.dx2} ${cmd.dy2} ${cmd.dx} ${cmd.dy}"
                is PathCommand.QuadTo -> "Q${cmd.x1} ${cmd.y1} ${cmd.x} ${cmd.y}"
                is PathCommand.QuadToRelative -> "q${cmd.dx1} ${cmd.dy1} ${cmd.dx} ${cmd.dy}"
                is PathCommand.SmoothQuadTo -> "T${cmd.x} ${cmd.y}"
                is PathCommand.SmoothQuadToRelative -> "t${cmd.dx} ${cmd.dy}"
                is PathCommand.ArcTo -> "A${cmd.rx} ${cmd.ry} ${cmd.xAxisRotation} ${if (cmd.largeArcFlag) 1 else 0} ${if (cmd.sweepFlag) 1 else 0} ${cmd.x} ${cmd.y}"
                is PathCommand.ArcToRelative -> "a${cmd.rx} ${cmd.ry} ${cmd.xAxisRotation} ${if (cmd.largeArcFlag) 1 else 0} ${if (cmd.sweepFlag) 1 else 0} ${cmd.dx} ${cmd.dy}"
                PathCommand.Close -> "Z"
            }
        )
    }
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
                // Handle consecutive decimals like ".5.5" which means "0.5 0.5"
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
