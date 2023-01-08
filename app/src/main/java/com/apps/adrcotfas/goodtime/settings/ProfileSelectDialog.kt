package com.apps.adrcotfas.goodtime.settings

import androidx.preference.PreferenceDialogFragmentCompat
import com.apps.adrcotfas.goodtime.settings.ProfileSelectAdapter.OnProfileSelectedListener
import android.os.Bundle
import android.view.LayoutInflater
import android.annotation.SuppressLint
import com.apps.adrcotfas.goodtime.R
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.preference.ListPreference
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileSelectDialog : PreferenceDialogFragmentCompat(), OnProfileSelectedListener {

    private val viewModel: ProfilesViewModel by viewModels()

    var mClickedDialogEntryIndex/* synthetic access */ = 0
    private lateinit var mEntries: Array<CharSequence>
    private var mAdapter: ProfileSelectAdapter? = null
    private lateinit var mRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val preference = listPreference
            check(!(preference.entries == null || preference.entryValues == null)) { "ListPreference requires an entries array and an entryValues array." }
            mClickedDialogEntryIndex = preference.findIndexOfValue(preference.value)
            mEntries = preference.entries
        } else {
            mClickedDialogEntryIndex = savedInstanceState.getInt(SAVE_STATE_INDEX, 0)
            mEntries = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRIES)!!
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_INDEX, mClickedDialogEntryIndex)
        outState.putCharSequenceArray(SAVE_STATE_ENTRIES, mEntries)
    }

    private val listPreference: ListPreference
        get() = preference as ListPreference

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        val inflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams") val dialogView =
            inflater.inflate(R.layout.dialog_select_profile, null)
        mRecyclerView = dialogView.findViewById(R.id.list)
        mAdapter = ProfileSelectAdapter(requireContext(), mEntries, mClickedDialogEntryIndex, this)
        mRecyclerView.apply {
            adapter = mAdapter
            val lm = LinearLayoutManager(context)
            layoutManager = lm
            itemAnimator = DefaultItemAnimator()
        }
        builder.setTitle(listPreference.title)
        builder.setView(dialogView)
        builder.setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
        builder.setPositiveButton(null, null)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult && mClickedDialogEntryIndex >= 0) {
            val value = mEntries[mClickedDialogEntryIndex].toString()
            val preference = listPreference
            if (preference.callChangeListener(value)) {
                preference.value = value
                preference.summary = value
            }
        }
    }

    override fun onDelete(position: Int) {
        dialog?.let {
            if (listPreference.value == mEntries[position].toString()) {
                mClickedDialogEntryIndex = 0
                // if we're deleting the selected profile, fall back to the default one
                listPreference.value = mEntries[0].toString()
                listPreference.setValueIndex(0)
                mClickedDialogEntryIndex = 0
                onClick(
                        dialog!!,
                        DialogInterface.BUTTON_POSITIVE
                )
            }
            viewModel.deleteProfile(mEntries[position].toString())
        }
    }

    override fun onSelect(position: Int) {
        dialog?.let {
            mClickedDialogEntryIndex = position
            onClick(
                    dialog!!,
                    DialogInterface.BUTTON_POSITIVE
            )
            dialog!!.dismiss()
        }
    }

    companion object {
        private const val SAVE_STATE_INDEX = "ListPreferenceDialogFragment.index"
        private const val SAVE_STATE_ENTRIES = "ListPreferenceDialogFragment.entries"
        fun newInstance(key: String?): ProfileSelectDialog {
            val fragment = ProfileSelectDialog()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}