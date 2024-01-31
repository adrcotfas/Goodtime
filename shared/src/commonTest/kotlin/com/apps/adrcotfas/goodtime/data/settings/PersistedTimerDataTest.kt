package com.apps.adrcotfas.goodtime.data.settings

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class PersistedTimerDataTest {
    @Test
    fun `Compute remaining time budget`() = runTest {
        val persistedTimerData = PersistedTimerData(
            breakBudgetStart = 0,
            breakBudget = 10
        )

        val expected = mapOf(
            Pair(10.minutes.toInt(DurationUnit.MINUTES), 0.milliseconds.toLong(DurationUnit.MILLISECONDS)),
            Pair(9.minutes.toInt(DurationUnit.MINUTES), 1.minutes.toLong(DurationUnit.MILLISECONDS)),
            Pair(8.minutes.toInt(DurationUnit.MINUTES), 89.seconds.toLong(DurationUnit.MILLISECONDS)),
            Pair(8.minutes.toInt(DurationUnit.MINUTES), 90.seconds.toLong(DurationUnit.MILLISECONDS)),
            Pair(8.minutes.toInt(DurationUnit.MINUTES), 91.seconds.toLong(DurationUnit.MILLISECONDS)),
            Pair(0.minutes.toInt(DurationUnit.MINUTES), 9.minutes.plus(1.seconds).toLong(DurationUnit.MILLISECONDS)),
            Pair(0.minutes.toInt(DurationUnit.MINUTES), 10.minutes.toLong(DurationUnit.MILLISECONDS)),
            Pair(0.minutes.toInt(DurationUnit.MINUTES), 10.minutes.plus(1.seconds).toLong(DurationUnit.MILLISECONDS)),
        )

        for ((budget, elapsedTime) in expected) {
            assertEquals(
                budget,
                persistedTimerData.computeRemainingBreakBudget(elapsedTime)
            )
        }
    }
}