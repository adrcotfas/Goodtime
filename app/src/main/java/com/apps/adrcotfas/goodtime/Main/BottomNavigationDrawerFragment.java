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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.About.AboutActivity;
import com.apps.adrcotfas.goodtime.Backup.BackupFragment;
import com.apps.adrcotfas.goodtime.BuildConfig;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Settings.SettingsActivity;
import com.apps.adrcotfas.goodtime.Statistics.Main.StatisticsActivity;
import com.apps.adrcotfas.goodtime.Util.DeviceInfo;
import com.apps.adrcotfas.goodtime.databinding.DrawerMainBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {

    private NavigationView navigationView;

    public BottomNavigationDrawerFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        DrawerMainBinding binding = DataBindingUtil.inflate(inflater, R.layout.drawer_main, container, false);

        navigationView = binding.navigationView;

        binding.about.setOnClickListener(v -> {
            if (getDialog() != null) {
                getDialog().dismiss();
            }
            Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
            startActivity(aboutIntent);
        });

        binding.feedback.setOnClickListener(v -> {
            if (getDialog() != null) {
                getDialog().dismiss();
            }
            openFeedback();
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.edit_labels:
                    Intent intent = new Intent(getActivity(), AddEditLabelActivity.class);
                    startActivity(intent);
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
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    new BackupFragment().show(fragmentManager, "");
                    break;
            }
            if (getDialog() != null) {
                getDialog().dismiss();
            }
            return false;
        });
    }

    private void openFeedback() {
        Intent email = new Intent(Intent.ACTION_SENDTO);
        email.setData(new Uri.Builder().scheme("mailto").build());
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"goodtime-app@googlegroups.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "[Goodtime] Feedback");
        email.putExtra(Intent.EXTRA_TEXT, "\nMy device info: \n" + DeviceInfo.getDeviceInfo()
                + "\nApp version: " + BuildConfig.VERSION_NAME);
        try {
            startActivity(Intent.createChooser(email, getActivity().getResources().getString(R.string.feedback_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), R.string.about_no_email, Toast.LENGTH_SHORT).show();
        }
    }

}
