package com.aaronsedna.hopecards.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import android.media.AudioFocusRequest
import android.os.Build
import android.os.Handler
import android.os.Looper

/** Owned by the visible Daily Hope screen. No service, timer, or phone-state permission. */
internal class DailyHopeAudioPlayer(context: Context, @RawRes private val music: Int) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private var player: MediaPlayer? = null
    private var active = false
    private var resumeAfterInterruption = false
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val focusListener = AudioManager.OnAudioFocusChangeListener(::onAudioFocusChange)
    private val focusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(focusListener, Handler(Looper.getMainLooper()))
            .build()
    } else null

    @Suppress("DEPRECATION")
    private fun requestFocus(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        audioManager.requestAudioFocus(checkNotNull(focusRequest))
    } else {
        audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    @Suppress("DEPRECATION")
    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(checkNotNull(focusRequest))
        } else audioManager.abandonAudioFocus(focusListener)
    }

    fun start() {
        if (active) return
        active = true
        if (requestFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            play()
        } else {
            stop()
        }
    }

    fun stop() {
        active = false
        resumeAfterInterruption = false
        player?.release()
        player = null
        abandonFocus()
    }

    private fun play() {
        if (!active) return
        runCatching {
            if (player == null) {
                player = checkNotNull(MediaPlayer.create(appContext, music, attributes, 0)).apply {
                    isLooping = true
                    setVolume(.45f, .45f)
                }
            }
            player?.start()
        }.onFailure {
            Log.w("DailyHopeMusic", "Could not play Daily Hope music", it)
            stop()
        }
    }

    private fun onAudioFocusChange(change: Int) {
        if (!active) return
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (resumeAfterInterruption) {
                    resumeAfterInterruption = false
                    play()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                resumeAfterInterruption = true
                player?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS -> stop()
        }
    }
}
