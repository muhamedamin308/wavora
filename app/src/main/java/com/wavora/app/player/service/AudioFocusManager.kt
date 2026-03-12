package com.wavora.app.player.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log

/**
 * Manages Android audio focus lifecycle for WAVORA.
 *
 * Audio focus rules:
 *  - GAIN: Play at full volume.
 *  - LOSS (permanent): Pause playback and abandon focus. User must resume.
 *  - LOSS_TRANSIENT: Pause playback (e.g. phone call). Resume when focus returns.
 *  - LOSS_TRANSIENT_CAN_DUCK: Lower volume to [DUCK_VOLUME] (e.g. nav prompt). Resume when focus returns.
 *
 * BECOME_NOISY:
 *  Broadcast fired by the system when audio output changes from headphone to
 *  speaker (headset unplugged, Bluetooth disconnected). We pause playback to
 *  prevent music blasting unexpectedly from the speaker.
 *
 * Design:
 *  - [AudioFocusRequest] used on API 26+ (our minSdk — always available).
 *  - [callbacks] is a simple interface so [WavoraPlaybackService] can react
 *    without [AudioFocusManager] knowing about ExoPlayer directly.
 *  - All callbacks fire on the AudioManager callback thread; callers must
 *    post to the player thread if needed.
 */

class AudioFocusManager(
    private val context: Context,
    private val callbacks: Callbacks,
) {
    interface Callbacks {
        fun onAudioFocusGained()
        fun onAudioFocusLost(permanent: Boolean)
        fun onDuck()
        fun onUnDuck()
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusGranted = false
    private var isDucking = false

    // Audio Focus Requirements 'API +26'
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAcceptsDelayedFocusGain(true) // Accept focus when available
        .setWillPauseWhenDucked(false) // we handle ducking manually for smoother UX
        .setOnAudioFocusChangeListener(::onFocusChange)
        .build()

    // Become_noisy receiver
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                Log.d(TAG, "BECOME_NOISY — pausing playback")
                callbacks.onAudioFocusLost(permanent = false)
            }
        }
    }

    private var noisyReceiverRegistered = false

    // public APIs

    /**
     * Request audio focus before starting playback.
     * @return true if focus granted immediately; false if delayed or denied.
     */
    fun requestAudioFocus(): Boolean {
        val result = audioManager.requestAudioFocus(focusRequest)
        focusGranted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Log.d(TAG, "requestAudioFocus result=$result granted=$focusGranted")
        if (focusGranted) registerNoisyReceiver()
        return focusGranted
    }

    /** Abandon focus when stopping or the service is destroyed. */
    fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(focusRequest)
        focusGranted = false
        unregisterNoisyReceiver()
        Log.d(TAG, "Audio focus abandoned")
    }

    val isFocusGranted: Boolean get() = focusGranted

    // focus change handler
    private fun onFocusChange(focusChange: Int) {
        Log.d(TAG, "onAudioFocusChange: $focusChange")
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                focusGranted = true
                if (isDucking) {
                    isDucking = false
                    callbacks.onUnDuck()
                } else {
                    callbacks.onAudioFocusGained()
                }
                registerNoisyReceiver()
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss - stop playback, abandon focus
                focusGranted = false
                isDucking = false
                unregisterNoisyReceiver()
                callbacks.onAudioFocusLost(false)
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporary loss (e.g. incoming call) — pause, keep focus
                callbacks.onAudioFocusLost(false)
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Duck for navigation prompt / notification sound
                isDucking = true
                callbacks.onDuck()
            }
        }
    }

    // noisy registration
    private fun registerNoisyReceiver() {
        if (noisyReceiverRegistered) {
            context.registerReceiver(
                noisyReceiver,
                IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            )
            noisyReceiverRegistered = true
        }
    }

    private fun unregisterNoisyReceiver() {
        if (!noisyReceiverRegistered) {
            runCatching { context.unregisterReceiver(noisyReceiver) }
            noisyReceiverRegistered = false
        }
    }

    companion object {
        private const val TAG = "AudioFocusManager"
    }
}