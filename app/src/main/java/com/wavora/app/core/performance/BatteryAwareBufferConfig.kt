package com.wavora.app.core.performance

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryAwareBufferConfig @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    // ── Config profiles ───────────────────────────────────────────────────────

    data class BufferProfile(
        val minMs: Int,
        val maxMs: Int,
        val forPlaybackMs: Int,
        val forRebufferMs: Int,
        val label: String,
    )

    private val GENEROUS = BufferProfile(
        minMs = 5_000,
        maxMs = 50_000,
        forPlaybackMs = 2_500,
        forRebufferMs = 5_000,
        label = "generous",
    )

    private val CONSERVATIVE = BufferProfile(
        minMs = 1_000,
        maxMs = 8_000,
        forPlaybackMs = 800,
        forRebufferMs = 2_000,
        label = "conservative",
    )

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns a [DefaultLoadControl] tuned for the current battery state.
     * Thread-safe; reads sticky broadcast and power manager — both are fast.
     */
    @OptIn(UnstableApi::class)
    fun build(): DefaultLoadControl {
        val profile = selectProfile()
        android.util.Log.d(TAG, "Buffer profile: ${profile.label}")
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                profile.minMs,
                profile.maxMs,
                profile.forPlaybackMs,
                profile.forRebufferMs,
            )
            .build()
    }

    fun currentProfile(): BufferProfile = selectProfile()

    // ── Profile selection ─────────────────────────────────────────────────────

    private fun selectProfile(): BufferProfile {
        if (isCharging()) return GENEROUS
        if (isBatterySaverOn()) return CONSERVATIVE
        if (batteryPercent() < LOW_BATTERY_THRESHOLD) return CONSERVATIVE
        return GENEROUS
    }

    private fun isCharging(): Boolean {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        ) ?: return false
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun batteryPercent(): Int {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        ) ?: return 100
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return 100
        return (level * 100 / scale)
    }

    private fun isBatterySaverOn(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return pm.isPowerSaveMode
    }

    companion object {
        private const val TAG = "BatteryAwareBuffer"
        private const val LOW_BATTERY_THRESHOLD = 20
    }
}