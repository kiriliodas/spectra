package com.blood.spectra.ui.picker

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.blood.spectra.logic.ColorMath

/**
 * The 2D saturation × value square. X = saturation (0..1), Y = value (1..0).
 * The square is tinted by the current [hue]; dragging reports new (s, v).
 * A ring marker shows the current position.
 */
@Composable
fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (s: Float, v: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (hr, hg, hb) = ColorMath.hsvToRgb(hue, 1f, 1f)
    val hueColor = Color(hr, hg, hb)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapGestures { o ->
                    onChange(
                        (o.x / size.width).coerceIn(0f, 1f),
                        1f - (o.y / size.height).coerceIn(0f, 1f),
                    )
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onChange(
                        (change.position.x / size.width).coerceIn(0f, 1f),
                        1f - (change.position.y / size.height).coerceIn(0f, 1f),
                    )
                }
            }
            .drawWithCache {
                // saturation: white -> hue (left to right)
                val satBrush = Brush.horizontalGradient(listOf(Color.White, hueColor))
                // value: transparent -> black (top to bottom)
                val valBrush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
                onDrawBehind {
                    drawRect(satBrush)
                    drawRect(valBrush)
                    // marker ring at (s, 1-v)
                    val cx = saturation.coerceIn(0f, 1f) * size.width
                    val cy = (1f - value.coerceIn(0f, 1f)) * size.height
                    val r = 10f
                    drawCircle(Color.White, radius = r, center = Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                    drawCircle(Color(0x66000000), radius = r + 2f, center = Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))
                }
            },
    )
}
