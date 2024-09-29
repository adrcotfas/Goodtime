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
    private val log: Logger
) : SettingsRepository {

    private object Keys {
        val productivityReminderSettingsKey =
            stringPreferencesKey("productivityReminderSettingsKey")
        val uiSettingsKey = stringPreferencesKey("uiSettingsKey")
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
                    Json.decodeFromString<ProductivityReminderSettings>(p)
                } ?: ProductivityReminderSettings(),
                uiSettings = it[Keys.uiSettingsKey]?.let { u ->
                    Json.decodeFromString<UiSettings>(u)
                } ?: UiSettings(),
                workdayStart = it[Keys.workdayStartKey] ?: AppSettings().workdayStart,
                firstDayOfWeek = it[Keys.firstDayOfWeekKey] ?: AppSettings().firstDayOfWeek,
                workFinishedSound = it[Keys.workFinishedSoundKey] ?: AppSettings().workFinishedSound,
                breakFinishedSound = it[Keys.breakFinishedSoundKey] ?: AppSettings().breakFinishedSound,
                userSounds = it[Keys.userSoundsKey]?.let { u ->
                    Json.decodeFromString<Set<SoundData>>(u)
                } ?: emptySet(),
                vibrationStrength = it[Keys.vibrationStrengthKey] ?: AppSettings().vibrationStrength,
                enableTorch = it[Keys.enableTorchKey] ?: AppSettings().enableTorch,
                overrideSoundProfile = it[Keys.overrideSoundProfile] ?: AppSettings().overrideSoundProfile,
                insistentNotification = it[Keys.insistentNotificationKey] ?: AppSettings().insistentNotification,
                autoStartWork = it[Keys.autoStartWorkKey] ?: AppSettings().autoStartWork,
                autoStartBreak = it[Keys.autoStartBreakKey] ?: AppSettings().autoStartBreak,
                labelName = it[Keys.labelNameKey] ?: AppSettings().labelName,
                longBreakData = it[Keys.longBreakDataKey]?.let { l ->
                    Json.decodeFromString<LongBreakData>(l)
                } ?: LongBreakData(),
                breakBudgetData = it[Keys.breakBudgetDataKey]?.let { b ->
                    Json.decodeFromString<BreakBudgetData>(b)
                } ?: BreakBudgetData(),
                notificationPermissionState = it[Keys.notificationPermissionStateKey]?.let { key ->
                    NotificationPermissionState.entries[key]
                } ?: AppSettings().notificationPermissionState

            )
        }.catch {
            log.e("Error parsing settings", it)
            emit(AppSettings())
        }.distinctUntilChanged()

    override suspend fun saveReminderSettings(
        settings: ProductivityReminderSettings
    ) {
        dataStore.edit {
            it[Keys.productivityReminderSettingsKey] = Json.encodeToString(settings)
        }
    }

    override suspend fun saveUiSettings(settings: UiSettings) {
        dataStore.edit { it[Keys.uiSettingsKey] = Json.encodeToString(settings) }
    }

    override suspend fun saveWorkDayStart(secondOfDay: Int) {
        dataStore.edit { it[Keys.workdayStartKey] = secondOfDay }
    }

    override suspend fun saveFirstDayOfWeek(dayOfWeek: Int) {
        dataStore.edit { it[Keys.firstDayOfWeekKey] = dayOfWeek }
    }

    override suspend fun saveWorkFinishedSound(sound: String?) {
        dataStore.edit { it[Keys.workFinishedSoundKey] = sound ?: "" }
    }

    override suspend fun saveBreakFinishedSound(sound: String?) {
        dataStore.edit { it[Keys.breakFinishedSoundKey] = sound ?: "" }
    }

    override suspend fun addUserSound(sound: SoundData) {
        dataStore.add(Keys.userSoundsKey, sound)
    }

    override suspend fun removeUserSound(sound: SoundData) {
        dataStore.remove(Keys.userSoundsKey, sound)
    }

    override suspend fun saveVibrationStrength(strength: Int) {
        dataStore.edit { it[Keys.vibrationStrengthKey] = strength }
    }

    override suspend fun saveEnableTorch(enabled: Boolean) {
        dataStore.edit { it[Keys.enableTorchKey] = enabled }
    }

    override suspend fun saveOverrideSoundProfile(enabled: Boolean) {
        dataStore.edit { it[Keys.overrideSoundProfile] = enabled }
    }

    override suspend fun saveInsistentNotification(enabled: Boolean) {
        dataStore.edit { it[Keys.insistentNotificationKey] = enabled }
    }

    override suspend fun saveAutoStartWork(enabled: Boolean) {
        dataStore.edit { it[Keys.autoStartWorkKey] = enabled }
    }

    override suspend fun saveAutoStartBreak(enabled: Boolean) {
        dataStore.edit { it[Keys.autoStartBreakKey] = enabled }
    }

    override suspend fun saveLongBreakData(longBreakData: LongBreakData) {
        dataStore.edit { it[Keys.longBreakDataKey] = Json.encodeToString(longBreakData) }
    }

    override suspend fun saveBreakBudgetData(breakBudgetData: BreakBudgetData) {
        dataStore.edit { it[Keys.breakBudgetDataKey] = Json.encodeToString(breakBudgetData) }
    }

    override suspend fun saveNotificationPermissionState(state: NotificationPermissionState) {
        dataStore.edit { it[Keys.notificationPermissionStateKey] = state.ordinal }
    }

    override suspend fun activateLabelWithName(labelName: String) {
        dataStore.edit { it[Keys.labelNameKey] = labelName }
    }

    override suspend fun activateDefaultLabel() = activateLabelWithName(Label.DEFAULT_LABEL_NAME)
}