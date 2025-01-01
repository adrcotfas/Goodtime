/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.apps.adrcotfas.goodtime.bl.FinishedSessionsHandler
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimeProviderImpl
import com.apps.adrcotfas.goodtime.data.local.Database
import com.apps.adrcotfas.goodtime.data.local.DatabaseExt.invoke
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepositoryImpl
import com.apps.adrcotfas.goodtime.data.local.backup.BackupManager
import com.apps.adrcotfas.goodtime.data.local.backup.BackupPrompter
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

private val coroutineScopeModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}

fun insertKoin(appModule: Module): KoinApplication {
    return startKoin {
        modules(
            appModule,
            coroutineScopeModule,
            platformModule,
            coreModule,
            localDataModule,
            timerManagerModule,
            viewModelModule,
        )
    }
}

expect fun isDebug(): Boolean
private val String.withPrefixIfDebug
    get() = if (isDebug()) "### $this" else this

expect val platformModule: Module

private val coreModule = module {
    val baseLogger =
        Logger(
            config = StaticConfig(
                logWriterList = listOf(platformLogWriter()),
                minSeverity = if (isDebug()) Severity.Verbose else Severity.Info,
            ),
            tag = "Goodtime".withPrefixIfDebug,
        )
    factory { (tag: String?) -> if (tag != null) baseLogger.withTag(tag.withPrefixIfDebug) else baseLogger }

    single<LocalDataRepository> {
        LocalDataRepositoryImpl(Database(driver = get<SqlDriver>()))
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            get<DataStore<Preferences>>(named(SETTINGS_NAME)),
            getWith(SettingsRepository::class.simpleName),
        )
    }
    single<TimeProvider> {
        TimeProviderImpl()
    }

    single<FinishedSessionsHandler> {
        FinishedSessionsHandler(
            get<CoroutineScope>(),
            get<LocalDataRepository>(),
            getWith(FinishedSessionsHandler::class.simpleName),
        )
    }

    single<BackupManager> {
        BackupManager(
            get<FileSystem>(),
            get<String>(named(DB_PATH_KEY)),
            get<String>(named(FILES_DIR_PATH_KEY)),
            get<SqlDriver>(),
            get<TimeProvider>(),
            get<BackupPrompter>(),
            get<LocalDataRepository>(),
            getWith(BackupManager::class.simpleName),
        )
    }
}

internal const val SETTINGS_NAME = "productivity_settings.preferences"
internal const val SETTINGS_FILE_NAME = SETTINGS_NAME + "_pb"
internal const val DB_PATH_KEY = "db_path"
internal const val FILES_DIR_PATH_KEY = "tmp_path"

internal fun getDataStore(producePath: () -> String): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
}

inline fun <reified T> Scope.getWith(vararg params: Any?): T {
    return get(parameters = { parametersOf(*params) })
}

fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }
