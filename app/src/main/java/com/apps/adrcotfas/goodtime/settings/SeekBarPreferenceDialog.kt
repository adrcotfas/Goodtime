package com.apps.adrcotfas.goodtime.settings

import android.app.Dialog
import android.text.TextUtils
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.SeekBarPreference
import com.apps.adrcotfas.goodtime.databinding.DialogSetSeekbarValueBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SeekBarPreferenceDialog : DialogFragment() {

    interface Listener {
        fun onValueSet()
    }

    private lateinit var listener: Listener
    private lateinit var seekbarPreference: SeekBarPreference

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogSetSeekbarValueBinding.inflate(layoutInflater)
        val valueEditText = binding.value
        valueEditText.setText(seekbarPreference.value.toString())

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle(seekbarPreference.title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val input = binding.value
                val value = input.text.toString()
                if (!TextUtils.isEmpty(value)) {
                    val seekBarValue = value.toInt()
                    if (seekbarPreference.value != seekBarValue) {
                        listener.onValueSet()
                        seekbarPreference.value = seekBarValue
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
        return builder.create()
    }

    companion object {
        fun newInstance(seekbarPreference: SeekBarPreference, listener: Listener): SeekBarPreferenceDialog {
            val fragment = SeekBarPreferenceDialog()
            fragment.seekbarPreference = seekbarPreference
            fragment.listener = listener
            return fragment
        }
    }
}