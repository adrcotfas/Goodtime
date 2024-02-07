package com.apps.adrcotfas.goodtime.bl

import kotlinx.datetime.Clock
import platform.Darwin.mach_absolute_time
import platform.Darwin.mach_timebase_info
import platform.Darwin.mach_timebase_info_data_t

actual class TimeProviderImpl : TimeProvider {
    override fun now(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }

    override fun elapsedRealtime(): Long {
        val timebase = mach_timebase_info_data_t()
        mach_timebase_info(timebase.ptr)
        val elapsedNano = mach_absolute_time() * timebase.numer / timebase.denom
        return elapsedNano / 1_000_000 // Convert to milliseconds
    }
}