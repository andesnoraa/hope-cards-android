package com.aaronsedna.hopecards.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.model.BillingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal object BillingEntitlementPolicy {
    fun resolve(
        currentAdFree: Boolean,
        ownsLifetimePurchase: Boolean,
        hasLegacySubscription: Boolean,
        inAppVerified: Boolean,
        subscriptionsVerified: Boolean,
    ): Boolean =
        ownsLifetimePurchase || hasLegacySubscription ||
            (currentAdFree && !(inAppVerified && subscriptionsVerified))
}

class BillingManager(
    context: Context,
    private val repository: AppRepository,
) : PurchasesUpdatedListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(BillingState())
    val state: StateFlow<BillingState> = _state.asStateFlow()

    private var productDetails: ProductDetails? = null
    private var ownedRemoveAds = false
    private var activeLegacySubscription = false
    private var connecting = false
    private var refreshing = false
    private var closed = false

    private val client = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .enableAutoServiceReconnection()
        .build()

    init {
        scope.launch {
            repository.cachedAdFree.collect { cached ->
                if (cached && !_state.value.isAdFree) {
                    _state.value = _state.value.copy(isAdFree = true)
                }
            }
        }
    }

    fun connect(reportErrors: Boolean = false, preserveMessage: Boolean = false) {
        if (closed) return
        if (client.isReady) {
            refresh(reportErrors, preserveMessage)
            return
        }
        if (connecting) return
        connecting = true
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (closed) return
                connecting = false
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _state.value = _state.value.copy(connected = true)
                    refresh(preserveMessage = preserveMessage)
                } else {
                    _state.value = _state.value.copy(
                        loading = false,
                        connected = false,
                        message = if (reportErrors) friendlyMessage(result) else null,
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                if (closed) return
                connecting = false
                _state.value = _state.value.copy(connected = false)
            }
        })
    }

    fun refresh(reportErrors: Boolean = false, preserveMessage: Boolean = false) {
        if (closed || refreshing) return
        if (!client.isReady) {
            connect(reportErrors, preserveMessage)
            return
        }
        refreshing = true
        _state.value = _state.value.copy(
            loading = true,
            message = if (preserveMessage) _state.value.message else null,
        )
        queryProduct()
        queryOwnedInApp { inAppSucceeded ->
            queryLegacySubscriptions { subscriptionsSucceeded ->
                // A verified purchase in either category grants access immediately. Revocation
                // requires both categories to succeed so an unsupported/offline legacy query can
                // never take ad-free access away from an existing customer.
                if (inAppSucceeded || subscriptionsSucceeded) {
                    updateEntitlement(
                        inAppVerified = inAppSucceeded,
                        subscriptionsVerified = subscriptionsSucceeded,
                    )
                }
                refreshing = false
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        val product = productDetails
        if (!client.isReady || product == null) {
            refresh(reportErrors = true, preserveMessage = true)
            _state.value = _state.value.copy(
                message = if (BuildConfig.DEBUG) {
                    "Google Play purchases cannot open in this sideloaded test build. The button will open the purchase flow in the Play testing version."
                } else {
                    "Remove Ads is not available from Google Play right now. Please try again in a moment."
                },
            )
            return
        }

        val detailsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
        product.oneTimePurchaseOfferDetailsList
            ?.firstOrNull()
            ?.offerToken
            ?.let(detailsBuilder::setOfferToken)

        val result = client.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(detailsBuilder.build()))
                .build(),
        )
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.value = _state.value.copy(message = friendlyMessage(result))
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> processPurchases(purchases.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> _state.value = _state.value.copy(message = friendlyMessage(result))
        }
    }

    private fun queryProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BuildConfig.REMOVE_ADS_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        client.queryProductDetailsAsync(params) { result, detailsResult ->
            if (closed) return@queryProductDetailsAsync
            productDetails = detailsResult.productDetailsList.firstOrNull()
            val offer = productDetails?.oneTimePurchaseOfferDetailsList?.firstOrNull()
            _state.value = _state.value.copy(
                price = offer?.formattedPrice,
                canPurchase = result.responseCode == BillingClient.BillingResponseCode.OK && productDetails != null,
            )
        }
    }

    private fun queryOwnedInApp(complete: (Boolean) -> Unit) {
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
        ) { result, purchases ->
            if (closed) return@queryPurchasesAsync
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                ownedRemoveAds = purchases.any { purchase ->
                    BuildConfig.REMOVE_ADS_PRODUCT_ID in purchase.products &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                processPurchases(purchases, commitEntitlement = false)
            }
            complete(result.responseCode == BillingClient.BillingResponseCode.OK)
        }
    }

    private fun queryLegacySubscriptions(complete: (Boolean) -> Unit) {
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
        ) { result, purchases ->
            if (closed) return@queryPurchasesAsync
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                // Hope Cards previously sold only its Premium subscription. Recognizing any still-active
                // subscription keeps those closed-test customers ad-free during the billing transition.
                activeLegacySubscription = purchases.any {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                purchases
                    .filter {
                        it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
                    }
                    .forEach(::acknowledge)
            }
            complete(result.responseCode == BillingClient.BillingResponseCode.OK)
        }
    }

    private fun processPurchases(purchases: List<Purchase>, commitEntitlement: Boolean = true) {
        val pending = purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }
        purchases
            .filter {
                it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    BuildConfig.REMOVE_ADS_PRODUCT_ID in it.products
            }
            .forEach { purchase ->
                ownedRemoveAds = true
                if (!purchase.isAcknowledged) {
                    acknowledge(purchase)
                }
            }
        _state.value = _state.value.copy(pending = pending)
        if (commitEntitlement) updateEntitlement()
    }

    private fun acknowledge(purchase: Purchase) {
        client.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build(),
        ) { result ->
            if (!closed && result.responseCode != BillingClient.BillingResponseCode.OK) {
                _state.value = _state.value.copy(message = friendlyMessage(result))
            }
        }
    }

    private fun updateEntitlement(
        inAppVerified: Boolean = false,
        subscriptionsVerified: Boolean = false,
    ) {
        val adFree = BillingEntitlementPolicy.resolve(
            currentAdFree = _state.value.isAdFree,
            ownsLifetimePurchase = ownedRemoveAds,
            hasLegacySubscription = activeLegacySubscription,
            inAppVerified = inAppVerified,
            subscriptionsVerified = subscriptionsVerified,
        )
        _state.value = _state.value.copy(isAdFree = adFree)
        scope.launch { repository.setAdFree(adFree) }
    }

    private fun friendlyMessage(result: BillingResult): String =
        result.debugMessage.takeIf(String::isNotBlank)
            ?: "Google Play Billing is temporarily unavailable."

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    fun close() {
        closed = true
        scope.cancel()
        client.endConnection()
    }
}
