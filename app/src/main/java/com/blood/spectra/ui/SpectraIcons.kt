package com.blood.spectra.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Self-contained vector icons for the bottom navigation.
 *
 * We draw these ourselves (instead of relying on Material icon names, whose
 * availability varies by library version) so the project always compiles. They
 * are simple line icons on a 24x24 grid; the tint is applied by the caller via
 * [Icon]'s `tint`, so the stroke color here is just a placeholder.
 */
object SpectraIcons {

    /** Eyedropper / color picker. */
    val Eyedropper: ImageVector = lineIcon("Eyedropper") {
        // dropper body (diagonal) + bulb at top-right
        stroke("M4 20 L11 13")
        stroke("M10.5 13.5 L15.5 8.5")
        stroke("M13 6 L18 11 L19.2 9.8 a2 2 0 0 0 0 -2.8 L16 4 a2 2 0 0 0 -2.8 0 Z")
        // small drop highlight
        stroke("M4 20 L6 18")
    }

    /** Palette / swatches. */
    val Palette: ImageVector = lineIcon("Palette") {
        // classic palette blob with thumb hole
        stroke("M12 4 a8 7 0 1 0 0 16 c1.2 0 1.8 -1 1.8 -2 c0 -1.4 1 -2 2.2 -2 H18 a3 3 0 0 0 3 -3.4 A8 7 0 0 0 12 4 Z")
        // paint dots
        dot(8.2f, 9.5f)
        dot(11.5f, 7.8f)
        dot(15.2f, 9.5f)
        dot(16.4f, 12.8f)
    }

    /** Contrast (half-filled circle). */
    val Contrast: ImageVector = lineIcon("Contrast") {
        stroke("M12 4 a8 8 0 1 0 0 16 a8 8 0 1 0 0 -16 Z")
        // filled right half
        fill("M12 4 a8 8 0 0 1 0 16 Z")
    }
}

// ---- builder helpers --------------------------------------------------------

private fun lineIcon(
    name: String,
    block: ImageVector.Builder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply(block).build()

private fun ImageVector.Builder.stroke(pathData: String) {
    addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
}

private fun ImageVector.Builder.fill(pathData: String) {
    addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(Color.Black),
    )
}

private fun ImageVector.Builder.dot(cx: Float, cy: Float, r: Float = 1.1f) {
    // approximate a small filled circle with a path
    val d = "M${cx - r} $cy a$r $r 0 1 0 ${r * 2} 0 a$r $r 0 1 0 ${-r * 2} 0 Z"
    fill(d)
}
