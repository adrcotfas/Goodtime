package com.apps.adrcotfas.goodtime.Statistics;

import org.joda.time.DateTime;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddEditEntryDialogViewModel extends ViewModel {

    public MutableLiveData<Integer> duration = new MutableLiveData<>();
    public MutableLiveData<DateTime> date = new MutableLiveData<>();
    public MutableLiveData<String> label = new MutableLiveData<>();
    public long sessionToEditId = -1;

    public AddEditEntryDialogViewModel() {
        date.setValue(new DateTime().withHourOfDay(9).withMinuteOfHour(0));
    }
}
