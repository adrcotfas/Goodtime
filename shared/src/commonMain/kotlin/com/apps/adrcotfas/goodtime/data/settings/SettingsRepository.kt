package com.apps.adrcotfas.goodtime.data.settings

import com.apps.adrcotfas.goodtime.data.TimerProfile
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun saveReminderSettings(settings: ProductivityReminderSettings)
    suspend fun saveUiSettings(settings: UiSettings)
    suspend fun saveDefaultTimerProfile(settings: TimerProfile)
    suspend fun saveCurrentLabelName(label: String)
    suspend fun saveCurrentLabelNameAsUnlabeled()
    suspend fun saveNotificationSoundEnabled(enabled: Boolean)
    suspend fun saveWorkFinishedSound(sound: String?)
    suspend fun saveBreakFinishedSound(sound: String?)
    suspend fun saveVibrationStrength(strength: VibrationStrength)
    suspend fun saveFlashType(type: FlashType)
    suspend fun saveInsistentNotification(enabled: Boolean)
    suspend fun saveAutoStartWork(enabled: Boolean)
    suspend fun saveAutoStartBreak(enabled: Boolean)
    suspend fun saveDndDuringWork(enabled: Boolean)
}