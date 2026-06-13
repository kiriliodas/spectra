package com.blood.spectra.ui.picker

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.blood.spectra.SpectraViewModel
import com.blood.spectra.logic.ColorFormat
import com.blood.spectra.logic.ColorFormats
import com.blood.spectra.logic.ColorValue
import com.blood.spectra.ui.common.CopyRow
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
    val composeColor = Color(color.argb)

    fun copy(label: String, value: String) {
        clipboard.setText(AnnotatedString(value))
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch { snackbar.showSnackbar("Copied $value") }
    }

    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreviewCard(color)

            HexField(
                text = vm.hexInput,
                isError = vm.hexError,
                onChange = vm::onHexChange,
            )

            // RGB sliders
            SectionLabel("RGB")
            ChannelSlider(
                label = "R", value = color.r.toFloat(), valueRange = 0f..255f,
                valueText = color.r.toString(), onValueChange = { vm.setR(it.roundToInt()) },
            )
            ChannelSlider(
                label = "G", value = color.g.toFloat(), valueRange = 0f..255f,
                valueText = color.g.toString(), onValueChange = { vm.setG(it.roundToInt()) },
            )
            ChannelSlider(
                label = "B", value = color.b.toFloat(), valueRange = 0f..255f,
                valueText = color.b.toString(), onValueChange = { vm.setB(it.roundToInt()) },
            )

            // HSL sliders
            SectionLabel("HSL")
            val hsl = color.hsl
            ChannelSlider(
                label = "H", value = hsl.h, valueRange = 0f..360f,
                valueText = "${hsl.h.roundToInt()}°", onValueChange = { vm.setHue(it) },
            )
            ChannelSlider(
                label = "S", value = hsl.s, valueRange = 0f..1f,
                valueText = "${(hsl.s * 100).roundToInt()}%", onValueChange = { vm.setSaturation(it) },
            )
            ChannelSlider(
                label = "L", value = hsl.l, valueRange = 0f..1f,
                valueText = "${(hsl.l * 100).roundToInt()}%", onValueChange = { vm.setLightness(it) },
            )

            // Alpha
            SectionLabel("Alpha")
            ChannelSlider(
                label = "A", value = color.a, valueRange = 0f..1f,
                valueText = "${(color.a * 100).roundToInt()}%", onValueChange = { vm.setAlpha(it) },
            )

            // Format readout
            SectionLabel("Formats — tap to copy")
            FormatList(color, onCopy = ::copy)

            Spacer(Modifier.height(8.dp))
        }

        SnackbarHost(
            snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PreviewCard(color: ColorValue) {
    val animated by animateColorAsState(Color(color.argb), label = "preview")
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(10.dp)
                .clip(MaterialTheme.shapes.large)
                .checkerboard(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(animated),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = ColorFormats.hex(color),
                    style = MonoValueLargeStyle,
                    color = readableOn(color),
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
            Text(
                text = "#",
                style = MonoValueLargeStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(6.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (text.isEmpty()) {
                    Text(
                        "RRGGBB",
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
private fun FormatList(color: ColorValue, onCopy: (String, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ColorFormat.entries.forEach { fmt ->
            val value = ColorFormats.format(color, fmt)
            CopyRow(label = fmt.label, value = value, onClick = { onCopy(fmt.label, value) })
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

/** Pick black or white text for readability on [color] (per luminance). */
private fun readableOn(color: ColorValue): Color =
    if (color.luminance > 0.45) Color(0xFF111111) else Color(0xFFFFFFFF)
