package com.apps.adrcotfas.goodtime.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.apps.adrcotfas.goodtime.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_layout);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment(), "SettingsFragment")
                .commit();
    }
}