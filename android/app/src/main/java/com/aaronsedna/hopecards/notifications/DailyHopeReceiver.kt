package com.aaronsedna.hopecards.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.VerseRepository
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyHopeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scheduler = ReminderScheduler(context.applicationContext)
                val repository = AppRepository(context.applicationContext)
                repository.initialize()
                val settings = repository.currentSettings()
                if (settings.dailyHopeReminderEnabled) {
                    // Schedule first so a failed asset load/post cannot break future reminders.
                    scheduler.schedule(settings.dailyHopeReminderHour, settings.dailyHopeReminderMinute, settings.preferredTranslation)
                    val verse = repository.dailyHopeVerse(VerseRepository(context.applicationContext), settings.preferredTranslation)
                    // Respect a toggle changed while the verse was loading.
                    if (repository.currentSettings().dailyHopeReminderEnabled) scheduler.showNotification(verse)
                    else scheduler.cancel()
                }
            } catch (error: Exception) {
                Log.e("DailyHope", "Could not deliver daily verse", error)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
