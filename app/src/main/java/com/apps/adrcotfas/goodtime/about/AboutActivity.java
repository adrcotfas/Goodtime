package com.apps.adrcotfas.goodtime.about;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.apps.adrcotfas.goodtime.ProductTourActivity;
import com.apps.adrcotfas.goodtime.R;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);
        getFragmentManager().beginTransaction()
                .replace(R.id.about_content, new AboutFragment())
                .commit();
    }

    public static class AboutFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.about);

            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.aboutToolbar);
            if (toolbar != null) {
                ((AboutActivity) getActivity()).setSupportActionBar(toolbar);

                ((AboutActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
                ((AboutActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

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

            Preference licences = findPreference("about_licences");
            licences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), LicencesActivity.class);
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

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }
}
