package com.blood.spectra.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * On-device store for saved palettes.
 *
 * PRIVACY: writes to the app's private DataStore file only — never uploaded or
 * synced (the app has no INTERNET permission). Fully consistent with the
 * offline, zero-collection promise.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spectra_palettes")

class PaletteRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val key = stringPreferencesKey("palettes_json")

    val palettes: Flow<List<Palette>> = context.dataStore.data.map { prefs ->
        prefs[key]?.let { runCatching { json.decodeFromString<List<Palette>>(it) }.getOrNull() }
            ?: emptyList()
    }

    private suspend fun update(transform: (List<Palette>) -> List<Palette>) {
        context.dataStore.edit { prefs ->
            val current = prefs[key]?.let {
                runCatching { json.decodeFromString<List<Palette>>(it) }.getOrNull()
            } ?: emptyList()
            prefs[key] = json.encodeToString(transform(current))
        }
    }

    suspend fun createPalette(name: String): String {
        val id = UUID.randomUUID().toString()
        update { list ->
            // Auto-number unnamed palettes: "Palette 1", "Palette 2", ...
            val finalName = name.ifBlank { "Palette ${list.size + 1}" }
            list + Palette(id, finalName, emptyList(), now())
        }
        return id
    }

    suspend fun renamePalette(id: String, name: String) = update { list ->
        list.map { if (it.id == id) it.copy(name = name.ifBlank { it.name }, updatedAt = now()) else it }
    }

    suspend fun deletePalette(id: String) = update { list -> list.filterNot { it.id == id } }

    suspend fun addColor(paletteId: String, argb: Long) = update { list ->
        list.map { p ->
            if (p.id != paletteId) p
            else if (p.colors.any { it.argb == argb }) p // de-dupe
            else p.copy(colors = p.colors + SavedColor(argb), updatedAt = now())
        }
    }

    suspend fun removeColor(paletteId: String, argb: Long) = update { list ->
        list.map { p ->
            if (p.id != paletteId) p
            else p.copy(colors = p.colors.filterNot { it.argb == argb }, updatedAt = now())
        }
    }

    /** Move the color at [index] by [delta] positions (e.g. -1 up, +1 down). */
    suspend fun moveColor(paletteId: String, index: Int, delta: Int) = update { list ->
        list.map { p ->
            if (p.id != paletteId) return@map p
            val target = index + delta
            if (index !in p.colors.indices || target !in p.colors.indices) return@map p
            val mutable = p.colors.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(target, item)
            p.copy(colors = mutable, updatedAt = now())
        }
    }

    private fun now() = System.currentTimeMillis()
}
