package com.apps.adrcotfas.goodtime.Main;

import android.app.Application;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.Database.LabelAndColorDao;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import static com.apps.adrcotfas.goodtime.Statistics.Utils.getInstanceTotalLabel;

public class LabelsViewModel extends AndroidViewModel {

    private LabelAndColorDao mLabelsDao;
    private ExecutorService mExecutorService;

    /**
     * The current selected label in the Statistics view
     * "extended" because it might be "total" or "unlabeled"
     */
    public MutableLiveData<LabelAndColor> crtExtendedLabel = new MutableLiveData<>();

    /**
     * Used to signal the current visible fragment in StatisticsActivity
     */
    public MutableLiveData<Boolean> mIsMainView = new MutableLiveData<>();

    public LabelsViewModel(@NonNull Application application) {
        super(application);
        mLabelsDao = AppDatabase.getDatabase(application).labelAndColor();
        mExecutorService = Executors.newSingleThreadExecutor();
        crtExtendedLabel.setValue(getInstanceTotalLabel(application.getBaseContext()));
        mIsMainView.setValue(true);
    }

    public LiveData<List<LabelAndColor>> getLabels() {
        return mLabelsDao.getLabels();
    }

    public LiveData<Integer> getColorOfLabel(String label) {
        return mLabelsDao.getColor(label);
    }

    public void addLabel(LabelAndColor labelAndColor) {
        mExecutorService.execute(() -> mLabelsDao.addLabel(labelAndColor));
    }

    public void updateLabel(String label, String newLabel, int color) {
        mExecutorService.execute(() -> mLabelsDao.updateLabel(label, newLabel, color));
    }

    public void editLabelName(String label, String newLabel) {
        mExecutorService.execute(() -> mLabelsDao.editLabelName(label, newLabel));
    }

    public void editLabelColor(String label, int color) {
        mExecutorService.execute(() -> mLabelsDao.editLabelColor(label, color));
    }

    public void deleteLabel(String label) {
        mExecutorService.execute(() -> mLabelsDao.deleteLabel(label));
    }
}
