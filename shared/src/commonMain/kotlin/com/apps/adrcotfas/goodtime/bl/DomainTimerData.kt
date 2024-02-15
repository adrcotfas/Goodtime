package com.apps.adrcotfas.goodtime.bl

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.duration
import com.apps.adrcotfas.goodtime.data.settings.PersistedTimerData

/**
 * Data class that captures the current timer state.
 * There can only be one running timer at a time.
 */
data class DomainTimerData(
    val label: Label? = null,
    var persistedTimerData: PersistedTimerData = PersistedTimerData(),
    val startTime: Long = 0,
    val lastStartTime: Long = 0,
    val endTime: Long = 0,
    val remainingTimeAtPause: Long = 0,
    val state: TimerState = TimerState.RESET,
    val type: TimerType = TimerType.WORK,
    val pausedTime: Long = 0
) {
    fun reset() = DomainTimerData(label = label, persistedTimerData = persistedTimerData)

    fun getDuration() = label?.timerProfile?.duration(type) ?: 0
}

enum class TimerState {
    RESET, RUNNING, PAUSED, FINISHED
}

enum class TimerType {
    WORK, BREAK, LONG_BREAK
}