package com.apps.adrcotfas.goodtime.domain

import android.os.SystemClock
import kotlinx.datetime.Clock

actual class TimeProviderImpl : TimeProvider {
    override fun now(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }

    override fun elapsedRealtime(): Long {
        return SystemClock.elapsedRealtime()
    }
}