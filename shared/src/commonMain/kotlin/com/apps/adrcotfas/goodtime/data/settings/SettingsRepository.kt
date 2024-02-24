package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.coroutines.flow.Flow

/**
 * Repository for the app settings.
 */
interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun saveReminderSettings(settings: ProductivityReminderSettings)
    suspend fun saveUiSettings(settings: UiSettings)
    suspend fun saveNotificationSoundEnabled(enabled: Boolean)
    suspend fun saveWorkFinishedSound(sound: String?)
    suspend fun saveBreakFinishedSound(sound: String?)
    suspend fun saveVibrationStrength(strength: VibrationStrength)
    suspend fun saveFlashType(type: FlashType)
    suspend fun saveInsistentNotification(enabled: Boolean)
    suspend fun saveAutoStartWork(enabled: Boolean)
    suspend fun saveAutoStartBreak(enabled: Boolean)
    suspend fun saveDndDuringWork(enabled: Boolean)
    suspend fun saveLabelName(labelName: String?)
    suspend fun saveLongBreakData(longBreakData: LongBreakData)
    suspend fun saveBreakBudgetData(breakBudgetData: BreakBudgetData)
}