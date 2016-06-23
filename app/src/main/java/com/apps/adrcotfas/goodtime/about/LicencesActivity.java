package com.apps.adrcotfas.goodtime.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.apps.adrcotfas.goodtime.R;


public class LicencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_source_licences);
        getFragmentManager().beginTransaction()
                .replace(R.id.licences_content, new ContributorsFragment())
                .commit();

    }

    public static class ContributorsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.about_licences);

            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.licencesToolbar);
            if (toolbar != null) {
                ((LicencesActivity) getActivity()).setSupportActionBar(toolbar);
                ((LicencesActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
                ((LicencesActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            Preference patrickFord = findPreference("patrick_ford");
            patrickFord.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("https://www.freesound.org/people/hykenfreak");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            });

            Preference appRater = findPreference("app_rater");
            appRater.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("https://github.com/delight-im/AppRater");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            });

            Preference materialIntro = findPreference("material-intro");
            materialIntro.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("https://github.com/HeinrichReimer/material-intro");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            });

            Preference seekbar = findPreference("seekbar-pref");
            seekbar.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("https://github.com/MrBIMC/MaterialSeekBarPreference");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            });


            Preference arthurShlain = findPreference("arthur_shlain");
            arthurShlain.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("http://creativecommons.org/licenses/by/3.0/us/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            });

            Preference dominicWhittle = findPreference("dominic_whittle");
            dominicWhittle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("http://creativecommons.org/licenses/by/3.0/us/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            });
            Preference michalKucera = findPreference("michal-kucera");
            michalKucera.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("http://creativecommons.org/licenses/by/3.0/us/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    return true;
                }
            });


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), LicencesActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
