package com.blood.spectra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * App theme.
 *
 * Material You dynamic color is intentionally DISABLED: this is a color tool, so
 * the chrome stays a fixed neutral palette and the colors the user is editing
 * are the only vivid thing on screen (they always read truly).
 */
@Composable
fun SpectraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SpectraTypography,
        shapes = SpectraShapes,
        content = content,
    )
}
