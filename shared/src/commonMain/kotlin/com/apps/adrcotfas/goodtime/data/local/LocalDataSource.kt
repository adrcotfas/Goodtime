package com.apps.adrcotfas.goodtime.data.local

import com.apps.adrcotfas.goodtime.Label
import com.apps.adrcotfas.goodtime.Session
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {

    suspend fun insertSession(session: Session)
    suspend fun updateSession(id: Long, newSession: Session)
    fun getAllSessions(): Flow<List<Session>>
    fun getSessionsByIsArchived(isArchived: Boolean): Flow<List<Session>>
    fun getSessionsByLabel(label: String?): Flow<List<Session>>
    suspend fun deleteSession(id: Long)
    suspend fun deleteSessionAfter(timestamp: Long)

    suspend fun insertLabel(label: Label)
    suspend fun updateLabelName(name: String, newName: String)
    suspend fun updateLabelColorIndex(name: String, newColorIndex: Long)
    suspend fun updateLabelOrderIndex(name: String, newOrderIndex: Long)
    suspend fun updateShouldFollowDefaultTimeProfile(name:String, newShouldFollowDefaultTimeProfile: Boolean)
    suspend fun updateLabelIsArchived(name: String, newIsArchived: Boolean)
    fun getAllLabels(): Flow<List<Label>>
    fun getAllLabelsByArchived(isArchived: Boolean): Flow<List<Label>>
    suspend fun deleteLabel(name: String)
}