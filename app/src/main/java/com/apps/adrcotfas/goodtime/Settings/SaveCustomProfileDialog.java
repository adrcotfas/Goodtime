package com.apps.adrcotfas.goodtime.Settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.apps.adrcotfas.goodtime.Profile;
import com.apps.adrcotfas.goodtime.R;

public class SaveCustomProfileDialog extends PreferenceDialogFragmentCompat {

    private ProfilesViewModel mProfilesViewModel;
    private Profile mProfileToAdd;
    private String mTitle;
    private String mCrtProfileName;

    public static SaveCustomProfileDialog newInstance(String key, String title, Profile profileToAdd, String crtProfileName) {
        SaveCustomProfileDialog fragment = new SaveCustomProfileDialog();
        fragment.mProfileToAdd = profileToAdd;
        fragment.mTitle = title;
        fragment.mCrtProfileName = crtProfileName;
        Bundle b = new Bundle(1);
        b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        mProfilesViewModel = ViewModelProviders.of(this).get(ProfilesViewModel.class);
        ListPreference profilePreference = (ListPreference) getPreference();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams")
        View dialogView = inflater.inflate(R.layout.dialog_set_profile_name, null);
        builder.setView(dialogView);

        builder.setTitle(mTitle);
        builder.setPositiveButton(android.R.string.ok, (di, i) -> {
            final EditText input = dialogView.findViewById(R.id.value);
            final String name = input.getText().toString();
            if (!TextUtils.isEmpty(name)) {
                mProfileToAdd.name = name;
                mProfilesViewModel.addProfile(mProfileToAdd);
            }
            if (!mCrtProfileName.equals(name)) {
                profilePreference.setValue(name);
                profilePreference.setSummary(name);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (di, i) -> {
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // do nothing
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return d;
    }
}
