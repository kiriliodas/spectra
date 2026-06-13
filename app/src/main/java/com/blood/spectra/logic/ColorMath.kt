package com.blood.spectra.logic

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Pure color-science functions. No Android, no state — easy to unit test.
 *
 * References:
 *  - HSL/HSV per the standard formulas (hue in degrees 0..360).
 *  - WCAG relative luminance & contrast ratio per W3C definitions.
 */
object ColorMath {

    // ---- RGB <-> HSL --------------------------------------------------------

    fun rgbToHsl(r: Int, g: Int, b: Int): Hsl {
        val rf = r / 255.0; val gf = g / 255.0; val bf = b / 255.0
        val maxC = max(rf, max(gf, bf))
        val minC = min(rf, min(gf, bf))
        val delta = maxC - minC
        val l = (maxC + minC) / 2.0

        val h: Double
        val s: Double
        if (delta == 0.0) {
            h = 0.0
            s = 0.0
        } else {
            s = delta / (1.0 - abs(2.0 * l - 1.0))
            h = when (maxC) {
                rf -> 60.0 * (((gf - bf) / delta) % 6.0)
                gf -> 60.0 * (((bf - rf) / delta) + 2.0)
                else -> 60.0 * (((rf - gf) / delta) + 4.0)
            }
        }
        val hue = ((h % 360.0) + 360.0) % 360.0
        return Hsl(hue.toFloat(), s.toFloat().coerceIn(0f, 1f), l.toFloat().coerceIn(0f, 1f))
    }

    fun hslToRgb(h: Float, s: Float, l: Float): Triple<Int, Int, Int> {
        val hh = ((h % 360f) + 360f) % 360f
        val ss = s.coerceIn(0f, 1f).toDouble()
        val ll = l.coerceIn(0f, 1f).toDouble()
        val c = (1.0 - abs(2.0 * ll - 1.0)) * ss
        val x = c * (1.0 - abs((hh / 60.0) % 2.0 - 1.0))
        val m = ll - c / 2.0
        val (r1, g1, b1) = when {
            hh < 60 -> Triple(c, x, 0.0)
            hh < 120 -> Triple(x, c, 0.0)
            hh < 180 -> Triple(0.0, c, x)
            hh < 240 -> Triple(0.0, x, c)
            hh < 300 -> Triple(x, 0.0, c)
            else -> Triple(c, 0.0, x)
        }
        return Triple(
            ((r1 + m) * 255.0).roundToInt().coerceIn(0, 255),
            ((g1 + m) * 255.0).roundToInt().coerceIn(0, 255),
            ((b1 + m) * 255.0).roundToInt().coerceIn(0, 255),
        )
    }

    // ---- RGB <-> HSV --------------------------------------------------------

    fun rgbToHsv(r: Int, g: Int, b: Int): Hsv {
        val rf = r / 255.0; val gf = g / 255.0; val bf = b / 255.0
        val maxC = max(rf, max(gf, bf))
        val minC = min(rf, min(gf, bf))
        val delta = maxC - minC
        val h = when {
            delta == 0.0 -> 0.0
            maxC == rf -> 60.0 * (((gf - bf) / delta) % 6.0)
            maxC == gf -> 60.0 * (((bf - rf) / delta) + 2.0)
            else -> 60.0 * (((rf - gf) / delta) + 4.0)
        }
        val hue = ((h % 360.0) + 360.0) % 360.0
        val s = if (maxC == 0.0) 0.0 else delta / maxC
        return Hsv(hue.toFloat(), s.toFloat().coerceIn(0f, 1f), maxC.toFloat().coerceIn(0f, 1f))
    }

    fun hsvToRgb(h: Float, s: Float, v: Float): Triple<Int, Int, Int> {
        val hh = ((h % 360f) + 360f) % 360f
        val ss = s.coerceIn(0f, 1f).toDouble()
        val vv = v.coerceIn(0f, 1f).toDouble()
        val c = vv * ss
        val x = c * (1.0 - abs((hh / 60.0) % 2.0 - 1.0))
        val m = vv - c
        val (r1, g1, b1) = when {
            hh < 60 -> Triple(c, x, 0.0)
            hh < 120 -> Triple(x, c, 0.0)
            hh < 180 -> Triple(0.0, c, x)
            hh < 240 -> Triple(0.0, x, c)
            hh < 300 -> Triple(x, 0.0, c)
            else -> Triple(c, 0.0, x)
        }
        return Triple(
            ((r1 + m) * 255.0).roundToInt().coerceIn(0, 255),
            ((g1 + m) * 255.0).roundToInt().coerceIn(0, 255),
            ((b1 + m) * 255.0).roundToInt().coerceIn(0, 255),
        )
    }

    // ---- WCAG luminance & contrast -----------------------------------------

    /** sRGB channel (0..255) -> linear value. */
    private fun linearize(channel: Int): Double {
        val c = channel / 255.0
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    /** WCAG relative luminance, 0f..1f. */
    fun relativeLuminance(r: Int, g: Int, b: Int): Double =
        0.2126 * linearize(r) + 0.7152 * linearize(g) + 0.0722 * linearize(b)

    /**
     * WCAG contrast ratio between two opaque colors, 1.0 .. 21.0.
     * (Alpha is ignored; for translucent fg the UI should pre-composite first.)
     */
    fun contrastRatio(a: ColorValue, b: ColorValue): Double {
        val la = relativeLuminance(a.r, a.g, a.b)
        val lb = relativeLuminance(b.r, b.g, b.b)
        val lighter = max(la, lb)
        val darker = min(la, lb)
        return (lighter + 0.05) / (darker + 0.05)
    }

    /** Composite a (possibly translucent) foreground over an opaque background. */
    fun compositeOver(fg: ColorValue, bg: ColorValue): ColorValue {
        val af = fg.a.coerceIn(0f, 1f)
        fun mix(f: Int, b: Int) = (f * af + b * (1f - af)).roundToInt().coerceIn(0, 255)
        return ColorValue(mix(fg.r, bg.r), mix(fg.g, bg.g), mix(fg.b, bg.b), 1f)
    }
}
