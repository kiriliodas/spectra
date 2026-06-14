package com.blood.spectra.logic

import kotlin.math.roundToInt

/** Output formats the user can copy. */
enum class ColorFormat(val label: String) {
    HEX("HEX"),
    HEXA("HEXA"),
    RGB("RGB"),
    RGBA("RGBA"),
    HSL("HSL"),
    HSLA("HSLA"),
    HWB("HWB"),
    OKLCH("OKLCH"),
    CMYK("CMYK"),
}

/** Hex parse + all string formatting. Pure functions. */
object ColorFormats {

    // ---- HEX parsing --------------------------------------------------------

    /**
     * Parses a hex color. Accepts (with or without leading '#'):
     *   #RGB, #RGBA, #RRGGBB, #RRGGBBAA  (case-insensitive)
     * Returns null if it isn't a valid hex string.
     */
    fun parseHex(input: String): ColorValue? {
        var s = input.trim()
        if (s.startsWith("#")) s = s.substring(1)
        if (s.isEmpty() || !s.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) return null

        fun hx(str: String): Int = str.toInt(16)

        return when (s.length) {
            3 -> { // RGB -> expand
                val r = hx("" + s[0] + s[0])
                val g = hx("" + s[1] + s[1])
                val b = hx("" + s[2] + s[2])
                ColorValue(r, g, b, 1f)
            }
            4 -> { // RGBA
                val r = hx("" + s[0] + s[0])
                val g = hx("" + s[1] + s[1])
                val b = hx("" + s[2] + s[2])
                val a = hx("" + s[3] + s[3]) / 255f
                ColorValue(r, g, b, a)
            }
            6 -> ColorValue(hx(s.substring(0, 2)), hx(s.substring(2, 4)), hx(s.substring(4, 6)), 1f)
            8 -> ColorValue(
                hx(s.substring(0, 2)), hx(s.substring(2, 4)), hx(s.substring(4, 6)),
                hx(s.substring(6, 8)) / 255f,
            )
            else -> null
        }
    }

    // ---- Formatting ---------------------------------------------------------

    private fun Int.hex2(): String = toString(16).uppercase().padStart(2, '0')
    private fun Float.alpha255(): Int = (coerceIn(0f, 1f) * 255f).roundToInt()

    fun hex(c: ColorValue, withHash: Boolean = true): String =
        (if (withHash) "#" else "") + c.r.hex2() + c.g.hex2() + c.b.hex2()

    fun hexa(c: ColorValue, withHash: Boolean = true): String =
        hex(c, withHash) + c.a.alpha255().hex2()

    fun rgb(c: ColorValue): String = "rgb(${c.r}, ${c.g}, ${c.b})"

    fun rgba(c: ColorValue): String {
        val a = ((c.a * 100).roundToInt() / 100.0)
        return "rgba(${c.r}, ${c.g}, ${c.b}, ${trimZeros(a)})"
    }

    fun hsl(c: ColorValue): String {
        val h = c.hsl
        return "hsl(${h.h.roundToInt()}, ${(h.s * 100).roundToInt()}%, ${(h.l * 100).roundToInt()}%)"
    }

    fun hsla(c: ColorValue): String {
        val h = c.hsl
        val a = ((c.a * 100).roundToInt() / 100.0)
        return "hsla(${h.h.roundToInt()}, ${(h.s * 100).roundToInt()}%, ${(h.l * 100).roundToInt()}%, ${trimZeros(a)})"
    }

    /** The canonical display/copy string for a given format. */
    fun format(c: ColorValue, fmt: ColorFormat): String = when (fmt) {
        ColorFormat.HEX -> hex(c)
        ColorFormat.HEXA -> hexa(c)
        ColorFormat.RGB -> rgb(c)
        ColorFormat.RGBA -> rgba(c)
        ColorFormat.HSL -> hsl(c)
        ColorFormat.HSLA -> hsla(c)
        ColorFormat.HWB -> ColorSpacesExtra.hwbString(c)
        ColorFormat.OKLCH -> ColorSpacesExtra.oklchString(c)
        ColorFormat.CMYK -> ColorSpacesExtra.cmykString(c)
    }

    private fun trimZeros(d: Double): String {
        val s = d.toString()
        return if (s.endsWith(".0")) s.dropLast(2) else s
    }
}
