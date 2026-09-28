package com.aaronsedna.hopecards.ads

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aaronsedna.hopecards.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import java.lang.ref.WeakReference

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val width = maxWidth.value.toInt().coerceAtLeast(1)
        val adSize = remember(context, width) {
            // SDK 25 replaces the deprecated standard sizing methods with a single adaptive size
            // that follows the current orientation and supports modern banner inventory.
            AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, width)
        }
        val banner = remember(context, adSize) {
            // SDK WebView tracking can outlive destroy(). Do not give it an Activity/window
            // context; adaptive sizing above still uses the current screen configuration.
            BannerAdLifecycle(AdView(context.applicationContext).apply {
                adUnitId = BuildConfig.BANNER_AD_UNIT_ID
                setAdSize(adSize)
            })
        }
        // AndroidView otherwise retains the old (disposed) view after a window resize.
        key(banner) {
            AndroidView(
                factory = { checkNotNull(banner.view) },
                modifier = Modifier.fillMaxWidth().height(adSize.height.dp),
                onRelease = { banner.release() },
            )
        }
        DisposableEffect(lifecycle, banner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> banner.resume()
                    Lifecycle.Event.ON_PAUSE -> banner.pause()
                    Lifecycle.Event.ON_DESTROY -> banner.release()
                    else -> Unit
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
                banner.release()
            }
        }
    }
}

/** Owns only an application-context view, including any bounded deferred teardown. */
private class BannerAdLifecycle(adView: AdView) {
    var view: AdView? = adView
        private set
    private val mainHandler = Handler(Looper.getMainLooper())
    private val deferredDestroy = Runnable { destroy() }
    private val releasePendingOwnership = Runnable {
        pendingInitialView = view?.let(::WeakReference)
        view = null
    }
    private var resumed = false
    private var released = false
    private var initialLoadInFlight = true
    private var pendingInitialView: WeakReference<AdView>? = null

    init {
        // Register before loadAd: a failed request can finish before the view is attached.
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() = initialLoadFinished()

            override fun onAdFailedToLoad(error: LoadAdError) = initialLoadFinished()
        }
        adView.loadAd(AdRequest.Builder().build())
    }

    fun resume() {
        val current = view ?: return
        if (released || resumed) return
        current.resume()
        resumed = true
    }

    fun pause() {
        val current = view ?: return
        if (!resumed) return
        current.pause()
        resumed = false
    }

    fun release() {
        if (released) return
        released = true
        val current = view ?: return
        // Also pause requests released before their first lifecycle ON_RESUME.
        current.pause()
        resumed = false
        // Detach before destroy, as required by the banner resource-release contract.
        (current.parent as? ViewGroup)?.removeView(current)
        if (initialLoadInFlight || current.isLoading) {
            // SDK 25.4 can finish an initial load after destroy(), allocating a new WebView.
            // Wait for that result, with a deadline on our strong ownership of the view.
            mainHandler.postDelayed(releasePendingOwnership, INITIAL_LOAD_RELEASE_TIMEOUT_MILLIS)
        } else {
            destroy()
        }
    }

    private fun initialLoadFinished() {
        initialLoadInFlight = false
        if (released) {
            mainHandler.removeCallbacks(releasePendingOwnership)
            // Finish the SDK callback before destroying the view it is reporting about.
            mainHandler.post(deferredDestroy)
        }
    }

    private fun destroy() {
        mainHandler.removeCallbacks(deferredDestroy)
        mainHandler.removeCallbacks(releasePendingOwnership)
        val current = view ?: pendingInitialView?.get() ?: return
        view = null
        pendingInitialView = null
        // Keep the callback alive while a request is pending, including after the ownership
        // deadline. A terminal result destroys exactly once, with no later SDK method calls.
        current.adListener = ReleasedBannerListener
        current.onPaidEventListener = null
        current.destroy()
    }

    private companion object {
        const val INITIAL_LOAD_RELEASE_TIMEOUT_MILLIS = 60_000L
    }
}

private object ReleasedBannerListener : AdListener()
