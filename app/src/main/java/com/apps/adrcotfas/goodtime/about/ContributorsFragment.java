package com.apps.adrcotfas.goodtime.about;

import android.os.Bundle;
import com.apps.adrcotfas.goodtime.R;

public class ContributorsFragment extends AbstractContributorsFragment {

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
}
