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

package com.apps.adrcotfas.goodtime.Main;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.apps.adrcotfas.goodtime.About.AboutActivity;
import com.apps.adrcotfas.goodtime.AddEditLabels.AddEditLabelActivity;
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper;
import com.apps.adrcotfas.goodtime.Backup.BackupFragment;
import com.apps.adrcotfas.goodtime.BuildConfig;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Settings.SettingsActivity;
import com.apps.adrcotfas.goodtime.Statistics.Main.StatisticsActivity;
import com.apps.adrcotfas.goodtime.databinding.DrawerMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

import static com.apps.adrcotfas.goodtime.Util.UpgradeActivityHelper.launchUpgradeActivity;

public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {

    private NavigationView navigationView;
    private NestedScrollView layout;
    public BottomNavigationDrawerFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        DrawerMainBinding binding = DataBindingUtil.inflate(inflater, R.layout.drawer_main, container, false);

        if (PreferenceHelper.isPro() && !BuildConfig.F_DROID) {
            binding.separator1.setVisibility(View.GONE);
            binding.upgrade.setVisibility(View.GONE);
        } else {
            binding.upgrade.setOnClickListener(v -> {
                launchUpgradeActivity(getActivity());
                if (getDialog() != null) {
                    getDialog().dismiss();
                }
            });
        }

        Window window = getDialog().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        navigationView = binding.navigationView;

        layout = binding.layout;
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null) {

            layout.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        View view = getView();
        if (view != null) {
            view.post(() -> {
                View parent = (View) view.getParent();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) (parent).getLayoutParams();
                CoordinatorLayout.Behavior behavior = params.getBehavior();
                BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
                }
            });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.edit_labels:
                    if (PreferenceHelper.isPro()) {
                        Intent intent = new Intent(getActivity(), AddEditLabelActivity.class);
                        startActivity(intent);
                    } else {
                        launchUpgradeActivity(getContext());
                    }
                    break;
                case R.id.action_settings:
                    Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                case R.id.action_statistics:
                    Intent statisticsIntent = new Intent(getActivity(), StatisticsActivity.class);
                    startActivity(statisticsIntent);
                    break;
                case R.id.action_backup:
                    if (PreferenceHelper.isPro()) {
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        new BackupFragment().show(fragmentManager, "");
                    } else {
                        launchUpgradeActivity(getContext());
                    }
                    break;
                case R.id.action_about:
                    Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
                    startActivity(aboutIntent);
                    break;
            }
            if (getDialog() != null) {
                getDialog().dismiss();
            }
            return false;
        });
    }
}
