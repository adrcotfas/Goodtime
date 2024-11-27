package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class BreakBudgetDataTest {
    @Test
    fun `Compute remaining time budget`() = runTest {
        val persistedTimerData = BreakBudgetData(
            breakBudgetStart = 0,
            breakBudget = 10.minutes
        )

        val expected = mapOf(
            Pair(10.minutes, 0.milliseconds.toLong(DurationUnit.MILLISECONDS)),
            Pair( 9.minutes, 1.minutes.toLong(DurationUnit.MILLISECONDS)),
            Pair( 8.minutes, 89.seconds.toLong(DurationUnit.MILLISECONDS)),
            Pair( 8.minutes, 90.seconds.toLong(DurationUnit.MILLISECONDS)),
            Pair( 8.minutes, 91.seconds.toLong(DurationUnit.MILLISECONDS)),
            Pair( 0.minutes, 9.minutes.plus(1.seconds).toLong(DurationUnit.MILLISECONDS)),
            Pair( 0.minutes, 10.minutes.toLong(DurationUnit.MILLISECONDS)),
            Pair( 0.minutes, 10.minutes.plus(1.seconds).toLong(DurationUnit.MILLISECONDS)),
        )

        for ((budget, elapsedTime) in expected) {
            assertEquals(
                budget.inWholeMinutes,
                persistedTimerData.getRemainingBreakBudget(elapsedTime).inWholeMinutes
            )
        }
    }
}