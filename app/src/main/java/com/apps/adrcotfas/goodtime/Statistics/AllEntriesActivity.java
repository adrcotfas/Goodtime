package com.apps.adrcotfas.goodtime.Statistics;

import android.os.Bundle;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

//TODO: delete this and use only StatisticsActivity
public class AllEntriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this);

        DataBindingUtil.setContentView(this, R.layout.activity_all_entries);

        if (savedInstanceState == null) {
            Fragment fragment = new AllEntriesFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, fragment);
            ft.commitAllowingStateLoss();
        }
    }
}
