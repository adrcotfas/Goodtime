package com.apps.adrcotfas.goodtime.Settings;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreference;
import androidx.core.app.ActivityCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;

import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.DISABLE_SOUND_AND_VIBRATION;
import com.takisoft.preferencex.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat  implements ActivityCompat.OnRequestPermissionsResultCallback {

    CheckBoxPreference disableSoundCheckbox;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        SwitchPreference enableLongBreakPref = (SwitchPreference) findPreference(PreferenceHelper.ENABLE_LONG_BREAK);
        toggleLongBreakPreference(enableLongBreakPref.isChecked());
        enableLongBreakPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                toggleLongBreakPreference((Boolean) newValue);
                return true;
            }
        });

        findPreference(PreferenceHelper.PROFILE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                switchProfile((CharSequence) newValue);
                return true;
            }
        });

        SwitchPreference enableRingtonePref = (SwitchPreference) findPreference(PreferenceHelper.ENABLE_RINGTONE);

        enableRingtonePref.setVisible(true);
        toggleEnableRingtonePreference(enableRingtonePref.isChecked());
        enableRingtonePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                toggleEnableRingtonePreference((Boolean) newValue);
                return true;
            }
        });

        findPreference(PreferenceHelper.THEME).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (getActivity() != null) {
                    getActivity().recreate();
                }
                return true;
            }
        });

        //TODO: handle with IAP
//        final RingtonePreference prefWork = (RingtonePreference) findPreference(PreferenceHelper.RINGTONE_WORK);
//        final RingtonePreference prefBreak = (RingtonePreference) findPreference(PreferenceHelper.RINGTONE_BREAK);
//
//        if (true) {
//            prefBreak.setEnabled(true);
//            prefWork.setOnPreferenceChangeListener(null);
//
//        } else {
//            prefBreak.setEnabled(false);
//            prefBreak.setRingtone(prefWork.getRingtone());
//            prefWork.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    prefBreak.setRingtone((Uri) newValue);
//                    return true;
//                }
//            });
//        }
        // Continuous mode versus insistent notification
        findPreference(PreferenceHelper.ENABLE_CONTINUOUS_MODE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final CheckBoxPreference pref = (CheckBoxPreference) findPreference(PreferenceHelper.INSISTENT_RINGTONE);
                if (pref.isChecked()) {
                    pref.setChecked(false);
                }
                return true;
            }
        });

        findPreference(PreferenceHelper.INSISTENT_RINGTONE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final CheckBoxPreference pref = (CheckBoxPreference) findPreference(PreferenceHelper.ENABLE_CONTINUOUS_MODE);
                if (pref.isChecked()) {
                    pref.setChecked(false);
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        disableSoundCheckbox = (CheckBoxPreference)
                findPreference(DISABLE_SOUND_AND_VIBRATION);
        setupDisableSoundCheckBox();
    }

    private void updateDisableSoundCheckBoxState(boolean notificationPolicyAccessGranted) {
        disableSoundCheckbox.setChecked(notificationPolicyAccessGranted);
        updateDisableSoundCheckBoxSummary(notificationPolicyAccessGranted);
    }

    private void updateDisableSoundCheckBoxSummary(boolean notificationPolicyAccessGranted) {
        if (notificationPolicyAccessGranted) {
            disableSoundCheckbox.setSummary("");
        } else {
            disableSoundCheckbox.setSummary("Click to grant permission");
        }
    }
    private void setupDisableSoundCheckBox() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isNotificationPolicyAccessGranted()) {
            updateDisableSoundCheckBoxState(false);
            disableSoundCheckbox.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            requestNotificationPolicyAccess();
                            return false;
                        }
                    }
            );
        } else {
            updateDisableSoundCheckBoxSummary(true);
        }
    }

    private void requestNotificationPolicyAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.
                    ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isNotificationPolicyAccessGranted() {
        NotificationManager notificationManager = (NotificationManager)
                getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }

    private void switchProfile(CharSequence newValue) {
        if (newValue.equals(getResources().getText(R.string.pref_profile_pomodoro))) {

            ProperSeekBarPreference workDurationPref =
                    (ProperSeekBarPreference) findPreference(PreferenceHelper.WORK_DURATION);
            workDurationPref.setValue(Constants.DEFAULT_WORK_DURATION_POMODORO);

            ProperSeekBarPreference breakDurationPref =
                    (ProperSeekBarPreference) findPreference(PreferenceHelper.BREAK_DURATION);
            breakDurationPref.setValue(Constants.DEFAULT_BREAK_DURATION_POMODORO);

            SwitchPreference enableLongBreakPref =
                    (SwitchPreference) findPreference(PreferenceHelper.ENABLE_LONG_BREAK);
            enableLongBreakPref.setChecked(true);
            toggleLongBreakPreference(true);

            ProperSeekBarPreference longBreakDurationPref =
                    (ProperSeekBarPreference) findPreference(PreferenceHelper.LONG_BREAK_DURATION);
            longBreakDurationPref.setValue(Constants.DEFAULT_LONG_BREAK_DURATION);

            ProperSeekBarPreference sessionsBeforeLongBreakPref =
                    (ProperSeekBarPreference) findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK);
            sessionsBeforeLongBreakPref.setValue(Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK);

        } else if (newValue.equals(getResources().getText(R.string.pref_profile_5217))) {

            ProperSeekBarPreference workDurationPref =
                    (ProperSeekBarPreference) findPreference(PreferenceHelper.WORK_DURATION);
            workDurationPref.setValue(Constants.DEFAULT_WORK_DURATION_5217);

            ProperSeekBarPreference breakDurationPref =
                    (ProperSeekBarPreference) findPreference(PreferenceHelper.BREAK_DURATION);
            breakDurationPref.setValue(Constants.DEFAULT_BREAK_DURATION_5217);

            SwitchPreference enableLongBreakPref =
                    (SwitchPreference) findPreference(PreferenceHelper.ENABLE_LONG_BREAK);
            enableLongBreakPref.setChecked(false);
            toggleLongBreakPreference(false);
        }
    }

    private void toggleEnableRingtonePreference(Boolean newValue) {
        findPreference(PreferenceHelper.RINGTONE_WORK).setVisible(newValue);
        findPreference(PreferenceHelper.RINGTONE_BREAK).setVisible(newValue);
    }

    private void toggleLongBreakPreference(Boolean newValue) {
        findPreference(PreferenceHelper.LONG_BREAK_DURATION).setVisible(newValue);
        findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK).setVisible(newValue);
    }
}
