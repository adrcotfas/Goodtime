package com.apps.adrcotfas.goodtime.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;

import com.apps.adrcotfas.goodtime.R;

import static android.content.Intent.ACTION_VIEW;

public abstract class AbstractContributorsFragment
        extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addToolbar();
    }

    private void addToolbar() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.licencesToolbar);
        if (toolbar != null) {
            AboutSubActivity activity = (AboutSubActivity) getActivity();
            //TODO: check if this is needed or it can be implemented as in the settings fragment
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar()
                    .setHomeButtonEnabled(true);
            activity.getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);
        }
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
