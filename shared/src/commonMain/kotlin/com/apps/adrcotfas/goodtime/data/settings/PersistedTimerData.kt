package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Serializable
data class BreakBudgetData(
    val breakBudget: Int = 0, // minutes
    val breakBudgetStart: Long = 0, // millis since boot
) {
    fun getRemainingBreakBudget(millis: Long): Duration {
        val timeSinceBreakBudgetStart = millis - breakBudgetStart
        val breakBudgetMs = breakBudget.minutes.inWholeMilliseconds
        return max(0, (breakBudgetMs - timeSinceBreakBudgetStart)).milliseconds
    }
}

@Serializable
data class LongBreakData(
    val streak: Int = 0,
    val lastWorkEndTime: Long = 0, // millis since boot
)

fun LongBreakData.streakInUse(sessionsBeforeLongBreak: Int): Int {
    return streak % sessionsBeforeLongBreak
}