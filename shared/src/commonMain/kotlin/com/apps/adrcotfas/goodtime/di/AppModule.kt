package com.apps.adrcotfas.goodtime.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.apps.adrcotfas.goodtime.data.local.Database
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import com.apps.adrcotfas.goodtime.data.local.DatabaseHelper
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepositoryImpl
import okio.Path.Companion.toPath
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun insertKoin(appModule: Module): KoinApplication {
    val koinApplication = startKoin {
        modules(
            appModule,
            platformModule,
            coreModule
        )
    }
    return koinApplication
}

expect val platformModule: Module

private val coreModule = module {
    single<DatabaseHelper> {
        DatabaseHelper(Database(driver = get<DatabaseDriverFactory>().create()))
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(get<DataStore<Preferences>>())
    }
}

internal const val dataStoreFileName = "goodtime_productivity_preferences"
internal fun getDataStore(producePath: () -> String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
}