package com.apps.adrcotfas.goodtime.Main;

import android.app.Application;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.Database.LabelAndColorDao;
import com.apps.adrcotfas.goodtime.LabelAndColor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class LabelsViewModel extends AndroidViewModel {

    private LabelAndColorDao mLabelsDao;
    private ExecutorService mExecutorService;

    public LabelsViewModel(@NonNull Application application) {
        super(application);
        mLabelsDao = AppDatabase.getDatabase(application).labelAndColor();
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<LabelAndColor>> getLabels() {
        return mLabelsDao.getLabels();
    }

    public void editLabelName(String label, String newLabel) {
        mExecutorService.execute(() -> mLabelsDao.editLabelName(label, newLabel));
    }

    public void deleteLabel(String label) {
        mExecutorService.execute(() -> mLabelsDao.deleteLabel(label));
    }
}
