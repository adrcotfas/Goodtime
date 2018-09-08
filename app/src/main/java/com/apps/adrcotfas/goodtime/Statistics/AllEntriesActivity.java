package com.apps.adrcotfas.goodtime.Statistics;

import android.os.Bundle;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

public class AllEntriesActivity extends AppCompatActivity {

    private SessionViewModel mSessionViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this);

        DataBindingUtil.setContentView(this, R.layout.activity_all_entries);

        mSessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);

        if (savedInstanceState == null) {
            Fragment fragment = new AllEntriesFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commitAllowingStateLoss();
        }
    }

    public SessionViewModel getSessionViewModel() {
        return mSessionViewModel;
    }
}
