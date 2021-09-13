/*
 * Copyright 2021 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.apps.adrcotfas.goodtime

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.apps.adrcotfas.goodtime.billing.BillingDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BillingRepository(
    private val billingDataSource: BillingDataSource,
    private val defaultScope: CoroutineScope
) {
    private val _newPurchases: MutableSharedFlow<String> = MutableSharedFlow()

    /**
     * Sets up the event that we can use to send messages up to the UI.
     * This collects new purchase events from the BillingDataSource
     */
    private fun postNewPurchasesFromBillingFlow() {
        defaultScope.launch {
            try {
                billingDataSource.getNewPurchases().collect { skuList ->
                    skuList.forEach { _newPurchases.emit(it) }
                }
            } catch (e: Throwable) {
                Log.d(TAG, "Collection complete")
            }
            Log.d(TAG, "Collection Coroutine Scope Exited")
        }
    }

    fun buySku(activity: Activity, sku: String) {
        billingDataSource.launchBillingFlow(activity, sku)
    }

    /**
     * Return Flow that indicates whether the sku is currently purchased.
     *
     * @param sku the SKU to get and observe the value for
     * @return Flow that returns true if the sku is purchased.
     */
    fun isPurchased(sku: String): Flow<BillingDataSource.PurchaseState> {
        return billingDataSource.isPurchased(sku)
    }

    /**
     * For other skus, we rely on just the data from the billing data source.
     *
     * @param sku the SKU to get and observe the value for
     * @return Flow<Boolean> that returns true if the sku can be purchased
     */
    fun canPurchase(sku: String): Flow<Boolean> = billingDataSource.canPurchase(sku)

    val billingLifecycleObserver: LifecycleObserver
        get() = billingDataSource

    val newPurchases: Flow<String>
        get() = _newPurchases

    companion object {
        const val LICENCE_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgH+Nj4oEbJKEnrds3qaDcdj" +
                    "ti0hnL1hlYsOoX5hVNUs4CpTzVmiAtO3LHwLGJzvtDmagsszKgVFn3SmVeA7y+GS93I6FwsCEmXNGdaCJW4" +
                    "TftLqSxT9Q4Qn8R8hWk3OXgo1ZF2FxGuicwq9zt4W+6pW7QMhpoBA0DyCLhoCulINVTkEKBBWeCS4CDkhXr" +
                    "nXCoAbhmYn2R7Ifhn7voy1YR9Vr/G9tCHzvLM1k4bntyOebxdMwPy49Dsrzam1hgPhzmEMqwolchLx95DFX" +
                    "VfHcWSFtBpZwR4sPFhXny5CQ255CruCdQd8L5CHdRhrHyNkzBVrwoYg8WWZUQ3Ijcu2e5wIDAQAB"
        const val SKU_PREMIUM = "upgraded_version"

        val TAG = BillingRepository::class.simpleName
        val INAPP_SKUS = arrayOf(SKU_PREMIUM)
    }

    init {
        postNewPurchasesFromBillingFlow()
    }
}