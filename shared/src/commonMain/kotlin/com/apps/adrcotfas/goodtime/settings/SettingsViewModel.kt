package com.apps.adrcotfas.goodtime.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.data.settings.AppSettings
import com.apps.adrcotfas.goodtime.data.settings.DarkModePreference
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
    val showThemePicker: Boolean = false,
    val showFirstDayOfWeekPicker: Boolean = false,
    val showWorkdayStartPicker: Boolean = false
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

    fun setShowThemePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showThemePicker = show)
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

    fun setShowFirstDayOfWeekPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFirstDayOfWeekPicker = show)
    }

    fun setShowWorkdayStartPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showWorkdayStartPicker = show)
    }

    companion object {
        val firstDayOfWeekOptions = listOf(
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY
        )
    }
}