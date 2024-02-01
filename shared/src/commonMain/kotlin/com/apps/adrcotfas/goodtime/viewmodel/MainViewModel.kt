package com.apps.adrcotfas.goodtime.viewmodel

import com.apps.adrcotfas.goodtime.domain.TimerManager
import com.apps.adrcotfas.goodtime.domain.TimerType

class MainViewModel(
    private val timerManager: TimerManager,
) : ViewModel() {
    val timerData = timerManager.timerData

    fun startTimer(type: TimerType) {
        timerManager.start(TimerType.BREAK)
    }

    fun pauseTimer() {
        timerManager.pause()
    }

    fun resetTimer() {
        timerManager.reset()
    }

    fun finish() {
        timerManager.finish()
    }
}