package com.apps.adrcotfas.goodtime.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()
    }

    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )

    single<DataStore<Preferences>>(named(SETTINGS_NAME)) {
        getDataStore(
            producePath = {
                requireNotNull(documentDirectory).path + "/$SETTINGS_FILE_NAME"
            }
        )
    }
    single<DataStore<Preferences>>(named(TIMER_DATA_NAME)) {
        getDataStore(
            producePath = {
                requireNotNull(documentDirectory).path + "/$TIMER_DATA_FILE_NAME"
            }
        )
    }
}