package io.github.fuyuz.svgicon.core

import androidx.compose.ui.graphics.Path

/**
 * Converts a list of PathCommands to a Compose Path.
 */
fun List<PathCommand>.toPath(): Path {
    val path = Path()
    var currentX = 0f
    var currentY = 0f
    var lastControlX = 0f
    var lastControlY = 0f

    for (cmd in this) {
        when (cmd) {
            is PathCommand.MoveTo -> {
                currentX = cmd.x
                currentY = cmd.y
                path.moveTo(currentX, currentY)
                // Reset control point for smooth curves
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.MoveToRelative -> {
                currentX += cmd.dx
                currentY += cmd.dy
                path.moveTo(currentX, currentY)
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.LineTo -> {
                currentX = cmd.x
                currentY = cmd.y
                path.lineTo(currentX, currentY)
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.LineToRelative -> {
                currentX += cmd.dx
                currentY += cmd.dy
                path.lineTo(currentX, currentY)
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.HorizontalLineTo -> {
                currentX = cmd.x
                path.lineTo(currentX, currentY)
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.HorizontalLineToRelative -> {
                currentX += cmd.dx
                path.lineTo(currentX, currentY)
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.VerticalLineTo -> {
                currentY = cmd.y
                path.lineTo(currentX, currentY)
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.VerticalLineToRelative -> {
                currentY += cmd.dy
                path.lineTo(currentX, currentY)
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.CubicTo -> {
                path.cubicTo(cmd.x1, cmd.y1, cmd.x2, cmd.y2, cmd.x, cmd.y)
                lastControlX = cmd.x2
                lastControlY = cmd.y2
                currentX = cmd.x
                currentY = cmd.y
            }
            is PathCommand.CubicToRelative -> {
                val x1 = currentX + cmd.dx1
                val y1 = currentY + cmd.dy1
                val x2 = currentX + cmd.dx2
                val y2 = currentY + cmd.dy2
                currentX += cmd.dx
                currentY += cmd.dy
                path.cubicTo(x1, y1, x2, y2, currentX, currentY)
                lastControlX = x2
                lastControlY = y2
            }
            is PathCommand.SmoothCubicTo -> {
                val x1 = 2 * currentX - lastControlX
                val y1 = 2 * currentY - lastControlY
                path.cubicTo(x1, y1, cmd.x2, cmd.y2, cmd.x, cmd.y)
                lastControlX = cmd.x2
                lastControlY = cmd.y2
                currentX = cmd.x
                currentY = cmd.y
            }
            is PathCommand.SmoothCubicToRelative -> {
                val x1 = 2 * currentX - lastControlX
                val y1 = 2 * currentY - lastControlY
                val x2 = currentX + cmd.dx2
                val y2 = currentY + cmd.dy2
                currentX += cmd.dx
                currentY += cmd.dy
                path.cubicTo(x1, y1, x2, y2, currentX, currentY)
                lastControlX = x2
                lastControlY = y2
            }
            is PathCommand.QuadTo -> {
                path.quadraticTo(cmd.x1, cmd.y1, cmd.x, cmd.y)
                lastControlX = cmd.x1
                lastControlY = cmd.y1
                currentX = cmd.x
                currentY = cmd.y
            }
            is PathCommand.QuadToRelative -> {
                val x1 = currentX + cmd.dx1
                val y1 = currentY + cmd.dy1
                currentX += cmd.dx
                currentY += cmd.dy
                path.quadraticTo(x1, y1, currentX, currentY)
                lastControlX = x1
                lastControlY = y1
            }
            is PathCommand.SmoothQuadTo -> {
                val x1 = 2 * currentX - lastControlX
                val y1 = 2 * currentY - lastControlY
                path.quadraticTo(x1, y1, cmd.x, cmd.y)
                lastControlX = x1
                lastControlY = y1
                currentX = cmd.x
                currentY = cmd.y
            }
            is PathCommand.SmoothQuadToRelative -> {
                val x1 = 2 * currentX - lastControlX
                val y1 = 2 * currentY - lastControlY
                currentX += cmd.dx
                currentY += cmd.dy
                path.quadraticTo(x1, y1, currentX, currentY)
                lastControlX = x1
                lastControlY = y1
            }
            is PathCommand.ArcTo -> {
                drawArcToPath(path, currentX, currentY, cmd.x, cmd.y, cmd.rx, cmd.ry, cmd.xAxisRotation, cmd.largeArcFlag, cmd.sweepFlag)
                currentX = cmd.x
                currentY = cmd.y
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.ArcToRelative -> {
                val endX = currentX + cmd.dx
                val endY = currentY + cmd.dy
                drawArcToPath(path, currentX, currentY, endX, endY, cmd.rx, cmd.ry, cmd.xAxisRotation, cmd.largeArcFlag, cmd.sweepFlag)
                currentX = endX
                currentY = endY
                lastControlX = currentX
                lastControlY = currentY
            }
            is PathCommand.Close -> {
                path.close()
                // Note: Close doesn't reset control point per SVG spec
            }
        }
    }
    return path
}

/**
 * Converts SvgPath to a Compose Path.
 */
fun SvgPath.toPath(): Path = commands.toPath()

private fun drawArcToPath(
    path: Path,
    startX: Float, startY: Float,
    endX: Float, endY: Float,
    rx: Float, ry: Float,
    xAxisRotation: Float,
    largeArcFlag: Boolean,
    sweepFlag: Boolean
) {
    if (rx == 0f || ry == 0f) {
        path.lineTo(endX, endY)
        return
    }

    val phi = xAxisRotation * (kotlin.math.PI / 180.0)
    val cosPhi = kotlin.math.cos(phi).toFloat()
    val sinPhi = kotlin.math.sin(phi).toFloat()

    val dx = (startX - endX) / 2f
    val dy = (startY - endY) / 2f

    val x1p = cosPhi * dx + sinPhi * dy
    val y1p = -sinPhi * dx + cosPhi * dy

    var rxAbs = kotlin.math.abs(rx)
    var ryAbs = kotlin.math.abs(ry)

    val x1pSq = x1p * x1p
    val y1pSq = y1p * y1p
    var rxSq = rxAbs * rxAbs
    var rySq = ryAbs * ryAbs

    val lambda = x1pSq / rxSq + y1pSq / rySq
    if (lambda > 1f) {
        val lambdaSqrt = kotlin.math.sqrt(lambda)
        rxAbs *= lambdaSqrt
        ryAbs *= lambdaSqrt
        rxSq = rxAbs * rxAbs
        rySq = ryAbs * ryAbs
    }

    var sq = ((rxSq * rySq) - (rxSq * y1pSq) - (rySq * x1pSq)) / ((rxSq * y1pSq) + (rySq * x1pSq))
    sq = if (sq < 0f) 0f else sq

    val coef = (if (largeArcFlag == sweepFlag) -1f else 1f) * kotlin.math.sqrt(sq)
    val cxp = coef * (rxAbs * y1p / ryAbs)
    val cyp = coef * -(ryAbs * x1p / rxAbs)

    val cx = cosPhi * cxp - sinPhi * cyp + (startX + endX) / 2f
    val cy = sinPhi * cxp + cosPhi * cyp + (startY + endY) / 2f

    fun angle(ux: Float, uy: Float, vx: Float, vy: Float): Float {
        val dot = ux * vx + uy * vy
        val len = kotlin.math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy))
        var ang = kotlin.math.acos((dot / len).coerceIn(-1f, 1f))
        if (ux * vy - uy * vx < 0f) ang = -ang
        return ang
    }

    val theta1 = angle(1f, 0f, (x1p - cxp) / rxAbs, (y1p - cyp) / ryAbs)
    var dTheta = angle((x1p - cxp) / rxAbs, (y1p - cyp) / ryAbs, (-x1p - cxp) / rxAbs, (-y1p - cyp) / ryAbs)

    if (!sweepFlag && dTheta > 0) {
        dTheta -= (2 * kotlin.math.PI).toFloat()
    } else if (sweepFlag && dTheta < 0) {
        dTheta += (2 * kotlin.math.PI).toFloat()
    }

    val segments = kotlin.math.ceil(kotlin.math.abs(dTheta) / (kotlin.math.PI / 2)).toInt().coerceAtLeast(1)
    val deltaTheta = dTheta / segments

    for (seg in 0 until segments) {
        val t1 = theta1 + seg * deltaTheta
        val t2 = theta1 + (seg + 1) * deltaTheta

        val alpha = kotlin.math.sin(deltaTheta) * (kotlin.math.sqrt(4 + 3 * kotlin.math.tan(deltaTheta / 2).let { it * it }) - 1) / 3

        val cos1 = kotlin.math.cos(t1)
        val sin1 = kotlin.math.sin(t1)
        val cos2 = kotlin.math.cos(t2)
        val sin2 = kotlin.math.sin(t2)

        val p2x = cosPhi * rxAbs * cos2 - sinPhi * ryAbs * sin2 + cx
        val p2y = sinPhi * rxAbs * cos2 + cosPhi * ryAbs * sin2 + cy

        val p1x = cosPhi * rxAbs * cos1 - sinPhi * ryAbs * sin1 + cx
        val p1y = sinPhi * rxAbs * cos1 + cosPhi * ryAbs * sin1 + cy

        val d1x = -rxAbs * sin1
        val d1y = ryAbs * cos1
        val d2x = -rxAbs * sin2
        val d2y = ryAbs * cos2

        val cp1x = p1x + alpha * (cosPhi * d1x - sinPhi * d1y)
        val cp1y = p1y + alpha * (sinPhi * d1x + cosPhi * d1y)
        val cp2x = p2x - alpha * (cosPhi * d2x - sinPhi * d2y)
        val cp2y = p2y - alpha * (sinPhi * d2x + cosPhi * d2y)

        path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2x, p2y)
    }
}
