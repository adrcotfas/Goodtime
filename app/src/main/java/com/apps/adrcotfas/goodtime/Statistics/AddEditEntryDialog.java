package com.apps.adrcotfas.goodtime.Statistics;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.EditLabelDialog;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;

import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class AddEditEntryDialog {

    private AlertDialog mAlertDialog;

    AddEditEntryDialog(Fragment fragment, List<LabelAndColor> labels, boolean isEditDialog, Long sessionToEditId, ActionMode actionMode) {
        View promptView = fragment.getLayoutInflater().inflate(R.layout.dialog_add_entry, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(fragment.getActivity()).
                setTitle(isEditDialog ? "Edit entry" : "Add entry");
        alertDialogBuilder.setView(promptView);

        final EditText durationEditText = promptView.findViewById(R.id.duration);
        final SingleDateAndTimePicker picker = promptView.findViewById(R.id.single_day_picker);
        final TextView editLabel = promptView.findViewById(R.id.edit_label);

        if (isEditDialog) {
            AppDatabase.getDatabase(fragment.getActivity().getApplicationContext()).sessionModel().getSession(sessionToEditId)
                    .observe(fragment.getActivity(), session -> {
                        durationEditText.setText(Long.toString(session.totalTime));
                        picker.setDefaultDate(new Date(session.endTime));
                        editLabel.setText(session.label);
                    });
        }

        editLabel.setOnClickListener(view -> {
            //TODO: extract to string
            final EditLabelDialog dialog = new EditLabelDialog(
                    fragment.getActivity(),
                    labels,
                    isEditDialog ? editLabel.getText().toString() : "unlabeled");
            dialog.setOnPositiveButtonClickListener((dialogInterface, i) -> editLabel.setText(dialog.getLabel()));
            dialog.show();
        });

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> {
                    String input = durationEditText.getText().toString();
                    if (input.isEmpty()) {
                        Toast.makeText(fragment.getActivity(), "Please enter a valid duration", Toast.LENGTH_LONG).show();
                    } else {
                        final long duration = Math.min(Long.parseLong(input), 120);
                        if (duration > 0) {
                            String label = editLabel.getText().toString();
                            if (isEditDialog) {
                                AsyncTask.execute(() ->
                                        AppDatabase.getDatabase(fragment.getActivity().getApplicationContext()).sessionModel()
                                                .editSession(sessionToEditId, picker.getDate().getTime(), duration, editLabel.getText().toString()));
                            } else {
                                AsyncTask.execute(() -> AppDatabase.getDatabase(fragment.getActivity().getApplicationContext()).sessionModel()
                                        //TODO: extract to string
                                        .addSession(new Session(0, picker.getDate().getTime(), duration, !label.equals("Edit label") ? label : "unlabeled")));
                            }
                            mAlertDialog.dismiss();
                            if (isEditDialog) {
                                actionMode.finish();
                            }
                        } else {
                            Toast.makeText(fragment.getActivity(), "Please enter a valid duration", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                )
                .setNegativeButton("Cancel",
                        (dialog, id) -> {
                            dialog.cancel();
                            if (isEditDialog) {
                                actionMode.finish();
                            }
                        });

        mAlertDialog = alertDialogBuilder.create();
    }

    public void show() {
        mAlertDialog.show();
    }
}