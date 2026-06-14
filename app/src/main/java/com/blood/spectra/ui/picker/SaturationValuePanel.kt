package com.blood.spectra.ui.picker

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.blood.spectra.logic.ColorMath

/**
 * The 2D saturation × value square. X = saturation (0..1), Y = value (1..0).
 * Tinted by the current [hue]; dragging reports new (s, v). A clearly-visible
 * ring marker (white fill outline + dark halo) shows the current position.
 *
 * Uses drawBehind (not drawWithCache) so the tint/marker always reflect state.
 */
@Composable
fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (s: Float, v: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
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
            .drawBehind {
                val (hr, hg, hb) = ColorMath.hsvToRgb(hue, 1f, 1f)
                val hueColor = Color(hr, hg, hb)
                // saturation: white -> hue (left to right)
                drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
                // value: transparent -> black (top to bottom)
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))

                // marker ring at (s, 1-v), large & clearly visible
                val cx = saturation.coerceIn(0f, 1f) * size.width
                val cy = (1f - value.coerceIn(0f, 1f)) * size.height
                val r = 12f
                // dark halo (so it shows on light areas)
                drawCircle(Color(0x66000000), radius = r + 2.5f, center = Offset(cx, cy),
                    style = Stroke(width = 2f))
                // white ring (so it shows on dark areas)
                drawCircle(Color.White, radius = r, center = Offset(cx, cy),
                    style = Stroke(width = 3.5f))
            },
    )
}
