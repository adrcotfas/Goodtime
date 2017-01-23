package com.apps.adrcotfas.goodtime.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.apps.adrcotfas.goodtime.R;


public class AboutFragment extends PreferenceFragment {

    protected static String ABOUT_FRAGMENT_ID = "com.apps.adrcotfas.goodtime.ABOUT_FRAGMENT_ID";
    protected static int CONTRIBUTORS_FRAGMENT_ID = 1;
    protected static int LICENCES_FRAGMENT_ID = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);

        Preference appVersion = findPreference("about_version");
        appVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("https://github.com/adrcotfas/Goodtime");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });

        Preference contributors = findPreference("about_contributors");
        contributors.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), AboutSubActivity.class);
                intent.putExtra(ABOUT_FRAGMENT_ID, CONTRIBUTORS_FRAGMENT_ID);
                startActivity(intent);
                return true;
            }
        });

        Preference licences = findPreference("about_licences");
        licences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), AboutSubActivity.class);
                intent.putExtra(ABOUT_FRAGMENT_ID, LICENCES_FRAGMENT_ID);
                startActivity(intent);
                return true;
            }
        });

        Preference translate = findPreference("about_translate");
        translate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("https://poeditor.com/join/project/DsP4ey4Kb9");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });

        Preference productTour = findPreference("about_product_tour");
        productTour.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ProductTourActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }
}
