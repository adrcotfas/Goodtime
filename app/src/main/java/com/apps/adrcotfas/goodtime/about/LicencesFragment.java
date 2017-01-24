package com.apps.adrcotfas.goodtime.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.apps.adrcotfas.goodtime.R;

import static android.content.Intent.ACTION_VIEW;

public class LicencesFragment extends PreferenceFragment {

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

    protected Preference.OnPreferenceClickListener createPreferenceClickListener(
            final String uriString
    ) {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        };
    }
}
