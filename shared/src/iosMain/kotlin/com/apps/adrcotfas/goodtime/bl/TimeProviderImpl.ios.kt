/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
