package com.apps.adrcotfas.goodtime.Statistics.AllSessions;

import org.joda.time.DateTime;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddEditEntryDialogViewModel extends ViewModel {

    public static int INVALID_SESSION_TO_EDIT_ID = -1;
    public MutableLiveData<Integer> duration = new MutableLiveData<>();
    public MutableLiveData<DateTime> date = new MutableLiveData<>();
    public MutableLiveData<String> label = new MutableLiveData<>();
    public long sessionToEditId;

    public AddEditEntryDialogViewModel() {
        date.setValue(new DateTime().withHourOfDay(9).withMinuteOfHour(0));
        label.setValue(null);
        sessionToEditId = INVALID_SESSION_TO_EDIT_ID;
    }
}
