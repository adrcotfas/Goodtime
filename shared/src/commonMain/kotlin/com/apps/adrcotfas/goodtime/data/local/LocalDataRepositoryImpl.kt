package com.apps.adrcotfas.goodtime.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository.Companion.DEFAULT_LABEL_NAME
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import com.apps.adrcotfas.goodtime.data.model.toExternalLabelMapper
import com.apps.adrcotfas.goodtime.data.model.toExternalSessionMapper
import com.apps.adrcotfas.goodtime.data.model.toLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class LocalDataRepositoryImpl(
    private val database: Database,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : LocalDataRepository {

    init {
        insertDefaultLabel()
    }

    private fun insertDefaultLabel() {
        val localLabel = Label(
            name = DEFAULT_LABEL_NAME,
            colorIndex = 0,
            orderIndex = 0,
            useDefaultTimeProfile = true,
            timerProfile = TimerProfile(),
            isArchived = false
        ).toLocal()
        database.localLabelQueries.insert(localLabel)
    }

    override suspend fun insertSession(session: Session) {
        withContext(coroutineContext) {
            val localSession = session.toLocal()
            database.localSessionQueries.insert(localSession)
        }
    }

    override suspend fun updateSession(id: Long, newSession: Session) {
        withContext(coroutineContext) {
            val localSession = newSession.toLocal()
            database.localSessionQueries.update(id = id, newSession = localSession)
        }
    }

    override fun selectAllSessions(): Flow<List<Session>> {
        return database.localSessionQueries
            .selectAll(mapper = ::toExternalSessionMapper)
            .asFlow()
            .mapToList(coroutineContext)
    }

    override fun selectByIsArchived(isArchived: Boolean): Flow<List<Session>> {
        return database.localSessionQueries
            .selectByIsArchived(isArchived, mapper = ::toExternalSessionMapper)
            .asFlow()
            .mapToList(coroutineContext)
    }

    override fun selectSessionsByLabel(label: String?): Flow<List<Session>> {
        return database.localSessionQueries
            .selectByLabel(label, mapper = ::toExternalSessionMapper)
            .asFlow()
            .mapToList(coroutineContext)
    }

    override suspend fun deleteSession(id: Long) {
        withContext(coroutineContext) {
            database.localSessionQueries.delete(id)
        }
    }

    override suspend fun deleteSessionAfter(timestamp: Long) {
        withContext(coroutineContext) {
            database.localSessionQueries.deleteAfter(timestamp)
        }
    }

    override suspend fun deleteAllSessions() {
        withContext(coroutineContext) {
            database.localSessionQueries.deleteAll()
        }
    }

    override suspend fun insertLabel(label: Label) {
        if (label.name == DEFAULT_LABEL_NAME) return
        withContext(coroutineContext) {
            val localLabel = label.toLocal()
            database.localLabelQueries.insert(localLabel)
        }
    }

    override suspend fun updateLabelName(name: String, newName: String) {
        if (name == DEFAULT_LABEL_NAME) return
        withContext(coroutineContext) {
            database.localLabelQueries.updateName(newName = newName, name = name)
        }
    }

    override suspend fun updateLabelColorIndex(name: String, newColorIndex: Long) {
        if (name == DEFAULT_LABEL_NAME) return
        withContext(coroutineContext) {
            database.localLabelQueries.updateColorIndex(newColorIndex = newColorIndex, name = name)
        }
    }

    override suspend fun updateLabelOrderIndex(name: String, newOrderIndex: Long) {
        if (name == DEFAULT_LABEL_NAME) return
        withContext(coroutineContext) {
            database.localLabelQueries.updateOrderIndex(newOrderIndex = newOrderIndex, name = name)
        }
    }

    override suspend fun updateShouldFollowDefaultTimeProfile(
        name: String,
        newShouldFollowDefaultTimeProfile: Boolean
    ) {
        //TODO: at least log these attempts with warning
        if (name == DEFAULT_LABEL_NAME) return
        withContext(coroutineContext) {
            database.localLabelQueries.updateShouldFollowDefaultTimeProfile(
                newShouldFollowDefaultTimeProfile = newShouldFollowDefaultTimeProfile,
                name = name
            )
        }
    }

    override suspend fun updateDefaultLabelTimerProfile(newTimerProfile: TimerProfile) =
        updateLabelTimerProfile(
            DEFAULT_LABEL_NAME,
            newTimerProfile
        )

    override suspend fun updateLabelTimerProfile(name: String, newTimerProfile: TimerProfile) {
        withContext(coroutineContext) {
            database.localLabelQueries.updateTimerProfile(name, newTimerProfile)
        }
    }

    override fun selectDefaultLabel(): Flow<Label> {
        return database.localLabelQueries
            .selectByName(DEFAULT_LABEL_NAME, mapper = ::toExternalLabelMapper)
            .asFlow()
            .mapToList(coroutineContext)
            .map { it.first() }
    }

    override suspend fun updateLabelIsArchived(name: String, newIsArchived: Boolean) {
        if (name == DEFAULT_LABEL_NAME) return
        withContext(coroutineContext) {
            database.localLabelQueries.updateIsArchived(newIsArchived, name)
        }
    }

    override fun selectLabelByName(name: String): Flow<Label> {
        return database.localLabelQueries
            .selectByName(name, mapper = ::toExternalLabelMapper)
            .asFlow()
            .mapToList(coroutineContext)
            .map { it.first() }
    }

    override fun selectAllLabels(): Flow<List<Label>> {
        return database.localLabelQueries
            .selectAll(mapper = ::toExternalLabelMapper)
            .asFlow().mapToList(coroutineContext)
    }

    override fun selectLabelsByArchived(isArchived: Boolean): Flow<List<Label>> {
        return database.localLabelQueries
            .selectByIsArchived(isArchived, mapper = ::toExternalLabelMapper)
            .asFlow()
            .mapToList(coroutineContext)
    }

    override suspend fun deleteLabel(name: String) {
        if (name == DEFAULT_LABEL_NAME) return
        withContext(coroutineContext) {
            database.localLabelQueries.delete(name)
        }
    }

    override suspend fun deleteAllLabels() {
        withContext(coroutineContext) {
            database.localLabelQueries.deleteAll()
        }
    }
}