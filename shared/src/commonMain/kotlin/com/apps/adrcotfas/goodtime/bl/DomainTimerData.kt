package com.apps.adrcotfas.goodtime.bl

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.duration
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
import kotlin.time.Duration.Companion.minutes

/**
 * Data class that captures the current timer state.
 * There can only be one running timer at a time.
 */
data class DomainTimerData(
    val label: Label? = null,
    val startTime: Long = 0, // millis since boot
    val lastStartTime: Long = 0, // millis since boot
    val endTime: Long = 0, // millis since boot
    val remainingTimeAtPause: Long = 0, // millis
    val state: TimerState = TimerState.RESET,
    val type: TimerType = TimerType.WORK,
    val pausedTime: Long = 0, // millis

    /**
     * This is persisted in the settings too for cases where the app is killed.
     * I don't like updating the data in two places(see [TimerManager.incrementStreak]
     * and [TimerManager.resetStreakIfNeeded]) but I don't want to risk invalid data
     *  when updating the repository and possibly waiting for the flow to emit the new data
     *  while executing the next command.
     */
    val longBreakData: LongBreakData = LongBreakData(),
    val breakBudgetData: BreakBudgetData = BreakBudgetData(),
) {
    fun reset() = DomainTimerData(
        label = label,
        longBreakData = longBreakData,
        breakBudgetData = breakBudgetData
    )

    fun getDuration() = (label?.timerProfile?.duration(type) ?: 0).minutes.inWholeMilliseconds

    fun getBreakBudget(workDuration: Int): Int {
        return if (label == null || label.timerProfile.isCountdown) {
            0
        } else {
            workDuration / label.timerProfile.workBreakRatio
        }
    }
}

enum class TimerState {
    RESET, RUNNING, PAUSED, FINISHED
}

enum class TimerType {
    WORK, BREAK, LONG_BREAK
}

fun DomainTimerData.getBaseTime(timerProvider: TimeProvider): Long {
    if (label == null) {
        return 0
    }
    val countdown = label.timerProfile.isCountdown

    if (state == TimerState.RESET) {
        return if (countdown) {
            label.timerProfile.duration(type).minutes.inWholeMilliseconds
        } else {
            0
        }
    } else if(state == TimerState.PAUSED) {
        return remainingTimeAtPause
    }

    return if (countdown) endTime - timerProvider.elapsedRealtime()
            else timerProvider.elapsedRealtime() - startTime
}