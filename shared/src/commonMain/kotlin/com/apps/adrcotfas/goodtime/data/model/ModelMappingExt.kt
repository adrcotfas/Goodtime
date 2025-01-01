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
package com.apps.adrcotfas.goodtime.data.model

import com.apps.adrcotfas.goodtime.LocalLabel
import com.apps.adrcotfas.goodtime.LocalSession

fun Label.toLocal(): LocalLabel {
    return LocalLabel(
        id = id,
        name = name,
        colorIndex = colorIndex,
        orderIndex = orderIndex,
        useDefaultTimeProfile = useDefaultTimeProfile,
        isCountdown = timerProfile.isCountdown,
        workDuration = timerProfile.workDuration,
        isBreakEnabled = timerProfile.isBreakEnabled,
        breakDuration = timerProfile.breakDuration,
        isLongBreakEnabled = timerProfile.isLongBreakEnabled,
        longBreakDuration = timerProfile.longBreakDuration,
        sessionsBeforeLongBreak = timerProfile.sessionsBeforeLongBreak,
        workBreakRatio = timerProfile.workBreakRatio,
        isArchived = isArchived,
    )
}

fun toExternalLabelMapper(
    id: Long,
    name: String,
    colorIndex: Long,
    orderIndex: Long,
    useDefaultTimeProfile: Boolean,
    isCountdown: Boolean,
    workDuration: Int,
    isBreakEnabled: Boolean,
    breakDuration: Int,
    isLongBreakEnabled: Boolean,
    longBreakDuration: Int,
    sessionsBeforeLongBreak: Int,
    workBreakRatio: Int,
    isArchived: Boolean,
) = Label(
    id = id,
    name = name,
    colorIndex = colorIndex,
    orderIndex = orderIndex,
    useDefaultTimeProfile = useDefaultTimeProfile,
    timerProfile = TimerProfile(
        isCountdown = isCountdown,
        workDuration = workDuration,
        isBreakEnabled = isBreakEnabled,
        breakDuration = breakDuration,
        isLongBreakEnabled = isLongBreakEnabled,
        longBreakDuration = longBreakDuration,
        sessionsBeforeLongBreak = sessionsBeforeLongBreak,
        workBreakRatio = workBreakRatio,
    ),
    isArchived = isArchived,
)

fun Session.toLocal() = LocalSession(
    id = id,
    timestamp = timestamp,
    duration = duration,
    interruptions = interruptions,
    labelName = label,
    notes = notes,
    isWork = isWork,
    isArchived = isArchived,
)

fun toExternalSessionMapper(
    id: Long,
    timestamp: Long,
    duration: Long,
    interruptions: Long,
    label: String,
    notes: String?,
    isWork: Boolean,
    isArchived: Boolean,
) = Session(
    id = id,
    timestamp = timestamp,
    duration = duration,
    interruptions = interruptions,
    label = label,
    notes = notes,
    isWork = isWork,
    isArchived = isArchived,
)
