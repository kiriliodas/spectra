package com.blood.spectra.ui.picker

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.blood.spectra.SpectraViewModel
import com.blood.spectra.logic.ColorFormat
import com.blood.spectra.logic.ColorFormats
import com.blood.spectra.logic.ColorMath
import com.blood.spectra.logic.ColorSchemes
import com.blood.spectra.logic.ColorValue
import com.blood.spectra.ui.common.CopyRow
import com.blood.spectra.ui.common.SwatchStrip
import com.blood.spectra.ui.common.checkerboard
import com.blood.spectra.ui.theme.MonoValueLargeStyle
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PickerScreen(
    vm: SpectraViewModel,
    modifier: Modifier = Modifier,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current

    val color = vm.current
    var showAddToPalette by remember { mutableStateOf(false) }
    var showEyedropper by remember { mutableStateOf(false) }

    fun copy(value: String) {
        clipboard.setText(AnnotatedString(value))
        vm.rememberRecent()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch { snackbar.showSnackbar("Copied $value") }
    }

    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PreviewCard(
                color,
                onAddToPalette = { showAddToPalette = true },
                onEyedropper = { showEyedropper = true },
            )

            if (vm.recentColors.isNotEmpty()) {
                RecentStrip(vm.recentColors, onPick = { vm.loadColor(it) })
            }

            // 2D saturation/value canvas + hue bar
            val hsv = color.hsv
            SaturationValuePanel(
                hue = hsv.h,
                saturation = hsv.s,
                value = hsv.v,
                onChange = { s, v -> vm.setSaturationValue(s, v) },
            )
            GradientSlider(
                value = hsv.h / 360f,
                trackColors = hueSpectrum(),
                onValueChange = { vm.setHsvHue(it * 360f) },
            )

            HexField(text = vm.hexInput, isError = vm.hexError, onChange = vm::onHexChange)

            SectionHeader("RGB")
            ChannelSlider(
                label = "R", normalized = color.r / 255f, valueText = "${color.r}",
                trackColors = listOf(Color(0, color.g, color.b), Color(255, color.g, color.b)),
                onNormalizedChange = { vm.setR((it * 255).roundToInt()) },
            )
            ChannelSlider(
                label = "G", normalized = color.g / 255f, valueText = "${color.g}",
                trackColors = listOf(Color(color.r, 0, color.b), Color(color.r, 255, color.b)),
                onNormalizedChange = { vm.setG((it * 255).roundToInt()) },
            )
            ChannelSlider(
                label = "B", normalized = color.b / 255f, valueText = "${color.b}",
                trackColors = listOf(Color(color.r, color.g, 0), Color(color.r, color.g, 255)),
                onNormalizedChange = { vm.setB((it * 255).roundToInt()) },
            )
            RangeHint("0–255")

            // HSL — no hue slider here (the rainbow hue bar above already does
            // hue); S and L give fine control of the other two channels.
            SectionHeader("HSL — saturation & lightness")
            val hsl = color.hsl
            ChannelSlider(
                label = "S", normalized = hsl.s, valueText = "${(hsl.s * 100).roundToInt()}%",
                trackColors = listOf(
                    Color(ColorValue.fromHsl(hsl.h, 0f, hsl.l).argb),
                    Color(ColorValue.fromHsl(hsl.h, 1f, hsl.l).argb),
                ),
                onNormalizedChange = { vm.setSaturation(it) },
            )
            ChannelSlider(
                label = "L", normalized = hsl.l, valueText = "${(hsl.l * 100).roundToInt()}%",
                trackColors = listOf(
                    Color.Black,
                    Color(ColorValue.fromHsl(hsl.h, hsl.s, 0.5f).argb),
                    Color.White,
                ),
                onNormalizedChange = { vm.setLightness(it) },
            )

            SectionHeader("Alpha")
            ChannelSlider(
                label = "A", normalized = color.a, valueText = "${(color.a * 100).roundToInt()}%",
                trackColors = listOf(Color(color.r, color.g, color.b, 0), Color(color.r, color.g, color.b)),
                showCheckerboard = true,
                onNormalizedChange = { vm.setAlpha(it) },
            )

            SectionHeader("Formats — tap to copy")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ColorFormat.entries.forEach { fmt ->
                    val value = ColorFormats.format(color, fmt)
                    CopyRow(label = fmt.label, value = value, onClick = { copy(value) })
                }
            }

            SectionHeader("Tints & shades")
            SwatchStrip(
                colors = ColorSchemes.tintShadeRamp(color, steps = 4),
                onPick = { vm.loadColor(it) },
            )

            SectionHeader("Harmonies")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ColorSchemes.Harmony.entries.forEach { harmony ->
                    Column {
                        Text(
                            harmony.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp),
                        )
                        SwatchStrip(
                            colors = ColorSchemes.harmony(color, harmony),
                            onPick = { vm.loadColor(it) },
                            height = 44.dp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // Floats above the bottom nav bar.
        SnackbarHost(
            snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
        )
    }

    if (showAddToPalette) {
        AddToPaletteSheet(
            palettes = vm.palettes.collectAsState().value,
            onAdd = { paletteId ->
                vm.saveCurrentToPalette(paletteId)
                showAddToPalette = false
                scope.launch { snackbar.showSnackbar("Added to palette") }
            },
            onCreate = { name -> vm.createPalette(name) { id -> vm.saveCurrentToPalette(id) }; showAddToPalette = false },
            onDismiss = { showAddToPalette = false },
        )
    }

    if (showEyedropper) {
        EyedropperSheet(
            onColorPicked = { vm.loadColor(it) },
            onDismiss = { showEyedropper = false },
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EyedropperSheet(onColorPicked: (ColorValue) -> Unit, onDismiss: () -> Unit) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        ImageEyedropperSheetContent(onColorPicked = onColorPicked)
    }
}

@Composable
private fun PreviewCard(color: ColorValue, onAddToPalette: () -> Unit, onEyedropper: () -> Unit) {
    val animated by animateColorAsState(Color(color.argb), label = "preview")
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(10.dp)
                .clip(MaterialTheme.shapes.large)
                .checkerboard(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(animated)
                    // subtle inner border so very light colors don't vanish
                    .border(1.dp, Color(0x14000000), MaterialTheme.shapes.large)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AaSample(textColor = Color.Black, bg = color)
                    Text(
                        ColorFormats.hex(color),
                        style = MonoValueLargeStyle,
                        color = readableOn(color),
                    )
                    AaSample(textColor = Color.White, bg = color)
                }
            }
            // Eyedropper + add-to-palette buttons (top-right)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    onClick = onEyedropper,
                    shape = CircleShape,
                    color = Color(0x33FFFFFF),
                    contentColor = readableOn(color),
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Icon(
                            com.blood.spectra.ui.SpectraIcons.Eyedropper,
                            contentDescription = "Pick color from image",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Surface(
                    onClick = onAddToPalette,
                    shape = CircleShape,
                    color = Color(0x33FFFFFF),
                    contentColor = readableOn(color),
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

/** "Aa" sample with a WCAG pass/fail badge against the background color. */
@Composable
private fun AaSample(textColor: Color, bg: ColorValue) {
    val sampleRgb = if (textColor == Color.Black) ColorValue.BLACK else ColorValue.WHITE
    val ratio = ColorMath.contrastRatio(sampleRgb, ColorMath.compositeOver(bg, ColorValue.WHITE))
    val passes = ratio >= 4.5 // AA for normal text
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Aa", style = MaterialTheme.typography.headlineMedium, color = textColor)
        Spacer(Modifier.height(2.dp))
        Surface(
            shape = RoundedCornerShape(50),
            color = if (passes) Color(0xCC1B8A3A) else Color(0xCCC02626),
        ) {
            Text(
                if (passes) "✓ AA" else "✗ AA",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun RecentStrip(recents: List<ColorValue>, onPick: (ColorValue) -> Unit) {
    Column {
        Text(
            "Recent",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
        ) {
            items(recents, key = { it.argb }) { c ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .checkerboard(cell = 6.dp)
                        .background(Color(c.argb), CircleShape)
                        .border(1.dp, Color(0x22000000), CircleShape)
                        .clickable { onPick(c) },
                )
            }
        }
    }
}

@Composable
private fun HexField(text: String, isError: Boolean, onChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isError) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Non-editable decorative '#', tight against the value.
            Text(
                text = "#",
                style = MonoValueLargeStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (text.removePrefix("#").isEmpty()) {
                    Text(
                        "Enter hex…",
                        style = MonoValueLargeStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
                BasicTextField(
                    value = text.removePrefix("#"),
                    onValueChange = onChange,
                    singleLine = true,
                    textStyle = MonoValueLargeStyle.copy(
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 2.dp, top = 10.dp, bottom = 2.dp),
        )
    }
}

@Composable
private fun RangeHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 32.dp),
    )
}

/** The 7-stop rainbow used by hue tracks. */
private fun hueSpectrum(): List<Color> = listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map {
    val (r, g, b) = ColorMath.hsvToRgb(it, 1f, 1f)
    Color(r, g, b)
}

/** Pick black or white text for readability on [color] (per luminance). */
private fun readableOn(color: ColorValue): Color =
    if (color.luminance > 0.45) Color(0xFF111111) else Color(0xFFFFFFFF)

/** Bottom sheet to add the current color to an existing palette or a new one. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AddToPaletteSheet(
    palettes: List<com.blood.spectra.data.Palette>,
    onAdd: (String) -> Unit,
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var creating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Text(
            "Add to palette",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            palettes.forEach { p ->
                Surface(
                    onClick = { onAdd(p.id) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(p.name, style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Text("${p.colors.size}", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (creating) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicTextField(
                            value = newName,
                            onValueChange = { newName = it.take(40) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { inner ->
                                if (newName.isEmpty()) Text("Palette name",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                inner()
                            },
                            modifier = Modifier.weight(1f),
                        )
                        Text("Create", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { if (newName.isNotBlank()) onCreate(newName) }
                                .padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            } else {
                Surface(
                    onClick = { creating = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("+ New palette", style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}
