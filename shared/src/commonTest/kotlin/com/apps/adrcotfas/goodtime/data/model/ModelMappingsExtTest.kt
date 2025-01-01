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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

class ModelMappingsExtTest {

    @Test
    fun `Convert local session to external and back`() {
        val localSession = LocalSession(
            id = 1,
            timestamp = 3.minutes.inWholeMilliseconds,
            duration = 3.minutes.toLong(DurationUnit.MINUTES),
            interruptions = 0,
            labelName = "label",
            notes = "notes",
            isWork = true,
            isArchived = true,
        )
        val session = toExternalSessionMapper(
            localSession.id,
            localSession.timestamp,
            localSession.duration,
            localSession.interruptions,
            localSession.labelName,
            localSession.notes,
            localSession.isWork,
            localSession.isArchived,
        )
        assertEquals(localSession, session.toLocal())
    }

    @Test
    fun `Convert external session to local and back`() {
        val session = Session(
            id = 1,
            timestamp = 3.minutes.inWholeMilliseconds,
            duration = 3.minutes.toLong(DurationUnit.MINUTES),
            interruptions = 0,
            label = "label",
            notes = "notes",
            isWork = false,
            isArchived = true,
        )
        val localSession = session.toLocal()
        assertEquals(
            session,
            toExternalSessionMapper(
                localSession.id,
                localSession.timestamp,
                localSession.duration,
                localSession.interruptions,
                localSession.labelName,
                localSession.notes,
                localSession.isWork,
                localSession.isArchived,
            ),
        )
    }

    @Test
    fun `Convert local label to external and back`() {
        val localLabel = LocalLabel(
            id = 0,
            name = "name",
            colorIndex = 1,
            orderIndex = 2,
            useDefaultTimeProfile = false,
            isCountdown = true,
            workDuration = 3,
            isBreakEnabled = false,
            breakDuration = 4,
            isLongBreakEnabled = false,
            longBreakDuration = 5,
            sessionsBeforeLongBreak = 6,
            workBreakRatio = 7,
            isArchived = true,
        )
        val label = toExternalLabelMapper(
            localLabel.id,
            localLabel.name,
            localLabel.colorIndex,
            localLabel.orderIndex,
            localLabel.useDefaultTimeProfile,
            localLabel.isCountdown,
            localLabel.workDuration,
            localLabel.isBreakEnabled,
            localLabel.breakDuration,
            localLabel.isLongBreakEnabled,
            localLabel.longBreakDuration,
            localLabel.sessionsBeforeLongBreak,
            localLabel.workBreakRatio,
            localLabel.isArchived,
        )
        assertEquals(localLabel, label.toLocal())
    }

    @Test
    fun `Convert external label to local and back`() {
        val label = Label(
            id = 0,
            name = "name",
            colorIndex = 1,
            orderIndex = 2,
            useDefaultTimeProfile = true,
            timerProfile = TimerProfile(
                isCountdown = false,
                workDuration = 3,
                isBreakEnabled = false,
                breakDuration = 4,
                isLongBreakEnabled = false,
                longBreakDuration = 5,
                sessionsBeforeLongBreak = 6,
                workBreakRatio = 7,
            ),
            isArchived = true,
        )
        val localLabel = label.toLocal()
        assertEquals(
            label,
            toExternalLabelMapper(
                localLabel.id,
                localLabel.name,
                localLabel.colorIndex,
                localLabel.orderIndex,
                localLabel.useDefaultTimeProfile,
                localLabel.isCountdown,
                localLabel.workDuration,
                localLabel.isBreakEnabled,
                localLabel.breakDuration,
                localLabel.isLongBreakEnabled,
                localLabel.longBreakDuration,
                localLabel.sessionsBeforeLongBreak,
                localLabel.workBreakRatio,
                localLabel.isArchived,
            ),
        )
    }
}
