package com.apps.adrcotfas.goodtime.Util;

import android.app.Dialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import com.apps.adrcotfas.goodtime.R;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener listener;
    private Calendar calendar;

    public DatePickerFragment() {
    }

    public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener, Calendar calendar) {
        DatePickerFragment dialog = new DatePickerFragment();
        dialog.listener = listener;
        dialog.calendar = calendar;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), R.style.DialogTheme, listener, year, month, day);
    }
}
