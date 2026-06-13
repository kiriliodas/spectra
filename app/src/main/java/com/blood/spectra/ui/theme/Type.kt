package com.blood.spectra.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Type scale built on Fredoka. Every style pins an explicit lineHeight with
 * fixed alignment so text rows have stable vertical bounds.
 *
 * A monospace family is used for shifting numeric/code-like values (HEX, RGB,
 * ratios) so the layout never jumps as values change.
 */

private val Fixed = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

val SpectraTypography = Typography(
    displayLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp, lineHeightStyle = Fixed),
    displayMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp, lineHeightStyle = Fixed),
    displaySmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp, lineHeightStyle = Fixed),
    headlineLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp, lineHeightStyle = Fixed),
    headlineMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, lineHeightStyle = Fixed),
    headlineSmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, lineHeightStyle = Fixed),
    titleLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, lineHeightStyle = Fixed),
    titleMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp, lineHeightStyle = Fixed),
    titleSmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp, lineHeightStyle = Fixed),
    bodyLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp, lineHeightStyle = Fixed),
    bodyMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp, lineHeightStyle = Fixed),
    labelLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp, lineHeightStyle = Fixed),
    labelMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp, lineHeightStyle = Fixed),
    labelSmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp, lineHeightStyle = Fixed),
)

/** Monospace style for HEX / RGB / ratio values that change as the user edits. */
val MonoValueStyle: TextStyle = SpectraTypography.titleMedium.copy(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Medium,
    fontFeatureSettings = "tnum",
)

val MonoValueLargeStyle: TextStyle = SpectraTypography.headlineSmall.copy(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.SemiBold,
    fontFeatureSettings = "tnum",
)
