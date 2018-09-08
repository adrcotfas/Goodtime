package com.apps.adrcotfas.goodtime.Statistics;

import android.app.Application;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.Database.SessionDao;
import com.apps.adrcotfas.goodtime.Session;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class SessionViewModel extends AndroidViewModel {

    private SessionDao mSessionDao;
    private ExecutorService mExecutorService;

    public SessionViewModel(@NonNull Application application) {
        super(application);
        mSessionDao = AppDatabase.getDatabase(application).sessionModel();
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    LiveData<List<Session>> getAllSessionsByEndTime() {
        return mSessionDao.getAllSessionsByEndTime();
    }

    LiveData<List<Session>> getAllSessionsByDuration() {
        return mSessionDao.getAllSessionsByDuration();
    }

    LiveData<Session> getSession(long id) {
        return mSessionDao.getSession(id);
    }

    void addSession(Session session) {
        mExecutorService.execute(() -> mSessionDao.addSession(session));
    }

    void editSession(Long id, long endTime, long totalTime, String label) {
        mExecutorService.execute(() -> mSessionDao.editSession(
                id, endTime, totalTime, label));
    }

    void deleteSession(long id) {
        mExecutorService.execute(() -> mSessionDao.deleteSession(id));
    }
}
