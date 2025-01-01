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

sealed class Event {
    data class Start(val autoStarted: Boolean = false, val endTime: Long = -1) : Event()
    data object Pause : Event()
    data class AddOneMinute(val endTime: Long = -1) : Event()
    data class Finished(val type: TimerType, val autostartNextSession: Boolean = false) : Event()
    data object Reset : Event()
}

interface EventListener {
    fun onEvent(event: Event)
    companion object
}
