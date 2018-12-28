package com.apps.adrcotfas.goodtime.Util;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import com.apps.adrcotfas.goodtime.R;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment {

    private TimePickerDialog.OnTimeSetListener listener;
    private Calendar calendar;

    public TimePickerFragment() {
    }

    public static TimePickerFragment newInstance(TimePickerDialog.OnTimeSetListener listener, Calendar calendar) {
        TimePickerFragment dialog = new TimePickerFragment();
        dialog.listener = listener;
        dialog.calendar = calendar;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), R.style.DialogTheme, listener, hour, minute, true);
    }
}

