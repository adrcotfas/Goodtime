package com.apps.adrcotfas.goodtime.data.local

import com.apps.adrcotfas.goodtime.Label
import com.apps.adrcotfas.goodtime.data.TimerProfile

object LabelExtension {
    /**
     * Returns the [TimerProfile] associated to the [Label] unless it's the default one,
     * which should be retrieved from the repository since it can be changed any time.
     * @throws IllegalStateException if the label is using the default time profile.
     */
    val Label.timerProfile: TimerProfile
        get() {
            if (useDefaultTimeProfile) throw IllegalStateException("Should not be called for default time profiles")

            return if (isCountdown) TimerProfile.Countdown(
                workDuration = workDuration,
                breakDuration = breakDuration,
                longBreakDuration = longBreakDuration,
                sessionsBeforeLongBreak = sessionsBeforeLongBreak
            ) else TimerProfile.Flow(
                workBreakRatio = workBreakRatio
            )
        }

}