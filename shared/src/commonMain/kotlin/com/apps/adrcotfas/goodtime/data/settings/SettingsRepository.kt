package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.coroutines.flow.Flow

/**
 * Repository for the app settings.
 */
interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun saveReminderSettings(settings: ProductivityReminderSettings)
    suspend fun saveUiSettings(settings: UiSettings)
    suspend fun saveWorkDayStart(secondOfDay: Int)
    suspend fun saveFirstDayOfWeek(dayOfWeek: Int)
    suspend fun saveWorkFinishedSound(sound: String?)
    suspend fun saveBreakFinishedSound(sound: String?)
    suspend fun addUserSound(sound: SoundData)
    suspend fun removeUserSound(sound: SoundData)
    suspend fun saveVibrationStrength(strength: Int)
    suspend fun saveFlashType(type: FlashType)
    suspend fun saveInsistentNotification(enabled: Boolean)
    suspend fun saveAutoStartWork(enabled: Boolean)
    suspend fun saveAutoStartBreak(enabled: Boolean)
    suspend fun saveLongBreakData(longBreakData: LongBreakData)
    suspend fun saveBreakBudgetData(breakBudgetData: BreakBudgetData)
    suspend fun activateLabelWithName(labelName: String)
    suspend fun activateDefaultLabel()
}