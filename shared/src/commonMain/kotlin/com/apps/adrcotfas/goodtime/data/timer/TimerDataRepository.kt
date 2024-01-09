package com.apps.adrcotfas.goodtime.data.timer

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for the timer data. There can only be one active timer at a time.
 * Calling this a repository may be a bit of a stretch because some properties don't need
 * to be persisted on disk(timer state, end time) but others do, like the label.
 * Keeping everything in one place makes it easier to reason about the timer.
 */
interface TimerDataRepository {
    val timerData: Flow<TimerData>
    suspend fun start()
    suspend fun addOneMinute()
    suspend fun pause()
    suspend fun resume()
    suspend fun skip()
    suspend fun reset()
    suspend fun finish()
    suspend fun setLabelId(labelId: Long)
}