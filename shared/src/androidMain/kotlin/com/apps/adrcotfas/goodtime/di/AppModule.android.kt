package com.apps.adrcotfas.goodtime.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apps.adrcotfas.goodtime.bl.ALARM_MANAGER_HANDLER
import com.apps.adrcotfas.goodtime.bl.EventListener
import com.apps.adrcotfas.goodtime.bl.SOUND_AND_VIBRATION_PLAYER
import com.apps.adrcotfas.goodtime.bl.TIMER_SERVICE_HANDLER
import com.apps.adrcotfas.goodtime.data.local.DATABASE_NAME
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import com.apps.adrcotfas.goodtime.shared.BuildConfig
import okio.FileSystem
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DatabaseDriverFactory> { DatabaseDriverFactory(get<Context>()) }
    single<FileSystem> { FileSystem.SYSTEM }
    single<String>(named(DB_PATH_KEY)) { getDbPath { get<Context>().getDatabasePath(DATABASE_NAME).absolutePath } }
    single<String>(named(FILES_DIR_PATH_KEY)) { getTmpPath { get<Context>().filesDir.absolutePath + "/tmp" } }

    single<DataStore<Preferences>>(named(SETTINGS_NAME)) {
        getDataStore(
            producePath = { get<Context>().filesDir.resolve(SETTINGS_FILE_NAME).absolutePath }
        )
    }
    single<List<EventListener>> {
        listOf(
            get<EventListener>(named(EventListener.TIMER_SERVICE_HANDLER)),
            get<EventListener>(named(EventListener.ALARM_MANAGER_HANDLER)),
            get<EventListener>(named(EventListener.SOUND_AND_VIBRATION_PLAYER))
        )
    }
}

actual fun isDebug(): Boolean = BuildConfig.DEBUG