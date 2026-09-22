package com.aaronsedna.hopecards.notifications

import android.Manifest
import android.app.Notification
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.ui.forTranslation
import java.time.ZonedDateTime

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun ensureChannel(translation: Translation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val localized = context.forTranslation(translation)
        val manager = context.getSystemService(NotificationManager::class.java)
        val activeChannelId = channelId()
        manager.getNotificationChannel(activeChannelId)?.let { existing ->
            existing.name = localized.getString(R.string.daily_hope_channel_name)
            existing.description = localized.getString(R.string.daily_hope_channel_description)
            manager.createNotificationChannel(existing)
            return
        }
        val legacy = manager.getNotificationChannel(LEGACY_CHANNEL_ID)
        // Channels cannot be upgraded in place. The verse channel replaces the old generic
        // reminder, carrying forward user restrictions instead of bypassing them.
        val importance = verseChannelImportance(
            legacy?.importance,
            legacy != null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || legacy.hasUserSetImportance()),
        )
        val channel = NotificationChannel(
            activeChannelId,
            localized.getString(R.string.daily_hope_channel_name),
            importance,
        ).apply {
            description = localized.getString(R.string.daily_hope_channel_description)
            if (legacy != null) {
                setSound(legacy.sound, legacy.audioAttributes)
                legacy.vibrationPattern?.let { vibrationPattern = it }
                enableLights(legacy.shouldShowLights())
                lightColor = legacy.lightColor
                setShowBadge(legacy.canShowBadge())
            }
            // Set after the waveform: assigning a pattern can enable vibration implicitly.
            enableVibration(legacy?.shouldVibrate() ?: true)
        }
        manager.createNotificationChannel(channel)
    }

    fun schedule(hour: Int, minute: Int, translation: Translation) {
        ensureChannel(translation)
        val next = nextReminderTime(ZonedDateTime.now(), hour, minute)

        // A daily reflection is user-visible but not second-sensitive. An inexact idle-aware
        // alarm avoids exact-alarm permission and lets Android batch wakeups for battery health.
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            next.toInstant().toEpochMilli(),
            reminderIntent(),
        )
    }

    fun cancel() {
        alarmManager.cancel(reminderIntent())
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    fun showNotification(verse: Verse) {
        ensureChannel(verse.edition)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, buildNotification(verse))
        } catch (error: SecurityException) {
            // Permission can be revoked between the check and notify; tomorrow is still scheduled.
            Log.w("DailyHope", "Notification permission was revoked", error)
        }
    }

    internal fun buildNotification(verse: Verse): Notification {
        val openIntent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_OPEN_DAILY, true)
            .putExtra(MainActivity.EXTRA_DAILY_VERSE_ID, verse.id)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val contentIntent = PendingIntent.getActivity(
            context,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, channelId())
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(0xFF132142.toInt())
            .setContentTitle(verse.displayReference)
            .setContentText(verse.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(verse.text))
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(java.time.Duration.between(
                ZonedDateTime.now(),
                java.time.LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()),
            ).toMillis().coerceAtLeast(1L))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .build()
    }

    fun openSystemSettings(translation: Translation) {
        ensureChannel(translation)
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, channelId())
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(android.net.Uri.parse("package:${context.packageName}"))
        }
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun channelId(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return CHANNEL_ID
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return CHANNEL_ID
        val legacy = manager.getNotificationChannel(LEGACY_CHANNEL_ID) ?: return CHANNEL_ID
        // Visibility cannot be copied by an app; keep the channel containing user privacy choices.
        val keepLegacy = legacy.lockscreenVisibility == Notification.VISIBILITY_PRIVATE ||
            legacy.lockscreenVisibility == Notification.VISIBILITY_SECRET ||
            legacy.importance < NotificationManager.IMPORTANCE_DEFAULT ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || legacy.hasUserSetImportance()
        return if (keepLegacy) LEGACY_CHANNEL_ID else CHANNEL_ID
    }

    private fun reminderIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        1,
        Intent(context, DailyHopeReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val CHANNEL_ID = "daily-hope-verses"
        internal const val LEGACY_CHANNEL_ID = "daily-hope"
        const val EXTRA_OPEN_DAILY = MainActivity.EXTRA_OPEN_DAILY_HOPE
        private const val NOTIFICATION_ID = 2025
    }
}
