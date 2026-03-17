package com.wavora.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wavora.app.domain.model.SortOrder
import com.wavora.app.domain.model.UserPreferences
import com.wavora.app.domain.repository.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Top-level property creates a single DataStore instance for the app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wavora_prefs")

/**
 * @author Muhamed Amin Hassan on 17,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    // Keys
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val SKIP_DURATION_SEC = intPreferencesKey("skip_duration_sec")
        val CROSSFADE_DURATION_MS = intPreferencesKey("crossfade_duration_ms")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val LOCK_SCREEN_ART = booleanPreferencesKey("lock_screen_art")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val LAST_SCAN_TIMESTAMP_MS = longPreferencesKey("last_scan_timestamp_ms")
    }

    // Read

    override val preferences: Flow<UserPreferences> = context.dataStore.data
        .catch { cause ->
            if (cause is IOException) emit(emptyPreferences())
            else throw cause
        }
        .map { prefs -> prefs.toUserPreferences() }

    private fun Preferences.toUserPreferences() = UserPreferences(
        isDarkTheme = this[Keys.DARK_THEME] ?: true,
        useDynamicColors = this[Keys.DYNAMIC_COLOR] ?: true,
        skipDurationSec = this[Keys.SKIP_DURATION_SEC] ?: 10,
        crossfadeDurationMs = this[Keys.CROSSFADE_DURATION_MS] ?: 0,
        gaplessPlayback = this[Keys.GAPLESS_PLAYBACK] ?: true,
        showAlbumArtOnLockScreen = this[Keys.LOCK_SCREEN_ART] ?: true,
        sortOrder = this[Keys.SORT_ORDER]
            ?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() }
            ?: SortOrder.TITLE_ASC,
        lastScanTimestampMs = this[Keys.LAST_SCAN_TIMESTAMP_MS] ?: 0L,
    )

    // Write

    override suspend fun setDarkTheme(enabled: Boolean) = update {
        it[Keys.DARK_THEME] = enabled
    }

    override suspend fun setDynamicColors(enabled: Boolean) = update {
        it[Keys.DYNAMIC_COLOR] = enabled
    }

    override suspend fun setSkipDuration(seconds: Int) = update {
        it[Keys.SKIP_DURATION_SEC] = seconds
    }

    override suspend fun setCrossfadeDuration(ms: Int) = update {
        it[Keys.CROSSFADE_DURATION_MS] = ms
    }

    override suspend fun setGaplessPlayback(enabled: Boolean) = update {
        it[Keys.GAPLESS_PLAYBACK] = enabled
    }

    override suspend fun setShowAlbumArtOnLockScreen(enabled: Boolean) = update {
        it[Keys.LOCK_SCREEN_ART] = enabled
    }

    override suspend fun setSortOrder(sortOrder: SortOrder) = update {
        it[Keys.SORT_ORDER] = sortOrder.name
    }

    override suspend fun setLastScanTimestamp(ms: Long) = update {
        it[Keys.LAST_SCAN_TIMESTAMP_MS] = ms
    }

    private suspend inline fun update(
        crossinline block: (MutablePreferences) -> Unit,
    ) {
        context.dataStore.edit { block(it) }
    }
}