package com.apps.adrcotfas.goodtime.ui

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ActivityWithBilling : AppCompatActivity() {
    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    abstract fun showSnackBar(@StringRes resourceId: Int)
}