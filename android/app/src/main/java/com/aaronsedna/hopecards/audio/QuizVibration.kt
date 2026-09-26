package com.aaronsedna.hopecards.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings

/** Basic motors may reject Android's semantic REJECT haptic. Use two short motor pulses. */
internal class QuizVibration(context: Context) {
    private val appContext = context.applicationContext
    private val vibrator = appContext.getSystemService(Vibrator::class.java)
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    @Suppress("DEPRECATION")
    fun wrongAnswer() {
        if (vibrator?.hasVibrator() != true) return
        if (Settings.System.getInt(appContext.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) return
        // On/off timing needs neither predefined-effect support nor amplitude control.
        val timings = longArrayOf(0L, 90L, 70L, 90L)
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, -1), attributes)
        } else {
            vibrator.vibrate(timings, -1, attributes)
        }
    }

    fun stop() { vibrator?.cancel() }
}
