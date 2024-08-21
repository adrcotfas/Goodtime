package com.apps.adrcotfas.goodtime.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.data.settings.AppSettings
import com.apps.adrcotfas.goodtime.data.settings.DarkModePreference
import com.apps.adrcotfas.goodtime.data.settings.FlashType
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

data class SettingsUiState(
    val showTimePicker: Boolean = false,
    val showWorkdayStartPicker: Boolean = false,
    val showSelectWorkSoundPicker: Boolean = false,
    val showSelectBreakSoundPicker: Boolean = false,
    val notificationSoundCandidate: String? = null
)

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    val settings =
        settingsRepository.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AppSettings()
        )

    fun setUseDynamicColor(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUiSettings(settings.value.uiSettings.copy(useDynamicColor = enable))
        }
    }

    fun onToggleProductivityReminderDay(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            val days = settings.value.productivityReminderSettings.days
            val alreadyEnabled = days.contains(dayOfWeek.isoDayNumber)
            settingsRepository.saveReminderSettings(
                settings.value.productivityReminderSettings.copy(
                    days = if (alreadyEnabled) days - dayOfWeek.isoDayNumber else days + dayOfWeek.isoDayNumber
                )
            )
        }
    }

    fun setShowTimePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTimePicker = show)
    }

    fun setReminderTime(secondOfDay: Int) {
        viewModelScope.launch {
            settingsRepository.saveReminderSettings(
                settings.value.productivityReminderSettings.copy(secondOfDay = secondOfDay)
            )
        }
    }

    fun setThemeOption(themePreference: DarkModePreference) {
        viewModelScope.launch {
            settingsRepository.saveUiSettings(settings.value.uiSettings.copy(darkModePreference = themePreference))
        }
    }

    fun setFullscreenMode(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUiSettings(settings.value.uiSettings.copy(fullscreenMode = enable))
        }
    }

    fun setKeepScreenOn(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUiSettings(settings.value.uiSettings.copy(keepScreenOn = enable))
            if (!enable && settings.value.uiSettings.screensaverMode) {
                settingsRepository.saveUiSettings(settings.value.uiSettings.copy(screensaverMode = false))
            }
            if (!enable && settings.value.flashType == FlashType.SCREEN) {
                setFlashType(FlashType.OFF)
            }
        }
    }

    fun setScreensaverMode(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUiSettings(settings.value.uiSettings.copy(screensaverMode = enable))
        }
    }

    fun setWorkDayStart(secondOfDay: Int) {
        viewModelScope.launch {
            settingsRepository.saveWorkDayStart(secondOfDay)
        }
    }

    fun setFirstDayOfWeek(dayOfWeek: Int) {
        viewModelScope.launch {
            settingsRepository.saveFirstDayOfWeek(dayOfWeek)
        }
    }

    fun setShowWorkdayStartPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showWorkdayStartPicker = show)
    }

    fun setVibrationStrength(vibrationStrength: Int) {
        viewModelScope.launch {
            settingsRepository.saveVibrationStrength(vibrationStrength)
        }
    }

    fun setFlashType(flashType: FlashType) {
        viewModelScope.launch {
            settingsRepository.saveFlashType(flashType)
            if (flashType == FlashType.SCREEN && !settings.value.uiSettings.keepScreenOn) {
                setKeepScreenOn(true)
            }
        }
    }

    fun setInsistentNotification(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveInsistentNotification(enable)
            if (enable) {
                if (settings.value.autoStartWork) {
                    setAutoStartWork(false)
                }
                if (settings.value.autoStartBreak) {
                    setAutoStartBreak(false)
                }
            }
        }
    }

    fun setAutoStartWork(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveAutoStartWork(enable)
            if (enable) {
                if (settings.value.insistentNotification) {
                    setInsistentNotification(false)
                }
            }
        }
    }

    fun setAutoStartBreak(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveAutoStartBreak(enable)
            if (enable) {
                if (settings.value.insistentNotification) {
                    setInsistentNotification(false)
                }
            }
        }
    }

    fun setDndDuringWork(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUiSettings(settings.value.uiSettings.copy(dndDuringWork = enable))
        }
    }

    fun setWorkFinishedSound(ringtone: String) {
        viewModelScope.launch {
            settingsRepository.saveWorkFinishedSound(ringtone)
        }
    }

    fun setBreakFinishedSound(ringtone: String) {
        viewModelScope.launch {
            settingsRepository.saveBreakFinishedSound(ringtone)
        }
    }

    fun setShowSelectWorkSoundPicker(show: Boolean) {
        _uiState.value =
            _uiState.value.copy(showSelectWorkSoundPicker = show, notificationSoundCandidate = null)
    }

    fun setShowSelectBreakSoundPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSelectBreakSoundPicker = show)
    }

    fun setNotificationSoundCandidate(uri: String) {
        _uiState.value = _uiState.value.copy(notificationSoundCandidate = uri)
    }

    companion object {
        val firstDayOfWeekOptions = listOf(
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY
        )
    }
}