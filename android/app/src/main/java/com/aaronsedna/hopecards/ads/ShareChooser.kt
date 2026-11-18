package com.aaronsedna.hopecards.ads

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.service.chooser.ChooserResult
import androidx.core.content.IntentCompat

/** Adds a callback only when the user chooses a share target, not when opening or dismissing the chooser. */
internal object ShareChooser {
    fun create(context: Context, target: Intent, title: CharSequence): Intent {
        val callback = Intent(context, ShareTargetChosenReceiver::class.java).apply {
            action = ACTION_TARGET_CHOSEN
            data = Uri.parse("hopecards-share://${context.packageName}/${SystemClock.elapsedRealtimeNanos()}")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            SystemClock.elapsedRealtime().toInt(),
            callback,
            // Android's chooser must add the selected-target/result extra to this explicit callback.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        return Intent.createChooser(target, title, pendingIntent.intentSender)
    }

    internal const val PREFERENCES = "share_actions"
    internal const val PENDING_SHARE_AD = "pending_share_ad"
    private const val ACTION_TARGET_CHOSEN = "com.aaronsedna.hopecards.SHARE_TARGET_CHOSEN"
}

class ShareTargetChosenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val selectedComponent = IntentCompat.getParcelableExtra(
                intent,
                Intent.EXTRA_CHOSEN_COMPONENT,
                android.content.ComponentName::class.java,
            )
        val completed = if (Build.VERSION.SDK_INT >= 35) {
            val chooserResult = IntentCompat.getParcelableExtra(
                intent,
                Intent.EXTRA_CHOOSER_RESULT,
                ChooserResult::class.java,
            )
            chooserResult?.type in setOf(
                ChooserResult.CHOOSER_RESULT_SELECTED_COMPONENT,
                ChooserResult.CHOOSER_RESULT_COPY,
                ChooserResult.CHOOSER_RESULT_EDIT,
            )
        } else selectedComponent != null
        if (!completed) return
        context.getSharedPreferences(ShareChooser.PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(ShareChooser.PENDING_SHARE_AD, true)
            .apply()
    }
}
