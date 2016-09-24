package com.apps.adrcotfas.goodtime.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.apps.adrcotfas.goodtime.R;

import static android.content.Intent.ACTION_VIEW;

public class ContributorsFragment
        extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_licences);

        addToolbar();
        addLicenses();
    }

    private void addToolbar() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.licencesToolbar);
        if (toolbar != null) {
            LicencesActivity activity = (LicencesActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar()
                    .setHomeButtonEnabled(true);
            activity.getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addLicenses() {
        findPreference("patrick_ford").setOnPreferenceClickListener(
                createPreferenceClickListener("https://www.freesound.org/people/hykenfreak")
        );
        findPreference("fdw").setOnPreferenceClickListener(
                createPreferenceClickListener("https://github.com/fdw")
        );
        findPreference("wolfgang42").setOnPreferenceClickListener(
                createPreferenceClickListener("https://github.com/wolfgang42")
        );
        findPreference("app_rater").setOnPreferenceClickListener(
                createPreferenceClickListener("https://github.com/delight-im/AppRater")
        );
        findPreference("material-intro").setOnPreferenceClickListener(
                createPreferenceClickListener("https://github.com/HeinrichReimer/material-intro")
        );
        findPreference("seekbar-pref").setOnPreferenceClickListener(
                createPreferenceClickListener("https://github.com/MrBIMC/MaterialSeekBarPreference")
        );
        findPreference("arthur_shlain").setOnPreferenceClickListener(
                createPreferenceClickListener("http://creativecommons.org/licenses/by/3.0/us/")
        );
        findPreference("dominic_whittle").setOnPreferenceClickListener(
                createPreferenceClickListener("http://creativecommons.org/licenses/by/3.0/us/")
        );
        findPreference("michal-kucera").setOnPreferenceClickListener(
                createPreferenceClickListener("http://creativecommons.org/licenses/by/3.0/us/")
        );
    }

    private Preference.OnPreferenceClickListener createPreferenceClickListener(
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

    @Override
    public boolean onOptionsItemSelected(
            final MenuItem item
    ) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), LicencesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
