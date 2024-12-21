package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.coroutines.flow.Flow

/**
 * Repository for the app settings.
 */
interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun updateReminderSettings(transform: (ProductivityReminderSettings) -> ProductivityReminderSettings)
    suspend fun updateUiSettings(transform: (UiSettings) -> UiSettings)
    suspend fun updateTimerStyle(transform: (TimerStyleData) -> TimerStyleData)
    suspend fun setWorkDayStart(secondOfDay: Int)
    suspend fun setFirstDayOfWeek(dayOfWeek: Int)
    suspend fun setWorkFinishedSound(sound: String?)
    suspend fun setBreakFinishedSound(sound: String?)
    suspend fun addUserSound(sound: SoundData)
    suspend fun removeUserSound(sound: SoundData)
    suspend fun setVibrationStrength(strength: Int)
    suspend fun setEnableTorch(enabled: Boolean)
    suspend fun setOverrideSoundProfile(enabled: Boolean)
    suspend fun setInsistentNotification(enabled: Boolean)
    suspend fun setAutoStartWork(enabled: Boolean)
    suspend fun setAutoStartBreak(enabled: Boolean)
    suspend fun setLongBreakData(longBreakData: LongBreakData)
    suspend fun setBreakBudgetData(breakBudgetData: BreakBudgetData)
    suspend fun setNotificationPermissionState(state: NotificationPermissionState)
    suspend fun activateLabelWithName(labelName: String)
    suspend fun activateDefaultLabel()
}