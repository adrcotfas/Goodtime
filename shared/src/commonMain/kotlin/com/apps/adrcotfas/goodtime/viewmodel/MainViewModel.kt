package com.apps.adrcotfas.goodtime.viewmodel

import com.apps.adrcotfas.goodtime.domain.TimerManager
import com.apps.adrcotfas.goodtime.domain.TimerType

class MainViewModel(
    private val timerManager: TimerManager,
) : ViewModel() {
    // expose flow for remaining time, currentSession and streak to be used in UI

    val timerData = timerManager.timerData

    fun startTimer(type: TimerType) {
        timerManager.start(type)
    }
}