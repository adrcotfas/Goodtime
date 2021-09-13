/*
 * Copyright 2020-2021 Adrian Cotfas
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

package com.apps.adrcotfas.goodtime.ui.upgrade_dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.databinding.DialogUpgradeBinding
import com.apps.adrcotfas.goodtime.util.showOnce
import com.apps.adrcotfas.goodtime.viewmodel.MakePurchasesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpgradeDialog
    : DialogFragment(){

    private lateinit var binding : DialogUpgradeBinding
    private val viewModel : MakePurchasesViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(requireActivity()), R.layout.dialog_upgrade, null, false)

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

        viewModel.canBuyPro().observe(this, {
            binding.buttonPro.isEnabled = it
        })

        binding.buttonPro.setOnClickListener {
            viewModel.buyPro(requireActivity())
            dismiss()
        }

        val builder = AlertDialog.Builder(requireActivity())
                .setView(binding.root)
                .setTitle("")
        return builder.create()
    }

    companion object {
        fun showNewInstance(fragmentManager: FragmentManager) {
                val dialog = UpgradeDialog()
                dialog.showOnce(fragmentManager, UpgradeDialog::class.java.simpleName)
        }
    }
}