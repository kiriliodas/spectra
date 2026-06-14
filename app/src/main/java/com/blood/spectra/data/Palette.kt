package com.blood.spectra.data

import kotlinx.serialization.Serializable

/** A single saved color (ARGB packed) with an optional name. */
@Serializable
data class SavedColor(
    val argb: Long,
    val name: String? = null,
)

/** A named, ordered collection of colors. Stored on-device only. */
@Serializable
data class Palette(
    val id: String,
    val name: String,
    val colors: List<SavedColor>,
    val updatedAt: Long,
)
