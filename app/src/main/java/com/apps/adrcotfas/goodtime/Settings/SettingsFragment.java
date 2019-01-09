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

package com.apps.adrcotfas.goodtime.Settings;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Upgrade.UpgradeActivity;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.takisoft.preferencex.ColorPickerPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;
import com.takisoft.preferencex.RingtonePreference;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.DISABLE_SOUND_AND_VIBRATION;

public class SettingsFragment extends PreferenceFragmentCompat implements ActivityCompat.OnRequestPermissionsResultCallback {

    private ProperSeekBarPreference mPrefWorkDuration;
    private ProperSeekBarPreference mPrefBreakDuration;
    private SwitchPreference mPrefEnableLongBreak;
    private ProperSeekBarPreference mPrefLongBreakDuration;
    private ProperSeekBarPreference mPrefSessionsBeforeLongBreak;

    private CheckBoxPreference mPrefDisableSoundCheckbox;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        mPrefDisableSoundCheckbox = findPreference(DISABLE_SOUND_AND_VIBRATION);

        setupProfile();
        setupDurations();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        setupTheme();
        setupRingtone();
        setupScreensaver();
        setupAutoStartSessionVsInsistentNotification();
        setupDisableSoundCheckBox();

        final Preference disableBatteryOptimizationPref = findPreference(PreferenceHelper.DISABLE_BATTERY_OPTIMIZATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations()) {
            disableBatteryOptimizationPref.setVisible(true);
            disableBatteryOptimizationPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(intent);
                return true;
            });
        } else {
            disableBatteryOptimizationPref.setVisible(false);
        }
    }

    private void setupDurations() {
        mPrefWorkDuration = findPreference(PreferenceHelper.WORK_DURATION);
        mPrefBreakDuration = findPreference(PreferenceHelper.BREAK_DURATION);
        mPrefEnableLongBreak = findPreference(PreferenceHelper.ENABLE_LONG_BREAK);
        toggleLongBreakPreference(mPrefEnableLongBreak.isChecked());
        mPrefEnableLongBreak.setOnPreferenceChangeListener((preference, newValue) -> {
            toggleLongBreakPreference((Boolean) newValue);
            return true;
        });
        mPrefLongBreakDuration = findPreference(PreferenceHelper.LONG_BREAK_DURATION);
        mPrefSessionsBeforeLongBreak = findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK);
    }

    private void setupProfile() {
        ListPreference mPrefProfile = findPreference(PreferenceHelper.PROFILE);
        mPrefProfile.setOnPreferenceChangeListener((preference, newValue) -> {
            switchProfile((CharSequence) newValue);
            return true;
        });
    }

    private void setupAutoStartSessionVsInsistentNotification() {
        // Continuous mode versus insistent notification
        findPreference(PreferenceHelper.AUTO_START_WORK).setOnPreferenceChangeListener((preference, newValue) -> {
            final CheckBoxPreference pref = findPreference(PreferenceHelper.INSISTENT_RINGTONE);
            if (pref.isChecked()) {
                pref.setChecked(false);
            }
            return true;
        });
        findPreference(PreferenceHelper.AUTO_START_BREAK).setOnPreferenceChangeListener((preference, newValue) -> {
            final CheckBoxPreference pref = findPreference(PreferenceHelper.INSISTENT_RINGTONE);
            if (pref.isChecked()) {
                pref.setChecked(false);
            }
            return true;
        });

        final CheckBoxPreference insistentRingPref = findPreference(PreferenceHelper.INSISTENT_RINGTONE);
        insistentRingPref.setOnPreferenceClickListener(PreferenceHelper.isPro() ? null : preference -> {
            UpgradeActivity.launchUpgradeActivity(getActivity());
            insistentRingPref.setChecked(false);
            return true;
        });
        insistentRingPref.setOnPreferenceChangeListener(PreferenceHelper.isPro() ? null : (preference, newValue) -> {
            final CheckBoxPreference p1 = findPreference(PreferenceHelper.AUTO_START_BREAK);
            if (p1.isChecked()) {
                p1.setChecked(false);
            }
            final CheckBoxPreference p2 = findPreference(PreferenceHelper.AUTO_START_WORK);
            if (p2.isChecked()) {
                p2.setChecked(false);
            }
            return true;
        });
    }

    private void setupRingtone() {
        final SwitchPreference prefEnableRingtone = findPreference(PreferenceHelper.ENABLE_RINGTONE);
        prefEnableRingtone.setVisible(true);
        toggleEnableRingtonePreference(prefEnableRingtone.isChecked());
        prefEnableRingtone.setOnPreferenceChangeListener((preference, newValue) -> {
            toggleEnableRingtonePreference((Boolean) newValue);
            return true;
        });

        final RingtonePreference prefWork = findPreference(PreferenceHelper.RINGTONE_WORK_FINISHED);
        final RingtonePreference prefBreak = findPreference(PreferenceHelper.RINGTONE_BREAK_FINISHED);
        final Preference prefBreakDummy = findPreference(PreferenceHelper.RINGTONE_BREAK_FINISHED_DUMMY);
        prefBreakDummy.setOnPreferenceClickListener(preference -> {
            UpgradeActivity.launchUpgradeActivity(getActivity());
            return true;
        });

        if (PreferenceHelper.isPro()) {
            prefBreakDummy.setVisible(false);
            prefBreak.setVisible(true);
            prefWork.setOnPreferenceChangeListener(null);
        } else {
            prefBreakDummy.setVisible(true);
            prefBreak.setVisible(false);
            prefBreakDummy.setSummary(prefBreak.getSummary());

            prefBreak.setRingtone(prefWork.getRingtone());
            prefWork.setOnPreferenceChangeListener((preference, newValue) -> {
                prefBreak.setRingtone((Uri) newValue);
                prefBreakDummy.setSummary(prefBreak.getSummary());
                return true;
            });
        }
    }

    private void setupScreensaver() {
        final CheckBoxPreference screensaverPref = SettingsFragment.this.findPreference(PreferenceHelper.ENABLE_SCREENSAVER_MODE);
        findPreference(PreferenceHelper.ENABLE_SCREENSAVER_MODE).setOnPreferenceClickListener(PreferenceHelper.isPro() ? null : preference -> {
            UpgradeActivity.launchUpgradeActivity(getActivity());
            screensaverPref.setChecked(false);
            return true;
        });

        findPreference(PreferenceHelper.ENABLE_SCREEN_ON).setOnPreferenceChangeListener((preference, newValue) -> {
            if (!((boolean) newValue)) {
                if (screensaverPref.isChecked()) {
                    screensaverPref.setChecked(false);
                }
            }
            return true;
        });
    }

    private void setupTheme() {
        ColorPickerPreference prefTheme = findPreference(PreferenceHelper.THEME);
        final Preference dummy = findPreference(PreferenceHelper.THEME_DUMMY);

        if (PreferenceHelper.isPro()) {
            prefTheme.setVisible(true);
            dummy.setVisible(false);
        } else {
            prefTheme.setVisible(false);
            dummy.setVisible(true);
        }

        prefTheme.setOnPreferenceChangeListener((preference, newValue) -> {
            if (prefTheme.getColor() != (int) newValue) {
                if (SettingsFragment.this.getActivity() != null) {
                    SettingsFragment.this.getActivity().recreate();
                }
            }
            return true;
        });

        dummy.setOnPreferenceClickListener(preference -> {
            UpgradeActivity.launchUpgradeActivity(getActivity());
            return true;
        });
    }

    private void updateDisableSoundCheckBoxSummary(boolean notificationPolicyAccessGranted) {
        if (notificationPolicyAccessGranted) {
            mPrefDisableSoundCheckbox.setSummary("");
        } else {
            mPrefDisableSoundCheckbox.setSummary(R.string.settings_grant_permission);
        }
    }

    private void setupDisableSoundCheckBox() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNotificationPolicyAccessDenied()) {
            updateDisableSoundCheckBoxSummary(false);
            mPrefDisableSoundCheckbox.setChecked(false);
            mPrefDisableSoundCheckbox.setOnPreferenceClickListener(
                    preference -> {
                        requestNotificationPolicyAccess();
                        return false;
                    }
            );
        } else {
            updateDisableSoundCheckBoxSummary(true);
        }
    }

    private boolean isIgnoringBatteryOptimizations(){
        PowerManager pwrm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(getActivity().getPackageName());
        }
        return true;
    }

    private void requestNotificationPolicyAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNotificationPolicyAccessDenied()) {
            Intent intent = new Intent(android.provider.Settings.
                    ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isNotificationPolicyAccessDenied() {
        NotificationManager notificationManager = (NotificationManager)
                getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        return !notificationManager.isNotificationPolicyAccessGranted();
    }

    private void switchProfile(CharSequence newValue) {
        if (newValue.equals(getResources().getText(R.string.pref_profile_default))) {
            mPrefWorkDuration.setValue(Constants.DEFAULT_WORK_DURATION_DEFAULT);
            mPrefBreakDuration.setValue(Constants.DEFAULT_BREAK_DURATION_DEFAULT);
            mPrefEnableLongBreak.setChecked(false);
            toggleLongBreakPreference(false);
            mPrefLongBreakDuration.setValue(Constants.DEFAULT_LONG_BREAK_DURATION);
            mPrefSessionsBeforeLongBreak.setValue(Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK);
        } else if (newValue.equals(getResources().getText(R.string.pref_profile_5217))) {
            mPrefWorkDuration.setValue(Constants.DEFAULT_WORK_DURATION_5217);
            mPrefBreakDuration.setValue(Constants.DEFAULT_BREAK_DURATION_5217);
            mPrefEnableLongBreak.setChecked(false);
            toggleLongBreakPreference(false);
        }
    }

    private void toggleEnableRingtonePreference(Boolean newValue) {
        findPreference(PreferenceHelper.RINGTONE_WORK_FINISHED).setVisible(newValue);
        findPreference(PreferenceHelper.RINGTONE_BREAK_FINISHED).setVisible(newValue);
    }

    private void toggleLongBreakPreference(Boolean newValue) {
        findPreference(PreferenceHelper.LONG_BREAK_DURATION).setVisible(newValue);
        findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK).setVisible(newValue);
    }
}

