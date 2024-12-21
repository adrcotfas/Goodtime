package com.apps.adrcotfas.goodtime.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.data.settings.AppSettings
import com.apps.adrcotfas.goodtime.data.settings.NotificationPermissionState
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlin.math.floor

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val showTimePicker: Boolean = false,
    val showWorkdayStartPicker: Boolean = false,
    val showSelectWorkSoundPicker: Boolean = false,
    val showSelectBreakSoundPicker: Boolean = false,
    val notificationSoundCandidate: String? = null,
)

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.onStart {
        loadData()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            settingsRepository.settings.distinctUntilChanged().collect { settings ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        settings = settings
                    )
                }
            }
        }
    }

    fun onToggleProductivityReminderDay(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            settingsRepository.updateReminderSettings {
                val days = it.days
                val alreadyEnabled = days.contains(dayOfWeek.isoDayNumber)
                it.copy(
                    days = if (alreadyEnabled) days - dayOfWeek.isoDayNumber else days + dayOfWeek.isoDayNumber
                )
            }
        }
    }

    fun setShowTimePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTimePicker = show)
    }

    fun setReminderTime(secondOfDay: Int) {
        viewModelScope.launch {
            settingsRepository.updateReminderSettings {
                it.copy(secondOfDay = secondOfDay)
            }
        }
    }

    fun setThemeOption(themePreference: ThemePreference) {
        viewModelScope.launch {
            settingsRepository.updateUiSettings {
                it.copy(themePreference = themePreference)
            }
        }
    }

    fun setFullscreenMode(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateUiSettings {
                it.copy(fullscreenMode = enable)
            }
        }
    }

    fun setKeepScreenOn(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateUiSettings {
                it.copy(keepScreenOn = enable)
            }
            if (!enable && uiState.value.settings.uiSettings.screensaverMode) {
                setScreensaverMode(false)
            }
        }
    }

    fun setScreensaverMode(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateUiSettings {
                it.copy(screensaverMode = enable)
            }
        }
    }

    fun setWorkDayStart(secondOfDay: Int) {
        viewModelScope.launch {
            settingsRepository.setWorkDayStart(secondOfDay)
        }
    }

    fun setFirstDayOfWeek(dayOfWeek: Int) {
        viewModelScope.launch {
            settingsRepository.setFirstDayOfWeek(dayOfWeek)
        }
    }

    fun setShowWorkdayStartPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showWorkdayStartPicker = show)
    }

    fun setVibrationStrength(vibrationStrength: Int) {
        viewModelScope.launch {
            settingsRepository.setVibrationStrength(vibrationStrength)
        }
    }

    fun setEnableTorch(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEnableTorch(enable)
        }
    }

    fun setInsistentNotification(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setInsistentNotification(enable)
            if (enable) {
                val settings = settingsRepository.settings.first()
                if (settings.autoStartWork) {
                    setAutoStartWork(false)
                }
                if (settings.autoStartBreak) {
                    setAutoStartBreak(false)
                }
            }
        }
    }

    fun setAutoStartWork(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoStartWork(enable)
            if (enable) {
                val settings = settingsRepository.settings.first()
                if (settings.insistentNotification) {
                    setInsistentNotification(false)
                }
            }
        }
    }

    fun setAutoStartBreak(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoStartBreak(enable)
            if (enable) {
                val settings = settingsRepository.settings.first()
                if (settings.insistentNotification) {
                    setInsistentNotification(false)
                }
            }
        }
    }

    fun setDndDuringWork(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateUiSettings {
                it.copy(dndDuringWork = enable)
            }
        }
    }

    fun setWorkFinishedSound(ringtone: String) {
        viewModelScope.launch {
            settingsRepository.setWorkFinishedSound(ringtone)
        }
    }

    fun setBreakFinishedSound(ringtone: String) {
        viewModelScope.launch {
            settingsRepository.setBreakFinishedSound(ringtone)
        }
    }

    fun setOverrideSoundProfile(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOverrideSoundProfile(enabled)
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

    fun setNotificationPermissionGranted(granted: Boolean) {
        viewModelScope.launch {
            val state =
                if (granted) NotificationPermissionState.GRANTED else NotificationPermissionState.DENIED
            settingsRepository.setNotificationPermissionState(state)
        }
    }

    fun initTimerStyle(maxSize: Float, screenWidth: Float) {
        viewModelScope.launch {
            settingsRepository.updateTimerStyle {
                it.copy(
                    minSize = floor(maxSize / 1.5f),
                    maxSize = maxSize,
                    fontSize = floor(maxSize * 0.9f),
                    currentScreenWidth = screenWidth
                )
            }
        }
    }

    fun setTimerWeight(weight: Int) {
        viewModelScope.launch {
            settingsRepository.updateTimerStyle {
                it.copy(fontWeight = weight)
            }
        }
    }

    fun setTimerSize(size: Float) {
        viewModelScope.launch {
            settingsRepository.updateTimerStyle {
                it.copy(
                    fontSize = size
                )
            }
        }
    }

    fun setTimerMinutesOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateTimerStyle {
                it.copy(minutesOnly = enabled)
            }
        }
    }

    fun setTimerFont(fontIndex: Int) {
        viewModelScope.launch {
            settingsRepository.updateTimerStyle {
                it.copy(fontIndex = fontIndex)
            }
        }
    }

    fun setShowStatus(showStatus: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateTimerStyle {
                it.copy(showStatus = showStatus)
            }
        }
    }

    fun setShowStreak(showStreak: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateTimerStyle {
                it.copy(showStreak = showStreak)
            }
        }
    }

    fun setShowLabel(showLabel: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateTimerStyle {
                it.copy(showLabel = showLabel)
            }
        }
    }

    companion object {
        val firstDayOfWeekOptions = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
    }
}