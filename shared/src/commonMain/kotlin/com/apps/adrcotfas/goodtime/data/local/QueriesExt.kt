package com.apps.adrcotfas.goodtime.data.local

import com.apps.adrcotfas.goodtime.LocalLabel
import com.apps.adrcotfas.goodtime.LocalLabelQueries
import com.apps.adrcotfas.goodtime.LocalSession
import com.apps.adrcotfas.goodtime.LocalSessionQueries

fun LocalLabelQueries.insert(label: LocalLabel) {
    insert(
        name = label.name,
        colorIndex = label.colorIndex,
        orderIndex = label.orderIndex,
        useDefaultTimeProfile = label.useDefaultTimeProfile,
        isCountdown = label.isCountdown,
        workDuration = label.workDuration,
        isBreakEnabled = label.isBreakEnabled,
        breakDuration = label.breakDuration,
        isLongBreakEnabled = label.isLongBreakEnabled,
        longBreakDuration = label.longBreakDuration,
        sessionsBeforeLongBreak = label.sessionsBeforeLongBreak,
        workBreakRatio = label.workBreakRatio,
        isArchived = label.isArchived
    )
}

fun LocalSessionQueries.insert(session: LocalSession) {
    insert(
        timestamp = session.timestamp,
        duration = session.duration,
        interruptions = session.interruptions,
        labelName = session.labelName,
        notes = session.notes,
        isWork = session.isWork,
        isArchived = session.isArchived
    )
}

fun LocalSessionQueries.update(id: Long, newSession: LocalSession) {
    update(
        newTimestamp = newSession.timestamp,
        newDuration = newSession.duration,
        newInterruptions = newSession.interruptions,
        newLabel = newSession.labelName,
        newNotes = newSession.notes,
        id = id
    )
}