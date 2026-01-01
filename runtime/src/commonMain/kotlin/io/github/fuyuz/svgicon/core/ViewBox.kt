package io.github.fuyuz.svgicon.core

/**
 * Alignment for preserveAspectRatio.
 * Specifies how to align the viewBox within the viewport.
 */
enum class AspectRatioAlign {
    /** Do not force uniform scaling */
    NONE,
    /** Align min-X of viewBox with min-X of viewport, min-Y with min-Y */
    X_MIN_Y_MIN,
    /** Align mid-X of viewBox with mid-X of viewport, min-Y with min-Y */
    X_MID_Y_MIN,
    /** Align max-X of viewBox with max-X of viewport, min-Y with min-Y */
    X_MAX_Y_MIN,
    /** Align min-X of viewBox with min-X of viewport, mid-Y with mid-Y */
    X_MIN_Y_MID,
    /** Align mid-X of viewBox with mid-X of viewport, mid-Y with mid-Y (default) */
    X_MID_Y_MID,
    /** Align max-X of viewBox with max-X of viewport, mid-Y with mid-Y */
    X_MAX_Y_MID,
    /** Align min-X of viewBox with min-X of viewport, max-Y with max-Y */
    X_MIN_Y_MAX,
    /** Align mid-X of viewBox with mid-X of viewport, max-Y with max-Y */
    X_MID_Y_MAX,
    /** Align max-X of viewBox with max-X of viewport, max-Y with max-Y */
    X_MAX_Y_MAX;

    companion object {
        fun parse(value: String): AspectRatioAlign = when (value.lowercase()) {
            "none" -> NONE
            "xminymin" -> X_MIN_Y_MIN
            "xmidymin" -> X_MID_Y_MIN
            "xmaxymin" -> X_MAX_Y_MIN
            "xminymid" -> X_MIN_Y_MID
            "xmidymid" -> X_MID_Y_MID
            "xmaxymid" -> X_MAX_Y_MID
            "xminymax" -> X_MIN_Y_MAX
            "xmidymax" -> X_MID_Y_MAX
            "xmaxymax" -> X_MAX_Y_MAX
            else -> X_MID_Y_MID
        }
    }
}

/**
 * Meet or slice option for preserveAspectRatio.
 */
enum class MeetOrSlice {
    /** Scale to fit entirely within viewport (default) */
    MEET,
    /** Scale to cover entire viewport, may be clipped */
    SLICE;

    companion object {
        fun parse(value: String): MeetOrSlice = when (value.lowercase()) {
            "slice" -> SLICE
            else -> MEET
        }
    }
}

/**
 * SVG preserveAspectRatio attribute.
 * Controls how viewBox is scaled and positioned within the viewport.
 *
 * @param align How to align the viewBox within the viewport
 * @param meetOrSlice Whether to fit (meet) or cover (slice) the viewport
 */
data class PreserveAspectRatio(
    val align: AspectRatioAlign = AspectRatioAlign.X_MID_Y_MID,
    val meetOrSlice: MeetOrSlice = MeetOrSlice.MEET
) {
    companion object {
        /** Default: xMidYMid meet */
        val Default = PreserveAspectRatio()

        /** No uniform scaling */
        val None = PreserveAspectRatio(AspectRatioAlign.NONE)

        /** Parse from SVG preserveAspectRatio string */
        fun parse(value: String): PreserveAspectRatio {
            val parts = value.trim().split("\\s+".toRegex())
            val align = if (parts.isNotEmpty()) AspectRatioAlign.parse(parts[0]) else AspectRatioAlign.X_MID_Y_MID
            val meetOrSlice = if (parts.size > 1) MeetOrSlice.parse(parts[1]) else MeetOrSlice.MEET
            return PreserveAspectRatio(align, meetOrSlice)
        }
    }
}

/**
 * Type-safe representation of SVG viewBox.
 *
 * @param minX Minimum X coordinate
 * @param minY Minimum Y coordinate
 * @param width Width of the viewBox
 * @param height Height of the viewBox
 */
data class ViewBox(
    val minX: Float = 0f,
    val minY: Float = 0f,
    val width: Float = 24f,
    val height: Float = 24f
) {
    /**
     * Converts to SVG viewBox string format.
     */
    fun toSvgString(): String = "$minX $minY $width $height"

    companion object {
        /** Default 24x24 viewBox */
        val Default = ViewBox(0f, 0f, 24f, 24f)

        /** 16x16 viewBox */
        val Size16 = ViewBox(0f, 0f, 16f, 16f)

        /** 32x32 viewBox */
        val Size32 = ViewBox(0f, 0f, 32f, 32f)

        /** 48x48 viewBox */
        val Size48 = ViewBox(0f, 0f, 48f, 48f)

        /** Create a square viewBox */
        fun square(size: Float) = ViewBox(0f, 0f, size, size)

        /** Parse from SVG viewBox string */
        fun parse(viewBox: String): ViewBox {
            val parts = viewBox.split(" ", ",").mapNotNull { it.trim().toFloatOrNull() }
            return when (parts.size) {
                4 -> ViewBox(parts[0], parts[1], parts[2], parts[3])
                2 -> ViewBox(0f, 0f, parts[0], parts[1])
                else -> Default
            }
        }
    }
}
