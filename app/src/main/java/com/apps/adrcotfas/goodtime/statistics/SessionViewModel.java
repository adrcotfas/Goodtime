/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.statistics;

import android.app.Application;

import com.apps.adrcotfas.goodtime.database.AppDatabase;
import com.apps.adrcotfas.goodtime.database.SessionDao;
import com.apps.adrcotfas.goodtime.database.Session;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class SessionViewModel extends AndroidViewModel {

    private final SessionDao mSessionDao;
    private final ExecutorService mExecutorService;

    public SessionViewModel(@NonNull Application application) {
        super(application);
        mSessionDao = AppDatabase.getDatabase(application).sessionModel();
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Session>> getAllSessionsByEndTime() {
        return mSessionDao.getAllSessionsByEndTime();
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

    public void editLabel(Long id, String label) {
        mExecutorService.execute(() -> mSessionDao.editLabel(
                id, label));
    }

    public void deleteSession(long id) {
        mExecutorService.execute(() -> mSessionDao.deleteSession(id));
    }

    public void deleteSessionsFinishedToday() {
        mExecutorService.execute(() -> mSessionDao.deleteSessionsAfter(
                new LocalDate().toDateTimeAtStartOfDay().getMillis()));
    }

    public LiveData<List<Session>> getSessions(String label) {
        return mSessionDao.getSessions(label);
    }

    public LiveData<List<Session>> getAllSessionsUnlabeled() {
        return mSessionDao.getAllSessionsUnlabeled();
    }

    public LiveData<List<Session>> getAllSessions() {
        return mSessionDao.getAllSessions();
    }
}
