package com.apps.adrcotfas.goodtime.data.model

import com.apps.adrcotfas.goodtime.LocalLabel
import com.apps.adrcotfas.goodtime.LocalSession
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelMappingsExtTest {

    @Test
    fun `Convert local session to external and back`() {
        val localSession = LocalSession(
            id = 1,
            timestamp = 2,
            duration = 3,
            labelName = "label",
            notes = "notes",
            isArchived = true
        )
        val session = toExternalSessionMapper(
            localSession.id,
            localSession.timestamp,
            localSession.duration,
            localSession.labelName,
            localSession.notes,
            localSession.isArchived)
        assertEquals(localSession, session.toLocal())
    }

    @Test
    fun `Convert external session to local and back`() {
        val session = Session(
            id = 1,
            timestamp = 2,
            duration = 3,
            label = "label",
            notes = "notes",
            isArchived = true
        )
        val localSession = session.toLocal()
        assertEquals(session, toExternalSessionMapper(
            localSession.id,
            localSession.timestamp,
            localSession.duration,
            localSession.labelName,
            localSession.notes,
            localSession.isArchived))
    }

    @Test
    fun `Convert local label to external and back`() {
        val localLabel = LocalLabel(
            id = 0,
            name = "name",
            colorIndex = 1,
            orderIndex = 2,
            useDefaultTimeProfile = true,
            isCountdown = true,
            workDuration = 3,
            breakDuration = 4,
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
            localLabel.breakDuration,
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
                isCountdown = true,
                workDuration = 3,
                breakDuration = 4,
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
            localLabel.breakDuration,
            localLabel.longBreakDuration,
            localLabel.sessionsBeforeLongBreak,
            localLabel.workBreakRatio,
            localLabel.isArchived
        ))

    }
}