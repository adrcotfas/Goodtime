package com.apps.adrcotfas.goodtime.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual val platformModule: Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()
    }

    single<DataStore<Preferences>>(named(SETTINGS_NAME)) {
        getDataStore(
            producePath = {
                val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
                requireNotNull(documentDirectory).path + "/$SETTINGS_FILE_NAME"
            }
        )
    }
}
actual fun isDebug(): Boolean = Platform.isDebugBinary
