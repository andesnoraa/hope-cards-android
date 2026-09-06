package com.aaronsedna.hopecards.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aaronsedna.hopecards.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED)) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = AppRepository(context.applicationContext).currentSettings()
                if (settings.dailyHopeReminderEnabled) {
                    ReminderScheduler(context.applicationContext).schedule(
                        settings.dailyHopeReminderHour,
                        settings.dailyHopeReminderMinute,
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
