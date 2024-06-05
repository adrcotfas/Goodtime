package com.apps.adrcotfas.goodtime.data.local

import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for finished sessions and labels.
 */
interface LocalDataRepository {

    suspend fun insertSession(session: Session)
    suspend fun updateSession(id: Long, newSession: Session)
    fun selectAllSessions(): Flow<List<Session>>
    fun selectSessionById(id: Long): Flow<Session>
    fun selectByIsArchived(isArchived: Boolean): Flow<List<Session>>
    fun selectSessionsByLabel(label: String?): Flow<List<Session>>
    fun selectLastInsertSessionId(): Long?
    suspend fun deleteSession(id: Long)
    suspend fun deleteAllSessions()

    suspend fun insertLabel(label: Label)
    suspend fun updateLabelName(name: String, newName: String)
    suspend fun updateLabelColorIndex(name: String, newColorIndex: Long)
    suspend fun updateLabelOrderIndex(name: String, newOrderIndex: Long)
    suspend fun updateLabelIsArchived(name: String, newIsArchived: Boolean)
    suspend fun updateShouldFollowDefaultTimeProfile(
        name: String,
        newShouldFollowDefaultTimeProfile: Boolean
    )
    suspend fun updateDefaultLabelTimerProfile(newTimerProfile: TimerProfile)
    suspend fun updateLabelTimerProfile(name: String, newTimerProfile: TimerProfile)
    fun selectDefaultLabel(): Flow<Label>
    fun selectLabelByName(name: String): Flow<Label>
    fun selectAllLabels(): Flow<List<Label>>
    fun selectAllLabelsArchived(): Flow<List<Label>>
    fun selectLastInsertLabelId(): Long?
    suspend fun deleteLabel(name: String)
    suspend fun deleteAllLabels()
}