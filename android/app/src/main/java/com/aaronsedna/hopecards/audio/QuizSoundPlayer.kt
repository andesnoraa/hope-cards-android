package com.aaronsedna.hopecards.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.aaronsedna.hopecards.R
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections

/** Short original quiz cues. Owned by the visible quiz; never loops or changes device volume. */
internal class QuizSoundPlayer(context: Context) {
    private val audio = context.applicationContext.getSystemService(AudioManager::class.java)
    private val loaded: MutableSet<Int> = Collections.newSetFromMap(ConcurrentHashMap<Int, Boolean>())
    @Volatile private var released = false
    private var streamId = 0
    private val pool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),
    ).build().apply {
        setOnLoadCompleteListener { _, id, status -> if (status == 0 && !released) loaded.add(id) }
    }
    private val correct = pool.load(context.applicationContext, R.raw.quiz_correct, 1)
    private val incorrect = pool.load(context.applicationContext, R.raw.quiz_incorrect, 1)
    // Ramped sine tones: a 140ms soft tick, then a distinct 575ms descending two-note finish.
    private val countdown = pool.load(context.applicationContext, R.raw.quiz_countdown, 1)
    private val timeUp = pool.load(context.applicationContext, R.raw.quiz_time_up, 1)

    fun play(isCorrect: Boolean) = playSound(if (isCorrect) correct else incorrect, .55f)

    fun playCountdown(ended: Boolean) = playSound(if (ended) timeUp else countdown, if (ended) .55f else .45f)

    private fun playSound(id: Int, volume: Float) {
        if (released) return
        // Also honor silent/vibrate mode when a device routes sonification differently.
        if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL || audio.getStreamVolume(AudioManager.STREAM_SYSTEM) == 0) return
        if (id in loaded) {
            stop()
            streamId = pool.play(id, volume, volume, 1, 0, 1f)
        }
    }

    fun stop() {
        if (!released && streamId != 0) pool.stop(streamId)
        streamId = 0
    }

    fun release() {
        if (released) return
        stop()
        released = true
        pool.setOnLoadCompleteListener(null)
        pool.release()
        loaded.clear()
    }
}
