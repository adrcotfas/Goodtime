package com.apps.adrcotfas.goodtime.data.timer


/**
 * Data class that captures the current timer state.
 * There can only be one running timer at a time.
 */
data class TimerData(
    val startTime: Long = 0,
    val lastStartTime: Long = 0,
    val lastEndTime: Long = 0,
    val remaining: Long = 0,
    val labelId: Long? = null,
    val state: TimerState = TimerState.RESET,
    val type: TimerType = TimerType.WORK,
    val streak: Int = 0,
)

enum class TimerState {
    RESET, RUNNING, PAUSED, FINISHED
}
enum class TimerType {
    WORK, BREAK, LONG_BREAK
}