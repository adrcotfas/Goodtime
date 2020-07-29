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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper;
import com.apps.adrcotfas.goodtime.Label;
import com.apps.adrcotfas.goodtime.AddEditLabels.AddEditLabelActivity;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.Profile;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Settings.ProfilesViewModel;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.DialogSelectLabelBinding;
import com.google.android.material.chip.Chip;


import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import static com.apps.adrcotfas.goodtime.Statistics.Utils.getInstanceTotalLabel;
import static com.apps.adrcotfas.goodtime.Statistics.Utils.getInstanceUnlabeledLabel;
import static com.apps.adrcotfas.goodtime.Util.ThemeHelper.COLOR_INDEX_ALL_LABELS;
import static com.apps.adrcotfas.goodtime.Util.UpgradeActivityHelper.launchUpgradeActivity;

public class SelectLabelDialog extends DialogFragment {

    /**
     * The callback used to indicate the user is done selecting the title
     */
    public interface OnLabelSelectedListener {

        /**
         * @param label     the label that was set
         */
        void onLabelSelected(Label label);
    }

    private List<Profile> mProfiles;
    private String mLabel;
    private WeakReference<OnLabelSelectedListener> mCallback;
    /**
     * The extended version of this dialog is used in the Statistics
     * where it also contains "all" as a label.
     * The regular version contains an extra neutral button for selecting the current profile.
     */
    private boolean mIsExtendedVersion;
    private AlertDialog mAlertDialog;

    public SelectLabelDialog() {
        // Empty constructor required for DialogFragment
    }

    public static SelectLabelDialog newInstance(OnLabelSelectedListener listener, String label, boolean isExtendedVersion) {
        SelectLabelDialog dialog = new SelectLabelDialog();
        dialog.mCallback = new WeakReference<>(listener);
        dialog.mLabel = label;
        dialog.mIsExtendedVersion = isExtendedVersion;
        return dialog;
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        DialogSelectLabelBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.dialog_select_label,
                null,
                false);

