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
package com.apps.adrcotfas.goodtime.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.apps.adrcotfas.goodtime.BillingRepository
import com.apps.adrcotfas.goodtime.BillingRepository.Companion.SKU_PREMIUM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MakePurchasesViewModel @Inject constructor(private val billingRepository: BillingRepository) :ViewModel() {

    // to enable/disable the buy button
    fun canBuyPro(): LiveData<Boolean> {
        return billingRepository.canPurchase(SKU_PREMIUM).asLiveData()
    }

    fun buyPro(activity: Activity) {
        billingRepository.buySku(activity, SKU_PREMIUM)
    }
}