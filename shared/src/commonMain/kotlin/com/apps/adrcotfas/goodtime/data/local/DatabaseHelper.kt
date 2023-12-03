package com.apps.adrcotfas.goodtime.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.apps.adrcotfas.goodtime.Label
import com.apps.adrcotfas.goodtime.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DatabaseHelper(private val database: Database) {

    private val coroutineScope = Dispatchers.IO
    suspend fun insertSession(session: Session) {
        withContext(coroutineScope) {
            database.sessionQueries.insert(
                timestamp = session.timestamp,
                duration = session.duration,
                label = session.label,
                notes = session.notes,
                isArchived = session.isArchived
            )
        }
    }

    suspend fun updateSession(id: Long, newSession: Session) {
        withContext(coroutineScope) {
            database.sessionQueries.update(
                newTimestamp = newSession.timestamp,
                newDuration = newSession.duration,
                newLabel = newSession.label,
                newNotes = newSession.notes,
                id = id
            )
        }
    }

    fun selectAllSessions(): Flow<List<Session>> {
        return database.sessionQueries.selectAll().asFlow().mapToList(coroutineScope)
    }

    fun selectSessionsByIsArchived(isArchived: Boolean): Flow<List<Session>> {
        return database.sessionQueries.selectByIsArchived(isArchived).asFlow()
            .mapToList(coroutineScope)
    }

    fun selectSessionsByLabel(label: String?): Flow<List<Session>> {
        return database.sessionQueries.selectByLabel(label).asFlow().mapToList(coroutineScope)
    }

    suspend fun deleteSession(id: Long) {
        withContext(coroutineScope) {
            database.sessionQueries.delete(id)
        }
    }

    suspend fun deleteSessionAfter(timestamp: Long) {
        withContext(coroutineScope) {
            database.sessionQueries.deleteAfter(timestamp)
        }
    }

    internal suspend fun deleteAllSessions() {
        withContext(coroutineScope) {
            database.sessionQueries.deleteAll()
        }
    }

    suspend fun insertLabel(label: Label) {
        withContext(coroutineScope) {
            database.labelQueries.insert(
                name = label.name,
                colorIndex = label.colorIndex,
                orderIndex = label.orderIndex,
                shouldFollowDefaultTimeProfile = label.shouldFollowDefaultTimeProfile,
                isCountdown = label.isCountdown,
                workDuration = label.workDuration,
                breakDuration = label.breakDuration,
                longBreakDuration = label.sessionsBeforeLongBreak,
                sessionsBeforeLongBreak = label.sessionsBeforeLongBreak,
                workBreakRatio = label.workBreakRatio,
                isArchived = label.isArchived
            )
        }
    }

    suspend fun updateLabelName(name: String, newName: String) {
        withContext(coroutineScope) {
            database.labelQueries.updateName(newName = newName, name = name)
        }
    }

    suspend fun updateLabelColorIndex(name: String, newColorIndex: Long) {
        withContext(coroutineScope) {
            database.labelQueries.updateColorIndex(newColorIndex = newColorIndex, name = name)
        }
    }

    suspend fun updateLabelOrderIndex(name: String, newOrderIndex: Long) {
        withContext(coroutineScope) {
            database.labelQueries.updateOrderIndex(newOrderIndex = newOrderIndex, name = name)
        }
    }

    suspend fun updateShouldFollowDefaultTimeProfile(
        name: String,
        newShouldFollowDefaultTimeProfile: Boolean
    ) {
        withContext(coroutineScope) {
            database.labelQueries.updateShouldFollowDefaultTimeProfile(
                newShouldFollowDefaultTimeProfile = newShouldFollowDefaultTimeProfile,
                name = name
            )
        }
    }

    suspend fun updateLabelIsArchived(name: String, newIsArchived: Boolean) {
        withContext(coroutineScope) {
            database.labelQueries.updateIsArchived(newIsArchived, name)
        }
    }

    fun selectAllLabels(): Flow<List<Label>> {
        return database.labelQueries.selectAll().asFlow().mapToList(coroutineScope)
    }

    fun selectLabelsByArchived(isArchived: Boolean): Flow<List<Label>> {
        return database.labelQueries.selectByIsArchived(isArchived).asFlow()
            .mapToList(coroutineScope)
    }

    suspend fun deleteLabel(name: String) {
        withContext(coroutineScope) {
            database.labelQueries.delete(name)
        }
    }

    internal suspend fun deleteAllLabels() {
        withContext(coroutineScope) {
            database.labelQueries.deleteAll()
        }
    }
}