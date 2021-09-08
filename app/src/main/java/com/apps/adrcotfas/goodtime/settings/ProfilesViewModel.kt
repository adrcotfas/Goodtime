package com.apps.adrcotfas.goodtime.settings

import com.apps.adrcotfas.goodtime.database.AppDatabase.Companion.getDatabase
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.apps.adrcotfas.goodtime.database.ProfileDao
import androidx.lifecycle.LiveData
import com.apps.adrcotfas.goodtime.database.Profile
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: ProfileDao = getDatabase(application).profileDao()
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    val profiles: LiveData<List<Profile>>
        get() = dao.profiles

    fun addProfile(profile: Profile) {
        executorService.execute { dao.addProfile(profile) }
    }

    fun deleteProfile(name: String) {
        executorService.execute { dao.deleteProfile(name) }
    }

}