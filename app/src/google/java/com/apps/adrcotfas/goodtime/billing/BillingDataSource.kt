/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apps.adrcotfas.goodtime.billing

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryPurchasesAsync
import com.android.billingclient.api.querySkuDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * This is based on the Billing samples from Google (https://github.com/android/play-billing-samples).
 * The BillingDataSource implements billing functionality for non-consumable purchases.
 * Purchases can happen while in the app or at any time while out of the app, so the
 * BillingDataSource has to account for that.
 *
 * Since every SKU (Product ID) can have an individual state, all SKUs have an associated StateFlow
 * to allow their state to be observed.
 *
 * This BillingDataSource knows nothing about the application; all necessary information is either
 * passed into the constructor, exported as observable Flows, or exported through callbacks.
 * This code can be reused in a variety of apps.
 *
 * Beginning a purchase flow involves passing an Activity into the Billing Library, but we merely
 * pass it along to the API.
 *
 * This data source has a few automatic features:
 * 1) It checks for a valid signature on all purchases before attempting to acknowledge them.
 * 2) It automatically acknowledges all known SKUs for non-consumables, and doesn't set the state
 * to purchased until the acknowledgement is complete.
 * 3) If the BillingService is disconnected, it will attempt to reconnect with exponential
 * fallback.
 *
 * This data source attempts to keep billing library specific knowledge confined to this file;
 * The only thing that clients of the BillingDataSource need to know are the SKUs used by their
 * application.
 *
 * The BillingClient needs access to the Application context in order to bind the remote billing
 * service.
 *
 * The BillingDataSource can also act as a LifecycleObserver for an Activity; this allows it to
 * refresh purchases during onResume.
 */

private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L // 15 minutes
private const val SKU_DETAILS_REQUERY_TIME = 1000L * 60L * 60L * 4L // 4 hours

