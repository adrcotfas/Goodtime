package com.apps.adrcotfas.goodtime.Statistics;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.databinding.DialogSelectLabelBinding;
import com.google.android.material.chip.Chip;


import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

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
    private OnLabelSelectedListener mCallback;

    public SelectLabelDialog() {
        // Empty constructor required for DialogFragment
    }

    public static SelectLabelDialog newInstance(OnLabelSelectedListener listener, String label) {
        SelectLabelDialog dialog = new SelectLabelDialog();
        dialog.mCallback = listener;
        dialog.mLabel = label;
        return dialog;
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        DialogSelectLabelBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_select_label, null, false);

        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);
        mLabelsViewModel.getLabels().observe(this, labels -> {
            int i = 0;
            for (LabelAndColor label : labels) {
                Chip chip = new Chip(getContext());
                chip.setText(label.label);
                chip.setChipBackgroundColor(ColorStateList.valueOf(label.color));
                chip.setCheckable(true);
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
                        notifyLabelSelected(null);
                    }
                    dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    private void notifyLabelSelected(LabelAndColor labelAndColor) {
        if (mCallback != null) {
            mCallback.onLabelSelected(labelAndColor);
        }
    }
}
