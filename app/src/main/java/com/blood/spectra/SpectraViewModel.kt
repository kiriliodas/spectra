package com.blood.spectra

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blood.spectra.data.Palette
import com.blood.spectra.data.PaletteRepository
import com.blood.spectra.logic.ColorFormats
import com.blood.spectra.logic.ColorValue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Shared app state. Holds the "current" color the user is editing on the Picker
 * (so it flows across tabs) plus the saved palettes (persisted on-device).
 *
 * RGBA is the source of truth. Slider edits update specific channels and the
 * derived HSL/HSV are recomputed from RGBA on read (no drift).
 */
class SpectraViewModel(app: Application) : AndroidViewModel(app) {

    private val paletteRepo = PaletteRepository(app)

    val palettes: StateFlow<List<Palette>> =
        paletteRepo.palettes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var current by mutableStateOf(ColorValue(33, 150, 243)) // a pleasant default blue
        private set

    /** Recently committed colors (most-recent first), in-memory for quick reuse. */
    var recentColors by mutableStateOf<List<ColorValue>>(emptyList())
        private set

    // ---- Contrast tab state --------------------------------------------------

    var contrastFg by mutableStateOf(ColorValue(0x21, 0x21, 0x21))   // near-black text
        private set
    var contrastBg by mutableStateOf(ColorValue(255, 255, 255))      // white bg
        private set

    fun updateContrastFg(c: ColorValue) { contrastFg = c }
    fun updateContrastBg(c: ColorValue) { contrastBg = c }
    fun swapContrast() { val f = contrastFg; contrastFg = contrastBg; contrastBg = f }

    /** Send the current Picker color into a contrast slot. */
    fun useCurrentAsContrastFg() { contrastFg = current }
    fun useCurrentAsContrastBg() { contrastBg = current }

    /** Snapshot the current color into the recents strip (e.g. on copy/save). */
    fun rememberRecent() {
        val c = current
        val deduped = recentColors.filter { it.argb != c.argb }
        recentColors = (listOf(c) + deduped).take(12)
    }

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

    // ---- HSV edits (for the 2D saturation/value panel) -----------------------

    fun setSaturationValue(s: Float, v: Float) {
        val hsv = current.hsv
        setColor(ColorValue.fromHsv(hsv.h, s, v, current.a))
    }

    /** Hue edit that preserves HSV saturation/value (used by the hue bar). */
    fun setHsvHue(h: Float) {
        val hsv = current.hsv
        setColor(ColorValue.fromHsv(h, hsv.s, hsv.v, current.a))
    }

    // ---- HEX field -----------------------------------------------------------

    fun onHexChange(raw: String) {
        // keep only hex-ish characters, allow a leading '#', cap length
        val filtered = raw.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == '#' }
            .take(9)
        hexInput = filtered
        ColorFormats.parseHex(filtered)?.let { setColor(it, syncHex = false) }
    }

    // ---- Palettes (persisted on-device) --------------------------------------

    fun createPalette(name: String, onCreated: (String) -> Unit = {}) {
        viewModelScope.launch { onCreated(paletteRepo.createPalette(name)) }
    }

    fun renamePalette(id: String, name: String) {
        viewModelScope.launch { paletteRepo.renamePalette(id, name) }
    }

    fun deletePalette(id: String) {
        viewModelScope.launch { paletteRepo.deletePalette(id) }
    }

    fun addColorToPalette(paletteId: String, argb: Long) {
        viewModelScope.launch { paletteRepo.addColor(paletteId, argb) }
    }

    fun removeColorFromPalette(paletteId: String, argb: Long) {
        viewModelScope.launch { paletteRepo.removeColor(paletteId, argb) }
    }

    /** Convenience: save the current Picker color to a palette. */
    fun saveCurrentToPalette(paletteId: String) =
        addColorToPalette(paletteId, current.argb)
}
