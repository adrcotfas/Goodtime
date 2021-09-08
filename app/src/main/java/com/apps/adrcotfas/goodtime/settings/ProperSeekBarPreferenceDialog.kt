package com.apps.adrcotfas.goodtime.settings

import androidx.preference.PreferenceDialogFragmentCompat
import android.view.LayoutInflater
import android.annotation.SuppressLint
import android.app.Dialog
import com.apps.adrcotfas.goodtime.R
import android.widget.EditText
import android.text.TextUtils
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog

class ProperSeekBarPreferenceDialog : PreferenceDialogFragmentCompat() {

    interface Listener {
        fun onValueSet()
    }

    private lateinit var listener: Listener

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        val seekBarPreference = preference as ProperSeekBarPreference
        val inflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams") val dialogView =
            inflater.inflate(R.layout.dialog_set_seekbar_value, null)
        builder.setView(dialogView)
        builder.setTitle(seekBarPreference.title)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val input = dialogView.findViewById<EditText>(R.id.value)
            val value = input.text.toString()
            if (!TextUtils.isEmpty(value)) {
                val seekBarValue = value.toInt()
                if (seekBarPreference.value != seekBarValue) {
                    listener.onValueSet()
                    seekBarPreference.value = seekBarValue
                }
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ -> }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        // do nothing
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        d.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return d
    }

    companion object {
        fun newInstance(key: String, listener: Listener): ProperSeekBarPreferenceDialog {
            val fragment = ProperSeekBarPreferenceDialog()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            fragment.listener = listener
            return fragment
        }
    }
}