package com.apps.adrcotfas.goodtime.Settings;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.adrcotfas.goodtime.R;


public class ProfileSelectDialog extends PreferenceDialogFragmentCompat implements ProfileSelectAdapter.OnProfileSelectedListener{

    private static final String SAVE_STATE_INDEX = "ListPreferenceDialogFragment.index";
    private static final String SAVE_STATE_ENTRIES = "ListPreferenceDialogFragment.entries";

    @SuppressWarnings("WeakerAccess") /* synthetic access */
            int mClickedDialogEntryIndex;
    private CharSequence[] mEntries;

    private ProfilesViewModel mProfilesViewModel;

    private ProfileSelectAdapter mAdapter;
    private RecyclerView mRecyclerView;

    public static ProfileSelectDialog newInstance(String key) {
        final ProfileSelectDialog fragment =
                new ProfileSelectDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            final ListPreference preference = getListPreference();

            if (preference.getEntries() == null || preference.getEntryValues() == null) {
                throw new IllegalStateException(
                        "ListPreference requires an entries array and an entryValues array.");
            }
            mClickedDialogEntryIndex = preference.findIndexOfValue(preference.getValue());
            mEntries = preference.getEntries();
        } else {
            mClickedDialogEntryIndex = savedInstanceState.getInt(SAVE_STATE_INDEX, 0);
            mEntries = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRIES);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_STATE_INDEX, mClickedDialogEntryIndex);
        outState.putCharSequenceArray(SAVE_STATE_ENTRIES, mEntries);
    }

    private ListPreference getListPreference() {
        return (ListPreference) getPreference();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        mProfilesViewModel = ViewModelProviders.of(this).get(ProfilesViewModel.class);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams")
        View dialogView = inflater.inflate(R.layout.dialog_select_profile, null);

        mRecyclerView = dialogView.findViewById(R.id.list);
        mAdapter = new ProfileSelectAdapter(getContext(), mEntries, mClickedDialogEntryIndex, this);
        mRecyclerView.setAdapter(mAdapter);

        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        builder.setTitle(getListPreference().getTitle());
        builder.setView(dialogView);

        builder.setNegativeButton(android.R.string.cancel, (di, i) -> { });
        builder.setPositiveButton(null, null);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mClickedDialogEntryIndex >= 0) {
            String value = mEntries[mClickedDialogEntryIndex].toString();
            final ListPreference preference = getListPreference();
            if (preference.callChangeListener(value)) {
                preference.setValue(value);
                preference.setSummary(value);
            }
        }
    }

    @Override
    public void onDelete(int position) {
        if (getListPreference().getValue().equals(mEntries[position].toString())){
            mClickedDialogEntryIndex = 0;
            // if we're deleting the selected profile, fall back to the default one
            getListPreference().setValue(mEntries[0].toString());
            getListPreference().setValueIndex(0);

            mClickedDialogEntryIndex = 0;
            ProfileSelectDialog.this.onClick(getDialog(),
                    DialogInterface.BUTTON_POSITIVE);
        }
        mProfilesViewModel.deleteProfile(mEntries[position].toString());
    }

    @Override
    public void onSelect(int position) {
        mClickedDialogEntryIndex = position;
                    ProfileSelectDialog.this.onClick(getDialog(),
                            DialogInterface.BUTTON_POSITIVE);
        getDialog().dismiss();
    }
}
