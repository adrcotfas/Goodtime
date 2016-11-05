package com.apps.adrcotfas.goodtime.settings;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.apps.adrcotfas.goodtime.R;

import static com.apps.adrcotfas.goodtime.Preferences.DISABLE_SOUND_AND_VIBRATION;
import static com.apps.adrcotfas.goodtime.Preferences.PREFERENCES_NAME;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupDisableSoundCheckBox();
    }

    private void updateDisableSoundCheckBoxState(boolean notificationPolicyAccessGranted) {
        final CheckBoxPreference disableSoundCheckbox = (CheckBoxPreference)
                findPreference(DISABLE_SOUND_AND_VIBRATION);

        disableSoundCheckbox.setChecked(notificationPolicyAccessGranted);
        updateDisableSoundCheckBoxSummary(notificationPolicyAccessGranted);

        SharedPreferences.Editor edit = disableSoundCheckbox.getEditor();
        edit.putBoolean(DISABLE_SOUND_AND_VIBRATION, notificationPolicyAccessGranted);
        edit.commit();
    }

    private void updateDisableSoundCheckBoxSummary(boolean notificationPolicyAccessGranted) {
        final CheckBoxPreference disableSoundCheckbox = (CheckBoxPreference)
                findPreference(DISABLE_SOUND_AND_VIBRATION);
        if (notificationPolicyAccessGranted) {
            disableSoundCheckbox.setSummary("");
        } else {
            disableSoundCheckbox.setSummary("Click to grant permission");
        }
    }
    private void setupDisableSoundCheckBox() {
        final CheckBoxPreference disableSoundCheckbox = (CheckBoxPreference)
                findPreference(DISABLE_SOUND_AND_VIBRATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isNotificationPolicyAccessGranted()) {
            disableSoundCheckbox.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            requestNotificationPolicyAccess();
                            if(!isNotificationPolicyAccessGranted()) {
                                updateDisableSoundCheckBoxState(false);
                            }
                            return false;
                        }
                    }
            );
        } else {
            updateDisableSoundCheckBoxSummary(true);
        }
    }

    private void requestNotificationPolicyAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.
                    ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isNotificationPolicyAccessGranted()  {
        NotificationManager notificationManager = (NotificationManager)
                getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }
}