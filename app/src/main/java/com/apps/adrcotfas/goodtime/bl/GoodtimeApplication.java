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

package com.apps.adrcotfas.goodtime.bl;
import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.apps.adrcotfas.goodtime.settings.PreferenceHelper;
import com.apps.adrcotfas.goodtime.settings.reminders.ReminderHelper;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class GoodtimeApplication extends Application {

    @Inject ReminderHelper reminderHelper;
    @Inject PreferenceHelper preferenceHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
        preferenceHelper.migratePreferences();
    }
}