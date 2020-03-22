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

package com.apps.adrcotfas.goodtime.BL;
import android.app.Application;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper;
import com.apps.adrcotfas.goodtime.Settings.reminders.ReminderHelper;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static com.apps.adrcotfas.goodtime.Settings.PreferenceHelper.WORK_DURATION;
import static com.apps.adrcotfas.goodtime.Util.Constants.DEFAULT_WORK_DURATION_DEFAULT;

/**
 * Maintains a global state of the app and the {@link CurrentSession}
 */
public class GoodtimeApplication extends Application {

    private static volatile GoodtimeApplication INSTANCE;
    private static CurrentSessionManager mCurrentSessionManager;
    private static SharedPreferences mPreferences;

    private static ReminderHelper mReminderHelper;

    public static GoodtimeApplication getInstance() {
        return INSTANCE;
    }

    static { AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES); }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        mPreferences = getSharedPreferences(getPackageName() + "_private_preferences", MODE_PRIVATE);
        PreferenceHelper.migratePreferences();

        mCurrentSessionManager = new CurrentSessionManager(this, new CurrentSession(TimeUnit.MINUTES.toMillis(
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getInt(WORK_DURATION, DEFAULT_WORK_DURATION_DEFAULT))));
        mReminderHelper = new ReminderHelper(this);
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSessionManager.getCurrentSession();
    }

    public static CurrentSessionManager getCurrentSessionManager() {
        return mCurrentSessionManager;
    }

    public static SharedPreferences getPrivatePreferences() {
        return mPreferences;
    }

    public ReminderHelper getReminderHelper() {
        return mReminderHelper;
    }
}