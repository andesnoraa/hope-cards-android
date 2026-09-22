package com.aaronsedna.hopecards.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aaronsedna.hopecards.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = AppRepository(context.applicationContext)
                repository.initialize()
                val settings = repository.currentSettings()
                if (settings.dailyHopeReminderEnabled) {
                    ReminderScheduler(context.applicationContext).schedule(
                        settings.dailyHopeReminderHour,
                        settings.dailyHopeReminderMinute,
                        settings.preferredTranslation,
                    )
                }
            } catch (error: Exception) {
                Log.e("DailyHope", "Could not reschedule daily verse", error)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
