/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.Statistics.Main;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Statistics.AllSessions.AddEditEntryDialog;
import com.apps.adrcotfas.goodtime.Statistics.AllSessions.AllSessionsFragment;
import com.apps.adrcotfas.goodtime.Upgrade.UpgradeActivity;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.StatisticsActivityMainBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

public class StatisticsActivity extends AppCompatActivity implements SelectLabelDialog.OnLabelSelectedListener {

    private LabelsViewModel mLabelsViewModel;
    private MenuItem mMenuItemCrtLabel;
    private boolean mIsMainView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);

        ThemeHelper.setTheme(this);
        StatisticsActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.statistics_activity_main);

        setSupportActionBar(binding.toolbarWrapper.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mLabelsViewModel.crtExtendedLabel.observe(this, labelAndColor -> refreshCurrentLabel());

        mIsMainView = false;
        toggleStatisticsView();
    }

    private void refreshCurrentLabel() {
        if (mLabelsViewModel.crtExtendedLabel.getValue() != null && mMenuItemCrtLabel != null) {
            MenuItemCompat.setIconTintList(mMenuItemCrtLabel, ColorStateList.valueOf(mLabelsViewModel.crtExtendedLabel.getValue().color));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_statistics_main, menu);

        mMenuItemCrtLabel = menu.findItem(R.id.action_select_label);
        refreshCurrentLabel();

        menu.findItem(R.id.action_view_list).setIcon(ContextCompat.getDrawable(
                this, mIsMainView ? R.drawable.ic_details : R.drawable.ic_show_list));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.action_add:

                if (PreferenceHelper.isPro()) {
                    AddEditEntryDialog newFragment = new AddEditEntryDialog();
                    newFragment.show(fragmentManager, "");
                } else {
                    UpgradeActivity.launchUpgradeActivity(this);
                }

                // TODO: remove this later
//                for (int i = 0; i < 1000; ++i) {
//                    Session session = new Session(
//                            0,
//                            System.currentTimeMillis(),
//                            42,
//                            null);
//
//                    mSessionViewModel.addSession(session);
//                }

                break;
            case R.id.action_select_label:
                if (PreferenceHelper.isPro()) {
                    SelectLabelDialog.newInstance(this, mLabelsViewModel.crtExtendedLabel.getValue().label, true)
                            .show(fragmentManager, "");
                } else {
                    UpgradeActivity.launchUpgradeActivity(this);
                }

                break;
            case R.id.action_view_list:
                toggleStatisticsView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleStatisticsView() {
        mIsMainView = !mIsMainView;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, mIsMainView ? new StatisticsFragment() : new AllSessionsFragment())
                .commitAllowingStateLoss();
    }

    @Override
    public void onLabelSelected(LabelAndColor labelAndColor) {
        if (labelAndColor != null) {
            mLabelsViewModel.crtExtendedLabel.setValue(labelAndColor);
        } else {
            mLabelsViewModel.crtExtendedLabel.setValue(null);
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
        else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
