package com.wavora.app.core.performance

import android.util.Log
import com.wavora.app.domain.repository.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanThrottleManager @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
) {

    /**
     * Returns true if enough time has passed since the last scan,
     * or if [forceAllow] is true (manual rescan).
     */
    suspend fun shouldScan(forceAllow: Boolean = false): Boolean {
        if (forceAllow) return true

        val lastScan = prefsRepository.preferences.first().lastScanTimestampMs
        val elapsed = System.currentTimeMillis() - lastScan

        return if (elapsed >= MIN_SCAN_INTERVAL_MS) {
            true
        } else {
            val remaining = (MIN_SCAN_INTERVAL_MS - elapsed) / 1000 / 60
            Log.d(TAG, "Scan throttled — next allowed in ~${remaining}min")
            false
        }
    }

    /** Call after a successful scan to update the cooldown timer. */
    suspend fun recordScan() {
        prefsRepository.setLastScanTimestamp(System.currentTimeMillis())
    }

    companion object {
        private const val TAG = "ScanThrottleManager"

        /** Minimum gap between automatic scans — 15 minutes. */
        const val MIN_SCAN_INTERVAL_MS = 15 * 60 * 1000L
    }
}
