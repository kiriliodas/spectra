package com.blood.spectra

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.blood.spectra.logic.ColorFormats
import com.blood.spectra.logic.ColorValue

/**
 * Shared app state. Holds the single "current" color the user is editing on the
 * Picker, so a color chosen anywhere flows across tabs later. Pure in-memory for
 * now; persistence (palettes) arrives in a later phase.
 *
 * RGBA is the source of truth. Slider edits update specific channels and the
 * derived HSL/HSV are recomputed from RGBA on read (no drift).
 */
class SpectraViewModel : ViewModel() {

    var current by mutableStateOf(ColorValue(33, 150, 243)) // a pleasant default blue
        private set

    /** Raw text in the HEX field; kept separate so partial typing doesn't fight us. */
    var hexInput by mutableStateOf(ColorFormats.hex(current, withHash = false))
        private set

    /** True when [hexInput] is non-blank but not a valid hex color. */
    val hexError: Boolean
        get() = hexInput.isNotBlank() && ColorFormats.parseHex(hexInput) == null

    private fun setColor(c: ColorValue, syncHex: Boolean = true) {
        current = c
        if (syncHex) hexInput = ColorFormats.hex(c, withHash = false)
    }

    fun loadColor(c: ColorValue) = setColor(c)

    // ---- RGB channel edits ---------------------------------------------------

    fun setR(v: Int) = setColor(current.copy(r = v.coerceIn(0, 255)))
    fun setG(v: Int) = setColor(current.copy(g = v.coerceIn(0, 255)))
    fun setB(v: Int) = setColor(current.copy(b = v.coerceIn(0, 255)))
    fun setAlpha(v: Float) = setColor(current.withAlpha(v))

    // ---- HSL channel edits (preserve the other two) --------------------------

    fun setHue(h: Float) {
        val hsl = current.hsl
        setColor(ColorValue.fromHsl(h, hsl.s, hsl.l, current.a))
    }

    fun setSaturation(s: Float) {
        val hsl = current.hsl
        setColor(ColorValue.fromHsl(hsl.h, s, hsl.l, current.a))
    }

    fun setLightness(l: Float) {
        val hsl = current.hsl
        setColor(ColorValue.fromHsl(hsl.h, hsl.s, l, current.a))
    }

    // ---- HEX field -----------------------------------------------------------

    fun onHexChange(raw: String) {
        // keep only hex-ish characters, allow a leading '#', cap length
        val filtered = raw.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == '#' }
            .take(9)
        hexInput = filtered
        ColorFormats.parseHex(filtered)?.let { setColor(it, syncHex = false) }
    }
}
