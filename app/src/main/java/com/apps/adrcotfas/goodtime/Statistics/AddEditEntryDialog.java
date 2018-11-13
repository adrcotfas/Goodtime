package com.apps.adrcotfas.goodtime.Statistics;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.EditLabelDialog;
import com.apps.adrcotfas.goodtime.Main.LabelsViewModel;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Util.StringUtils;
import com.apps.adrcotfas.goodtime.databinding.DialogAddEntryBinding;
import com.google.android.material.chip.Chip;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

public class AddEditEntryDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private AddEditEntryDialogViewModel mViewModel;
    private LabelsViewModel mLabelsViewModel;
    private SessionViewModel mSessionViewModel;

    private EditText mDurationView;
    private Chip mLabelView;
    private TextView mDateView;
    private TextView mTimeView;
    private Session mSessionToEdit;
    private List<LabelAndColor> mLabels;

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        DialogAddEntryBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_add_entry, null, false);

        mViewModel = ViewModelProviders.of(this).get(AddEditEntryDialogViewModel.class);
        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);
        mSessionViewModel = ViewModelProviders.of(this).get(SessionViewModel.class);

        mDurationView = binding.duration;
        mLabelView = binding.editLabel;
        // TODO: implement onClicks with DataBinding
        mLabelView.setOnClickListener(labelView -> onLabelViewClicked());

        mViewModel.duration.observe(this, duration -> mDurationView.setText(duration.toString()));
        mViewModel.date.observe(this, date ->  {
            mDateView.setText(StringUtils.formatDate(date.getMillis()));
            mTimeView.setText(StringUtils.formatTime(date.getMillis()));
        });
        mViewModel.label.observe(this, label -> {
            if (label != null) {
                mLabelView.setText(label);
                mLabelsViewModel.getColorOfLabel(label).observe(
                        this, color -> mLabelView.setChipBackgroundColor(ColorStateList.valueOf(color)));
            } else {
                mLabelView.setText("add label");
                mLabelView.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            }
        });

        mLabelsViewModel.getLabels().observe(this, labelAndColors -> {
            mLabels = labelAndColors;
        });

        mDateView = binding.editDate;
        mDateView.setOnClickListener(dateView -> onDateViewClick());

        mTimeView = binding.editTime;
        mTimeView.setOnClickListener(view -> onTimeViewClick());

        if (mSessionToEdit != null) {
            mViewModel.date.setValue(new DateTime(mSessionToEdit.endTime));
            mViewModel.duration.setValue(mSessionToEdit.totalTime);
            mViewModel.label.setValue(mSessionToEdit.label);
            mViewModel.sessionToEditId = mSessionToEdit.id;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_add_entry, null))
                .setView(binding.getRoot())
        .setTitle(mSessionToEdit == null ? "Add session" : "Edit session")
        .setPositiveButton("OK", (dialog, which) -> {
            if (mDurationView.getText().toString().isEmpty()) {
                Toast.makeText(getActivity(), "Please enter a valid duration", Toast.LENGTH_LONG).show();
            }
            else {
                final int duration = Math.min(Integer.parseInt(mDurationView.getText().toString()), 120);
                Session sessionToAdd = new Session(0, mViewModel.date.getValue().getMillis(), duration, mViewModel.label.getValue());
                if (mViewModel.sessionToEditId != -1) {
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

    private void onLabelViewClicked() {
        final EditLabelDialog dialog = new EditLabelDialog(
                getActivity(),
                mLabels,
                mViewModel.label.getValue());
        dialog.setPositiveButtonClickListener((dialogInterface, i) -> {
            if (dialog.getLabel() != null) {
                mViewModel.label.setValue(dialog.getLabel());
            }
        });
        dialog.show();
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