class BillingDataSource private constructor(
    context: Context,
    private val defaultScope: CoroutineScope,
    private val publicKey: String,
    knownInAppSKUs: Array<String>
) :
    LifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {

    // Billing client, connection, cached data
    private val billingClient: BillingClient

    // known SKUs (used to query sku data and validate responses)
    private val knownInAppSKUs: List<String> = listOf(*knownInAppSKUs)

    // how long before the data source tries to reconnect to Google play
    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS

    // when was the last successful SkuDetailsResponse?
    private var skuDetailsResponseTime = -SKU_DETAILS_REQUERY_TIME

    private enum class SkuState {
        SKU_STATE_UNPURCHASED, SKU_STATE_PENDING, SKU_STATE_PURCHASED, SKU_STATE_PURCHASED_AND_ACKNOWLEDGED
    }

    // Flows that are mostly maintained so they can be transformed into observables.
    private val skuStateMap: MutableMap<String, MutableStateFlow<SkuState>> = HashMap()
    private val skuDetailsMap: MutableMap<String, MutableStateFlow<SkuDetails?>> = HashMap()

    // Observables that are used to communicate state.
    private val newPurchaseFlow = MutableSharedFlow<List<String>>(extraBufferCapacity = 1)

    private val billingFlowInProcess = MutableStateFlow(false)
    private val purchasesUpdated = MutableStateFlow(false)

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onBillingSetupFinished: $responseCode $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                // The billing client is ready. You can query purchases here.
                // This doesn't mean that your app is set up correctly in the console -- it just
                // means that you have a connection to the Billing service.
                reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS
                defaultScope.launch {
                    querySkuDetailsAsync()
                    refreshPurchases()
                }
            }
            else -> retryBillingServiceConnectionWithExponentialBackoff()
        }
    }

    /**
     * This is a pretty unusual occurrence. It happens primarily if the Google Play Store
     * self-upgrades or is force closed.
     */
    override fun onBillingServiceDisconnected() {
        retryBillingServiceConnectionWithExponentialBackoff()
    }

    /**
     * Retries the billing service connection with exponential backoff, maxing out at the time
     * specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
     */
    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed(
            { billingClient.startConnection(this@BillingDataSource) },
            reconnectMilliseconds
        )
        reconnectMilliseconds = min(
            reconnectMilliseconds * 2,
            RECONNECT_TIMER_MAX_TIME_MILLISECONDS
        )
    }

    /**
     * Called by initializeFlows to create the various Flow objects we're planning to emit.
     * @param skuList a List<String> of SKUs representing purchases
    </String> */
    private fun addSkuFlows(skuList: List<String>) {
        for (sku in skuList) {
            val skuState = MutableStateFlow(SkuState.SKU_STATE_UNPURCHASED)
            val details = MutableStateFlow<SkuDetails?>(null)
            details.subscriptionCount.map { count -> count > 0 } // map count into active/inactive flag
                .distinctUntilChanged() // only react to true<->false changes
                .onEach { isActive -> // configure an action
                    if (isActive && (SystemClock.elapsedRealtime() - skuDetailsResponseTime > SKU_DETAILS_REQUERY_TIME)) {
                        skuDetailsResponseTime = SystemClock.elapsedRealtime()
                        Log.v(TAG, "Skus not fresh, requerying")
                        querySkuDetailsAsync()
                    }
                }
                .launchIn(defaultScope) // launch it
            skuStateMap[sku] = skuState
            skuDetailsMap[sku] = details
        }
    }

    /**
     * Creates a Flow object for every known SKU so the state and SKU details can be observed
     * in other layers. The repository is responsible for mapping this data in ways that are more
     * useful for the application.
     */
    private fun initializeFlows() {
        addSkuFlows(knownInAppSKUs)
    }

    fun getNewPurchases() = newPurchaseFlow.asSharedFlow()

    enum class PurchaseState {
        PURCHASED,
        NOT_PURCHASED,
        INVALID
    }

    /**
     * Returns a Flow containing the state whether the user has purchased a SKU or not (or not sure yet).
     * @return a Flow that observes the SKUs purchase state
     */
    fun isPurchased(sku: String): Flow<PurchaseState> {
        val skuStateFlow = skuStateMap[sku]!!
        return skuStateFlow.combine(purchasesUpdated) { skuState, purchasesUpdated ->
            if (!purchasesUpdated) {
                PurchaseState.INVALID
            } else if (skuState == SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED) {
                PurchaseState.PURCHASED
            } else {
                PurchaseState.NOT_PURCHASED
            }
        }
    }

    /**
     * Returns whether or not the user can purchase a SKU. It does this by returning
     * a Flow combine transformation that returns true if the SKU is in the UNSPECIFIED state, as
     * well as if we have skuDetails for the SKU. (SKUs cannot be purchased without valid
     * SkuDetails.)
     * @return a Flow that observes the SKUs purchase state
     */
    fun canPurchase(sku: String): Flow<Boolean> {
        val skuDetailsFlow = skuDetailsMap[sku]!!
        val skuStateFlow = skuStateMap[sku]!!

        return skuStateFlow.combine(skuDetailsFlow) { skuState, skuDetails ->
            skuState == SkuState.SKU_STATE_UNPURCHASED && skuDetails != null
        }
    }

    // There's lots of information in SkuDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc. You can add to this for your app,
    // or create your own class to pass the information across.
    /**
     * The title of our SKU from SkuDetails.
     * @param sku to get the title from
     * @return title of the requested SKU as an observable Flow<String>
    </String> */
    fun getSkuTitle(sku: String): Flow<String> {
        val skuDetailsFlow = skuDetailsMap[sku]!!
        return skuDetailsFlow.mapNotNull { skuDetails ->
            skuDetails?.title
        }
    }

    fun getSkuPrice(sku: String): Flow<String> {
        val skuDetailsFlow = skuDetailsMap[sku]!!
        return skuDetailsFlow.mapNotNull { skuDetails ->
            skuDetails?.price
        }
    }

    fun getSkuDescription(sku: String): Flow<String> {
        val skuDetailsFlow = skuDetailsMap[sku]!!
        return skuDetailsFlow.mapNotNull { skuDetails ->
            skuDetails?.description
        }
    }

    /**
     * Receives the result from [.querySkuDetailsAsync]}.
     *
     * Store the SkuDetails and post them in the [.skuDetailsMap]. This allows other
     * parts of the app to use the [SkuDetails] to show SKU information and make purchases.
     */
    private fun onSkuDetailsResponse(
        billingResult: BillingResult,
        skuDetailsList: List<SkuDetails>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.i(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
                if (skuDetailsList == null || skuDetailsList.isEmpty()) {
                    Log.e(
                        TAG,
                        "onSkuDetailsResponse: " +
                                "Found null or empty SkuDetails. " +
                                "Check to see if the SKUs you requested are correctly published " +
                                "in the Google Play Console."
                    )
                } else {
                    for (skuDetails in skuDetailsList) {
                        val sku = skuDetails.sku
                        val detailsMutableFlow = skuDetailsMap[sku]
                        detailsMutableFlow?.tryEmit(skuDetails)
                            ?: Log.e(TAG, "Unknown sku: $sku")
                    }
                }
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR ->
                Log.e(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
            BillingClient.BillingResponseCode.USER_CANCELED ->
                Log.i(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED ->
                Log.wtf(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
            else -> Log.wtf(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
        }
        skuDetailsResponseTime = if (responseCode == BillingClient.BillingResponseCode.OK) {
            SystemClock.elapsedRealtime()
        } else {
            -SKU_DETAILS_REQUERY_TIME
        }
    }

    /**
     * Calls the billing client functions to query sku details for the in app SKUs.
     * SKU details are useful for displaying item names and price lists to the user, and are
     * required to make a purchase.
     */
    private suspend fun querySkuDetailsAsync() {
        if (!knownInAppSKUs.isNullOrEmpty()) {
            val skuDetailsResult = billingClient.querySkuDetails(
                SkuDetailsParams.newBuilder()
                    .setType(BillingClient.SkuType.INAPP)
                    .setSkusList(knownInAppSKUs)
                    .build()
            )
            onSkuDetailsResponse(skuDetailsResult.billingResult, skuDetailsResult.skuDetailsList)
        }
    }

    /*
        GPBLv3 now queries purchases synchronously, simplifying this flow. This only gets active
        purchases.
     */
    private suspend fun refreshPurchases() {
        Log.d(TAG, "Refreshing purchases.")
        val purchasesResult = billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)
        val billingResult = purchasesResult.billingResult
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Problem getting purchases: " + billingResult.debugMessage)
        } else {
            processPurchaseList(purchasesResult.purchasesList, knownInAppSKUs)
        }
        Log.d(TAG, "Refreshing purchases finished.")
    }

    /**
     * Calling this means that we have the most up-to-date information for a Sku in a purchase
     * object. This uses the purchase state (Pending, Unspecified, Purchased) along with the
     * acknowledged state.
     * @param purchase an up-to-date object to set the state for the Sku
     */
    private fun setSkuStateFromPurchase(purchase: Purchase) {
        for (purchaseSku in purchase.skus) {
            val skuStateFlow = skuStateMap[purchaseSku]
            if (null == skuStateFlow) {
                Log.e(
                    TAG,
                    "Unknown SKU " + purchaseSku + ". Check to make " +
                            "sure SKU matches SKUS in the Play developer console."
                )
            } else {
                Log.d(TAG, "Purchase $purchaseSku in state: ${purchase.purchaseState}")
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PENDING -> skuStateFlow.tryEmit(SkuState.SKU_STATE_PENDING)
                    Purchase.PurchaseState.UNSPECIFIED_STATE -> skuStateFlow.tryEmit(SkuState.SKU_STATE_UNPURCHASED)
                    Purchase.PurchaseState.PURCHASED -> if (purchase.isAcknowledged) {
                        skuStateFlow.tryEmit(SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED)
                    } else {
                        skuStateFlow.tryEmit(SkuState.SKU_STATE_PURCHASED)
                    }
                    else -> Log.e(TAG, "Purchase in unknown state: ${purchase.purchaseState}")
                }
            }
        }
    }

    /**
     * Since we (mostly) are getting sku states when we actually make a purchase or update
     * purchases, we keep some internal state when we do things like acknowledge or consume.
     * @param sku product ID to change the state of
     * @param newSkuState the new state of the sku.
     */
    private fun setSkuState(sku: String, newSkuState: SkuState) {
        val skuStateFlow = skuStateMap[sku]
        skuStateFlow?.tryEmit(newSkuState)
            ?: Log.e(
                TAG,
                "Unknown SKU " + sku + ". Check to make " +
                        "sure SKU matches SKUS in the Play developer console."
            )
    }

    /**
     * Goes through each purchase and makes sure that the purchase state is processed and the state
     * is available through Flows. Verifies signature and acknowledges purchases. PURCHASED isn't
     * returned until the purchase is acknowledged.
     *
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     *
     * Developers can choose to acknowledge purchases from a server using the
     * Google Play Developer API. The server has direct access to the user database,
     * so using the Google Play Developer API for acknowledgement might be more reliable.
     *
     * If the purchase token is not acknowledged within 3 days,
     * then Google Play will automatically refund and revoke the purchase.
     * This behavior helps ensure that users are not charged unless the user has successfully
     * received access to the content.
     * This eliminates a category of issues where users complain to developers
     * that they paid for something that the app is not giving to them.
     *
     * If a skusToUpdate list is passed-into this method, any purchases not in the list of
     * purchases will have their state set to UNPURCHASED.
     *
     * @param purchases the List of purchases to process.
     * @param skusToUpdate a list of skus that we want to update the state from --- this allows us
     * to set the state of non-returned SKUs to UNPURCHASED.
     */
    private fun processPurchaseList(purchases: List<Purchase>?, skusToUpdate: List<String>?) {
        val updatedSkus = HashSet<String>()
        if (null != purchases) {
            for (purchase in purchases) {
                for (sku in purchase.skus) {
                    val skuStateFlow = skuStateMap[sku]
                    if (null == skuStateFlow) {
                        Log.e(
                            TAG,
                            "Unknown SKU " + sku + ". Check to make " +
                                    "sure SKU matches SKUS in the Play developer console."
                        )
                        continue
                    }
                    updatedSkus.add(sku)
                }
                // Global check to make sure all purchases are signed correctly.
                // This check is best performed on your server.
                val purchaseState = purchase.purchaseState
                if (purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!isSignatureValid(purchase)) {
                        Log.e(
                            TAG,
                            "Invalid signature. Check to make sure your " +
                                    "public key is correct."
                        )
                        continue
                    }
                    // only set the purchased state after we've validated the signature.
                    setSkuStateFromPurchase(purchase)
                    defaultScope.launch {
                        if (!purchase.isAcknowledged) {
                            // acknowledge everything --- new purchases are ones not yet acknowledged
                            val billingResult = billingClient.acknowledgePurchase(
                                AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                            )
                            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                                Log.e(TAG, "Error acknowledging purchase: ${purchase.skus}")
                            } else {
                                // purchase acknowledged
                                for (sku in purchase.skus) {
                                    setSkuState(sku, SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED)
                                }
                            }
                            newPurchaseFlow.tryEmit(purchase.skus)
                        }
                    }
                } else {
                    // make sure the state is set
                    setSkuStateFromPurchase(purchase)
                }
            }
        } else {
            Log.d(TAG, "Empty purchase list.")
        }
        // Clear purchase state of anything that didn't come with this purchase list if this is
        // part of a refresh.
        if (null != skusToUpdate) {
            for (sku in skusToUpdate) {
                if (!updatedSkus.contains(sku)) {
                    setSkuState(sku, SkuState.SKU_STATE_UNPURCHASED)
                }
            }
        }
        defaultScope.launch {
            purchasesUpdated.emit(true)
        }
        logSkuStateMap()
    }

    /**
     * Launch the billing flow. This will launch an external Activity for a result, so it requires
     * an Activity reference.
     *
     * @param activity active activity to launch our billing flow from
     * @param sku SKU (Product ID) to be purchased
     */
    fun launchBillingFlow(activity: Activity?, sku: String) {
        val skuDetails = skuDetailsMap[sku]?.value
        if (null != skuDetails) {
            val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
            billingFlowParamsBuilder.setSkuDetails(skuDetails)
            defaultScope.launch {
                val br = billingClient.launchBillingFlow(
                    activity!!,
                    billingFlowParamsBuilder.build()
                )
                if (br.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingFlowInProcess.emit(true)
                } else {
                    Log.e(TAG, "Billing failed: + " + br.debugMessage)
                }
            }
        } else {
            Log.e(TAG, "SkuDetails not found for: $sku")
        }
    }

    /**
     * Returns a Flow that reports if a billing flow is in process, meaning that
     * launchBillingFlow has returned BillingResponseCode.OK and onPurchasesUpdated hasn't yet
     * been called.
     * @return Flow that indicates the known state of the billing flow.
     */
    fun getBillingFlowInProcess(): Flow<Boolean> {
        return billingFlowInProcess.asStateFlow()
    }

    /**
     * Returns a Flow that reports if the purchases were updated to their current value
     */
    fun getPurchasesUpdated(): Flow<Boolean> {
        return purchasesUpdated.asStateFlow()
    }

    /**
     * Called by the BillingLibrary when new purchases are detected; typically in response to a
     * launchBillingFlow.
     * @param billingResult result of the purchase flow.
     * @param list of new purchases.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> if (null != list) {
                processPurchaseList(list, null)
                return
            } else Log.d(TAG, "Null Purchase List Returned from OK response!")
            BillingClient.BillingResponseCode.USER_CANCELED -> Log.i(
                TAG,
                "onPurchasesUpdated: User canceled the purchase"
            )
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Log.i(
                TAG,
                "onPurchasesUpdated: The user already owns this item"
            )
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Log.e(
                TAG,
                "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys."
            )
            else -> Log.d(
                TAG,
                "BillingResult [" + billingResult.responseCode + "]: " + billingResult.debugMessage
            )
        }
        defaultScope.launch {
            billingFlowInProcess.emit(false)
        }
    }

    /**
     * Ideally your implementation will comprise a secure server, rendering this check
     * unnecessary. @see [Security]
     */
    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(publicKey, purchase.originalJson, purchase.signature)
    }

    /**
     * It's recommended to re-query purchases during onResume.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        Log.d(TAG, "ON_RESUME")

        defaultScope.launch {
            purchasesUpdated.emit(false)
        }

        // this just avoids an extra purchase refresh after we finish a billing flow
        if (!billingFlowInProcess.value) {
            if (billingClient.isReady) {
                defaultScope.launch {
                    refreshPurchases()
                }
            }
        }
    }

    companion object {
        private val TAG = BillingDataSource::class.java.simpleName

        @Volatile
        private var sInstance: BillingDataSource? = null
        private val handler = Handler(Looper.getMainLooper())

        // Standard boilerplate double check locking pattern for thread-safe singletons.
        @JvmStatic
        fun getInstance(
            context: Context,
            defaultScope: CoroutineScope,
            publicKey: String,
            knownInAppSKUs: Array<String>
        ) = sInstance ?: synchronized(this) {
            sInstance ?: BillingDataSource(
                context,
                defaultScope,
                publicKey,
                knownInAppSKUs
            )
                .also { sInstance = it }
        }
    }

    private fun logSkuStateMap() {
        for (p in skuStateMap) {
            Log.d(TAG, "SKU \"${p.key}\" is ${p.value.value.name}")
        }
    }

    /**
     * Our constructor.  Since we are a singleton, this is only used internally.
     */
    init {
        initializeFlows()
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(this)
    }
}
