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

import androidx.lifecycle.*
import com.apps.adrcotfas.goodtime.BillingRepository
import com.apps.adrcotfas.goodtime.BillingRepository.Companion.SKU_PREMIUM
import com.apps.adrcotfas.goodtime.billing.BillingDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainBillingViewModel @Inject constructor(private val billingRepository: BillingRepository) : ViewModel() {

    // use this to unlock PRO when purchasing the SKU
    val newPurchases: LiveData<String>
        get() = billingRepository.newPurchases.asLiveData()

    // use this to verify the purchase when restoring, revoking or canceling the IAP
    val isProPurchased: LiveData<BillingDataSource.PurchaseState>
        get() = billingRepository.isPurchased(SKU_PREMIUM).asLiveData()

    val billingLifecycleObserver: LifecycleObserver
        get() = billingRepository.billingLifecycleObserver
}