package com.apps.adrcotfas.goodtime.data.model

import com.apps.adrcotfas.goodtime.bl.TimerType
import kotlin.time.Duration.Companion.minutes

data class TimerProfile(
    val isCountdown: Boolean = true,
    /** Work duration in minutes; invalid for isCountdown false */
    val workDuration: Int = DEFAULT_WORK_DURATION,
    /** Break duration in minutes */
    val isBreakEnabled: Boolean = true,
    val breakDuration: Int = DEFAULT_BREAK_DURATION,
    val isLongBreakEnabled: Boolean = true,
    /** Long break duration in minutes */
    val longBreakDuration: Int = DEFAULT_LONG_BREAK_DURATION,
    /** Number of sessions before long break*/
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

/**
 * Returns the end time of the timer in milliseconds since Unix Epoch.
 * If the timer is not a countdown timer, returns 0.
 * @param timerType the type of the timer
 * @param elapsedRealTime the elapsed real time in milliseconds since boot, including time spent in sleep
 */
fun TimerProfile.endTime(timerType: TimerType, elapsedRealTime: Long): Long {

    return if (isCountdown) {
        elapsedRealTime + this.duration(timerType).minutes.inWholeMilliseconds
    } else 0
}

fun TimerProfile.duration(timerType: TimerType): Int {
    return when (timerType) {
        TimerType.WORK -> workDuration
        TimerType.BREAK -> breakDuration
        TimerType.LONG_BREAK -> longBreakDuration
    }
}