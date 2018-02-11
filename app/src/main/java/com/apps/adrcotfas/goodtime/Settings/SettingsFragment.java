package com.apps.adrcotfas.goodtime.Settings;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.DISABLE_SOUND_AND_VIBRATION;

public class SettingsFragment extends PreferenceFragmentCompatDividers implements ActivityCompat.OnRequestPermissionsResultCallback{

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
        toggleEnableRingtonePreference(enableRingtonePref.isChecked());

        enableRingtonePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                toggleEnableRingtonePreference((Boolean) newValue);
                return true;
            }
        });

        SwitchPreference enableVibrationPref = (SwitchPreference) findPreference(PreferenceHelper.ENABLE_VIBRATE);
        toggleEnableVibratePreference(enableVibrationPref.isChecked());

        enableVibrationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                toggleEnableVibratePreference((Boolean) newValue);
                return true;
            }
        });
        findPreference(PreferenceHelper.VIBRATE_PATTERN).setSummary(PreferenceHelper.getVibrationPattern());

        findPreference(PreferenceHelper.THEME).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (getActivity() != null){
                    getActivity().recreate();
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

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference.getKey().equals("pref_vibrate_pattern"))
        {
            dialogFragment = new VibrationPreferenceDialogFragmentCompat();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null)
        {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
        }
        else
        {
            super.onDisplayPreferenceDialog(preference);
        }
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
    private boolean isNotificationPolicyAccessGranted()  {
        NotificationManager notificationManager = (NotificationManager)
                getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }

    private void switchProfile(CharSequence newValue) {
        if (newValue.equals(getResources().getText(R.string.pref_profile_pomodoro))) {

            SeekBarPreferenceCompat workDurationPref =
                    (SeekBarPreferenceCompat) findPreference(PreferenceHelper.WORK_DURATION);
            workDurationPref.setCurrentValue(Constants.DEFAULT_WORK_DURATION_POMODORO);

            SeekBarPreferenceCompat breakDurationPref =
                    (SeekBarPreferenceCompat) findPreference(PreferenceHelper.BREAK_DURATION);
            breakDurationPref.setCurrentValue(Constants.DEFAULT_BREAK_DURATION_POMODORO);

            SwitchPreference enableLongBreakPref =
                    (SwitchPreference) findPreference(PreferenceHelper.ENABLE_LONG_BREAK);
            enableLongBreakPref.setChecked(true);
            toggleLongBreakPreference(true);

            SeekBarPreferenceCompat longBreakDurationPref =
                    (SeekBarPreferenceCompat) findPreference(PreferenceHelper.LONG_BREAK_DURATION);
            longBreakDurationPref.setCurrentValue(Constants.DEFAULT_LONG_BREAK_DURATION);

            SeekBarPreferenceCompat sessionsBeforeLongBreakPref =
                    (SeekBarPreferenceCompat) findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK);
            sessionsBeforeLongBreakPref.setCurrentValue(Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK);

        } else if (newValue.equals(getResources().getText(R.string.pref_profile_5217))) {

            SeekBarPreferenceCompat workDurationPref =
                    (SeekBarPreferenceCompat) findPreference(PreferenceHelper.WORK_DURATION);
            workDurationPref.setCurrentValue(Constants.DEFAULT_WORK_DURATION_5217);

            SeekBarPreferenceCompat breakDurationPref =
                    (SeekBarPreferenceCompat) findPreference(PreferenceHelper.BREAK_DURATION);
            breakDurationPref.setCurrentValue(Constants.DEFAULT_BREAK_DURATION_5217);

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

    private void toggleEnableVibratePreference(Boolean newValue) {
        findPreference(PreferenceHelper.VIBRATE_PATTERN).setVisible(newValue);
    }

    private void toggleLongBreakPreference(Boolean newValue) {
        findPreference(PreferenceHelper.LONG_BREAK_DURATION).setVisible(newValue);
        findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK).setVisible(newValue);
    }
}
