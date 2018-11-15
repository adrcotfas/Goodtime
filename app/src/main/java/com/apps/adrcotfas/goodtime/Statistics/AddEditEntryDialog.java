package com.apps.adrcotfas.goodtime.Statistics;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Util.StringUtils;
import com.apps.adrcotfas.goodtime.databinding.DialogAddEntryBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import static com.apps.adrcotfas.goodtime.Statistics.AddEditEntryDialogViewModel.INVALID_SESSION_TO_EDIT_ID;

public class AddEditEntryDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private AddEditEntryDialogViewModel mViewModel;
    private SessionViewModel mSessionViewModel;

    private EditText mDurationView;
    private ChipGroup mLabelView;
    private TextView mDateView;
    private TextView mTimeView;
    private Session mSessionToEdit;

    public AddEditEntryDialog() {
        // Empty constructor required for DialogFragment
    }

    /**
     * Creates a new instance from an existing session. To be used when editing a session.
     * @param session the session
     * @return the new instance initialized with the existing session's data
     */
    public static AddEditEntryDialog newInstance(Session session) {
        AddEditEntryDialog dialog = new AddEditEntryDialog();
        dialog.mSessionToEdit = session;
        return dialog;
    }

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        DialogAddEntryBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_add_entry, null, false);

        mViewModel = ViewModelProviders.of(this).get(AddEditEntryDialogViewModel.class);
        mSessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);
        LabelsViewModel labelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);

        mDurationView = binding.duration;
        mDurationView.setSelection(mDurationView.getText().length());
        mLabelView = binding.labels;
        // TODO: implement onClicks with DataBinding

        mViewModel.duration.observe(this, duration -> mDurationView.setText(duration.toString()));
        mViewModel.date.observe(this, date ->  {
            mDateView.setText(StringUtils.formatDate(date.getMillis()));
            mTimeView.setText(StringUtils.formatTime(date.getMillis()));
        });

        mLabelView.setOnCheckedChangeListener((chipGroup, id) -> {
            Chip chip = ((Chip) chipGroup.getChildAt(id));
            if (chip == null) {
                mViewModel.label.setValue(null);
                binding.labelDrawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_label_off));
            } else {
                mViewModel.label.setValue(chip.getText().toString());
                binding.labelDrawable.setImageDrawable(getResources().getDrawable(R.drawable.ic_label));
            }
        });

        labelsViewModel.getLabels().observe(this, labels -> {
            for (int i = 0; i < labels.size(); ++i) {
                Chip chip = new Chip(Objects.requireNonNull(getActivity()));
                chip.setText(labels.get(i).label);
                chip.setChipBackgroundColor(ColorStateList.valueOf(labels.get(i).color));
                chip.setCheckable(true);
                chip.setId(i);
                mLabelView.addView(chip, i);

                if (mViewModel.label.getValue() != null && mViewModel.label.getValue().equals(labels.get(i).label)) {
                    // move the current selected label to the front
                    for (int j = 0; j < chip.getId(); ++j) {
                        mLabelView.getChildAt(j).setId(j + 1);
                    }
                    mLabelView.removeView(chip);
                    mLabelView.addView(chip, 0);
                    chip.setId(0);
                    chip.setChecked(true);
                }
            }
        });

        mDateView = binding.editDate;
        mDateView.setOnClickListener(dateView -> onDateViewClick());

        mTimeView = binding.editTime;
        mTimeView.setOnClickListener(view -> onTimeViewClick());

        // this is for the edit dialog
        if (mSessionToEdit != null) {
            mViewModel.date.setValue(new DateTime(mSessionToEdit.endTime));
            mViewModel.duration.setValue(mSessionToEdit.totalTime);
            mViewModel.label.setValue(mSessionToEdit.label);
            mViewModel.sessionToEditId = mSessionToEdit.id;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_add_entry, null))
                .setView(binding.getRoot())
        .setTitle(mSessionToEdit == null ? "Add session" : "Edit session")
        .setPositiveButton("OK", (dialog, which) -> {
            if (mDurationView.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), "Enter a valid duration", Toast.LENGTH_LONG).show();
            }
            else {
                final int duration = Math.min(Integer.parseInt(mDurationView.getText().toString()), 120);
                final String label = mViewModel.label.getValue();
                Session sessionToAdd = new Session(0, mViewModel.date.getValue().getMillis(), duration, label);
                if (mViewModel.sessionToEditId != INVALID_SESSION_TO_EDIT_ID) {
                    mSessionViewModel.editSession(mViewModel.sessionToEditId, sessionToAdd.endTime, sessionToAdd.totalTime, sessionToAdd.label);
                } else {
                    mSessionViewModel.addSession(sessionToAdd);
                }
                dismiss();
            }
        })
        .setNegativeButton("Cancel", (dialog, which) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    private void onTimeViewClick() {
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                AddEditEntryDialog.this, 9, 0, true);
        tpd.setThemeDark(true);
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.show(requireFragmentManager(), "timepickerdialog");
    }

    private void onDateViewClick() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                AddEditEntryDialog.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setThemeDark(true);
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.show(requireFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mViewModel.date.setValue((mViewModel.date.getValue() == null)
                ? new DateTime()
                : mViewModel.date.getValue()
                .withYear(year)
                .withMonthOfYear(monthOfYear)
                .withDayOfMonth(dayOfMonth));
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        mViewModel.date.setValue((mViewModel.date.getValue() == null)
                ? new DateTime()
                : mViewModel.date.getValue()
                .withHourOfDay(hourOfDay)
                .withMinuteOfHour(minute));
    }
}
