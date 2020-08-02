/*
 * Copyright 2020 Adrian Cotfas
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

package com.apps.adrcotfas.goodtime.upgrade

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE
import com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE
import com.anjlab.android.iab.v3.TransactionDetails
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.Util.Constants
import com.apps.adrcotfas.goodtime.databinding.DialogUpgradeBinding

class UpgradeDialog
    : DialogFragment(), BillingProcessor.IBillingHandler {

    private var readyToPurchase = false
    private lateinit var billingProcessor : BillingProcessor
    private lateinit var binding : DialogUpgradeBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isOpen = true
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(requireActivity()), R.layout.dialog_upgrade, null, false)
        billingProcessor = BillingProcessor.newBillingProcessor(
                requireActivity(), getString(R.string.licence_key), getString(R.string.merchant_id), this)
        billingProcessor.initialize()

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ExtraFeaturesAdapter(context,
                    listOf(Pair(resources.getString(R.string.update_pro_1), R.drawable.ic_label),
                            Pair(resources.getString(R.string.update_pro_4) + "\n" + resources.getString(R.string.update_pro_5), R.drawable.ic_trending),
                            Pair(resources.getString(R.string.pref_save_custom_profile), R.drawable.ic_clock),
                            Pair(resources.getString(R.string.pref_screen_saver), R.drawable.ic_screen),
                            Pair(resources.getString(R.string.update_pro_6) + "\n" + resources.getString(R.string.update_pro_7), R.drawable.ic_cloud),
                            Pair(resources.getString(R.string.update_pro_10) + "\n"
                                    + resources.getString(R.string.pref_ringtone_insistent) + "\n"
                                    + resources.getString(R.string.pref_one_minute_left_notification), R.drawable.ic_notifications),
                            Pair(resources.getString(R.string.pref_flashing_notification) + " - " + resources.getString(R.string.pref_flashing_notification_summary), R.drawable.ic_flash),
                            Pair(resources.getString(R.string.update_pro_12) + "\n" + resources.getString(R.string.update_pro_13), R.drawable.ic_heart)
                    ))
        }

        binding.buttonPro.setOnClickListener {
            if (!billingProcessor.isOneTimePurchaseSupported) {
                Toast.makeText(
                        requireActivity(),
                        "In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16",
                        Toast.LENGTH_LONG).show()
            }
            if (!readyToPurchase) {
                Toast.makeText(
                        requireActivity(),
                        "Billing not initialized",
                        Toast.LENGTH_LONG).show()
            } else {
                billingProcessor.purchase(requireActivity(), Constants.sku)
                dismiss()
            }
        }

        val builder = AlertDialog.Builder(requireActivity())
                .setView(binding.root)
                .setTitle("")
        return builder.create()
    }

    override fun onBillingInitialized() {
        readyToPurchase = true
        billingProcessor.loadOwnedPurchasesFromGoogle()
        if (billingProcessor.isPurchased(Constants.sku)) {
            // should not happen
            binding.buttonPro.visibility = View.GONE
            PreferenceHelper.setPro(true)
        } else {
            if (billingProcessor.isPurchased(Constants.sku)) {
                binding.buttonPro.visibility = View.GONE
                PreferenceHelper.setPro(true)
            } else {
                PreferenceHelper.setPro(false)
            }

        }
    }

    override fun onPurchaseHistoryRestored() {
        var found = false
        for (sku in billingProcessor.listOwnedProducts()) {
            if (sku == Constants.sku) {
                binding.buttonPro.isEnabled = false
                found = true
            }
        }
        PreferenceHelper.setPro(found)
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        PreferenceHelper.setPro(true)
        binding.buttonPro.isEnabled = false
        dismiss()
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        when (errorCode) {
            BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE -> Toast.makeText(
                    requireActivity(),
                    "Billing API version is not supported for the type requested",
                    Toast.LENGTH_LONG).show()
            BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE -> Toast.makeText(
                    requireActivity(),
                    "Network connection is down",
                    Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        billingProcessor.release()
        isOpen = false
        super.onDestroy()
    }

    companion object {
        private var isOpen = false
        @JvmStatic
        fun showNewInstance(fragmentManager: FragmentManager) {
            if (!isOpen) {
                val dialog = UpgradeDialog()
                dialog.show(fragmentManager, UpgradeDialog::class.java.simpleName)
            }
        }
    }
}