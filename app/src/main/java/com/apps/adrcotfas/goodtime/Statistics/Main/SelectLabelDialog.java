package com.apps.adrcotfas.goodtime.Statistics.Main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.AddEditLabelActivity;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.DialogSelectLabelBinding;
import com.google.android.material.chip.Chip;


import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;
import static com.apps.adrcotfas.goodtime.Statistics.Utils.getInstanceTotalLabel;
import static com.apps.adrcotfas.goodtime.Statistics.Utils.getInstanceUnlabeledLabel;

public class SelectLabelDialog extends DialogFragment {

    /**
     * The callback used to indicate the user is done selecting the label
     */
    public interface OnLabelSelectedListener {

        /**
         * @param labelAndColor     the label that was set
         */
        void onLabelSelected(LabelAndColor labelAndColor);
    }

    private String mLabel;
    private LabelsViewModel mLabelsViewModel;
    private TextView mEmptyState;
    private OnLabelSelectedListener mCallback;
    private boolean mIsExtendedVersion;

    public SelectLabelDialog() {
        // Empty constructor required for DialogFragment
    }

    public static SelectLabelDialog newInstance(OnLabelSelectedListener listener, String label, boolean isExtendedVersion) {
        SelectLabelDialog dialog = new SelectLabelDialog();
        dialog.mCallback = listener;
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

        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);
        mLabelsViewModel.getLabels().observe(this, labels -> {

            int i = 0;
            if (mIsExtendedVersion) {
                Chip chip = new Chip(getActivity());
                LabelAndColor total = getInstanceTotalLabel(getActivity());
                chip.setText(total.label);
                chip.setChipBackgroundColor(ColorStateList.valueOf(total.color));
                chip.setCheckable(true);
                ThemeHelper.styleChip(getActivity(), chip);

                chip.setId(i++);
                if (chip.getText().toString().equals(mLabel)) {
                    chip.setChecked(true);
                }
                binding.labels.addView(chip);
            }

            if (labels.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
            }

            for (LabelAndColor label : labels) {
                Chip chip = new Chip(getActivity());
                chip.setText(label.label);
                chip.setChipBackgroundColor(ColorStateList.valueOf(label.color));
                chip.setCheckable(true);
                ThemeHelper.styleChip(getActivity(), chip);

                chip.setId(i++);
                if (label.label.equals(mLabel)) {
                    chip.setChecked(true);
                }
                binding.labels.addView(chip);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setView(binding.getRoot())
                .setTitle("Select label")
                .setPositiveButton("OK", (dialog, which) -> {
                    if (binding.labels.getCheckedChipId() != -1) {
                        Chip chip = (Chip) (binding.labels.getChildAt(binding.labels.getCheckedChipId()));
                        mLabel = chip.getText().toString();
                        int color = chip.getChipBackgroundColor().getDefaultColor();
                        notifyLabelSelected(new LabelAndColor(mLabel, color));
                    } else {
                        notifyLabelSelected(getInstanceUnlabeledLabel(getActivity()));
                    }
                    dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Edit labels", (dialog, i) -> {
                    Intent intent = new Intent(getActivity(), AddEditLabelActivity.class);
                    startActivity(intent);
                });
        return builder.create();
    }

    private void notifyLabelSelected(LabelAndColor labelAndColor) {
        if (mCallback != null) {
            mCallback.onLabelSelected(labelAndColor);
        }
    }
}
