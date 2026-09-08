package com.aaronsedna.hopecards

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.aaronsedna.hopecards.ui.HopeCardsApp
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val dailyHopeRequests = MutableStateFlow(0)
    private val updateReady = MutableStateFlow(false)
    private lateinit var appUpdateManager: AppUpdateManager
    private var updateCheckInFlight = false
    private var updateFlowStarted = false
    private var updatePromptAttemptedThisSession = false
    private var updateListenerRegistered = false

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        updateFlowStarted = false
    }

    private val installStateListener = InstallStateUpdatedListener { state ->
        updateReady.value = state.installStatus() == InstallStatus.DOWNLOADED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder().detectNetwork().penaltyLog().build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
                    .build(),
            )
        }
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        handleIntent(intent)
        appUpdateManager = AppUpdateManagerFactory.create(this)

        setContent {
            HopeCardsApp(
                dailyHopeRequests = dailyHopeRequests,
                updateReady = updateReady,
                onCompleteUpdate = ::completeAppUpdate,
            )
        }
    }

    override fun onStart() {
        super.onStart()
        if (!BuildConfig.DEBUG && !updateListenerRegistered) {
            appUpdateManager.registerListener(installStateListener)
            updateListenerRegistered = true
        }
    }

    override fun onResume() {
        super.onResume()
        checkForAppUpdate()
    }

    override fun onStop() {
        if (updateListenerRegistered) {
            appUpdateManager.unregisterListener(installStateListener)
            updateListenerRegistered = false
        }
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_DAILY_HOPE, false) == true) {
            dailyHopeRequests.value += 1
            intent.removeExtra(EXTRA_OPEN_DAILY_HOPE)
        }
    }

    private fun checkForAppUpdate() {
        if (BuildConfig.DEBUG || updateCheckInFlight) return
        updateCheckInFlight = true
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    updateReady.value = true
                    return@addOnSuccessListener
                }

                updateReady.value = false
                if (
                    shouldStartFlexibleUpdate(
                        updateAvailability = info.updateAvailability(),
                        flexibleUpdateAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
                        promptAttemptedThisSession = updatePromptAttemptedThisSession,
                        updateFlowStarted = updateFlowStarted,
                    )
                ) {
                    updatePromptAttemptedThisSession = true
                    updateFlowStarted = true
                    runCatching {
                        appUpdateManager.startUpdateFlowForResult(
                            info,
                            updateLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        )
                    }.onFailure {
                        updateFlowStarted = false
                    }
                }
            }
            .addOnCompleteListener { updateCheckInFlight = false }
    }

    private fun completeAppUpdate() {
        updateReady.value = false
        appUpdateManager.completeUpdate()
            .addOnFailureListener { updateReady.value = true }
    }

    companion object {
        const val EXTRA_OPEN_DAILY_HOPE = "open_daily_hope"
    }
}

internal fun shouldStartFlexibleUpdate(
    updateAvailability: Int,
    flexibleUpdateAllowed: Boolean,
    promptAttemptedThisSession: Boolean,
    updateFlowStarted: Boolean,
): Boolean =
    updateAvailability == UpdateAvailability.UPDATE_AVAILABLE &&
        flexibleUpdateAllowed &&
        !promptAttemptedThisSession &&
        !updateFlowStarted
