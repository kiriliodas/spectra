# Spectra — Build Plan

A premium, **100% offline** color tool for designers. Built with Jetpack
Compose + Material 3 Expressive, bootstrapped from the Converter template
(see `../UnitConverter/ARCHITECTURE.md`).

- **App name:** Spectra
- **Package / applicationId:** `com.blood.spectra`
- **Privacy:** no `INTERNET` permission. Palettes stored on-device. The image
  eyedropper uses the system **Photo Picker** (no broad storage permission, no
  network).
- **Navigation:** bottom tabs — **Picker · Palettes · Contrast**.

---

## 1. Feature scope (full MVP)

### Picker tab (home)
- Big live **color preview** surface.
- **Spectrum/wheel** picker (hue–saturation) + **value/alpha** sliders.
- Precise **RGB sliders** + **HSL sliders** + **HEX (with optional alpha)** field.
- Live **format readout**: HEX, RGB(A), HSL(A) — tap any to copy.
- **Live contrast badge** vs white & black (WCAG ratio + AA/AAA).
- **Harmonies** row: complementary / analogous / triadic / split-complementary /
  tetradic — tap a swatch to load it.
- **Tints & shades ramp**: light→dark steps of the current color; tap to load.
- **Image eyedropper**: pick a photo → tap to sample a pixel’s color.
- **Add to palette** action.

### Palettes tab
- List of saved palettes (name + swatches preview).
- Open a palette → grid of its colors; tap a color to load into the Picker;
  remove colors; rename/delete palette; **share/export** as text (HEX list).
- Create a new palette; add the current Picker color to a chosen palette.

### Contrast tab
- Two color slots: **foreground** + **background** (each opens the picker/HEX).
- Large preview of sample text on the background.
- **WCAG ratio** + pass/fail chips for **AA / AAA**, normal & large text.
- Swap fg/bg; quick "fix" suggestion later (post-MVP).

---

## 2. Color science (pure, testable — `logic/`)

All conversions are pure functions, no Android deps (like Converter's
`Converter.kt`). Core type: an immutable `ColorValue` holding RGBA (0–255 + alpha
0–1) as the source of truth, with derived HSL/HSV.

- `Rgb <-> Hsl <-> Hsv` conversions.
- `hex` parse/format (`#RGB`, `#RRGGBB`, `#RRGGBBAA`, with/without `#`).
- **Relative luminance** + **WCAG contrast ratio** (per W3C formula).
- **Harmonies**: rotate hue by fixed offsets.
- **Tints/shades**: mix toward white/black by step.
- Formatting helpers for HEX / `rgb()` / `rgba()` / `hsl()` / `hsla()`.

> These get unit-tested logic-style; verify against known values (e.g. contrast
> of #000 on #FFF = 21:1; #777 on #FFF ≈ 4.48:1).

---

## 3. Data model & persistence (`data/`)

Saved palettes accumulate, but volume is small (dozens, not thousands), so
**DataStore + kotlinx-serialization JSON** is enough (no need for Room here).
Mirrors Converter's `SettingsRepository` pattern.

```
@Serializable data class SavedColor(val argb: Long, val name: String? = null)
@Serializable data class Palette(
    val id: String,            // uuid
    val name: String,
    val colors: List<SavedColor>,
    val updatedAt: Long,
)
```

`PaletteRepository` (DataStore):
- `palettes: Flow<List<Palette>>`
- create / rename / delete palette
- add / remove color in a palette
- (settings) last-picked color, default copy format

---

## 4. State (`SpectraViewModel`, AndroidViewModel)

- `current: ColorValue` (the Picker's active color) + setters from each input
  (wheel, sliders, hex) that keep RGBA authoritative and re-derive the rest.
- `palettes` StateFlow (from repo); `contrastFg` / `contrastBg` state.
- Events: setHue/sat/val/alpha, setRgb, setHex, loadColor, addToPalette,
  create/rename/deletePalette, removeColor, setContrastFg/Bg, swapContrast.
- Eyedropper: receives a sampled `ColorValue` and calls `loadColor`.

Keep one ViewModel for the MVP (shared across tabs so a color picked anywhere
flows everywhere). Split later if it grows.

---

## 5. UI structure (`ui/`)

```
MainActivity                Scaffold + bottom NavigationBar (3 tabs), Compose nav.
ui/picker/PickerScreen      preview, wheel, sliders, format readout, harmonies,
                            ramp, eyedropper, add-to-palette.
ui/picker/ColorWheel        custom Canvas HS wheel + thumb (pointerInput drag).
ui/picker/components        slider rows, format chips, swatch strip.
ui/palettes/PalettesScreen  list + palette detail (sheet or sub-route).
ui/contrast/ContrastScreen  fg/bg slots, sample preview, WCAG chips.
ui/common/                  ColorSwatch, CopyChip, eyedropper sheet, AutoSizeText.
ui/theme/                   reused Color/Type/Shape/Motion/Theme/Fonts
                            (own color identity; keep Fredoka or pick a new face).
```

Reuse from Converter: `theme/*`, `AutoSizeText`, `morph/` (press-squeeze, morph
button), snackbar+haptics patterns, version catalog, CI workflows, icon setup.

---

## 6. Custom components needing care

- **Color wheel** — Canvas: sweep-gradient hue ring or full HS disc + radial
  saturation; draggable thumb via `pointerInput`/`detectDragGestures`; map
  touch↔(h,s). This is the main net-new build vs Converter.
- **Checkerboard alpha background** behind transparent previews/swatches.
- **Image eyedropper** — `PickVisualMedia` photo picker → load into a `Bitmap`
  → on tap, read `getPixel` at the scaled coordinate → `ColorValue`.

---

## 7. Build order (phased; iterate via reviews like Converter)

1. **Scaffold**: gradle (catalog from Converter), manifest (no INTERNET),
   theme, MainActivity + empty 3-tab nav, launcher icon.
2. **Color logic** (`logic/`) + tests-by-hand for conversions & contrast.
3. **Picker core**: preview + RGB/HSL sliders + HEX + live format readout/copy.
4. **Color wheel** component, wired to current color.
5. **Harmonies + tints/shades** rows.
6. **Live contrast badge** on Picker; then the **Contrast tab**.
7. **Persistence** + **Palettes tab** (save/add/remove/rename/delete/export).
8. **Image eyedropper**.
9. Polish: motion, haptics, empty states, accessibility, README + screenshots.

---

## 8. Open decisions (defaults if unspecified)

- **Typeface:** default = keep **Fredoka** (consistency with our house style);
  could switch to a more neutral face for a "designer tool" feel. → decide at theme step.
- **Color identity:** default = a neutral/dark-friendly scheme that lets sample
  colors pop (the UI shouldn't fight the content). → decide at theme step.
- **Dark theme:** support both; dynamic color optional (a color tool often wants
  a fixed neutral chrome — likely **disable** Material You here so swatches read
  truly). → decide at theme step.
