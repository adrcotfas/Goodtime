package com.apps.adrcotfas.goodtime.main

import com.apps.adrcotfas.goodtime.bl.DomainTimerData
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimerManager
import com.apps.adrcotfas.goodtime.bl.TimerState
import com.apps.adrcotfas.goodtime.bl.TimerType
import com.apps.adrcotfas.goodtime.bl.getBaseTime
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
import com.apps.adrcotfas.goodtime.viewmodel.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

data class MainUiState(
    val baseTime: Long = 0,
    val timerState: TimerState = TimerState.RESET,
    val timerType: TimerType = TimerType.WORK,
    val longBreakData: LongBreakData = LongBreakData(),
    val breakBudgetData: BreakBudgetData = BreakBudgetData()
)

class MainViewModel(
    private val timerManager: TimerManager,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    val uiState: Flow<MainUiState> = timerManager.timerData.flatMapLatest {
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

    fun startTimer(type: TimerType) {
        timerManager.start(type)
    }

    fun toggleTimer() {
        timerManager.toggle()
    }

    fun resetTimer() {
        timerManager.reset()
    }

    private suspend fun FlowCollector<MainUiState>.emitUiState(
        it: DomainTimerData
    ) {
        emit(
            MainUiState(
                it.getBaseTime(timeProvider),
                it.state,
                it.type,
                it.longBreakData,
                it.breakBudgetData
            )
        )
    }
}