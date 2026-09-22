package com.aaronsedna.hopecards.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.VerseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** ADB-only preview in debug builds, outside instrumentation's SystemUI environment. */
class NotificationPreviewReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = AppRepository(context)
                repository.initialize()
                val settings = repository.currentSettings()
                val verse = repository.dailyHopeVerse(VerseRepository(context), settings.preferredTranslation)
                ReminderScheduler(context).showNotification(verse)
            } catch (error: Exception) {
                Log.e("DailyHopePreview", "Could not show notification preview", error)
            } finally {
                result.finish()
            }
        }
    }
}
