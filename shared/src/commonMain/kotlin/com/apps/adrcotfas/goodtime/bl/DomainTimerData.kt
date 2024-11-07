package com.apps.adrcotfas.goodtime.bl

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import com.apps.adrcotfas.goodtime.data.model.duration
import com.apps.adrcotfas.goodtime.data.settings.BreakBudgetData
import com.apps.adrcotfas.goodtime.data.settings.LongBreakData
import kotlin.time.Duration.Companion.minutes


data class DomainLabel(
    val label: Label = Label.defaultLabel(),
    val profile: TimerProfile = TimerProfile()
) {
    fun getLabelName() = label.name
}

/**
 * Data class that captures the current timer state.
 * There can only be one running timer at a time.
 */
data class DomainTimerData(
    val isReady: Boolean = false,
    val label: DomainLabel = DomainLabel(),
    /**
     * This is persisted in the settings too for cases where the app is killed.
     * I don't like updating the data in two places(see [TimerManager.incrementStreak]
     * and [TimerManager.resetStreakIfNeeded]) but I don't want to risk invalid data
     *  when updating the repository and possibly waiting for the flow to emit the new data
     *  while executing the next command.
     */
    val longBreakData: LongBreakData = LongBreakData(),
    val breakBudgetData: BreakBudgetData = BreakBudgetData(),

    /**
     * Bellow we have the dynamic data that is not stored in persistent storage.
     */
    val startTime: Long = 0, // millis since boot
    val lastStartTime: Long = 0, // millis since boot
    val lastPauseTime: Long = 0, // millis since boot
    val endTime: Long = 0, // millis since boot
    val timeAtPause: Long = 0, // millis
    val state: TimerState = TimerState.RESET,
    val type: TimerType = TimerType.WORK,
    val pausedTime: Long = 0, // millis spent in pause
) {
    fun reset() = DomainTimerData(
        isReady = isReady,
        label = label,
        longBreakData = longBreakData,
        breakBudgetData = breakBudgetData
    )

    fun getDuration() = label.profile.duration(type).minutes.inWholeMilliseconds

    fun getBreakBudget(workDuration: Int): Int {
        return if (label.profile.isCountdown) {
            0
        } else {
            workDuration / label.profile.workBreakRatio
        }
    }

    fun getTimerProfile(): TimerProfile {
        return label.profile
    }

    fun getLabelName(): String {
        return label.getLabelName()
    }

    fun inUseSessionsBeforeLongBreak(): Int {
        val profile = label.profile
        return if (profile.isCountdown && profile.isBreakEnabled && profile.isLongBreakEnabled) {
            profile.sessionsBeforeLongBreak
        } else 0
    }

    fun isDefaultLabel() = label.getLabelName() == Label.DEFAULT_LABEL_NAME
}

enum class TimerState {
    RESET, RUNNING, PAUSED, FINISHED
}

val TimerState.isRunning: Boolean
    get() = this == TimerState.RUNNING

val TimerState.isPaused: Boolean
    get() = this == TimerState.PAUSED

val TimerState.isActive: Boolean
    get() = isRunning || isPaused

val TimerState.isFinished: Boolean
    get() = this == TimerState.FINISHED

val TimerState.isReset: Boolean
    get() = this == TimerState.RESET

enum class TimerType {
    WORK, BREAK, LONG_BREAK
}

val TimerType.isBreak: Boolean
    get() = this != TimerType.WORK

val TimerType.isWork: Boolean
    get() = this == TimerType.WORK

fun DomainTimerData.getBaseTime(timerProvider: TimeProvider): Long {
    val countdown = label.profile.isCountdown

    if (state == TimerState.RESET) {
        return if (countdown) {
            label.profile.duration(type).minutes.inWholeMilliseconds
        } else {
            0
        }
    } else if (state == TimerState.PAUSED) {
        return timeAtPause
    }

    return if (countdown) endTime - timerProvider.elapsedRealtime()
    else timerProvider.elapsedRealtime() - startTime - pausedTime
}