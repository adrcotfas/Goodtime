/*
 * Copyright 2022 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apps.adrcotfas.goodtime.billing

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Keep @Inject constructor(
    application: Application, val preferenceHelper: PreferenceHelper
) : AndroidViewModel(application) {


    private var billingClient: BillingClientWrapper =
        BillingClientWrapper(application, viewModelScope)
    private val _billingConnectionState = MutableLiveData(false)
    val billingConnectionState: LiveData<Boolean> = _billingConnectionState
    val productDetails = billingClient.productDetails.asLiveData()

    // Start the billing connection when the viewModel is initialized.
    init {
        billingClient.startBillingConnection(billingConnectionState = _billingConnectionState)
    }

    init {
        viewModelScope.launch {
            handlePro()
        }
    }

    private suspend fun handlePro() {
        data class ProState(val hasPro: Boolean?, val acknowledged: Boolean)
        billingClient.hasPro.combine(billingClient.isNewPurchaseAcknowledged) { hasPro, acknowledged ->
            ProState(hasPro, acknowledged)
        }.collect {
            Log.i(TAG, "Purchase state is: $it, persisted value: ${preferenceHelper.isPro()}")
            if (preferenceHelper.isPro() && it.hasPro == false) {
                Log.i(TAG, "Purchase was cancelled")
                preferenceHelper.setPro(false)
            } else if (!preferenceHelper.isPro()) {
                if (it.hasPro == true) {
                    Log.i(TAG, "Purchase was restored")
                    preferenceHelper.setPro(true)
                } else if (it.acknowledged) {
                    Log.i(TAG, "Purchase was confirmed")
                    preferenceHelper.setPro(true)
                }
            }
        }
    }

    /**
     * BillingFlowParams Builder for normal purchases.
     *
     * @param productDetails ProductDetails object returned by the library.
     * @return [BillingFlowParams] builder.
     */
    private fun billingFlowParamsBuilder(productDetails: ProductDetails) =
        BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
        )

    /**
     * Use the Google Play Billing Library to make a purchase.
     *
     * @param activity [Activity] instance.
     */
    fun buy(activity: Activity) {
        billingClient.productDetails.value?.let {
            billingClient.launchBillingFlow(
                activity, billingFlowParamsBuilder(productDetails = it).build()
            )
        } ?: Log.e(TAG, "buy: Invalid product details")
    }

    // When an activity is destroyed the viewModel's onCleared is called, so we terminate the
    // billing connection.
    override fun onCleared() = billingClient.terminateBillingConnection()

    companion object {
        private const val TAG = "BillingViewModel"
    }
}
