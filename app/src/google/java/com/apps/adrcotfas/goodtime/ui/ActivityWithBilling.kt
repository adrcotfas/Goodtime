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

import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.apps.adrcotfas.goodtime.billing.BillingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class ActivityWithBilling : AppCompatActivity() {
    private val viewModel: BillingViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.billingConnectionState.observe(this) {
            Log.i(TAG, "billingConnectionState: $it")
        }
    }
    companion object {
        private const val TAG = "ActivityWithBilling"
    }
}