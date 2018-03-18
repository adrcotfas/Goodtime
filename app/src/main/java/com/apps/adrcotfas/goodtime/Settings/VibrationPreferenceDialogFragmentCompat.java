package com.apps.adrcotfas.goodtime.Settings;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import android.view.View;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.VibrationPatterns;

public class VibrationPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat
{
    private int mClickedIndex;
    private Vibrator mVibrator;
    private ListPreference mPreference;

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
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mClickedIndex = which;
                        mVibrator.cancel();
                        if (mClickedIndex >= 0) mVibrator.vibrate(VibrationPatterns.LIST[mClickedIndex], -1);
                    }
                });

        builder.setPositiveButton("OK", this).setNegativeButton("Cancel", this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        mVibrator.cancel();

        if (positiveResult && mClickedIndex >= 0 && mPreference.getEntryValues() != null) {
            String value = mPreference.getEntryValues()[mClickedIndex].toString();
            if (mPreference.callChangeListener(value)) {
                mPreference.setValue(value);
                mPreference.setSummary(getResources().getStringArray(R.array.vibration_pattern_entries)[mClickedIndex]);
            }
        }
    }
}
