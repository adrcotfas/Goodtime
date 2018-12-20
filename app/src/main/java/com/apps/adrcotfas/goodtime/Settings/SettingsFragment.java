package com.apps.adrcotfas.goodtime.Settings;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.takisoft.preferencex.ColorPickerPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.DISABLE_SOUND_AND_VIBRATION;

public class SettingsFragment extends PreferenceFragmentCompat  implements ActivityCompat.OnRequestPermissionsResultCallback {

    // general
    private ListPreference mPrefProfile;
    private ProperSeekBarPreference mPrefWorkDuration;
    private ProperSeekBarPreference mPrefBreakDuration;
    private SwitchPreference mPrefEnableLongBreak;
    private ProperSeekBarPreference mPrefLongBreakDuration;
    private ProperSeekBarPreference mPrefSessionsBeforeLongBreak;

    private ColorPickerPreference mPrefTheme;

    private SwitchPreference mPrefEnableRingtone;
    private CheckBoxPreference mPrefDisableSoundCheckbox;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        // general
        mPrefProfile = (ListPreference) findPreference(PreferenceHelper.PROFILE);
        mPrefProfile.setOnPreferenceChangeListener((preference, newValue) -> {
            switchProfile((CharSequence) newValue);
            return true;
        });
        mPrefWorkDuration = (ProperSeekBarPreference) findPreference(PreferenceHelper.WORK_DURATION);
        mPrefBreakDuration = (ProperSeekBarPreference) findPreference(PreferenceHelper.BREAK_DURATION);
        mPrefEnableLongBreak = (SwitchPreference) findPreference(PreferenceHelper.ENABLE_LONG_BREAK);
        toggleLongBreakPreference(mPrefEnableLongBreak.isChecked());
        mPrefEnableLongBreak.setOnPreferenceChangeListener((preference, newValue) -> {
            toggleLongBreakPreference((Boolean) newValue);
            return true;
        });
        mPrefLongBreakDuration = (ProperSeekBarPreference) findPreference(PreferenceHelper.LONG_BREAK_DURATION);
        mPrefSessionsBeforeLongBreak = (ProperSeekBarPreference) findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK);

        mPrefTheme = (ColorPickerPreference) findPreference(PreferenceHelper.THEME);
        mPrefTheme.setOnPreferenceChangeListener((preference, newValue) -> {
            if (mPrefTheme.getColor() != (int) newValue) {
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
            return true;
        });

        mPrefEnableRingtone = (SwitchPreference) findPreference(PreferenceHelper.ENABLE_RINGTONE);
        mPrefEnableRingtone.setVisible(true);
        toggleEnableRingtonePreference(mPrefEnableRingtone.isChecked());
        mPrefEnableRingtone.setOnPreferenceChangeListener((preference, newValue) -> {
            toggleEnableRingtonePreference((Boolean) newValue);
            return true;
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
        findPreference(PreferenceHelper.ENABLE_CONTINUOUS_MODE).setOnPreferenceChangeListener((preference, newValue) -> {
            final CheckBoxPreference pref = (CheckBoxPreference) findPreference(PreferenceHelper.INSISTENT_RINGTONE);
            if (pref.isChecked()) {
                pref.setChecked(false);
            }
            return true;
        });

        findPreference(PreferenceHelper.INSISTENT_RINGTONE).setOnPreferenceChangeListener((preference, newValue) -> {
            final CheckBoxPreference pref = (CheckBoxPreference) findPreference(PreferenceHelper.ENABLE_CONTINUOUS_MODE);
            if (pref.isChecked()) {
                pref.setChecked(false);
            }
            return true;
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPrefDisableSoundCheckbox = (CheckBoxPreference)
                findPreference(DISABLE_SOUND_AND_VIBRATION);
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

    private void updateDisableSoundCheckBoxSummary(boolean notificationPolicyAccessGranted) {
        if (notificationPolicyAccessGranted) {
            mPrefDisableSoundCheckbox.setSummary("");
        } else {
            mPrefDisableSoundCheckbox.setSummary("Click to grant permission");
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
        findPreference(PreferenceHelper.RINGTONE_WORK).setVisible(newValue);
        findPreference(PreferenceHelper.RINGTONE_BREAK).setVisible(newValue);
    }

    private void toggleLongBreakPreference(Boolean newValue) {
        findPreference(PreferenceHelper.LONG_BREAK_DURATION).setVisible(newValue);
        findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK).setVisible(newValue);
    }
}
