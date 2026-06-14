package com.blood.spectra.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val Light = Color(0xFFFFFFFF)
private val Dark = Color(0xFFD9D9D9)

/**
 * A modifier that paints a light/grey checkerboard, used BEHIND translucent
 * colors so alpha is visible (the classic "transparency" pattern).
 */
fun Modifier.checkerboard(
    cell: Dp = 8.dp,
    light: Color = Light,
    dark: Color = Dark,
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

/**
 * Draws a checkerboard inside a rounded horizontal band [top, top+h], clipped to
 * the rounded-rect shape. Used as the alpha-slider underlay.
 */
fun DrawScope.checkerboardCells(
    top: Float,
    h: Float,
    radius: Float,
    cell: Float = 10f,
) {
    val path = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = 0f, top = top, right = size.width, bottom = top + h,
                cornerRadius = CornerRadius(radius, radius),
            )
        )
    }
    clipPath(path, clipOp = ClipOp.Intersect) {
        drawRect(Light, topLeft = Offset(0f, top), size = Size(size.width, h))
        val cols = (size.width / cell).toInt() + 1
        val rows = (h / cell).toInt() + 1
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if ((row + col) % 2 == 0) {
                    drawRect(
                        color = Dark,
                        topLeft = Offset(col * cell, top + row * cell),
                        size = Size(cell, cell),
                    )
                }
            }
        }
    }
}
