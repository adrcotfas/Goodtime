package com.apps.adrcotfas.goodtime.data.local

import com.apps.adrcotfas.goodtime.LocalLabel
import com.apps.adrcotfas.goodtime.LocalLabelQueries
import com.apps.adrcotfas.goodtime.LocalSession
import com.apps.adrcotfas.goodtime.LocalSessionQueries
import com.apps.adrcotfas.goodtime.data.model.TimerProfile

fun LocalLabelQueries.insert(label: LocalLabel) {
    insert(
        name = label.name,
        colorIndex = label.colorIndex,
        orderIndex = label.orderIndex,
        useDefaultTimeProfile = label.useDefaultTimeProfile,
        isCountdown = label.isCountdown,
        workDuration = label.workDuration,
        breakDuration = label.breakDuration,
        longBreakDuration = label.longBreakDuration,
        sessionsBeforeLongBreak = label.sessionsBeforeLongBreak,
        workBreakRatio = label.workBreakRatio,
        isArchived = label.isArchived
    )
}

fun LocalLabelQueries.updateTimerProfile(name: String, newTimerProfile: TimerProfile) {
    updateTimerProfile(
        name = name,
        newIsCountdown = newTimerProfile.isCountdown,
        newWorkDuration = newTimerProfile.workDuration,
        newBreakDuration = newTimerProfile.breakDuration,
        newLongBreakDuration = newTimerProfile.longBreakDuration,
        newSessionsBeforeLongBreak = newTimerProfile.sessionsBeforeLongBreak,
        newWorkBreakRatio = newTimerProfile.workBreakRatio
    )
}

fun LocalSessionQueries.insert(session: LocalSession) {
    insert(
        timestamp = session.timestamp,
        duration = session.duration,
        label = session.label,
        notes = session.notes,
        isArchived = session.isArchived
    )
}

fun LocalSessionQueries.update(id: Long, newSession: LocalSession) {
    update(
        newTimestamp = newSession.timestamp,
        newDuration = newSession.duration,
        newLabel = newSession.label,
        newNotes = newSession.notes,
        id = id
    )
}