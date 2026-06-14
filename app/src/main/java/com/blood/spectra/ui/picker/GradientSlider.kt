package com.blood.spectra.ui.picker

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.unit.dp
import com.blood.spectra.ui.common.checkerboardCells

/**
 * A custom, gradient-track slider — the heart of a color picker. The track shows
 * the actual gradient of the channel it controls (passed in as [trackColors]),
 * with an optional checkerboard underlay for alpha. A single consistent **pill**
 * thumb is used on every slider.
 *
 * [value] is normalized 0f..1f. [onValueChange] reports a normalized position.
 */
@Composable
fun GradientSlider(
    value: Float,
    trackColors: List<Color>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showCheckerboard: Boolean = false,
    trackHeight: androidx.compose.ui.unit.Dp = 22.dp,
) {
    val pos = value.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onValueChange((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onValueChange((change.position.x / size.width).coerceIn(0f, 1f))
                }
            }
            .drawWithCache {
                val h = trackHeight.toPx()
                val top = (size.height - h) / 2f
                val radius = h / 2f
                val brush = Brush.horizontalGradient(trackColors)
                onDrawBehind {
                    // optional checkerboard underlay (alpha)
                    if (showCheckerboard) {
                        checkerboardCells(top, h, radius)
                    }
                    // gradient track (rounded)
                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(0f, top),
                        size = Size(size.width, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                    )
                    // pill thumb
                    val cx = pos * size.width
                    val thumbW = h * 0.62f
                    val thumbH = size.height
                    val left = (cx - thumbW / 2f).coerceIn(0f, size.width - thumbW)
                    // white outer pill + subtle border
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(left, 0f),
                        size = Size(thumbW, thumbH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(thumbW / 2f, thumbW / 2f),
                    )
                    drawRoundRect(
                        color = Color(0x33000000),
                        topLeft = Offset(left, 0f),
                        size = Size(thumbW, thumbH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(thumbW / 2f, thumbW / 2f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
                    )
                }
            },
    )
}
