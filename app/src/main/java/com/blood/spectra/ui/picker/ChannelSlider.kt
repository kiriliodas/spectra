package com.blood.spectra.ui.picker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blood.spectra.ui.theme.MonoValueStyle

/**
 * A labeled gradient slider for one color channel: name on the left, the
 * gradient track in the middle (visually representing what it controls), and the
 * value text (monospace) on the right.
 *
 * [normalized] is the thumb position 0f..1f; [onNormalizedChange] reports it.
 */
@Composable
fun ChannelSlider(
    label: String,
    normalized: Float,
    valueText: String,
    trackColors: List<Color>,
    onNormalizedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showCheckerboard: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(24.dp),
            )
            Spacer(Modifier.width(8.dp))
            GradientSlider(
                value = normalized,
                trackColors = trackColors,
                onValueChange = onNormalizedChange,
                showCheckerboard = showCheckerboard,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = valueText,
                style = MonoValueStyle,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.width(52.dp),
            )
        }
    }
}
