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
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import com.apps.adrcotfas.goodtime.data.settings.ThemePreference
import com.apps.adrcotfas.goodtime.data.settings.TimerStyleData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

data class TimerUiState(
    val isReady: Boolean = false,
    val label: DomainLabel = DomainLabel(),
    val baseTime: Long = 0,
    val timerState: TimerState = TimerState.RESET,
    val timerType: TimerType = TimerType.WORK,
    val completedMinutes: Long = 0,
    val timeSpentPaused: Long = 0,
    val endTime: Long = 0,
    val sessionsBeforeLongBreak: Int = 0,
    val longBreakData: LongBreakData = LongBreakData(),
    val breakBudgetMinutes: Long = 0,
) {
    fun workSessionIsInProgress(): Boolean {
        return timerState.isActive && timerType == TimerType.WORK
    }

    val displayTime = max(baseTime, 0)

    val isPaused = timerState.isPaused
    val isActive = timerState.isActive
    val isBreak = timerType.isBreak
    val isFinished = timerState == TimerState.FINISHED
}

data class MainUiState(
    val isLoading: Boolean = false,
    val timerStyle: TimerStyleData = TimerStyleData(),
    val darkThemePreference: ThemePreference = ThemePreference.SYSTEM,
    val dynamicColor: Boolean = false,
    val screensaverMode: Boolean = false,
    val fullscreenMode: Boolean = false,
    val dndDuringWork: Boolean = false,
    val isMainScreen: Boolean = true,
) {
    fun isDarkTheme(isSystemInDarkTheme: Boolean): Boolean {
        return darkThemePreference == ThemePreference.DARK ||
                (darkThemePreference == ThemePreference.SYSTEM && isSystemInDarkTheme)
    }
}

data class HistoryUiState(
    val todayWorkMinutes: Long = 0,
    val todayBreakMinutes: Long = 0,
    val todayInterruptedMinutes: Long = 0,
)

class MainViewModel(
    private val timerManager: TimerManager,
    private val timeProvider: TimeProvider,
    private val settingsRepo: SettingsRepository,
    private val localDataRepo: LocalDataRepository
) : ViewModel() {

    val timerUiState: Flow<TimerUiState> = timerManager.timerData.flatMapLatest {
        when (it.state) {
            TimerState.RUNNING, TimerState.PAUSED -> flow {
                while (true) {
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
    val uiState = _uiState.onStart {
        loadData()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState())

    private val _historyUiState = MutableStateFlow(HistoryUiState())
    val historyUiState = _historyUiState.onStart {
        loadHistoryState()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            settingsRepo.settings.map { Pair(it.timerStyle, it.uiSettings) }.distinctUntilChanged()
                .collect { (timerStyle, uiSettings) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            timerStyle = timerStyle,
                            darkThemePreference = uiSettings.themePreference,
                            dynamicColor = uiSettings.useDynamicColor,
                            screensaverMode = uiSettings.screensaverMode,
                            fullscreenMode = uiSettings.fullscreenMode,
                            dndDuringWork = uiSettings.dndDuringWork
                        )
                    }
                }
        }
    }

    private fun toMillisOfToday(workdayStart: Int): Long {
        val hour = workdayStart / 3600
        val minute = (workdayStart % 3600) / 60
        val second = workdayStart % 60

        val instant = Instant.fromEpochMilliseconds(timeProvider.now())
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val timeAtSecondOfDay = LocalDateTime(dateTime.date, LocalTime(hour, minute, second))
        val instant2 = timeAtSecondOfDay.toInstant(TimeZone.currentSystemDefault())
        return instant2.toEpochMilliseconds()
    }

    private fun loadHistoryState() {
        viewModelScope.launch {
            settingsRepo.settings.map { it.workdayStart }.distinctUntilChanged()
                .map { toMillisOfToday(it) }.flatMapLatest {
                    localDataRepo.selectAllSessions()
                    localDataRepo.selectSessionsAfter(it)
                }.collect { sessions ->
                    val (todayWorkSessions, todayBreakSessions) =
                        sessions.partition { session -> session.isWork }

                    val todayWorkMinutes = todayWorkSessions.sumOf { it.duration }
                    val todayBreakMinutes = todayBreakSessions.sumOf { it.duration }

                    val todayInterruptedMinutes =
                        todayWorkSessions.sumOf {
                            (it.endTimestamp - it.startTimestamp).milliseconds.inWholeMilliseconds
                            -it.duration.minutes.inWholeMilliseconds
                        }

                    _historyUiState.update {
                        it.copy(
                            todayWorkMinutes = todayWorkMinutes,
                            todayBreakMinutes = todayBreakMinutes,
                            todayInterruptedMinutes = todayInterruptedMinutes
                        )
                    }
                }
        }
    }

    fun startTimer(type: TimerType = TimerType.WORK) {
        timerManager.start(type)
    }

    fun toggleTimer(): Boolean {
        val canToggle = timerManager.canToggle()
        if (canToggle) {
            timerManager.toggle()
            return true
        } else return false
    }

    fun resetTimer(updateWorkTime: Boolean = false) {
        timerManager.reset(updateWorkTime)
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
                completedMinutes = it.completedMinutes,
                timeSpentPaused = it.timeSpentPaused,
                endTime = it.endTime,
                sessionsBeforeLongBreak = it.inUseSessionsBeforeLongBreak(),
                longBreakData = it.longBreakData,
                breakBudgetMinutes = it.getBreakBudget(timeProvider.elapsedRealtime()).inWholeMinutes
            )
        )
    }

    fun skip() {
        timerManager.skip()
    }

    fun next(updateWorkTime: Boolean = false) {
        timerManager.next(updateWorkTime)
    }
}