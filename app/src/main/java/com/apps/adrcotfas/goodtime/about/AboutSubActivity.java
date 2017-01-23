package com.apps.adrcotfas.goodtime.about;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.apps.adrcotfas.goodtime.R;

import static com.apps.adrcotfas.goodtime.about.AboutFragment.ABOUT_FRAGMENT_ID;
import static com.apps.adrcotfas.goodtime.about.AboutFragment.CONTRIBUTORS_FRAGMENT_ID;
import static com.apps.adrcotfas.goodtime.about.AboutFragment.LICENCES_FRAGMENT_ID;

public class AboutSubActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_layout);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int fragmentId = bundle.getInt(ABOUT_FRAGMENT_ID);
            if (fragmentId == CONTRIBUTORS_FRAGMENT_ID) {
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new ContributorsFragment(), "ContributorsFragment")
                        .commit();
                setTitle(R.string.about_contributors);
            } else if (fragmentId == LICENCES_FRAGMENT_ID) {
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new LicencesFragment(), "LicencesFragment")
                        .commit();
                setTitle(R.string.about_licences);
            }
        }
    }
}