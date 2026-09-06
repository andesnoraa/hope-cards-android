package com.aaronsedna.hopecards.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aaronsedna.hopecards.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyHopeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scheduler = ReminderScheduler(context.applicationContext)
                scheduler.showNotification()
                val settings = AppRepository(context.applicationContext).currentSettings()
                if (settings.dailyHopeReminderEnabled) {
                    scheduler.schedule(settings.dailyHopeReminderHour, settings.dailyHopeReminderMinute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
