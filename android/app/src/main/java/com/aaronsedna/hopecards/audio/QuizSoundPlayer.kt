package com.aaronsedna.hopecards.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.aaronsedna.hopecards.R
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections

/** Two short, original chimes. Owned by the visible quiz; never loops or changes device volume. */
internal class QuizSoundPlayer(context: Context) {
    private val audio = context.applicationContext.getSystemService(AudioManager::class.java)
    private val loaded: MutableSet<Int> = Collections.newSetFromMap(ConcurrentHashMap<Int, Boolean>())
    private val pool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),
    ).build().apply {
        setOnLoadCompleteListener { _, id, status -> if (status == 0) loaded.add(id) }
    }
    private val correct = pool.load(context.applicationContext, R.raw.quiz_correct, 1)
    private val incorrect = pool.load(context.applicationContext, R.raw.quiz_incorrect, 1)

    fun play(isCorrect: Boolean) {
        // Also honor silent/vibrate mode when a device routes sonification differently.
        if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL || audio.getStreamVolume(AudioManager.STREAM_SYSTEM) == 0) return
        val id = if (isCorrect) correct else incorrect
        if (id in loaded) pool.play(id, .55f, .55f, 1, 0, 1f)
    }

    fun stop() = pool.autoPause()
    fun release() { pool.release(); loaded.clear() }
}
