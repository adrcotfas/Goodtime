package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@Serializable
data class PersistedTimerData(
    val labelName: String? = null,
    val streak: Int = 0,
    val breakBudgetStart: Long = 0,
    val breakBudget: Int = 0,
) {
    fun computeRemainingBreakBudget(now: Long): Int {
        val timeSinceBreakBudgetStart = now - breakBudgetStart
        return max(
            0,
            (timeSinceBreakBudgetStart - breakBudget.minutes.inWholeMilliseconds).milliseconds.inWholeMinutes.toInt()
        )
    }
}