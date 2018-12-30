package com.apps.adrcotfas.goodtime.Main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.About.AboutActivity;
import com.apps.adrcotfas.goodtime.Backup.BackupFragment;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Settings.SettingsActivity;
import com.apps.adrcotfas.goodtime.Statistics.Main.StatisticsActivity;
import com.apps.adrcotfas.goodtime.databinding.DrawerMainBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {

    NavigationView navigationView;

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

        binding.rateThisApp.setOnClickListener(v -> {
            if (getDialog() != null) {
                getDialog().dismiss();
            }
            final String appPackageName = getActivity().getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
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
}
