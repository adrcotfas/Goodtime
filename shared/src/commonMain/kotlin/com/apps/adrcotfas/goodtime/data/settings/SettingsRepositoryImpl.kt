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
package com.apps.adrcotfas.goodtime.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.apps.adrcotfas.goodtime.data.model.Label
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val log: Logger,
) : SettingsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val productivityReminderSettingsKey =
            stringPreferencesKey("productivityReminderSettingsKey")
        val uiSettingsKey = stringPreferencesKey("uiSettingsKey")
        val timerStyleKey = stringPreferencesKey("timerStyleKey")
        val workdayStartKey = intPreferencesKey("workdayStartKey")
        val firstDayOfWeekKey = intPreferencesKey("firstDayOfWeekKey")
        val workFinishedSoundKey = stringPreferencesKey("workFinishedSoundKey")
        val breakFinishedSoundKey = stringPreferencesKey("breakFinishedSoundKey")
        val userSoundsKey = stringPreferencesKey("userSoundsKey")
        val vibrationStrengthKey = intPreferencesKey("vibrationStrengthKey")
        val enableTorchKey = booleanPreferencesKey("enableTorchKey")
        val overrideSoundProfile = booleanPreferencesKey("overrideSoundProfileKey")
        val insistentNotificationKey = booleanPreferencesKey("insistentNotificationKey")
        val autoStartWorkKey = booleanPreferencesKey("autoStartWorkKey")
        val autoStartBreakKey = booleanPreferencesKey("autoStartBreakKey")
        val labelNameKey = stringPreferencesKey("labelNameKey")
        val longBreakDataKey = stringPreferencesKey("longBreakDataKey")
        val breakBudgetDataKey = stringPreferencesKey("breakBudgetDataKey")
        val notificationPermissionStateKey = intPreferencesKey("notificationPermissionStateKey")
    }

    override val settings: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                log.e("Error reading settings", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            AppSettings(
                productivityReminderSettings = it[Keys.productivityReminderSettingsKey]?.let { p ->
                    json.decodeFromString<ProductivityReminderSettings>(p)
                } ?: ProductivityReminderSettings(),
                uiSettings = it[Keys.uiSettingsKey]?.let { u ->
                    json.decodeFromString<UiSettings>(u)
                } ?: UiSettings(),
                timerStyle = it[Keys.timerStyleKey]?.let { t ->
                    json.decodeFromString<TimerStyleData>(t)
                } ?: TimerStyleData(),
                workdayStart = it[Keys.workdayStartKey] ?: AppSettings().workdayStart,
                firstDayOfWeek = it[Keys.firstDayOfWeekKey] ?: AppSettings().firstDayOfWeek,
                workFinishedSound = it[Keys.workFinishedSoundKey]
                    ?: AppSettings().workFinishedSound,
                breakFinishedSound = it[Keys.breakFinishedSoundKey]
                    ?: AppSettings().breakFinishedSound,
                userSounds = it[Keys.userSoundsKey]?.let { u ->
                    json.decodeFromString<Set<SoundData>>(u)
                } ?: emptySet(),
                vibrationStrength = it[Keys.vibrationStrengthKey]
                    ?: AppSettings().vibrationStrength,
                enableTorch = it[Keys.enableTorchKey] ?: AppSettings().enableTorch,
                overrideSoundProfile = it[Keys.overrideSoundProfile]
                    ?: AppSettings().overrideSoundProfile,
                insistentNotification = it[Keys.insistentNotificationKey]
                    ?: AppSettings().insistentNotification,
                autoStartWork = it[Keys.autoStartWorkKey] ?: AppSettings().autoStartWork,
                autoStartBreak = it[Keys.autoStartBreakKey] ?: AppSettings().autoStartBreak,
                labelName = it[Keys.labelNameKey] ?: AppSettings().labelName,
                longBreakData = it[Keys.longBreakDataKey]?.let { l ->
                    json.decodeFromString<LongBreakData>(l)
                } ?: LongBreakData(),
                breakBudgetData = it[Keys.breakBudgetDataKey]?.let { b ->
                    json.decodeFromString<BreakBudgetData>(b)
                } ?: BreakBudgetData(),
                notificationPermissionState = it[Keys.notificationPermissionStateKey]?.let { key ->
                    NotificationPermissionState.entries[key]
                } ?: AppSettings().notificationPermissionState,
            )
        }.catch {
            log.e("Error parsing settings", it)
            emit(AppSettings())
        }.distinctUntilChanged()

    override suspend fun updateReminderSettings(
        transform: (ProductivityReminderSettings) -> ProductivityReminderSettings,
    ) {
        dataStore.edit {
            val previous =
                it[Keys.productivityReminderSettingsKey]?.let { p -> json.decodeFromString(p) }
                    ?: ProductivityReminderSettings()
            val new = transform(previous)
            it[Keys.productivityReminderSettingsKey] = json.encodeToString(new)
        }
    }

    override suspend fun updateUiSettings(
        transform: (UiSettings) -> UiSettings,
    ) {
        dataStore.edit {
            val previous = it[Keys.uiSettingsKey]?.let { u -> json.decodeFromString(u) }
                ?: UiSettings()
            val new = transform(previous)
            it[Keys.uiSettingsKey] = json.encodeToString(new)
        }
    }

    override suspend fun updateTimerStyle(
        transform: (TimerStyleData) -> TimerStyleData,
    ) {
        dataStore.edit {
            val previous = it[Keys.timerStyleKey]?.let { t ->
                try {
                    json.decodeFromString(t)
                } catch (e: Exception) {
                    null
                }
            } ?: TimerStyleData()
            val new = transform(previous)
            it[Keys.timerStyleKey] = json.encodeToString(new)
        }
    }

    override suspend fun setWorkDayStart(secondOfDay: Int) {
        dataStore.edit { it[Keys.workdayStartKey] = secondOfDay }
    }

    override suspend fun setFirstDayOfWeek(dayOfWeek: Int) {
        dataStore.edit { it[Keys.firstDayOfWeekKey] = dayOfWeek }
    }

    override suspend fun setWorkFinishedSound(sound: String?) {
        dataStore.edit { it[Keys.workFinishedSoundKey] = sound ?: "" }
    }

    override suspend fun setBreakFinishedSound(sound: String?) {
        dataStore.edit { it[Keys.breakFinishedSoundKey] = sound ?: "" }
    }

    override suspend fun addUserSound(sound: SoundData) {
        dataStore.add(Keys.userSoundsKey, sound)
    }

    override suspend fun removeUserSound(sound: SoundData) {
        dataStore.remove(Keys.userSoundsKey, sound)
    }

    override suspend fun setVibrationStrength(strength: Int) {
        dataStore.edit { it[Keys.vibrationStrengthKey] = strength }
    }

    override suspend fun setEnableTorch(enabled: Boolean) {
        dataStore.edit { it[Keys.enableTorchKey] = enabled }
    }

    override suspend fun setOverrideSoundProfile(enabled: Boolean) {
        dataStore.edit { it[Keys.overrideSoundProfile] = enabled }
    }

    override suspend fun setInsistentNotification(enabled: Boolean) {
        dataStore.edit { it[Keys.insistentNotificationKey] = enabled }
    }

    override suspend fun setAutoStartWork(enabled: Boolean) {
        dataStore.edit { it[Keys.autoStartWorkKey] = enabled }
    }

    override suspend fun setAutoStartBreak(enabled: Boolean) {
        dataStore.edit { it[Keys.autoStartBreakKey] = enabled }
    }

    override suspend fun setLongBreakData(longBreakData: LongBreakData) {
        dataStore.edit { it[Keys.longBreakDataKey] = json.encodeToString(longBreakData) }
    }

    override suspend fun setBreakBudgetData(breakBudgetData: BreakBudgetData) {
        dataStore.edit { it[Keys.breakBudgetDataKey] = json.encodeToString(breakBudgetData) }
    }

    override suspend fun setNotificationPermissionState(state: NotificationPermissionState) {
        dataStore.edit { it[Keys.notificationPermissionStateKey] = state.ordinal }
    }

    override suspend fun activateLabelWithName(labelName: String) {
        dataStore.edit { it[Keys.labelNameKey] = labelName }
    }

    override suspend fun activateDefaultLabel() = activateLabelWithName(Label.DEFAULT_LABEL_NAME)
}
