package com.blood.spectra.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A modifier that paints a light/grey checkerboard, used BEHIND translucent
 * colors so alpha is visible (the classic "transparency" pattern).
 */
fun Modifier.checkerboard(
    cell: Dp = 8.dp,
    light: Color = Color(0xFFFFFFFF),
    dark: Color = Color(0xFFE0E0E0),
): Modifier = this.drawBehind {
    val c = cell.toPx()
    if (c <= 0f) return@drawBehind
    drawRect(light, size = Size(size.width, size.height))
    val cols = (size.width / c).toInt() + 1
    val rows = (size.height / c).toInt() + 1
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            if ((row + col) % 2 == 0) {
                drawRect(
                    color = dark,
                    topLeft = Offset(col * c, row * c),
                    size = Size(c, c),
                )
            }
        }
    }
}
