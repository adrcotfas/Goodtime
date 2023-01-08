/*
 * Copyright 2016-2019 Adrian Cotfas
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
package com.apps.adrcotfas.goodtime.main

import com.apps.adrcotfas.goodtime.util.UpgradeDialogHelper.Companion.launchUpgradeDialog
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import androidx.core.widget.NestedScrollView
import javax.inject.Inject
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.content.Intent
import android.view.*
import com.apps.adrcotfas.goodtime.BuildConfig
import com.apps.adrcotfas.goodtime.labels.AddEditLabelActivity
import com.apps.adrcotfas.goodtime.settings.SettingsActivity
import com.apps.adrcotfas.goodtime.statistics.main.StatisticsActivity
import com.apps.adrcotfas.goodtime.backup.BackupFragment
import com.apps.adrcotfas.goodtime.about.AboutActivity
import com.apps.adrcotfas.goodtime.databinding.DrawerMainBinding

@AndroidEntryPoint
class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {

    private lateinit var navigationView: NavigationView
    private lateinit var layout: NestedScrollView

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: DrawerMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.drawer_main, container, false)
        if (preferenceHelper.isPro() && !BuildConfig.F_DROID) {
            binding.separator1.visibility = View.GONE
            binding.upgrade.visibility = View.GONE
        } else {
            binding.upgrade.setOnClickListener {
                launchUpgradeDialog(requireActivity().supportFragmentManager)
                if (dialog != null) {
                    dialog!!.dismiss()
                }
            }
        }
        val window = dialog!!.window
        window!!.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        navigationView = binding.navigationView
        layout = binding.layout
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            layout.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val view = view
        view?.post {
            val parent = view.parent as View
            val params =
                parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as BottomSheetBehavior<*>?
            if (bottomSheetBehavior != null) {
                bottomSheetBehavior.peekHeight = view.measuredHeight
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.edit_labels -> if (preferenceHelper.isPro()) {
                    val intent = Intent(activity, AddEditLabelActivity::class.java)
                    startActivity(intent)
                } else {
                    launchUpgradeDialog(requireActivity().supportFragmentManager)
                }
                R.id.action_settings -> {
                    val settingsIntent = Intent(activity, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                }
                R.id.action_statistics -> {
                    val statisticsIntent = Intent(activity, StatisticsActivity::class.java)
                    startActivity(statisticsIntent)
                }
                R.id.action_backup -> if (preferenceHelper.isPro()) {
                    val fragmentManager = parentFragmentManager
                    BackupFragment().show(fragmentManager, "")
                } else {
                    launchUpgradeDialog(requireActivity().supportFragmentManager)
                }
                R.id.action_about -> {
                    val aboutIntent = Intent(activity, AboutActivity::class.java)
                    startActivity(aboutIntent)
                }
            }
            if (dialog != null) {
                dialog!!.dismiss()
            }
            false
        }
    }
}