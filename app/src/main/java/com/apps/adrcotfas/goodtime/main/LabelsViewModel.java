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

package com.apps.adrcotfas.goodtime.main;

import android.app.Application;

import com.apps.adrcotfas.goodtime.database.AppDatabase;
import com.apps.adrcotfas.goodtime.database.LabelDao;
import com.apps.adrcotfas.goodtime.database.Label;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.apps.adrcotfas.goodtime.statistics.Utils.getInstanceTotalLabel;

public class LabelsViewModel extends AndroidViewModel {

    private final LabelDao mLabelsDao;
    private final ExecutorService mExecutorService;

    /**
     * The current selected label in the Statistics view
     * "extended" because it might be "total" or "unlabeled"
     */
    public final MutableLiveData<Label> crtExtendedLabel = new MutableLiveData<>();

    public LabelsViewModel(@NonNull Application application) {
        super(application);
        mLabelsDao = AppDatabase.getDatabase(application).labelDao();
        mExecutorService = Executors.newSingleThreadExecutor();
        crtExtendedLabel.setValue(getInstanceTotalLabel(application.getBaseContext()));
    }

    /**
     *  Returns only the labels which are not archived
     */
    public LiveData<List<Label>> getLabels() {
        return mLabelsDao.getLabels();
    }

    /**
     *  Returns all labels, including the archived ones
     */
    public LiveData<List<Label>> getAllLabels() {
        return mLabelsDao.getAllLabels();
    }

    public LiveData<Integer> getColorOfLabel(String label) {
        return mLabelsDao.getColor(label);
    }

    public void addLabel(Label label) {
        mExecutorService.execute(() -> mLabelsDao.addLabel(label));
    }

    public void editLabelName(String label, String newLabel) {
        mExecutorService.execute(() -> mLabelsDao.editLabelName(label, newLabel));
    }

    public void editLabelColor(String label, int color) {
        mExecutorService.execute(() -> mLabelsDao.editLabelColor(label, color));
    }

    public void editLabelOrder(String label, int newOrder) {
        mExecutorService.execute(() -> mLabelsDao.editLabelOrder(label, newOrder));
    }

    public void toggleLabelArchive(String label, boolean archived) {
        mExecutorService.execute(() -> mLabelsDao.toggleLabelArchiveState(label, archived));
    }

    public void deleteLabel(String label) {
        mExecutorService.execute(() -> mLabelsDao.deleteLabel(label));
    }
}
