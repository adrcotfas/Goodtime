package com.apps.adrcotfas.goodtime.Statistics.Main;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Statistics.AllSessions.AddEditEntryDialog;
import com.apps.adrcotfas.goodtime.Statistics.AllSessions.AllSessionsFragment;
import com.apps.adrcotfas.goodtime.Statistics.SessionViewModel;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.StatisticsActivityMainBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

public class StatisticsActivity extends AppCompatActivity {

    private LabelsViewModel mLabelsViewModel;
    private ChipGroup mChipGroupLabels;
    private SessionViewModel mSessionViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this);
        StatisticsActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.statistics_activity_main);
        mChipGroupLabels = binding.toolbarWrapper.labelView.labels;
        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);
        mSessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);

        setSupportActionBar(binding.toolbarWrapper.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLabelsViewModel.isMainPage.observe(this, isMainPage -> {
            Fragment fragment = isMainPage ? new StatisticsFragment() : new AllSessionsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commitAllowingStateLoss();
            invalidateOptionsMenu();
        });

        setupLabelRadioGroup();

        if (savedInstanceState == null) {
            Fragment fragment = new StatisticsFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment, fragment);
            ft.commitAllowingStateLoss();
        }
    }

    @SuppressLint("ResourceType")
    private void setupLabelRadioGroup() {
        mChipGroupLabels.setOnCheckedChangeListener((chipGroup, id) -> {
            // this is called on screen rotation; maybe find a cleaner way
            if (id == -1) {
                return;
            }

            Chip chip = ((Chip) chipGroup.getChildAt(id));
            if (chip == null) {
                chip = chipGroup.findViewById(id);
            }

            for (int i = 0; i < mChipGroupLabels.getChildCount(); ++i) {
                mChipGroupLabels.getChildAt(i).setClickable(true);
            }
            chip.setClickable(false);

            if (mLabelsViewModel.crtExtendedLabel.getValue() != null) {
                switch (mLabelsViewModel.crtExtendedLabel.getValue().label) {
                    case "total":
                        mSessionViewModel.getAllSessionsByEndTime().removeObservers(this);
                        break;
                    case "unlabeled":
                        mSessionViewModel.getAllSessionsUnlabeled().removeObservers(this);
                        break;
                    default:
                        mSessionViewModel.getSessions(mLabelsViewModel.crtExtendedLabel.getValue().label).removeObservers(this);
                        break;
                }
            }

            mLabelsViewModel.crtExtendedLabel.setValue(new LabelAndColor(chip.getText().toString(), chip.getChipBackgroundColor().getDefaultColor()));
        });

        mLabelsViewModel.getLabels().observe(this, labels -> {
            for (int i = 0; i < labels.size(); ++i) {
                Chip chip = new Chip(this);
                chip.setText(labels.get(i).label);
                chip.setChipBackgroundColor(ColorStateList.valueOf(labels.get(i).color));
                chip.setCheckable(true);
                chip.setId(i + 1);
                mChipGroupLabels.addView(chip, i + 1);
                if (mLabelsViewModel.crtExtendedLabel.getValue().label.equals(labels.get(i).label)) {
                    chip.setChecked(true);
                }
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_list:
                mLabelsViewModel.isMainPage.setValue(!mLabelsViewModel.isMainPage.getValue());
                break;

            case R.id.action_add:

                FragmentManager fragmentManager = getSupportFragmentManager();
                AddEditEntryDialog newFragment = new AddEditEntryDialog();
                newFragment.show(fragmentManager, "");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_statistics_main, menu);

        if (mLabelsViewModel.isMainPage.getValue()) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_details));
        } else {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_show_list));
        }

        return super.onCreateOptionsMenu(menu);
    }
}
