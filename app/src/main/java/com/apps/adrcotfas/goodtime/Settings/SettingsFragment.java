package com.apps.adrcotfas.goodtime.Settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.R;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsFragment extends PreferenceFragmentCompatDividers {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        findPreference("ENABLE_LONG_BREAK").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                findPreference("LONG_BREAK_DURATION").setVisible((Boolean) newValue);
                findPreference("SESSIONS_BEFORE_LONG_BREAK").setVisible((Boolean) newValue);
                return true;
            }
        });
        findPreference("ENABLE_RINGTONE").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                findPreference("RINGTONE_WORK").setVisible((Boolean) newValue);
                findPreference("RINGTONE_BREAK").setVisible((Boolean) newValue);
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
