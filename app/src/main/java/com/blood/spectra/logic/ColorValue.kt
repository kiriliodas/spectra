package com.blood.spectra.logic

import kotlin.math.roundToInt

/**
 * The app's single source of truth for a color.
 *
 * RGBA channels are the canonical representation:
 *  - [r], [g], [b] are 0..255 integers
 *  - [a] is 0f..1f (alpha)
 *
 * HSL / HSV are *derived* on demand so we never accumulate rounding drift while
 * the user drags a slider. All conversions are pure functions (no Android),
 * which keeps this trivially testable.
 */
data class ColorValue(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Float = 1f,
) {
    init {
        require(r in 0..255 && g in 0..255 && b in 0..255) { "RGB out of range" }
    }

    /** Packed 0xAARRGGBB, handy for storage and for Compose Color(argb). */
    val argb: Long
        get() {
            val ai = (a.coerceIn(0f, 1f) * 255f).roundToInt()
            return ((ai.toLong() and 0xFF) shl 24) or
                ((r.toLong() and 0xFF) shl 16) or
                ((g.toLong() and 0xFF) shl 8) or
                (b.toLong() and 0xFF)
        }

    val hsl: Hsl get() = ColorMath.rgbToHsl(r, g, b)
    val hsv: Hsv get() = ColorMath.rgbToHsv(r, g, b)

    /** Relative luminance (WCAG), 0f (black) .. 1f (white). Ignores alpha. */
    val luminance: Double get() = ColorMath.relativeLuminance(r, g, b)

    fun withAlpha(newAlpha: Float): ColorValue = copy(a = newAlpha.coerceIn(0f, 1f))

    companion object {
        fun fromArgb(argb: Long): ColorValue {
            val a = ((argb shr 24) and 0xFF).toInt() / 255f
            val r = ((argb shr 16) and 0xFF).toInt()
            val g = ((argb shr 8) and 0xFF).toInt()
            val b = (argb and 0xFF).toInt()
            return ColorValue(r, g, b, a)
        }

        fun fromHsl(h: Float, s: Float, l: Float, a: Float = 1f): ColorValue {
            val (r, g, b) = ColorMath.hslToRgb(h, s, l)
            return ColorValue(r, g, b, a)
        }

        fun fromHsv(h: Float, s: Float, v: Float, a: Float = 1f): ColorValue {
            val (r, g, b) = ColorMath.hsvToRgb(h, s, v)
            return ColorValue(r, g, b, a)
        }

        val WHITE = ColorValue(255, 255, 255)
        val BLACK = ColorValue(0, 0, 0)
    }
}

/** Hue 0..360, saturation 0..1, lightness 0..1. */
data class Hsl(val h: Float, val s: Float, val l: Float)

/** Hue 0..360, saturation 0..1, value 0..1. */
data class Hsv(val h: Float, val s: Float, val v: Float)
