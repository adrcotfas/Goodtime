package com.apps.adrcotfas.goodtime.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.apps.adrcotfas.goodtime.Label
import com.apps.adrcotfas.goodtime.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LocalDataSourceImpl(private val database: Database) : LocalDataSource {

    private val coroutineScope = Dispatchers.IO
    override suspend fun insertSession(session: Session) {
        withContext(coroutineScope) {
            database.sessionQueries.insert(
                id = session.id,
                timestamp = session.timestamp,
                duration = session.duration,
                label = session.label,
                notes = session.notes,
                isArchived = session.isArchived
            )
        }
    }

    override suspend fun updateSession(id: Long, newSession: Session) {
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

    override fun getAllSessions(): Flow<List<Session>> {
        return database.sessionQueries.getAll().asFlow().mapToList(coroutineScope)
    }

    override fun getSessionsByIsArchived(isArchived: Boolean): Flow<List<Session>> {
        return database.sessionQueries.getByIsArchived(isArchived).asFlow()
            .mapToList(coroutineScope)
    }

    override fun getSessionsByLabel(label: String?): Flow<List<Session>> {
        return database.sessionQueries.getByLabel(label).asFlow().mapToList(coroutineScope)
    }

    override suspend fun deleteSession(id: Long) {
        withContext(coroutineScope) {
            database.sessionQueries.delete(id)
        }
    }

    override suspend fun deleteSessionAfter(timestamp: Long) {
        withContext(coroutineScope) {
            database.sessionQueries.deleteAfter(timestamp)
        }
    }

    override suspend fun insertLabel(label: Label) {
        withContext(coroutineScope) {
            database.labelQueries.insert(
                name = label.name,
                colorIndex = label.colorIndex,
                orderIndex = label.orderIndex,
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

    override suspend fun updateLabelName(name: String, newName: String) {
        withContext(coroutineScope) {
            database.labelQueries.updateName(newName = newName, name = name)
        }
    }

    override suspend fun updateLabelColorIndex(name: String, newColorIndex: Long) {
        withContext(coroutineScope) {
            database.labelQueries.updateColorIndex(newColorIndex = newColorIndex, name = name)
        }
    }

    override suspend fun updateLabelOrderIndex(name: String, newOrderIndex: Long) {
        withContext(coroutineScope) {
            database.labelQueries.updateOrderIndex(newOrderIndex = newOrderIndex, name = name)
        }
    }

    override suspend fun updateLabelIsArchived(name: String, newIsArchived: Boolean) {
        withContext(coroutineScope) {
            database.labelQueries.updateIsArchived(newIsArchived, name)
        }
    }

    override fun getAllLabels(): Flow<List<Label>> {
        return database.labelQueries.getAll().asFlow().mapToList(coroutineScope)
    }

    override fun getAllLabelsByArchived(isArchived: Boolean): Flow<List<Label>> {
        return database.labelQueries.getByIsArchived(isArchived).asFlow().mapToList(coroutineScope)
    }

    override suspend fun deleteLabel(name: String) {
        withContext(coroutineScope) {
            database.labelQueries.delete(name)
        }
    }
}