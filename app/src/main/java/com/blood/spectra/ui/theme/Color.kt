package com.blood.spectra.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * A deliberately NEUTRAL chrome. A color tool's UI shouldn't fight the colors
 * the user is working with, so the surfaces are near-grayscale with a single
 * restrained indigo accent. (Material You dynamic color is intentionally off —
 * see Theme.kt — so swatches always read truly.)
 */

private val Accent = Color(0xFF5B6CFF)        // restrained indigo
private val AccentLight = Color(0xFFC4CBFF)

val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E3FF),
    onPrimaryContainer = Color(0xFF0A1066),
    secondary = Color(0xFF5A5D72),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E1F4),
    onSecondaryContainer = Color(0xFF171A2C),
    tertiary = Color(0xFF75556E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD7F2),
    onTertiaryContainer = Color(0xFF2C1228),
    background = Color(0xFFFBFAFD),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFBFAFD),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF46464F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F3F7),
    surfaceContainer = Color(0xFFEEEDF1),
    surfaceContainerHigh = Color(0xFFE9E7EC),
    surfaceContainerHighest = Color(0xFFE3E1E6),
    outline = Color(0xFF777680),
    outlineVariant = Color(0xFFC7C5D0),
)

val DarkColors = darkColorScheme(
    primary = AccentLight,
    onPrimary = Color(0xFF161C8C),
    primaryContainer = Color(0xFF333CA6),
    onPrimaryContainer = Color(0xFFE0E3FF),
    secondary = Color(0xFFC3C5DD),
    onSecondary = Color(0xFF2C2F42),
    secondaryContainer = Color(0xFF424559),
    onSecondaryContainer = Color(0xFFE0E1F4),
    tertiary = Color(0xFFE3BBD7),
    onTertiary = Color(0xFF43273E),
    tertiaryContainer = Color(0xFF5B3D55),
    onTertiaryContainer = Color(0xFFFFD7F2),
    background = Color(0xFF131316),
    onBackground = Color(0xFFE5E1E6),
    surface = Color(0xFF131316),
    onSurface = Color(0xFFE5E1E6),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC7C5D0),
    surfaceContainerLowest = Color(0xFF0E0E11),
    surfaceContainerLow = Color(0xFF1B1B1F),
    surfaceContainer = Color(0xFF1F1F23),
    surfaceContainerHigh = Color(0xFF2A2A2E),
    surfaceContainerHighest = Color(0xFF353439),
    outline = Color(0xFF918F9A),
    outlineVariant = Color(0xFF46464F),
)
