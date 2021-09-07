/*
 * Copyright 2016-2021 Adrian Cotfas
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
package com.apps.adrcotfas.goodtime.bl

import android.annotation.SuppressLint
import dagger.hilt.android.HiltAndroidApp
import android.app.Application
import android.content.Context
import javax.inject.Inject
import com.apps.adrcotfas.goodtime.settings.reminders.ReminderHelper
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import androidx.appcompat.app.AppCompatDelegate

@HiltAndroidApp
class GoodtimeApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak") // should be fine in this class
        lateinit var context: Context
            private set
    }

    @Inject
    lateinit var reminderHelper: ReminderHelper
    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        preferenceHelper.migratePreferences()
    }
}