package com.apps.adrcotfas.goodtime.Settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;

import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.VibrationPatterns;

public class VibrationPreferenceDialogFragment extends PreferenceDialogFragmentCompat
{
    private int mClickedIndex;
    private Vibrator mVibrator;
    private ListPreference mPreference;

    public static VibrationPreferenceDialogFragment newInstance(String key) {
        VibrationPreferenceDialogFragment fragment = new VibrationPreferenceDialogFragment();
        Bundle b = new Bundle(1);
        b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }


    @Override
    protected View onCreateDialogView(Context context) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        return super.onCreateDialogView(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        mPreference = (ListPreference) getPreference();

        if (mPreference.getEntries() == null || mPreference.getEntryValues() == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }

        mClickedIndex = mPreference.findIndexOfValue(mPreference.getValue());
        builder.setSingleChoiceItems(mPreference.getEntries(), mClickedIndex,
                (dialog, which) -> {
                    mClickedIndex = which;
                    mVibrator.cancel();
                    if (mClickedIndex > 0) {
                        mVibrator.vibrate(VibrationPatterns.LIST[mClickedIndex], -1);
                    }
                });

        builder.setPositiveButton(getString(android.R.string.ok), this)
                .setNegativeButton(getString(android.R.string.cancel), this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        mVibrator.cancel();

        if (positiveResult && mClickedIndex >= 0 && mPreference.getEntryValues() != null) {
            String value = mPreference.getEntryValues()[mClickedIndex].toString();
            if (mPreference.callChangeListener(value)) {
                mPreference.setValue(value);
                mPreference.setSummary(getResources().getStringArray(R.array.pref_vibration_types)[mClickedIndex]);
            }
        }
    }
}

