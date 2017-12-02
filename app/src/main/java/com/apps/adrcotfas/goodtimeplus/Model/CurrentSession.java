package com.apps.adrcotfas.goodtimeplus.Model;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

public class CurrentSession {
    private MutableLiveData<Long> duration = new MutableLiveData<>();

    public CurrentSession(long duration) {
        this.duration.setValue(duration);
    }

    public LiveData<Long> getDuration() {
        return duration;
    }

    public void setDuration(long newDuration) {
        duration.setValue(newDuration);
    }
}
