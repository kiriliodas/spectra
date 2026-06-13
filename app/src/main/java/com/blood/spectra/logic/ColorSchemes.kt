package com.blood.spectra.logic

/**
 * Color harmony generators and tint/shade ramps. Pure functions operating on the
 * HSL representation (hue rotations + lightness mixing toward white/black).
 */
object ColorSchemes {

    enum class Harmony(val label: String, val hueOffsets: List<Float>) {
        COMPLEMENTARY("Complementary", listOf(0f, 180f)),
        ANALOGOUS("Analogous", listOf(-30f, 0f, 30f)),
        TRIADIC("Triadic", listOf(0f, 120f, 240f)),
        SPLIT("Split-complementary", listOf(0f, 150f, 210f)),
        TETRADIC("Tetradic", listOf(0f, 90f, 180f, 270f)),
        SQUARE("Square", listOf(0f, 90f, 180f, 270f)),
    }

    /** Returns the colors of a harmony based on [base]'s hue (keeps S & L). */
    fun harmony(base: ColorValue, type: Harmony): List<ColorValue> {
        val hsl = base.hsl
        return type.hueOffsets.map { offset ->
            val h = ((hsl.h + offset) % 360f + 360f) % 360f
            ColorValue.fromHsl(h, hsl.s, hsl.l, base.a)
        }
    }

    /**
     * A symmetric tints→base→shades ramp.
     * Tints mix toward white (lighter), shades toward black (darker).
     * [steps] is the count on EACH side; total = steps*2 + 1.
     */
    fun tintShadeRamp(base: ColorValue, steps: Int = 4): List<ColorValue> {
        val out = ArrayList<ColorValue>(steps * 2 + 1)
        // tints (lightest first)
        for (i in steps downTo 1) {
            val t = i.toFloat() / (steps + 1)
            out.add(mix(base, ColorValue.WHITE, t))
        }
        out.add(base)
        // shades (darkening)
        for (i in 1..steps) {
            val t = i.toFloat() / (steps + 1)
            out.add(mix(base, ColorValue.BLACK, t))
        }
        return out
    }

    /** Linear RGB-space mix of [a] toward [b] by [t] (0f=a, 1f=b). Keeps a's alpha. */
    fun mix(a: ColorValue, b: ColorValue, t: Float): ColorValue {
        val tt = t.coerceIn(0f, 1f)
        fun lerp(x: Int, y: Int) = (x + (y - x) * tt).toInt().coerceIn(0, 255)
        return ColorValue(lerp(a.r, b.r), lerp(a.g, b.g), lerp(a.b, b.b), a.a)
    }
}