        binding.editLabels.setOnClickListener(v -> {
            if (PreferenceHelper.isPro()) {
                Intent intent = new Intent(getActivity(), AddEditLabelActivity.class);
                startActivity(intent);
            } else {
                launchUpgradeActivity(getActivity());
            }

            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
            }
        });

        LabelsViewModel labelsVm = new ViewModelProvider(this).get(LabelsViewModel.class);
        labelsVm.getLabels().observe(this, labels -> {

            int i = 0;
            if (mIsExtendedVersion) {
                Chip chip = new Chip(requireContext());
                Label total = getInstanceTotalLabel(requireContext());
                chip.setText(total.title);
                chip.setChipBackgroundColor(ColorStateList.valueOf(ThemeHelper.getColor(getActivity(), COLOR_INDEX_ALL_LABELS)));
                chip.setCheckable(true);
                chip.setChipIcon(getResources().getDrawable(R.drawable.ic_check_off));
                chip.setCheckedIcon(getResources().getDrawable(R.drawable.ic_check));

                chip.setId(i++);
                if (chip.getText().toString().equals(mLabel)) {
                    chip.setChecked(true);
                }
                binding.labels.addView(chip);
            }

            for (int j = labels.size() - 1; j >= 0; --j) {
                Label crt = labels.get(j);
                Chip chip = new Chip(requireContext());
                chip.setText(crt.title);
                chip.setChipBackgroundColor(ColorStateList.valueOf(ThemeHelper.getColor(getActivity(), crt.colorId)));
                chip.setCheckable(true);
                chip.setChipIcon(getResources().getDrawable(R.drawable.ic_check_off));
                chip.setCheckedIcon(getResources().getDrawable(R.drawable.ic_check));

                chip.setId(i++);
                if (crt.title.equals(mLabel)) {
                    chip.setChecked(true);
                }
                binding.labels.addView(chip);
            }

            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                binding.progressBar.setVisibility(View.GONE);
                if (binding.labels.getChildCount() == 0) {
                    binding.emptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyState.setVisibility(View.GONE);
                    binding.labelsView.setVisibility(View.VISIBLE);
                }
            }, 100);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (binding.labels.getCheckedChipId() != -1) {
                        Chip chip = (Chip) (binding.labels.getChildAt(binding.labels.getCheckedChipId()));
                        mLabel = chip.getText().toString();
                        int color = chip.getChipBackgroundColor().getDefaultColor();
                        notifyLabelSelected(new Label(mLabel, ThemeHelper.getIndexOfColor(requireContext(), color)));
                    } else {
                        notifyLabelSelected(getInstanceUnlabeledLabel());
                    }
                    dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                });
        if (!mIsExtendedVersion) {
            builder.setNeutralButton(
                    PreferenceHelper.isUnsavedProfileActive()
                            ? getResources().getString(R.string.Profile)
                            : PreferenceHelper.getProfile(), null);
            mAlertDialog = builder.create();
            mAlertDialog.setOnShowListener(dialog -> {

                //TODO: Clean-up this mess
                Button neutral = mAlertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                neutral.setOnClickListener(v -> {
                    ProfilesViewModel profilesVm = new ViewModelProvider(SelectLabelDialog.this).get(ProfilesViewModel.class);
                    LiveData<List<Profile>> profilesLiveData = profilesVm.getProfiles();
                    profilesLiveData.observe(SelectLabelDialog.this, profiles -> {

                        mProfiles = profiles;
                        int profileIdx = 0;
                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(),
                                R.layout.checked_text_view);

                        String pref25 = SelectLabelDialog.this.getResources().getText(R.string.pref_profile_default).toString();
                        String pref52 = SelectLabelDialog.this.getResources().getText(R.string.pref_profile_5217).toString();
                        String crtProfileName = PreferenceHelper.getProfile();

                        if (crtProfileName.equals(pref25)) {
                            profileIdx = 0;
                        } else if (crtProfileName.equals(pref52)) {
                            profileIdx = 1;
                        }

                        arrayAdapter.add(pref25);
                        arrayAdapter.add(pref52);

                        final int PREDEFINED_PROFILES_NR = arrayAdapter.getCount();

                        for (int i = 0; i < profiles.size(); ++i) {
                            Profile p = profiles.get(i);
                            arrayAdapter.add(p.name);
                            if (crtProfileName.equals(p.name)) {
                                profileIdx = i + PREDEFINED_PROFILES_NR;
                            }
                        }

                        AlertDialog.Builder profileDialogBuilder =
                                new AlertDialog.Builder(requireContext())
                                        .setTitle(SelectLabelDialog.this.getResources().getString(R.string.Profile))
                                        .setSingleChoiceItems(
                                                arrayAdapter,
                                                PreferenceHelper.isUnsavedProfileActive() ? -1 : profileIdx,
                                                (dialogInterface, which) -> {
                                            String selected = arrayAdapter.getItem(which);
                                            updateProfile(which);

                                            dialogInterface.dismiss();
                                            if (mAlertDialog != null) {
                                                mAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText(selected);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, (dialog1, which) -> dialog1.dismiss());
                        profileDialogBuilder.show();
                    });
                });
            });
        } else {
            mAlertDialog = builder.create();
        }
        return mAlertDialog;
    }

    private void notifyLabelSelected(Label label) {
        if (mCallback != null) {
            mCallback.get().onLabelSelected(label);
        }
    }

    private void updateProfile(int index) {
        if (index == 0) {
            PreferenceHelper.setProfile25_5();
        } else if (index == 1) {
            PreferenceHelper.setProfile52_17();
        } else {
            int PREDEFINED_PROFILES_NR = 2;
            PreferenceHelper.setProfile(mProfiles.get(index - PREDEFINED_PROFILES_NR));
        }
    }
}
