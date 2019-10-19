package com.apps.adrcotfas.goodtime.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.Profile;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.apps.adrcotfas.goodtime.Util.UpgradeActivityHelper.launchUpgradeActivity;

public class DurationsSettingsFragment extends PreferenceFragmentCompat implements ProfilePreference.ProfileChangeListener {

    private ProfilePreference mPrefProfile;
    private ProperSeekBarPreference mPrefWorkDuration;
    private ProperSeekBarPreference mPrefBreakDuration;
    private SwitchPreferenceCompat mPrefEnableLongBreak;
    private ProperSeekBarPreference mPrefLongBreakDuration;
    private ProperSeekBarPreference mPrefSessionsBeforeLongBreak;
    private List<Profile> mProfiles;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setAlpha(0.f);
        view.animate().alpha(1.f).setDuration(150);
        return view;
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_durations, rootKey);
        ProfilesVieModel profilesViewModel = ViewModelProviders.of(getActivity()).get(ProfilesVieModel.class);
        profilesViewModel.getProfiles().observe(this, profiles -> {
            mProfiles = profiles;
            setupProfiles();
        });

        setupDurations();
    }

    private void setupDurations() {
        mPrefWorkDuration = findPreference(PreferenceHelper.WORK_DURATION);
        mPrefWorkDuration.setShowSeekBarValue(true);
        mPrefWorkDuration.setOnPreferenceChangeListener((preference, newValue) -> {
            mPrefProfile.setSummary("");
            return true;
        });
        mPrefBreakDuration = findPreference(PreferenceHelper.BREAK_DURATION);
        mPrefBreakDuration.setShowSeekBarValue(true);
        mPrefBreakDuration.setOnPreferenceChangeListener((preference, newValue) -> {
            mPrefProfile.setSummary("");
            return true;
        });

        mPrefEnableLongBreak = findPreference(PreferenceHelper.ENABLE_LONG_BREAK);
        toggleLongBreakPreference(mPrefEnableLongBreak.isChecked());
        mPrefEnableLongBreak.setOnPreferenceChangeListener((preference, newValue) -> {
            toggleLongBreakPreference((Boolean) newValue);
            mPrefProfile.setSummary("");
            return true;
        });
        mPrefLongBreakDuration = findPreference(PreferenceHelper.LONG_BREAK_DURATION);
        mPrefLongBreakDuration.setShowSeekBarValue(true);
        mPrefLongBreakDuration.setOnPreferenceChangeListener((preference, newValue) -> {
            mPrefProfile.setSummary("");
            return true;
        });
        mPrefSessionsBeforeLongBreak = findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK);
        mPrefSessionsBeforeLongBreak.setShowSeekBarValue(true);
        mPrefSessionsBeforeLongBreak.setOnPreferenceChangeListener((preference, newValue) -> {
            mPrefProfile.setSummary("");
            return true;
        });
    }

    private void setupProfiles() {
        mPrefProfile = findPreference(PreferenceHelper.PROFILE);
        mPrefProfile.attachListener(this);

        ArrayList<CharSequence> profileNames = new ArrayList<>();
        profileNames.add(getResources().getText(R.string.pref_profile_default));
        profileNames.add(getResources().getText(R.string.pref_profile_5217));
        for (Profile p : mProfiles) {
            profileNames.add(p.name);
        }

        mPrefProfile.setEntries(profileNames.toArray(new CharSequence[profileNames.size()]));
        mPrefProfile.setEntryValues(profileNames.toArray(new CharSequence[profileNames.size()]));

        mPrefProfile.setEnabled(true);
    }

    private void toggleLongBreakPreference(Boolean newValue) {
        findPreference(PreferenceHelper.LONG_BREAK_DURATION).setVisible(newValue);
        findPreference(PreferenceHelper.SESSIONS_BEFORE_LONG_BREAK).setVisible(newValue);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ProperSeekBarPreference) {
            ProperSeekBarPreferenceDialog dialog = ProperSeekBarPreferenceDialog.newInstance(preference.getKey());
            dialog.setTargetFragment(this, 0);
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals(PreferenceHelper.SAVE_CUSTOM_PROFILE)) {
            if (PreferenceHelper.isPro()) {
                Profile profile = mPrefEnableLongBreak.isChecked() ? new Profile(
                        "",
                        mPrefWorkDuration.getValue(),
                        mPrefBreakDuration.getValue(),
                        mPrefLongBreakDuration.getValue(),
                        mPrefSessionsBeforeLongBreak.getValue()) : new Profile(
                        "",
                        mPrefWorkDuration.getValue(),
                        mPrefBreakDuration.getValue());
                SaveCustomProfileDialog dialog = SaveCustomProfileDialog.newInstance(
                        PreferenceHelper.PROFILE,
                        getString(R.string.pref_save_custom_profile),
                        profile,
                        mPrefProfile.getValue());
                dialog.setTargetFragment(this, 0);
                dialog.show(getFragmentManager(), null);
            } else {
                launchUpgradeActivity(getActivity());
            }
        } else if (preference.getKey().equals(PreferenceHelper.PROFILE)) {
            ProfileSelectDialog dialog =
                    ProfileSelectDialog.newInstance(PreferenceHelper.PROFILE);
            dialog.setTargetFragment(this, 0);
            dialog.show(getFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onProfileChange(CharSequence newValue) {
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
        } else {
            for (Profile p : mProfiles) {
                if (newValue.equals(p.name)) {
                    mPrefWorkDuration.setValue(p.durationWork);
                    mPrefBreakDuration.setValue(p.durationBreak);
                    mPrefEnableLongBreak.setChecked(p.enableLongBreak);
                    toggleLongBreakPreference(p.enableLongBreak);
                    mPrefLongBreakDuration.setValue(p.durationLongBreak);
                    mPrefSessionsBeforeLongBreak.setValue(p.sessionsBeforeLongBreak);
                    break;
                }
            }
        }
    }
}
