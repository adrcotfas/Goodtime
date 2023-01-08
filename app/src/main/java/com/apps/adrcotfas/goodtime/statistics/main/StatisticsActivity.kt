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
package com.apps.adrcotfas.goodtime.statistics.main

import com.apps.adrcotfas.goodtime.util.UpgradeDialogHelper.Companion.launchUpgradeDialog
import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog.Companion.newInstance
import dagger.hilt.android.AndroidEntryPoint
import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog.OnLabelSelectedListener
import com.apps.adrcotfas.goodtime.main.LabelsViewModel
import javax.inject.Inject
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import android.os.Bundle
import com.apps.adrcotfas.goodtime.util.ThemeHelper
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import androidx.core.view.MenuItemCompat
import android.content.res.ColorStateList
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.databinding.StatisticsActivityMainBinding
import com.apps.adrcotfas.goodtime.statistics.all_sessions.AddEditEntryDialog
import com.apps.adrcotfas.goodtime.statistics.all_sessions.AllSessionsFragment
import com.apps.adrcotfas.goodtime.util.showOnce

@AndroidEntryPoint
class StatisticsActivity : AppCompatActivity(), OnLabelSelectedListener {

    private val labelsViewModel: LabelsViewModel by viewModels()
    private var mMenuItemCrtLabel: MenuItem? = null
    private var mIsMainView = false

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.setTheme(this, preferenceHelper.isAmoledTheme())
        val binding: StatisticsActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.statistics_activity_main)
        setSupportActionBar(binding.toolbarWrapper.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        labelsViewModel.crtExtendedLabel.observe(
            this
        ) { refreshCurrentLabel() }
        mIsMainView = false
        toggleStatisticsView()

        // dismiss at orientation changes
        dismissDialogs()
    }

    private fun dismissDialogs() {
        val fragmentManager = supportFragmentManager
        val dialogSelectLabel =
            fragmentManager.findFragmentByTag(DIALOG_SELECT_LABEL_TAG) as DialogFragment?
        dialogSelectLabel?.dismiss()
        val dialogDate =
            fragmentManager.findFragmentByTag(DIALOG_DATE_PICKER_TAG) as DialogFragment?
        dialogDate?.dismiss()
        val dialogTime =
            fragmentManager.findFragmentByTag(DIALOG_TIME_PICKER_TAG) as DialogFragment?
        dialogTime?.dismiss()
    }

    private fun refreshCurrentLabel() {
        if (labelsViewModel.crtExtendedLabel.value != null && mMenuItemCrtLabel != null) {
            MenuItemCompat.setIconTintList(
                mMenuItemCrtLabel!!,
                ColorStateList.valueOf(
                    ThemeHelper.getColor(
                        this,
                        labelsViewModel.crtExtendedLabel.value!!.colorId
                    )
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_statistics_main, menu)
        mMenuItemCrtLabel = menu.findItem(R.id.action_select_label)
        refreshCurrentLabel()
        menu.findItem(R.id.action_view_list).icon = ContextCompat.getDrawable(
            this, if (mIsMainView) R.drawable.ic_list else R.drawable.ic_trending
        )
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragmentManager = supportFragmentManager
        when (item.itemId) {
            R.id.action_add -> if (preferenceHelper.isPro()) {
                val newFragment = AddEditEntryDialog.newInstance(null)
                newFragment.showOnce(fragmentManager, DIALOG_ADD_ENTRY_TAG)
            } else {
                launchUpgradeDialog(supportFragmentManager)
            }
            R.id.action_select_label -> newInstance(
                this,
                labelsViewModel.crtExtendedLabel.value!!.title,
                true
            )
                .showOnce(fragmentManager, DIALOG_SELECT_LABEL_TAG)
            R.id.action_view_list -> toggleStatisticsView()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleStatisticsView() {
        mIsMainView = !mIsMainView
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment,
                if (mIsMainView) StatisticsFragment() else AllSessionsFragment()
            )
            .commitAllowingStateLoss()
    }

    override fun onLabelSelected(label: Label) {
        labelsViewModel.crtExtendedLabel.value = label
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    companion object {
        const val DIALOG_ADD_ENTRY_TAG = "dialogAddEntry"
        const val DIALOG_SELECT_LABEL_TAG = "dialogSelectLabel"
        const val DIALOG_DATE_PICKER_TAG = "datePickerDialog"
        const val DIALOG_TIME_PICKER_TAG = "timePickerDialog"
    }
}