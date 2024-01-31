package com.apps.adrcotfas.goodtime.domain

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.settings.PersistedTimerData

/**
 * Data class that captures the current timer state.
 * There can only be one running timer at a time.
 */
data class DomainTimerData(
    val label: Label? = null,
    val startTime: Long = 0,
    // there can be another start time if the timer was paused and resumed
    // retain this so we can compute the amount of time the timer was paused
    val lastStartTime: Long = 0,
    val endTime: Long = 0,
    // this is used to update the [endTime] when the timer is paused and resumed
    val tmpRemaining: Long = 0,
    val state: TimerState = TimerState.RESET,
    val type: TimerType = TimerType.WORK,
    // used to differentiate between a work session prolonged by pausing the timer
    // and a work session prolonged by adding minutes
    val minutesAdded: Int = 0,
    var persistedTimerData: PersistedTimerData = PersistedTimerData(),
)

enum class TimerState {
    RESET, RUNNING, PAUSED, FINISHED
}
enum class TimerType {
    WORK, BREAK, LONG_BREAK
}