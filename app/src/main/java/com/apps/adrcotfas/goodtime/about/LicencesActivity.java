package com.apps.adrcotfas.goodtime.about;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.apps.adrcotfas.goodtime.R;


public class LicencesActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_source_licences);
        getFragmentManager().beginTransaction()
                            .replace(R.id.licences_content, new ContributorsFragment())
                            .commit();

    }
}
