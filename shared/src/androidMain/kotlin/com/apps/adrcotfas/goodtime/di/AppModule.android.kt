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
            producePath = { get<Context>().filesDir.resolve(SETTINGS_FILE_NAME).absolutePath },
        )
    }
    single<List<EventListener>> {
        listOf(
            get<EventListener>(named(EventListener.TIMER_SERVICE_HANDLER)),
            get<EventListener>(named(EventListener.ALARM_MANAGER_HANDLER)),
            get<EventListener>(named(EventListener.SOUND_AND_VIBRATION_PLAYER)),
        )
    }
}

actual fun isDebug(): Boolean = BuildConfig.DEBUG
