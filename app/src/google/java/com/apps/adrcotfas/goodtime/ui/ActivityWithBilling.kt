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
package com.apps.adrcotfas.goodtime.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.billing.BillingDataSource
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.viewmodel.MainBillingViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ActivityWithBilling : AppCompatActivity() {

    private val viewModel: MainBillingViewModel by viewModels()
    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.newPurchases.observe(this) { purchase ->
            Log.d(TAG, "$purchase was purchased")
            preferenceHelper.setPro(true)
            showSnackBar(R.string.message_premium)
        }

        // Allows billing to refresh purchases during onResume
        lifecycle.addObserver(viewModel.billingLifecycleObserver)

        viewModel.isProPurchased.observe(this, {
            if (it != BillingDataSource.PurchaseState.INVALID) {
                val isPro = it == BillingDataSource.PurchaseState.PURCHASED
                Log.d(TAG, "Set PRO to: $isPro")
                preferenceHelper.setPro(isPro)
            }
        })
    }

    abstract fun showSnackBar(@StringRes resourceId: Int)

    companion object {
        private const val TAG = "ActivityWithBilling"
    }
}