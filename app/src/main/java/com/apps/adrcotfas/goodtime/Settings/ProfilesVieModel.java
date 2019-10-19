package com.apps.adrcotfas.goodtime.Settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.Database.ProfileDao;
import com.apps.adrcotfas.goodtime.Profile;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfilesVieModel extends AndroidViewModel {

    private final ProfileDao mProfilesDao;
    private final ExecutorService mExecutorService;

    public ProfilesVieModel(@NonNull Application application) {
        super(application);
        mProfilesDao = AppDatabase.getDatabase(application).profileDao();
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Profile>> getProfiles() {
        return mProfilesDao.getProfiles();
    }

    public void addProfile(Profile profile) {
        mExecutorService.execute(() -> mProfilesDao.addProfile(profile));
    }

    public void deleteProfile(String name) {
        mExecutorService.execute(() -> mProfilesDao.deleteProfile(name));
    }

}
