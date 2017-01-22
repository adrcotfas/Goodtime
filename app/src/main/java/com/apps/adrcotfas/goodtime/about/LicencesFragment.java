package com.apps.adrcotfas.goodtime.about;

import android.os.Bundle;
import com.apps.adrcotfas.goodtime.R;

public class LicencesFragment extends AbstractContributorsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_licences);
        addLinks();
    }

    private void addLinks() {
        findPreference("material-intro").setOnPreferenceClickListener(
                createPreferenceClickListener("https://github.com/HeinrichReimer/material-intro")
        );
        findPreference("seekbar-pref").setOnPreferenceClickListener(
                createPreferenceClickListener("https://github.com/MrBIMC/MaterialSeekBarPreference")
        );
    }
}
