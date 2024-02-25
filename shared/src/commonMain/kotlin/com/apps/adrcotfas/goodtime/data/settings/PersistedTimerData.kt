package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Serializable
data class BreakBudgetData(
    val breakBudget: Int = 0, // minutes
    val breakBudgetStart: Long = 0, // millis since boot
) {
    /**
     * Returns the remaining break budget in minutes.
     */
    fun getRemainingBreakBudget(millis: Long): Int {
        val timeSinceBreakBudgetStart = millis - breakBudgetStart
        val breakBudgetMs = breakBudget.minutes.inWholeMilliseconds
        return max(
            0,
            (breakBudgetMs - timeSinceBreakBudgetStart).milliseconds.inWholeMinutes.toInt()
        )
    } 
}

@Serializable
data class LongBreakData(
    val streak: Int = 0,
    val lastWorkEndTime: Long = 0, // millis since boot
)