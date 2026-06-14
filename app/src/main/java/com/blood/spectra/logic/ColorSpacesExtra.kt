package com.blood.spectra.logic

import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Additional color spaces used only for display/copy formats:
 *  - HWB  (hue / whiteness / blackness) — derived from HSV.
 *  - CMYK (print) — simple non-color-managed approximation.
 *  - OKLCH — perceptual lightness/chroma/hue (sRGB -> linear -> OKLab -> LCh).
 *
 * Pure functions, no Android.
 */
object ColorSpacesExtra {

    // ---- HWB ----------------------------------------------------------------

    data class Hwb(val h: Float, val w: Float, val b: Float)

    fun rgbToHwb(c: ColorValue): Hwb {
        val hsv = c.hsv
        val w = (1f - hsv.s) * hsv.v
        val bl = 1f - hsv.v
        return Hwb(hsv.h, w.coerceIn(0f, 1f), bl.coerceIn(0f, 1f))
    }

    fun hwbString(c: ColorValue): String {
        val hwb = rgbToHwb(c)
        return "hwb(${hwb.h.roundToInt()} ${(hwb.w * 100).roundToInt()}% ${(hwb.b * 100).roundToInt()}%)"
    }

    // ---- CMYK (approximate) -------------------------------------------------

    data class Cmyk(val c: Int, val m: Int, val y: Int, val k: Int)

    fun rgbToCmyk(color: ColorValue): Cmyk {
        val r = color.r / 255.0
        val g = color.g / 255.0
        val b = color.b / 255.0
        val k = 1.0 - maxOf(r, g, b)
        if (k >= 1.0) return Cmyk(0, 0, 0, 100)
        val c = (1.0 - r - k) / (1.0 - k)
        val m = (1.0 - g - k) / (1.0 - k)
        val y = (1.0 - b - k) / (1.0 - k)
        return Cmyk(
            (c * 100).roundToInt(),
            (m * 100).roundToInt(),
            (y * 100).roundToInt(),
            (k * 100).roundToInt(),
        )
    }

    fun cmykString(c: ColorValue): String {
        val v = rgbToCmyk(c)
        return "cmyk(${v.c}%, ${v.m}%, ${v.y}%, ${v.k}%)"
    }

    // ---- OKLCH --------------------------------------------------------------

    data class Oklch(val l: Double, val c: Double, val h: Double)

    private fun srgbToLinear(ch: Int): Double {
        val x = ch / 255.0
        return if (x <= 0.04045) x / 12.92 else ((x + 0.055) / 1.055).pow(2.4)
    }

    /** sRGB -> OKLab -> OKLCh. L in 0..1, C ~0..0.4, h in degrees 0..360. */
    fun rgbToOklch(color: ColorValue): Oklch {
        val r = srgbToLinear(color.r)
        val g = srgbToLinear(color.g)
        val b = srgbToLinear(color.b)

        val l = 0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b
        val m = 0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b
        val s = 0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b

        val l_ = cbrt(l)
        val m_ = cbrt(m)
        val s_ = cbrt(s)

        val okL = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_
        val okA = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_
        val okB = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_

        val chroma = sqrt(okA * okA + okB * okB)
        var hue = Math.toDegrees(atan2(okB, okA))
        if (hue < 0) hue += 360.0
        return Oklch(okL, chroma, hue)
    }

    fun oklchString(c: ColorValue): String {
        val v = rgbToOklch(c)
        val lPct = (v.l * 100).roundToInt()
        val chroma = (v.c * 1000).roundToInt() / 1000.0
        return "oklch($lPct% $chroma ${v.h.roundToInt()})"
    }
}
