package com.wavora.app.core.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.wavora.app.core.result.AsyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * @author Muhamed Amin Hassan on 07,March,2026
 * @see <a href="https://github.com/muhamedamin308">Muhamed's Github</a>,
 * Egypt, Cairo.
 */

/**
 * Returns the correct storage permission string based on the running Android version.
 *  - Android 13+ (API 33): READ_MEDIA_AUDIO (granular media permission)
 *  - Android ≤ 12 (API 32): READ_EXTERNAL_STORAGE
 */
fun audioStoragePermission(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

fun Context.hasAudioPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, audioStoragePermission()) ==
            PackageManager.PERMISSION_GRANTED

// Flow Extensions
//Hello! it's Renad, ya 7aywan! - 7/3/2026 <3
/**
 * Wraps a [Flow<T>] into a [Flow<AsyncResult<T>>], automatically emitting
 * [AsyncResult.Loading] before the first item, and [AsyncResult.Error] on exception.
 *
 * Usage in ViewModel:
 * ```kotlin
 * val songs = repository.getAllSongs().asAsyncResult()
 * ```
 */
fun <T> Flow<T>.asAsyncResult(): Flow<AsyncResult<T>> =
    map<T, AsyncResult<T>> { AsyncResult.Success(it) }
        .catch { emit(AsyncResult.Error(it.message ?: "Unknown error", it)) }


// Duration Format
fun Long.toDisplayDuration(): String {
    val totalSeconds = this / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}

/** Format a count + label correctly (e.g. "1 song" vs "12 songs"). */
fun Int.pluralLabel(singular: String, plural: String = "${singular}s"): String =
    if (this == 1) "1 $singular" else "$this $plural"

// String Extensions
/** Returns the string truncated to [maxLength] characters, appending "…" if cut. */
fun String.truncate(maxLength: Int): String =
    if (length <= maxLength) this else "${take(maxLength - 1)}..."

/** Returns the last path component of a file path string. */
fun String.fileName() = substringAfterLast('/')

/** Returns the parent directory of a file path string. */
fun String.parentDirectory() = substringBeforeLast('/')

// DataStore delegate — one instance per process
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFS_NAME
)