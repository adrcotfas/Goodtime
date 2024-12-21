package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.AppSettings
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
import com.apps.adrcotfas.goodtime.data.settings.NotificationPermissionState
import com.apps.adrcotfas.goodtime.data.settings.SoundData
import com.apps.adrcotfas.goodtime.data.settings.ProductivityReminderSettings
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import com.apps.adrcotfas.goodtime.data.settings.UiSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(settings: AppSettings = AppSettings()) : SettingsRepository {

    private val _settings = MutableStateFlow(settings)
    override val settings: Flow<AppSettings> = _settings

    override suspend fun updateReminderSettings(transform: (ProductivityReminderSettings) -> ProductivityReminderSettings) =
        _settings.emit(
            _settings.value.copy(
                productivityReminderSettings = transform(
                    ProductivityReminderSettings()
                )
            )
        )

    override suspend fun updateUiSettings(transform: (UiSettings) -> UiSettings) = _settings.emit(
        _settings.value.copy(uiSettings = transform(UiSettings()))
    )

    override suspend fun updateTimerStyle(transform: (TimerStyleData) -> TimerStyleData) {
        _settings.emit(
            _settings.value.copy(timerStyle = transform(TimerStyleData()))
        )
    }

    override suspend fun setWorkDayStart(secondOfDay: Int) {
        _settings.emit(
            _settings.value.copy(workdayStart = secondOfDay)
        )
    }

    override suspend fun setFirstDayOfWeek(dayOfWeek: Int) {
        _settings.emit(
            _settings.value.copy(firstDayOfWeek = dayOfWeek)
        )
    }

    override suspend fun setWorkFinishedSound(sound: String?) = _settings.emit(
        _settings.value.copy(workFinishedSound = sound ?: "")
    )

    override suspend fun setBreakFinishedSound(sound: String?) = _settings.emit(
        _settings.value.copy(breakFinishedSound = sound ?: "")
    )

    override suspend fun addUserSound(sound: SoundData) {
        val existingSounds = _settings.value.userSounds.toMutableSet()
        existingSounds.add(sound)
        _settings.emit(
            _settings.value.copy(userSounds = existingSounds)
        )
    }

    override suspend fun removeUserSound(sound: SoundData) {
        val existingSounds = _settings.value.userSounds.toMutableSet()
        existingSounds.remove(sound)
        _settings.emit(
            _settings.value.copy(userSounds = existingSounds)
        )
    }

    override suspend fun setVibrationStrength(strength: Int) = _settings.emit(
        _settings.value.copy(vibrationStrength = strength)
    )

    override suspend fun setEnableTorch(enabled: Boolean) = _settings.emit(
        _settings.value.copy(enableTorch = enabled)
    )

    override suspend fun setOverrideSoundProfile(enabled: Boolean) = _settings.emit(
        _settings.value.copy(overrideSoundProfile = enabled)
    )

    override suspend fun setInsistentNotification(enabled: Boolean) = _settings.emit(
        _settings.value.copy(insistentNotification = enabled)
    )

    override suspend fun setAutoStartWork(enabled: Boolean) = _settings.emit(
        _settings.value.copy(autoStartWork = enabled)
    )

    override suspend fun setAutoStartBreak(enabled: Boolean) = _settings.emit(
        _settings.value.copy(autoStartBreak = enabled)
    )

    override suspend fun activateLabelWithName(labelName: String) {
        _settings.emit(
            _settings.value.copy(labelName = labelName)
        )
    }

    override suspend fun activateDefaultLabel() {
        _settings.emit(
            _settings.value.copy(labelName = Label.DEFAULT_LABEL_NAME)
        )
    }

    override suspend fun setLongBreakData(longBreakData: LongBreakData) {
        _settings.emit(
            _settings.value.copy(longBreakData = longBreakData)
        )
    }

    override suspend fun setBreakBudgetData(breakBudgetData: BreakBudgetData) {
        _settings.emit(
            _settings.value.copy(breakBudgetData = breakBudgetData)
        )
    }

    override suspend fun setNotificationPermissionState(state: NotificationPermissionState) {
        _settings.emit(
            _settings.value.copy(notificationPermissionState = state)
        )
    }
}