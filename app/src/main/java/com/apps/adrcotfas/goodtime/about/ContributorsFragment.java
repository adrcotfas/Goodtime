package com.apps.adrcotfas.goodtime.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.apps.adrcotfas.goodtime.R;

import static android.content.Intent.ACTION_VIEW;

public class ContributorsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_contributors);
        addLinks();
    }

    private void addLinks() {
        findPreference("fdw").setOnPreferenceClickListener(
                createPreferenceClickListener("https://github.com/fdw")
        );
        findPreference("wolfgang42").setOnPreferenceClickListener(
                createPreferenceClickListener("https://github.com/wolfgang42")
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
