package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.AppSettings
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.FlashType
import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
import com.apps.adrcotfas.goodtime.data.settings.SoundData
import com.apps.adrcotfas.goodtime.data.settings.ProductivityReminderSettings
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.UiSettings
import com.apps.adrcotfas.goodtime.data.settings.VibrationStrength
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(settings: AppSettings = AppSettings()) : SettingsRepository {

    private val _settings = MutableStateFlow(settings)
    override val settings: Flow<AppSettings> = _settings

    override suspend fun saveReminderSettings(settings: ProductivityReminderSettings) =
        _settings.emit(
            _settings.value.copy(productivityReminderSettings = settings)
        )

    override suspend fun saveUiSettings(settings: UiSettings) = _settings.emit(
        _settings.value.copy(uiSettings = settings)
    )

    override suspend fun saveWorkDayStart(secondOfDay: Int) {
        _settings.emit(
            _settings.value.copy(workdayStart = secondOfDay)
        )
    }

    override suspend fun saveFirstDayOfWeek(dayOfWeek: Int) {
        _settings.emit(
            _settings.value.copy(firstDayOfWeek = dayOfWeek)
        )
    }

    override suspend fun saveWorkFinishedSound(sound: String?) = _settings.emit(
        _settings.value.copy(workFinishedSound = sound ?: "")
    )

    override suspend fun saveBreakFinishedSound(sound: String?) = _settings.emit(
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

    override suspend fun saveVibrationStrength(strength: VibrationStrength) = _settings.emit(
        _settings.value.copy(vibrationStrength = strength)
    )

    override suspend fun saveFlashType(type: FlashType) = _settings.emit(
        _settings.value.copy(flashType = type)
    )

    override suspend fun saveInsistentNotification(enabled: Boolean) = _settings.emit(
        _settings.value.copy(insistentNotification = enabled)
    )

    override suspend fun saveAutoStartWork(enabled: Boolean) = _settings.emit(
        _settings.value.copy(autoStartWork = enabled)
    )

    override suspend fun saveAutoStartBreak(enabled: Boolean) = _settings.emit(
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

    override suspend fun saveLongBreakData(longBreakData: LongBreakData) {
        _settings.emit(
            _settings.value.copy(longBreakData = longBreakData)
        )
    }

    override suspend fun saveBreakBudgetData(breakBudgetData: BreakBudgetData) {
        _settings.emit(
            _settings.value.copy(breakBudgetData = breakBudgetData)
        )
    }
}