package com.blood.spectra.ui.picker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.blood.spectra.logic.ColorValue
import kotlin.math.roundToInt

/**
 * Pick a color from a photo. Uses the system **Photo Picker** (no storage
 * permission, no network). Decodes a downscaled bitmap and samples the pixel
 * under the user's tap. Calls [onColorPicked] with the sampled color.
 *
 * Returns a launcher action via [content] so the caller can place the trigger.
 */
@Composable
fun ImageEyedropperSheetContent(
    onColorPicked: (ColorValue) -> Unit,
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var marker by remember { mutableStateOf<Offset?>(null) }
    var sampled by remember { mutableStateOf<ColorValue?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            bitmap = decodeDownscaled(context, uri, maxDim = 1600)
            marker = null
            sampled = null
        }
    }

    // open the picker automatically the first time
    LaunchedEffect(Unit) {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Eyedropper",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        val bmp = bitmap
        if (bmp == null) {
            Surface(
                onClick = {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Choose an image",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { viewSize = it }
                    .pointerInput(bmp) {
                        detectTapGestures { tap ->
                            val c = sampleAt(bmp, tap, viewSize)
                            if (c != null) {
                                marker = tap
                                sampled = c
                                onColorPicked(c)
                            }
                        }
                    },
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Tap to sample a color",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(),
                )
                marker?.let { m -> MarkerRing(m) }
            }
            sampled?.let { c ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 0.dp),
                        ) {
                            Surface(color = androidx.compose.ui.graphics.Color(c.argb),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxSize()) {}
                        }
                        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
                        Text(
                            com.blood.spectra.logic.ColorFormats.hex(c),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkerRing(at: Offset) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = androidx.compose.ui.graphics.Color(0x88000000),
            radius = 17f, center = at,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
        )
        drawCircle(
            color = androidx.compose.ui.graphics.Color.White,
            radius = 14f, center = at,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f),
        )
    }
}

/** Decode a bitmap from [uri], downscaled so the largest side ≈ [maxDim]. */
private fun decodeDownscaled(context: android.content.Context, uri: Uri, maxDim: Int): Bitmap? {
    return try {
        // first pass: bounds only
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
        val (w, h) = opts.outWidth to opts.outHeight
        if (w <= 0 || h <= 0) return null
        var sample = 1
        while (w / sample > maxDim || h / sample > maxDim) sample *= 2
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        }
    } catch (e: Exception) {
        null
    }
}

/** Map a tap in the (FillWidth) view to a bitmap pixel and read its color. */
private fun sampleAt(bmp: Bitmap, tap: Offset, viewSize: IntSize): ColorValue? {
    if (viewSize.width <= 0) return null
    // image is FillWidth: scale = viewWidth / bmpWidth; displayed height = bmpH*scale
    val scale = viewSize.width.toFloat() / bmp.width
    val px = (tap.x / scale).roundToInt().coerceIn(0, bmp.width - 1)
    val py = (tap.y / scale).roundToInt().coerceIn(0, bmp.height - 1)
    val pixel = bmp.getPixel(px, py)
    val a = ((pixel ushr 24) and 0xFF) / 255f
    val r = (pixel ushr 16) and 0xFF
    val g = (pixel ushr 8) and 0xFF
    val b = pixel and 0xFF
    return ColorValue(r, g, b, a)
}
