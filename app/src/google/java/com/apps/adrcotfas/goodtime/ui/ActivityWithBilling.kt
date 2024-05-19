/*
 * Copyright 2021-2024 Adrian Cotfas
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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.apps.adrcotfas.goodtime.billing.BillingViewModel
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class ActivityWithBilling : AppCompatActivity() {
    private val viewModel: BillingViewModel by viewModels()

    private var appUpdateManager: AppUpdateManager? = null
    private val updateType = AppUpdateType.IMMEDIATE
    private var updateInfo: AppUpdateInfo? = null

    override fun onResume() {
        super.onResume()
        appUpdateManager?.let {
            it.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    Log.i(TAG, "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS")
                    startUpdate(info)
                }
            }
        }
        viewModel.billingConnectionState.observe(this) {
            Log.i(TAG, "billingConnectionState: $it")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdate()
    }

    private fun checkForUpdate() {
        Log.i(TAG, "Checking for update")
        appUpdateManager?.let {
            it.appUpdateInfo.addOnSuccessListener { info ->
                val isUpdateAvailable =
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val isUpdateAllowed = info.isImmediateUpdateAllowed
                Log.i(TAG, "isUpdateAvailable: $isUpdateAvailable, isUpdateAllowed: $isUpdateAllowed")
                if (isUpdateAvailable && isUpdateAllowed) {
                    startUpdate(info)
                }
            }
        }
    }

    private fun startUpdate(info: AppUpdateInfo) {
        Log.i(TAG, "Starting update")
        updateInfo = info
        appUpdateManager?.startUpdateFlowForResult(
            info,
            this,
            AppUpdateOptions.newBuilder(updateType).setAllowAssetPackDeletion(true).build(),
            UPDATE_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "Update flow failed! Result code: $resultCode")
            }
        }
    }

    companion object {
        private const val TAG = "ActivityWithBilling"
        private const val UPDATE_REQUEST_CODE = 100
    }
}