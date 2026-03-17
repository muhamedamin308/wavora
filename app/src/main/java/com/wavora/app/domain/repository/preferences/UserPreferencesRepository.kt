package com.wavora.app.domain.repository.preferences

import androidx.datastore.preferences.core.Preferences
import com.wavora.app.domain.model.SortOrder
import com.wavora.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * @author Muhamed Amin Hassan on 17,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */
interface UserPreferencesRepository {

    /** Live stream of the current preferences. Emits immediately on subscription. */
    val preferences: Flow<UserPreferences>

    suspend fun setDarkTheme(enabled: Boolean)
    suspend fun setDynamicColors(enabled: Boolean)
    suspend fun setSkipDuration(seconds: Int)
    suspend fun setCrossfadeDuration(ms: Int)
    suspend fun setGaplessPlayback(enabled: Boolean)
    suspend fun setShowAlbumArtOnLockScreen(enabled: Boolean)
    suspend fun setSortOrder(sortOrder: SortOrder)
    suspend fun setLastScanTimestamp(ms: Long)
}
