package com.apps.adrcotfas.goodtime.data.local

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Session
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for finished sessions and labels.
 */
interface LocalDataRepository {
    fun reinitDatabase(database: Database)
    suspend fun insertSession(session: Session)
    suspend fun updateSession(id: Long, newSession: Session)
    fun selectAllSessions(): Flow<List<Session>>
    fun selectSessionsAfter(timestamp: Long): Flow<List<Session>>
    fun selectSessionById(id: Long): Flow<Session>
    fun selectByIsArchived(isArchived: Boolean): Flow<List<Session>>
    fun selectSessionsByLabel(label: String): Flow<List<Session>>
    fun selectLastInsertSessionId(): Long?
    suspend fun deleteSession(id: Long)
    suspend fun deleteAllSessions()

    suspend fun insertLabel(label: Label)
    suspend fun insertLabelAndBulkRearrange(label: Label, labelsToUpdate: List<Pair<String, Long>>)
    suspend fun updateLabelOrderIndex(name: String, newOrderIndex: Long)
    suspend fun bulkUpdateLabelOrderIndex(labelsToUpdate: List<Pair<String, Long>>)
    suspend fun updateLabelIsArchived(name: String, newIsArchived: Boolean)
    suspend fun updateLabel(name: String, newLabel: Label)
    suspend fun updateDefaultLabel(newDefaultLabel: Label)
    fun selectDefaultLabel(): Flow<Label?>
    fun selectLabelByName(name: String): Flow<Label?>
    fun selectAllLabels(): Flow<List<Label>>
    fun selectAllLabelsArchived(): Flow<List<Label>>
    fun selectLastInsertLabelId(): Long?
    suspend fun deleteLabel(name: String)
    suspend fun deleteAllLabels()
}