package com.apps.adrcotfas.goodtime.settings

import android.content.Context
import androidx.preference.PreferenceDialogFragmentCompat
import android.os.Vibrator
import android.content.DialogInterface
import com.apps.adrcotfas.goodtime.util.VibrationPatterns
import com.apps.adrcotfas.goodtime.R
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference

class VibrationPreferenceDialogFragment : PreferenceDialogFragmentCompat() {

    private var clickedIndex = 0
    private lateinit var vibrator: Vibrator

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        check(!((preference as ListPreference).entries == null || (preference as ListPreference).entryValues == null)) {
            "ListPreference requires an entries array and an entryValues array." }
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        clickedIndex = (preference as ListPreference).findIndexOfValue((preference as ListPreference).value)
        builder.setSingleChoiceItems(
            (preference as ListPreference).entries, clickedIndex
        ) { _: DialogInterface?, which: Int ->
            clickedIndex = which
            vibrator.cancel()
            if (clickedIndex > 0) {
                vibrator.vibrate(VibrationPatterns.LIST[clickedIndex], -1)
            }
        }
        builder.setPositiveButton(getString(android.R.string.ok), this)
            .setNegativeButton(getString(android.R.string.cancel), this)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        vibrator.cancel()
        if (positiveResult && clickedIndex >= 0 && (preference as ListPreference).entryValues != null) {
            val value = (preference as ListPreference).entryValues[clickedIndex].toString()
            if (preference.callChangeListener(value)) {
                (preference as ListPreference).value = value
                preference.summary =
                    resources.getStringArray(R.array.pref_vibration_types)[clickedIndex]
            }
        }
    }

    companion object {
        fun newInstance(key: String?): VibrationPreferenceDialogFragment {
            val fragment = VibrationPreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}