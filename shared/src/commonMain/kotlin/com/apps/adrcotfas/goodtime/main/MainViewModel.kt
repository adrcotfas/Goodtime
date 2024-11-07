package com.apps.adrcotfas.goodtime.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.bl.DomainLabel
import com.apps.adrcotfas.goodtime.bl.DomainTimerData
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimerManager
import com.apps.adrcotfas.goodtime.bl.TimerState
import com.apps.adrcotfas.goodtime.bl.TimerType
import com.apps.adrcotfas.goodtime.bl.getBaseTime
import com.apps.adrcotfas.goodtime.bl.isActive
import com.apps.adrcotfas.goodtime.bl.isBreak
import com.apps.adrcotfas.goodtime.bl.isPaused
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.ThemePreference
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
    val isReady: Boolean = false,
    val label: DomainLabel = DomainLabel(),
    val baseTime: Long = 0,
    val timerState: TimerState = TimerState.RESET,
    val timerType: TimerType = TimerType.WORK,
    val sessionsBeforeLongBreak: Int = 0,
    val longBreakData: LongBreakData = LongBreakData(),
    val breakBudgetData: BreakBudgetData = BreakBudgetData(),
) {
    fun workSessionIsInProgress(): Boolean {
        return timerState.isActive && timerType == TimerType.WORK
    }

    val isPaused = timerState.isPaused
    val isActive = timerState.isActive
    val isBreak = timerType.isBreak
}

data class MainUiState(
    val timerStyle: TimerStyleData = TimerStyleData(minSize = TimerStyleData.INVALID_MIN_SIZE),
    val darkThemePreference: ThemePreference = ThemePreference.SYSTEM,
    val fullscreenMode: Boolean = false,
    val dndDuringWork: Boolean = false,
    val isMainScreen: Boolean = true,
) {
    fun isDarkTheme(isSystemInDarkTheme: Boolean): Boolean {
        return darkThemePreference == ThemePreference.DARK ||
                (darkThemePreference == ThemePreference.SYSTEM && isSystemInDarkTheme)
    }
}

class MainViewModel(
    private val timerManager: TimerManager,
    private val timeProvider: TimeProvider,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    val timerUiState: Flow<TimerUiState> = timerManager.timerData.flatMapLatest {
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
                            darkThemePreference = uiSettings.themePreference,
                            fullscreenMode = uiSettings.fullscreenMode,
                            dndDuringWork = uiSettings.dndDuringWork
                        )
                    }
                }
        }
    }

    fun startTimer(type: TimerType = TimerType.WORK) {
        timerManager.start(type)
    }

    fun toggleTimer() {
        timerManager.toggle()
    }

    fun resetTimer() {
        timerManager.reset()
    }

    fun addOneMinute() {
        timerManager.addOneMinute()
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
                isReady = it.isReady,
                label = it.label,
                baseTime = it.getBaseTime(timeProvider),
                timerState = it.state,
                timerType = it.type,
                sessionsBeforeLongBreak = it.inUseSessionsBeforeLongBreak(),
                longBreakData = it.longBreakData,
                breakBudgetData = it.breakBudgetData,
            )
        )
    }

    fun skip() {
        timerManager.skip()
    }

    fun next() {
        timerManager.next()
    }

    fun initTimerStyle(maxSize: Float, screenWidth: Float) {
        viewModelScope.launch {
            val uiSettings = getUiSettings()
            settingsRepo.saveUiSettings(
                uiSettings.copy(
                    timerStyle = uiSettings.timerStyle.copy(
                        minSize = floor(maxSize / 1.5f),
                        maxSize = maxSize,
                        fontSize = floor(maxSize * 0.9f),
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

    fun setShowStatus(showStatus: Boolean) {
        viewModelScope.launch {
            val uiSettings = getUiSettings()
            settingsRepo.saveUiSettings(
                uiSettings.copy(
                    timerStyle = uiSettings.timerStyle.copy(
                        showStatus = showStatus
                    )
                )
            )
        }
    }

    fun setShowStreak(showStreak: Boolean) {
        viewModelScope.launch {
            val uiSettings = getUiSettings()
            settingsRepo.saveUiSettings(
                uiSettings.copy(
                    timerStyle = uiSettings.timerStyle.copy(
                        showStreak = showStreak
                    )
                )
            )
        }
    }

    fun setShowLabel(showLabel: Boolean) {
        viewModelScope.launch {
            val uiSettings = getUiSettings()
            settingsRepo.saveUiSettings(
                uiSettings.copy(
                    timerStyle = uiSettings.timerStyle.copy(
                        showLabel = showLabel
                    )
                )
            )
        }
    }

    private suspend fun getUiSettings() = settingsRepo.settings.first().uiSettings
}