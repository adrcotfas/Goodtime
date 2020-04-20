/*
 * Copyright 2016-2020 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.Main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.apps.adrcotfas.goodtime.BL.SessionType;
import com.apps.adrcotfas.goodtime.R;

public class FinishedSessionDialog extends DialogFragment {

    public interface Listener {
        void onFinishedSessionDialogPositiveButtonClick(SessionType sessionType);
        void onFinishedSessionDialogNeutralButtonClick(SessionType sessionType);
    }

    private Listener listener;
    private SessionType sessionType;

    public FinishedSessionDialog() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) getActivity();
        } catch(ClassCastException e) {
            throw new ClassCastException("hosting activity must implement FinishedSessionDialog::Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public static FinishedSessionDialog newInstance(Listener listener, SessionType sessionType) {
        FinishedSessionDialog dialog = new FinishedSessionDialog();
        dialog.listener = listener;
        dialog.sessionType = sessionType;
        return dialog;
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        if (sessionType == SessionType.WORK) {
            builder.setTitle(R.string.action_finished_session)
                    .setPositiveButton(R.string.action_start_break, (dialog, which)
                            -> listener.onFinishedSessionDialogPositiveButtonClick(sessionType))
                    .setNeutralButton(R.string.dialog_close, (dialog, which)
                            -> listener.onFinishedSessionDialogNeutralButtonClick(sessionType));
        } else {
            builder.setTitle(R.string.action_finished_break)
                    .setPositiveButton(R.string.action_start_work, (dialog, which)
                            -> listener.onFinishedSessionDialogPositiveButtonClick(sessionType))
                    .setNeutralButton(android.R.string.cancel, (dialog, which)
                            -> listener.onFinishedSessionDialogNeutralButtonClick(sessionType));
        }

        final Dialog d = builder
                .setCancelable(false)
                .create();
        d.setCanceledOnTouchOutside(false);
        return d;
    }
}
