package com.blood.spectra.ui.contrast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.blood.spectra.SpectraViewModel
import com.blood.spectra.logic.ColorFormats
import com.blood.spectra.logic.ColorMath
import com.blood.spectra.logic.ColorValue
import com.blood.spectra.ui.SpectraIcons
import com.blood.spectra.ui.morph.MorphIconButton
import com.blood.spectra.ui.theme.MonoValueStyle
import kotlin.math.roundToInt

@Composable
fun ContrastScreen(
    vm: SpectraViewModel,
    modifier: Modifier = Modifier,
) {
    val fg = vm.contrastFg
    val bg = vm.contrastBg
    // Composite fg over bg in case fg is translucent, for a truthful ratio.
    val effectiveFg = ColorMath.compositeOver(fg, bg)
    val ratio = ColorMath.contrastRatio(effectiveFg, bg)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Live sample preview
        SamplePreview(fg = Color(effectiveFg.argb), bg = Color(bg.argb))

        // Big ratio
        RatioCard(ratio)

        // Pass/fail matrix
        ResultMatrix(ratio)

        // Foreground / background editors with a swap button between
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ColorSlot(
                title = "Text",
                color = fg,
                onColor = vm::updateContrastFg,
                onUseCurrent = vm::useCurrentAsContrastFg,
                modifier = Modifier.weight(1f),
            )
            MorphIconButton(onClick = vm::swapContrast, size = 48.dp) {
                androidx.compose.material3.Icon(
                    SpectraIcons.Swap, contentDescription = "Swap text and background",
                    modifier = Modifier.size(22.dp),
                )
            }
            ColorSlot(
                title = "Background",
                color = bg,
                onColor = vm::updateContrastBg,
                onUseCurrent = vm::useCurrentAsContrastBg,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SamplePreview(fg: Color, bg: Color) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .padding(4.dp)
                .clip(MaterialTheme.shapes.large)
                .background(bg)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Large heading", color = fg, style = MaterialTheme.typography.headlineSmall)
            Text(
                "Body text — the quick brown fox jumps over the lazy dog. 0123456789",
                color = fg,
                style = MaterialTheme.typography.bodyMedium,
            )
            Surface(color = fg, shape = RoundedCornerShape(50)) {
                Text(
                    "Button",
                    color = bg,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun RatioCard(ratio: Double) {
    val rounded = (ratio * 100).roundToInt() / 100.0
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$rounded:1",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "contrast ratio",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResultMatrix(ratio: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ResultRow("Normal text", aa = ratio >= 4.5, aaa = ratio >= 7.0)
        ResultRow("Large text", aa = ratio >= 3.0, aaa = ratio >= 4.5)
        ResultRow("UI / graphics", aa = ratio >= 3.0, aaa = null)
    }
}

@Composable
private fun ResultRow(label: String, aa: Boolean, aaa: Boolean?) {
    Surface(
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
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Badge("AA", aa)
            Spacer(Modifier.size(8.dp))
            if (aaa != null) Badge("AAA", aaa) else Spacer(Modifier.size(58.dp))
        }
    }
}

@Composable
private fun Badge(label: String, pass: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (pass) Color(0x331B8A3A) else Color(0x33C02626),
    ) {
        Text(
            text = if (pass) "$label ✓" else "$label ✗",
            style = MaterialTheme.typography.labelMedium,
            color = if (pass) Color(0xFF1B8A3A) else Color(0xFFC02626),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun ColorSlot(
    title: String,
    color: ColorValue,
    onColor: (ColorValue) -> Unit,
    onUseCurrent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var hexText by remember(color.argb) { mutableStateOf(ColorFormats.hex(color, withHash = false)) }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(color.argb))
                    .border(1.dp, Color(0x22000000), RoundedCornerShape(12.dp)),
            )
            // editable HEX
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("#", style = MonoValueStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    BasicTextField(
                        value = hexText,
                        onValueChange = { raw ->
                            val filtered = raw.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }.take(8)
                            hexText = filtered
                            ColorFormats.parseHex(filtered)?.let(onColor)
                        },
                        singleLine = true,
                        textStyle = MonoValueStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            // use current picker color
            Surface(
                onClick = onUseCurrent,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Use picker color",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}
