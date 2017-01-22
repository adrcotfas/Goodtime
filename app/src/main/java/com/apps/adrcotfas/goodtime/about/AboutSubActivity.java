package com.apps.adrcotfas.goodtime.about;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.apps.adrcotfas.goodtime.R;

import static com.apps.adrcotfas.goodtime.about.AboutMainActivity.ABOUT_FRAGMENT_ID;
import static com.apps.adrcotfas.goodtime.about.AboutMainActivity.CONTRIBUTORS_FRAGMENT_ID;
import static com.apps.adrcotfas.goodtime.about.AboutMainActivity.LICENCES_FRAGMENT_ID;


public class AboutSubActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_source_licences);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int fragmentId = bundle.getInt(ABOUT_FRAGMENT_ID);
            if (fragmentId == CONTRIBUTORS_FRAGMENT_ID) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.licences_content, new ContributorsFragment())
                        .commit();
                setTitle(R.string.about_contributors);
            } else if (fragmentId == LICENCES_FRAGMENT_ID) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.licences_content, new LicencesFragment())
                        .commit();
                setTitle(R.string.about_licences);
            }
        }
    }
}
