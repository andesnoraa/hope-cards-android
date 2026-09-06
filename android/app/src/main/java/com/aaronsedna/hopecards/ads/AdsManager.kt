package com.aaronsedna.hopecards.ads

import android.app.Activity
import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

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
    private var loadingInterstitial = false
    private var adsEnabled = true
    private var closed = false

    fun initialize(activity: Activity, isAdFree: Boolean) {
        adsEnabled = !isAdFree
        if (isAdFree) {
            interstitial?.fullScreenContentCallback = null
            interstitial = null
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
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    _privacyOptionsRequired.value =
                        consentInformation.privacyOptionsRequirementStatus ==
                            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                    initializeSdkIfAllowed()
                }
            },
            { initializeSdkIfAllowed() },
        )
    }

    fun showPrivacyOptions(activity: Activity, onComplete: (FormError?) -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            _privacyOptionsRequired.value =
                consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
            onComplete(error)
        }
    }

    fun recordCompletedCard(activity: Activity, isAdFree: Boolean) {
        if (isAdFree || !_ready.value) return
        scope.launch {
            if (repository.recordCompletedCard(System.currentTimeMillis())) {
                showInterstitial(activity)
            } else {
                loadInterstitial()
            }
        }
    }

    private fun initializeSdkIfAllowed() {
        if (!consentInformation.canRequestAds() || !initializationStarted.compareAndSet(false, true)) return
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
                .build(),
        )
        MobileAds.initialize(appContext) {
            _ready.value = true
        }
    }

    private fun loadInterstitial() {
        if (!_ready.value || interstitial != null || loadingInterstitial) return
        loadingInterstitial = true
        InterstitialAd.load(
            appContext,
            BuildConfig.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadingInterstitial = false
                    if (!adsEnabled || closed) {
                        ad.fullScreenContentCallback = null
                        return
                    }
                    interstitial = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitial = null
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            interstitial = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            interstitial = null
                        }
                    }
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    loadingInterstitial = false
                    interstitial = null
                }
            },
        )
    }

    private fun showInterstitial(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return
        val ad = interstitial
        if (ad == null) {
            loadInterstitial()
        } else {
            ad.show(activity)
        }
    }

    fun close() {
        closed = true
        adsEnabled = false
        interstitial?.fullScreenContentCallback = null
        interstitial = null
        scope.cancel()
    }
}
