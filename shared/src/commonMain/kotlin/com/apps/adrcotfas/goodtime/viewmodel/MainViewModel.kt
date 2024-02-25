package com.apps.adrcotfas.goodtime.viewmodel

import com.apps.adrcotfas.goodtime.bl.TimerManager
import com.apps.adrcotfas.goodtime.bl.TimerType


//TODO: I don't need this one; the view can communicate directly to the TimerManager
//TODO: but I do need a viewmodel for stats and labels screens
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