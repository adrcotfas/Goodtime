package com.apps.adrcotfas.goodtime.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import com.apps.adrcotfas.goodtime.bl.EventListener
import com.apps.adrcotfas.goodtime.bl.TimerServiceHandler
import com.apps.adrcotfas.goodtime.shared.BuildConfig
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory(get<Context>())
    }
    single<DataStore<Preferences>>(named(SETTINGS_NAME)) {
        getDataStore(
            producePath = { get<Context>().filesDir.resolve(SETTINGS_FILE_NAME).absolutePath }
        )
    }
    single<List<EventListener>> {
        listOf(get<TimerServiceHandler>())
    }
}

actual fun isDebug(): Boolean = BuildConfig.DEBUG