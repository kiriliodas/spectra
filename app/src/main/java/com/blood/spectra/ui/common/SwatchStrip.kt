package com.blood.spectra.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blood.spectra.logic.ColorValue

/**
 * A row of equal-width color swatches (e.g. a harmony or a tint/shade ramp).
 * Tapping a swatch invokes [onPick]. Translucent colors show a checkerboard.
 */
@Composable
fun SwatchStrip(
    colors: List<ColorValue>,
    onPick: (ColorValue) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        colors.forEach { c ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .checkerboard(cell = 7.dp)
                    .background(Color(c.argb))
                    .border(0.5.dp, Color(0x14000000))
                    .clickable { onPick(c) },
            )
        }
    }
}
