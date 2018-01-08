package com.apps.adrcotfas.goodtime.Settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.BL.PreferenceManager;
import com.apps.adrcotfas.goodtime.BL.TimerState;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

public class SettingsFragment extends PreferenceFragmentCompatDividers {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        findPreference(PreferenceManager.ENABLE_LONG_BREAK).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                findPreference(PreferenceManager.LONG_BREAK_DURATION).setVisible((Boolean) newValue);
                findPreference(PreferenceManager.SESSIONS_BEFORE_LONG_BREAK).setVisible((Boolean) newValue);
                return true;
            }
        });
        findPreference(PreferenceManager.ENABLE_RINGTONE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                findPreference(PreferenceManager.RINGTONE_WORK).setVisible((Boolean) newValue);
                findPreference(PreferenceManager.RINGTONE_BREAK).setVisible((Boolean) newValue);
                return true;
            }
        });
        findPreference(PreferenceManager.WORK_DURATION).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (GoodtimeApplication.getInstance().getCurrentSession().getTimerState().getValue()
                        == TimerState.INACTIVE) {
                    GoodtimeApplication.getInstance().getBus().send(new Constants.WorkDurationUpdatedEvent());
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
