package com.apps.adrcotfas.goodtime.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import com.apps.adrcotfas.goodtime.data.model.toExternalLabelMapper
import com.apps.adrcotfas.goodtime.data.model.toExternalSessionMapper
import com.apps.adrcotfas.goodtime.data.model.toLocal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class LocalDataRepositoryImpl(
    private val database: Database,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LocalDataRepository {

    init {
        insertDefaultLabel()
    }

    private fun insertDefaultLabel() {
        val localLabel = Label().toLocal()
        database.localLabelQueries.selectByName(null, ::toExternalLabelMapper).executeAsList().let {
            if (it.isEmpty()) {
                database.localLabelQueries.insert(localLabel)
            }
        }
    }

    override suspend fun insertSession(session: Session) {
        withContext(defaultDispatcher) {
            val localSession = session.toLocal()
            database.localSessionQueries.insert(localSession)
        }
    }

    override suspend fun updateSession(id: Long, newSession: Session) {
        withContext(defaultDispatcher) {
            val localSession = newSession.toLocal()
            database.localSessionQueries.update(id = id, newSession = localSession)
        }
    }

    override fun selectAllSessions(): Flow<List<Session>> {
        return database.localSessionQueries
            .selectAll(mapper = ::toExternalSessionMapper)
            .asFlow()
            .mapToList(defaultDispatcher)
    }

    override fun selectSessionById(id: Long): Flow<Session> {
        return database.localSessionQueries
            .selectById(id, mapper = ::toExternalSessionMapper)
            .asFlow()
            .mapToList(defaultDispatcher)
            .filterNot { it.isEmpty() }
            .map { it.first() }
    }

    override fun selectByIsArchived(isArchived: Boolean): Flow<List<Session>> {
        return database.localSessionQueries
            .selectByIsArchived(isArchived, mapper = ::toExternalSessionMapper)
            .asFlow()
            .mapToList(defaultDispatcher)
    }

    override fun selectSessionsByLabel(label: String?): Flow<List<Session>> {
        return database.localSessionQueries
            .selectByLabel(label, mapper = ::toExternalSessionMapper)
            .asFlow()
            .mapToList(defaultDispatcher)
    }

    override fun selectLastInsertSessionId(): Long? {
        return database.localSessionQueries
            .selectLastInsertSessionId().executeAsOneOrNull()
    }

    override suspend fun deleteSession(id: Long) {
        withContext(defaultDispatcher) {
            database.localSessionQueries.delete(id)
        }
    }

    override suspend fun deleteAllSessions() {
        withContext(defaultDispatcher) {
            database.localSessionQueries.deleteAll()
        }
    }

    override suspend fun insertLabel(label: Label) {
        if (label.name == null) return
        withContext(defaultDispatcher) {
            val localLabel = label.toLocal()
            database.localLabelQueries.insert(localLabel)
        }
    }

    override suspend fun updateLabelName(name: String, newName: String) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateName(newName = newName, name = name)
        }
    }

    override suspend fun updateLabelColorIndex(name: String, newColorIndex: Long) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateColorIndex(newColorIndex = newColorIndex, name = name)
        }
    }

    override suspend fun updateLabelOrderIndex(name: String, newOrderIndex: Long) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateOrderIndex(newOrderIndex = newOrderIndex, name = name)
        }
    }

    override suspend fun updateShouldFollowDefaultTimeProfile(
        name: String,
        newShouldFollowDefaultTimeProfile: Boolean
    ) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateShouldFollowDefaultTimeProfile(
                newShouldFollowDefaultTimeProfile = newShouldFollowDefaultTimeProfile,
                name = name
            )
        }
    }

    override suspend fun updateDefaultLabelTimerProfile(newTimerProfile: TimerProfile) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateTimerProfile(null, newTimerProfile)
        }
    }

    override suspend fun updateLabelTimerProfile(name: String, newTimerProfile: TimerProfile) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateTimerProfile(name, newTimerProfile)
        }
    }

    override fun selectDefaultLabel(): Flow<Label> {
        return database.localLabelQueries
            .selectByName(null, mapper = ::toExternalLabelMapper)
            .asFlow()
            .mapToList(defaultDispatcher)
            .filterNot { it.isEmpty() }
            .map { it.first() }
    }

    override suspend fun updateLabelIsArchived(name: String, newIsArchived: Boolean) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateIsArchived(newIsArchived, name)
        }
    }

    override fun selectLabelByName(name: String): Flow<Label> {
        return database.localLabelQueries
            .selectByName(name, mapper = ::toExternalLabelMapper)
            .asFlow()
            .mapToList(defaultDispatcher)
            .filterNot { it.isEmpty() }
            .map { it.first() }
    }

    override fun selectAllLabels(): Flow<List<Label>> {
        return database.localLabelQueries
            .selectAll(mapper = ::toExternalLabelMapper)
            .asFlow().mapToList(defaultDispatcher)
    }

    override fun selectLabelsByArchived(isArchived: Boolean): Flow<List<Label>> {
        return database.localLabelQueries
            .selectByIsArchived(isArchived, mapper = ::toExternalLabelMapper)
            .asFlow()
            .mapToList(defaultDispatcher)
    }

    override fun selectLastInsertLabelId(): Long? {
        return database.localLabelQueries
            .selectLastInsertLabelId().executeAsOneOrNull()
    }

    override suspend fun deleteLabel(name: String) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.delete(name)
        }
    }

    override suspend fun deleteAllLabels() {
        withContext(defaultDispatcher) {
            database.localLabelQueries.deleteAll()
        }
    }
}