package com.apps.adrcotfas.goodtime.data.model

data class TimerProfile(
    val isCountdown: Boolean = true,
    /** Work duration in minutes; invalid for isCountdown false */
    val workDuration: Int = DEFAULT_WORK_DURATION,
    /** Break duration in minutes */
    val breakDuration: Int = DEFAULT_BREAK_DURATION,
    /** Long break duration in minutes */
    val longBreakDuration: Int = DEFAULT_LONG_BREAK_DURATION,
    /** Number of sessions before long break or 0 to have this feature disabled */
    val sessionsBeforeLongBreak: Int = DEFAULT_SESSIONS_BEFORE_LONG_BREAK,
    /** the ratio between work and break duration; invalid for isCountdown true */
    val workBreakRatio: Int = DEFAULT_WORK_BREAK_RATIO
) {
    companion object {
        const val DEFAULT_WORK_DURATION = 25
        const val DEFAULT_BREAK_DURATION = 5
        const val DEFAULT_LONG_BREAK_DURATION = 15
        const val DEFAULT_SESSIONS_BEFORE_LONG_BREAK = 4
        const val DEFAULT_WORK_BREAK_RATIO = 3
    }
}