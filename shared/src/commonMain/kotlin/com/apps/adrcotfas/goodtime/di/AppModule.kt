package com.apps.adrcotfas.goodtime.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.apps.adrcotfas.goodtime.data.local.Database
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import com.apps.adrcotfas.goodtime.data.local.DatabaseExt.invoke
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepositoryImpl
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepositoryImpl
import com.apps.adrcotfas.goodtime.data.timer.TimerDataRepository
import com.apps.adrcotfas.goodtime.data.timer.TimerDataRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import okio.Path.Companion.toPath
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
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
    single<LocalDataRepository> {
        LocalDataRepositoryImpl(Database(driver = get<DatabaseDriverFactory>().create()))
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(get<DataStore<Preferences>>(named(SETTINGS_NAME)))
    }
    single<TimerDataRepository> {
        TimerDataRepositoryImpl(
            get<DataStore<Preferences>>(named(TIMER_DATA_NAME)),
            get<LocalDataRepository>(),
            CoroutineScope(Dispatchers.IO)
        )
    }
}

internal const val SETTINGS_NAME = "productivity_settings.preferences"
internal const val SETTINGS_FILE_NAME = SETTINGS_NAME + "_pb"
internal const val TIMER_DATA_NAME = "productivity_timer_data.preferences"
internal const val TIMER_DATA_FILE_NAME = TIMER_DATA_NAME + "_pb"

internal fun getDataStore(producePath: () -> String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
}