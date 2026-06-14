package com.blood.spectra.ui.picker

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blood.spectra.ui.common.checkerboardCells

/**
 * A custom, gradient-track slider — the heart of a color picker. The track shows
 * the actual gradient of the channel it controls (passed in as [trackColors]),
 * with an optional checkerboard underlay for alpha. A single consistent **pill**
 * thumb is used on every slider.
 *
 * Drawn with [drawBehind] (NOT drawWithCache) so the gradient always reflects the
 * current [trackColors] — caching there caused stale gradients.
 *
 * [value] is normalized 0f..1f; [onValueChange] reports a normalized position.
 * Touch/position math is inset by half the thumb width so the thumb never clips
 * at the extremes (e.g. alpha at 100%).
 */
@Composable
fun GradientSlider(
    value: Float,
    trackColors: List<Color>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showCheckerboard: Boolean = false,
    trackHeight: Dp = 22.dp,
) {
    val pos = value.coerceIn(0f, 1f)
    val thumbWidthDp = 16.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(thumbWidthDp) {
                val half = thumbWidthDp.toPx() / 2f
                fun toValue(x: Float): Float {
                    val usable = (size.width - 2 * half).coerceAtLeast(1f)
                    return ((x - half) / usable).coerceIn(0f, 1f)
                }
                detectTapGestures { offset -> onValueChange(toValue(offset.x)) }
            }
            .pointerInput(thumbWidthDp) {
                val half = thumbWidthDp.toPx() / 2f
                fun toValue(x: Float): Float {
                    val usable = (size.width - 2 * half).coerceAtLeast(1f)
                    return ((x - half) / usable).coerceIn(0f, 1f)
                }
                detectDragGestures { change, _ ->
                    change.consume()
                    onValueChange(toValue(change.position.x))
                }
            }
            .drawBehind {
                val h = trackHeight.toPx()
                val top = (size.height - h) / 2f
                val radius = h / 2f
                val half = thumbWidthDp.toPx() / 2f

                if (showCheckerboard) checkerboardCells(top, h, radius)

                drawRoundRect(
                    brush = Brush.horizontalGradient(trackColors),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, h),
                    cornerRadius = CornerRadius(radius, radius),
                )

                // pill thumb — centre kept within [half, width-half] so it never clips
                val cx = half + pos * (size.width - 2 * half)
                val thumbW = thumbWidthDp.toPx()
                val thumbH = size.height
                val left = (cx - thumbW / 2f)
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(left, 0f),
                    size = Size(thumbW, thumbH),
                    cornerRadius = CornerRadius(thumbW / 2f, thumbW / 2f),
                )
                drawRoundRect(
                    color = Color(0x40000000),
                    topLeft = Offset(left, 0f),
                    size = Size(thumbW, thumbH),
                    cornerRadius = CornerRadius(thumbW / 2f, thumbW / 2f),
                    style = Stroke(width = 2f),
                )
            },
    )
}
