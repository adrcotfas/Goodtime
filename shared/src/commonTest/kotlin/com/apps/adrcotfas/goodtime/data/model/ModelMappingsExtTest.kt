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
            startTimestamp = 1,
            endTimestamp = 3.minutes.inWholeMilliseconds,
            duration = 3.minutes.toLong(DurationUnit.MINUTES),
            labelName = "label",
            notes = "notes",
            isWork = true,
            isArchived = true
        )
        val session = toExternalSessionMapper(
            localSession.id,
            localSession.startTimestamp,
            localSession.endTimestamp,
            localSession.duration,
            localSession.labelName,
            localSession.notes,
            localSession.isWork,
            localSession.isArchived)
        assertEquals(localSession, session.toLocal())
    }

    @Test
    fun `Convert external session to local and back`() {
        val session = Session(
            id = 1,
            startTimestamp = 1,
            endTimestamp = 3.minutes.inWholeMilliseconds,
            duration = 3.minutes.toLong(DurationUnit.MINUTES),
            label = "label",
            notes = "notes",
            isWork = false,
            isArchived = true
        )
        val localSession = session.toLocal()
        assertEquals(session, toExternalSessionMapper(
            localSession.id,
            localSession.startTimestamp,
            localSession.endTimestamp,
            localSession.duration,
            localSession.labelName,
            localSession.notes,
            localSession.isWork,
            localSession.isArchived))
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
            isArchived = true
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
            localLabel.isArchived
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
                workBreakRatio = 7
            ),
            isArchived = true
        )
        val localLabel = label.toLocal()
        assertEquals(label, toExternalLabelMapper(
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
            localLabel.isArchived
        ))

    }
}