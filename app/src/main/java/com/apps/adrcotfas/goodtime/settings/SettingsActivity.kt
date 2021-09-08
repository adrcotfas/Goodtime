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
package com.apps.adrcotfas.goodtime.settings

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.apps.adrcotfas.goodtime.util.ThemeHelper
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.databinding.GenericMainBinding

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.setTheme(this, preferenceHelper.isAmoledTheme())
        val binding: GenericMainBinding =
            DataBindingUtil.setContentView(this, R.layout.generic_main)
        binding.layout.alpha = 0f
        binding.layout.animate().alpha(1f).duration = 100
        setSupportActionBar(binding.toolbarWrapper.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        if (savedInstanceState == null) {
            val fragment = SettingsFragment()
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragment, fragment)
            ft.commitAllowingStateLoss()
        }
    }

    override fun onAttachedToWindow() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }
}