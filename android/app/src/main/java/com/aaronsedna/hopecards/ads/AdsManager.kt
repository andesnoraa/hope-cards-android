package com.aaronsedna.hopecards.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.data.AppRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

internal object InterstitialPresentationPolicy {
    fun canPresent(
        adsEnabled: Boolean,
        foreground: Boolean,
        consentAllowsAds: Boolean,
        activityResumed: Boolean,
        alreadyShowing: Boolean,
    ): Boolean =
        adsEnabled && foreground && consentAllowsAds && activityResumed && !alreadyShowing
}

class AdsManager(
    context: Context,
    private val repository: AppRepository,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val consentInformation = UserMessagingPlatform.getConsentInformation(appContext)
    private val consentUpdateStarted = AtomicBoolean(false)
    private val initializationStarted = AtomicBoolean(false)
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()
    private val _privacyOptionsRequired = MutableStateFlow(false)
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()
    private var interstitial: InterstitialAd? = null
    private var interstitialLoadedAt = 0L
    private var loadingInterstitial = false
    private var nextInterstitialLoadAt = 0L
    private var interstitialRetryJob: Job? = null
    private var interstitialExpirationJob: Job? = null
    private var adsEnabled = true
    private var isForeground = true
    private var sdkInitialized = false
    private var interstitialShowing = false
    private var adStateGeneration = 0L
    private var presentationGeneration = 0L
    private var closed = false

    fun initialize(activity: Activity, isAdFree: Boolean) {
        adsEnabled = !isAdFree
        if (isAdFree) {
            _ready.value = false
            presentationGeneration += 1
            clearCachedInterstitial(invalidatePendingLoad = true)
            return
        }
        if (sdkInitialized) {
            synchronizeAdAvailability()
            return
        }
        if (!consentUpdateStarted.compareAndSet(false, true)) return
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                _privacyOptionsRequired.value =
                    consentInformation.privacyOptionsRequirementStatus ==
                        ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (error != null) Log.w(TAG, "Consent form could not be shown: ${error.message}")
                    _privacyOptionsRequired.value =
                        consentInformation.privacyOptionsRequirementStatus ==
                            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                    synchronizeAdAvailability()
                }
            },
            { error ->
                Log.w(TAG, "Consent information update failed: ${error.message}")
                synchronizeAdAvailability()
            },
        )
    }

    fun showPrivacyOptions(activity: Activity, onComplete: (FormError?) -> Unit = {}) {
        presentationGeneration += 1
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            _privacyOptionsRequired.value =
                consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
            synchronizeAdAvailability()
            onComplete(error)
        }
    }

    fun onResume() {
        isForeground = true
        if (_ready.value && adsEnabled) loadInterstitial()
    }

    fun onPause() {
        isForeground = false
        presentationGeneration += 1
        interstitialRetryJob?.cancel()
        interstitialRetryJob = null
    }

    fun recordCompletedCard(activity: Activity, isAdFree: Boolean) {
        if (isAdFree) return
        initialize(activity, isAdFree = false)
        val requestGeneration = ++presentationGeneration
        scope.launch {
            if (repository.recordCompletedCard(System.currentTimeMillis())) {
                if (_ready.value && requestGeneration == presentationGeneration) {
                    showInterstitial(activity)
                }
            } else if (_ready.value) {
                loadInterstitial()
            }
        }
    }

    fun completeArtwork(
        activity: Activity,
        isAdFree: Boolean,
        isCurrentArtwork: () -> Boolean,
        continueToGallery: () -> Unit,
    ) {
        if (isAdFree || closed) {
            continueToGallery()
            return
        }
        val requestGeneration = ++presentationGeneration
        val cachedAd = interstitial
        val readyAtStart = _ready.value && cachedAd != null &&
            SystemClock.elapsedRealtime() - interstitialLoadedAt < INTERSTITIAL_EXPIRATION_MS
        // A pending storage operation must not retain a destroyed activity after rotation/exit.
        val transitionScope = (activity as? LifecycleOwner)?.lifecycleScope ?: scope
        transitionScope.launch {
            try {
                completeContentBreak(
                    adReadyAtStart = readyAtStart,
                    recordCompletion = { repository.recordCompletedCard(System.currentTimeMillis()) },
                    canStillPresent = {
                        requestGeneration == presentationGeneration && interstitial === cachedAd &&
                            _ready.value && isCurrentArtwork() && activity.hasWindowFocus()
                    },
                    showAd = { showInterstitial(activity) },
                    continueNavigation = continueToGallery,
                )
                if (_ready.value) loadInterstitial()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w(TAG, "Could not record completed artwork", error)
            }
        }
    }

    private fun synchronizeAdAvailability() {
        if (closed || !adsEnabled || !consentInformation.canRequestAds()) {
            _ready.value = false
            presentationGeneration += 1
            clearCachedInterstitial(invalidatePendingLoad = true)
            return
        }
        if (sdkInitialized) {
            _ready.value = true
            loadInterstitial()
            return
        }
        if (!initializationStarted.compareAndSet(false, true)) return
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
                .build(),
        )
        MobileAds.initialize(appContext) {
            scope.launch {
                if (closed) return@launch
                sdkInitialized = true
                synchronizeAdAvailability()
            }
        }
    }

    private fun loadInterstitial() {
        if (
            closed || !adsEnabled || !_ready.value || !isForeground ||
            !consentInformation.canRequestAds() || interstitialShowing ||
            interstitial != null || loadingInterstitial
        ) return
        if (SystemClock.elapsedRealtime() < nextInterstitialLoadAt) {
            scheduleInterstitialRetry()
            return
        }
        loadingInterstitial = true
        val loadGeneration = adStateGeneration
        InterstitialAd.load(
            appContext,
            BuildConfig.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadingInterstitial = false
                    nextInterstitialLoadAt = 0L
                    interstitialRetryJob?.cancel()
                    interstitialRetryJob = null
                    interstitialExpirationJob?.cancel()
                    interstitialExpirationJob = null
                    if (
                        loadGeneration != adStateGeneration || closed || !adsEnabled ||
                        !isForeground || !_ready.value || !consentInformation.canRequestAds()
                    ) {
                        ad.fullScreenContentCallback = null
                        return
                    }
                    interstitial = ad
                    Log.d(TAG, "Interstitial loaded.")
                    val loadedAt = SystemClock.elapsedRealtime()
                    interstitialLoadedAt = loadedAt
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialShowing = false
                            interstitialExpirationJob?.cancel()
                            interstitialExpirationJob = null
                            interstitial = null
                            interstitialLoadedAt = 0L
                            loadInterstitial()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            interstitialShowing = false
                            Log.w(TAG, "Interstitial failed to show: $error")
                            interstitialExpirationJob?.cancel()
                            interstitialExpirationJob = null
                            interstitial = null
                            interstitialLoadedAt = 0L
                            loadInterstitial()
                        }

                        override fun onAdShowedFullScreenContent() {
                            interstitialShowing = true
                            Log.d(TAG, "Interstitial shown.")
                            interstitialExpirationJob?.cancel()
                            interstitialExpirationJob = null
                            interstitial = null
                            interstitialLoadedAt = 0L
                            scope.launch {
                                repository.recordInterstitialShown(System.currentTimeMillis())
                            }
                        }
                    }
                    interstitialExpirationJob = scope.launch {
                        delay(INTERSTITIAL_EXPIRATION_MS)
                        if (interstitial === ad && interstitialLoadedAt == loadedAt) {
                            ad.fullScreenContentCallback = null
                            interstitial = null
                            interstitialLoadedAt = 0L
                            loadInterstitial()
                        }
                        interstitialExpirationJob = null
                    }
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    if (loadGeneration != adStateGeneration) return
                    loadingInterstitial = false
                    interstitial = null
                    nextInterstitialLoadAt = SystemClock.elapsedRealtime() + INTERSTITIAL_RETRY_DELAY_MS
                    Log.w(TAG, "Interstitial failed to load: $error")
                    scheduleInterstitialRetry()
                }
            },
        )
    }

    private fun scheduleInterstitialRetry() {
        if (
            closed || !adsEnabled || !isForeground || !_ready.value ||
            interstitialRetryJob?.isActive == true
        ) return
        val waitMs = (nextInterstitialLoadAt - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        interstitialRetryJob = scope.launch {
            delay(waitMs)
            interstitialRetryJob = null
            loadInterstitial()
        }
    }

    private fun showInterstitial(activity: Activity) {
        val activityResumed =
            (activity as? LifecycleOwner)?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED)
                ?: (!activity.isFinishing && !activity.isDestroyed)
        if (
            closed || activity.isFinishing || activity.isDestroyed ||
            !InterstitialPresentationPolicy.canPresent(
                adsEnabled = adsEnabled,
                foreground = isForeground,
                consentAllowsAds = consentInformation.canRequestAds(),
                activityResumed = activityResumed,
                alreadyShowing = interstitialShowing,
            )
        ) return
        if (SystemClock.elapsedRealtime() - interstitialLoadedAt >= INTERSTITIAL_EXPIRATION_MS) {
            interstitialExpirationJob?.cancel()
            interstitialExpirationJob = null
            interstitial?.fullScreenContentCallback = null
            interstitial = null
            interstitialLoadedAt = 0L
        }
        val ad = interstitial
        if (ad == null) {
            loadInterstitial()
        } else {
            interstitialShowing = true
            runCatching { ad.show(activity) }
                .onFailure { error ->
                    Log.w(TAG, "Interstitial could not be shown.", error)
                    interstitialShowing = false
                    clearCachedInterstitial(invalidatePendingLoad = false)
                    loadInterstitial()
                }
        }
    }

    private fun clearCachedInterstitial(invalidatePendingLoad: Boolean) {
        if (invalidatePendingLoad) {
            adStateGeneration += 1
            loadingInterstitial = false
        }
        interstitialRetryJob?.cancel()
        interstitialRetryJob = null
        interstitialExpirationJob?.cancel()
        interstitialExpirationJob = null
        interstitial?.fullScreenContentCallback = null
        interstitial = null
        interstitialLoadedAt = 0L
        nextInterstitialLoadAt = 0L
    }

    fun close() {
        closed = true
        adsEnabled = false
        _ready.value = false
        presentationGeneration += 1
        clearCachedInterstitial(invalidatePendingLoad = true)
        scope.cancel()
    }

    private companion object {
        const val TAG = "HopeCardsAds"
        const val INTERSTITIAL_RETRY_DELAY_MS = 60_000L
        const val INTERSTITIAL_EXPIRATION_MS = 55 * 60 * 1000L
    }
}
