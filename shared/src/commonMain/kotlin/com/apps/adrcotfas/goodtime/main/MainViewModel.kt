package com.apps.adrcotfas.goodtime.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.bl.DomainTimerData
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimerManager
import com.apps.adrcotfas.goodtime.bl.TimerState
import com.apps.adrcotfas.goodtime.bl.TimerType
import com.apps.adrcotfas.goodtime.bl.getBaseTime
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.DarkModePreference
import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.floor

data class TimerUiState(
    val baseTime: Long = 0,
    val timerState: TimerState = TimerState.RESET,
    val timerType: TimerType = TimerType.WORK,
    val longBreakData: LongBreakData = LongBreakData(),
    val breakBudgetData: BreakBudgetData = BreakBudgetData(),
) {
    fun workSessionIsInProgress(): Boolean {
        return (timerState == TimerState.RUNNING || timerState == TimerState.PAUSED)
                && timerType == TimerType.WORK
    }

    fun isActive(): Boolean {
        return timerState != TimerState.RESET
    }
}

data class MainUiState(
    val timerStyle: TimerStyleData = TimerStyleData(minSize = TimerStyleData.INVALID_MIN_SIZE),
    val dynamicColor: Boolean = false,
    val darkThemePreference: DarkModePreference = DarkModePreference.SYSTEM,
    val fullscreenMode: Boolean = false,
    val dndDuringWork: Boolean = false,
    val isMainScreen: Boolean = true,
) {
    fun isDarkTheme(isSystemInDarkTheme: Boolean): Boolean {
        return darkThemePreference == DarkModePreference.DARK ||
                (darkThemePreference == DarkModePreference.SYSTEM && isSystemInDarkTheme)
    }
}

class MainViewModel(
    private val timerManager: TimerManager,
    private val timeProvider: TimeProvider,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    val timerState: Flow<TimerUiState> = timerManager.timerData.flatMapLatest {
        when (it.state) {
            TimerState.RUNNING -> flow {
                while (it.state == TimerState.RUNNING) {
                    emitUiState(it)
                    delay(1000)
                }
            }

            else -> {
                flow { emitUiState(it) }
            }
        }
    }.distinctUntilChanged()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepo.settings.map { it.uiSettings }.distinctUntilChanged()
                .collect { uiSettings ->
                    _uiState.update {
                        it.copy(
                            timerStyle = uiSettings.timerStyle,
                            dynamicColor = uiSettings.useDynamicColor,
                            darkThemePreference = uiSettings.darkModePreference,
                            fullscreenMode = uiSettings.fullscreenMode,
                            dndDuringWork = uiSettings.dndDuringWork
                        )
                    }
                }
        }
    }

    fun startTimer(type: TimerType) {
        timerManager.start(type)
    }

    fun toggleTimer() {
        timerManager.toggle()
    }

    fun resetTimer() {
        timerManager.reset()
    }

    fun setIsMainScreen(isMainScreen: Boolean) {
        _uiState.update {
            it.copy(isMainScreen = isMainScreen)
        }
    }

    private suspend fun FlowCollector<TimerUiState>.emitUiState(
        it: DomainTimerData
    ) {
        emit(
            TimerUiState(
                it.getBaseTime(timeProvider),
                it.state,
                it.type,
                it.longBreakData,
                it.breakBudgetData
            )
        )
    }

    //TODO: testing purposes / remove this
    fun finishTimer() {
        timerManager.finish()
    }

    fun initTimerStyle(maxSize: Float, screenWidth: Int) {
        viewModelScope.launch {
            val uiSettings = getUiSettings()
            settingsRepo.saveUiSettings(
                uiSettings.copy(
                    timerStyle = uiSettings.timerStyle.copy(
                        minSize = floor(maxSize / 1.5f),
                        maxSize = maxSize,
                        fontSize = floor(maxSize * 0.75f),
                        currentScreenWidth = screenWidth
                    )
                )
            )
        }
    }

    fun setTimerWeight(weight: Int) {
        viewModelScope.launch {
            val uiSettings = getUiSettings()
            settingsRepo.saveUiSettings(
                uiSettings.copy(
                    timerStyle = uiSettings.timerStyle.copy(
                        fontWeight = weight
                    )
                )
            )
        }
    }

    fun setTimerSize(size: Float) {
        viewModelScope.launch {
            val uiSettings = getUiSettings()
            settingsRepo.saveUiSettings(
                uiSettings.copy(
                    timerStyle = uiSettings.timerStyle.copy(
                        fontSize = size
                    )
                )
            )
        }
    }

    fun setTimerMinutesOnly(enabled: Boolean) {
        viewModelScope.launch {
            val uiSettings = getUiSettings()
            settingsRepo.saveUiSettings(
                uiSettings.copy(
                    timerStyle = uiSettings.timerStyle.copy(
                        minutesOnly = enabled
                    )
                )
            )
        }
    }

    fun setTimerFont(fontIndex: Int) {
        viewModelScope.launch {
            val uiSettings = getUiSettings()
            settingsRepo.saveUiSettings(
                uiSettings.copy(
                    timerStyle = uiSettings.timerStyle.copy(
                        fontIndex = fontIndex
                    )
                )
            )
        }
    }

    private suspend fun getUiSettings() = settingsRepo.settings.first().uiSettings
}