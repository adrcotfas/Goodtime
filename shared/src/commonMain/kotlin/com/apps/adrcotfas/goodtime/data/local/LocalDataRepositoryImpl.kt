package com.apps.adrcotfas.goodtime.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Session
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
    private var database: Database,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LocalDataRepository {

    init {
        insertDefaultLabel()
    }

    override fun reinitDatabase(database: Database) {
        this.database = database
        insertDefaultLabel()
    }

    private fun insertDefaultLabel() {
        database.localLabelQueries.selectByName(Label.DEFAULT_LABEL_NAME, ::toExternalLabelMapper)
            .executeAsList().let {
                if (it.isEmpty()) {
                    val localLabel = Label.defaultLabel().toLocal()
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

    override fun selectSessionsByLabel(label: String): Flow<List<Session>> {
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
        if (label.name.isEmpty()) return
        withContext(defaultDispatcher) {
            val localLabel = label.toLocal()
            database.localLabelQueries.insert(localLabel)
        }
    }

    override suspend fun insertLabelAndBulkRearrange(
        label: Label,
        labelsToUpdate: List<Pair<String, Long>>
    ) {
        withContext(defaultDispatcher) {
            database.transaction {
                val localLabel = label.toLocal()
                database.localLabelQueries.insert(localLabel)
                labelsToUpdate.forEach { (name, newOrderIndex) ->
                    database.localLabelQueries.updateOrderIndex(newOrderIndex, name)
                }
            }
        }
    }

    override suspend fun updateLabelOrderIndex(name: String, newOrderIndex: Long) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateOrderIndex(newOrderIndex = newOrderIndex, name = name)
        }
    }

    override suspend fun bulkUpdateLabelOrderIndex(labelsToUpdate: List<Pair<String, Long>>) {
        withContext(defaultDispatcher) {
            database.transaction {
                labelsToUpdate.forEach { (name, newOrderIndex) ->
                    database.localLabelQueries.updateOrderIndex(newOrderIndex, name)
                }
            }
        }
    }

    override suspend fun updateLabel(
        name: String,
        newLabel: Label
    ) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateLabel(
                newName = newLabel.name,
                newColorIndex = newLabel.colorIndex,
                newUseDefaultTimeProfile = newLabel.useDefaultTimeProfile,
                newIsCountdown = newLabel.timerProfile.isCountdown,
                newWorkDuration = newLabel.timerProfile.workDuration,
                newIsBreakEnabled = newLabel.timerProfile.isBreakEnabled,
                newBreakDuration = newLabel.timerProfile.breakDuration,
                newIsLongBreakEnabled = newLabel.timerProfile.isLongBreakEnabled,
                newLongBreakDuration = newLabel.timerProfile.longBreakDuration,
                newSessionsBeforeLongBreak = newLabel.timerProfile.sessionsBeforeLongBreak,
                newWorkBreakRatio = newLabel.timerProfile.workBreakRatio,
                name = name
            )
        }
    }

    override suspend fun updateDefaultLabel(newDefaultLabel: Label) {
        updateLabel(Label.DEFAULT_LABEL_NAME, newDefaultLabel)
    }

    override fun selectDefaultLabel() = selectLabelByName(Label.DEFAULT_LABEL_NAME)

    override suspend fun updateLabelIsArchived(name: String, newIsArchived: Boolean) {
        withContext(defaultDispatcher) {
            database.localLabelQueries.updateIsArchived(newIsArchived, name)
        }
    }

    override fun selectLabelByName(name: String): Flow<Label?> {
        return database.localLabelQueries
            .selectByName(name, mapper = ::toExternalLabelMapper)
            .asFlow()
            .mapToList(defaultDispatcher).map {
                if (it.isEmpty()) null else it.first()
            }
    }

    override fun selectAllLabels(): Flow<List<Label>> {
        return database.localLabelQueries
            .selectAll(mapper = ::toExternalLabelMapper)
            .asFlow().mapToList(defaultDispatcher)
    }

    override fun selectAllLabelsArchived(): Flow<List<Label>> {
        return database.localLabelQueries
            .selectAllArchived(mapper = ::toExternalLabelMapper)
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