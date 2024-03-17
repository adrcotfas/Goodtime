package com.apps.adrcotfas.goodtime.viewmodel

import com.apps.adrcotfas.goodtime.bl.TimerManager
import com.apps.adrcotfas.goodtime.bl.TimerType


class MainViewModel(
    private val timerManager: TimerManager,
) : ViewModel() {
    val timerData = timerManager.timerData

    fun startTimer(type: TimerType) {
        timerManager.start(type)
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