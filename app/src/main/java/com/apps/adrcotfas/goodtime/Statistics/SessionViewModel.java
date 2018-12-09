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
    private Application mApplication;

    public SessionViewModel(@NonNull Application application) {
        super(application);
        mSessionDao = AppDatabase.getDatabase(application).sessionModel();
        mExecutorService = Executors.newSingleThreadExecutor();
        mApplication = application;
    }

    public LiveData<List<Session>> getAllSessionsByEndTime() {
        return mSessionDao.getAllSessionsByEndTime();
    }

    public LiveData<List<Session>> getAllSessionsByDuration() {
        return mSessionDao.getAllSessionsByDuration();
    }

    public Application getmApplication() {
        return mApplication;
    }

    public LiveData<Session> getSession(long id) {
        return mSessionDao.getSession(id);
    }

    public void addSession(Session session) {
        mExecutorService.execute(() -> mSessionDao.addSession(session));
    }

    public void editSession(Long id, long endTime, long totalTime, String label) {
        mExecutorService.execute(() -> mSessionDao.editSession(
                id, endTime, totalTime, label));
    }

    public void deleteSession(long id) {
        mExecutorService.execute(() -> mSessionDao.deleteSession(id));
    }

    public LiveData<List<Session>> getSessions(String label) {
        return mSessionDao.getSessions(label);
    }

    public LiveData<List<Session>> getAllSessionsUnlabeled() {
        return mSessionDao.getAllSessionsUnlabeled();
    }
}
