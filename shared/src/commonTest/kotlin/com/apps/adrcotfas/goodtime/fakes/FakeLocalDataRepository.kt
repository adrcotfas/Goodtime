package com.apps.adrcotfas.goodtime.fakes

import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.Session
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeLocalDataRepository(
    labels: List<Label> = emptyList(),
    sessions: List<Session> = emptyList()
) : LocalDataRepository {

    private val _labels: MutableList<Label> = labels.toMutableList()
    private val _sessions: MutableList<Session> = sessions.toMutableList()

    override suspend fun insertSession(session: Session) {
        _sessions.add(session)
    }

    override suspend fun updateSession(id: Long, newSession: Session) {
        _sessions.find { it.id == id }?.let {
            _sessions.remove(it)
            _sessions.add(newSession)
        }
    }

    override fun selectAllSessions(): Flow<List<Session>> {
        return flowOf(_sessions)
    }

    override fun selectByIsArchived(isArchived: Boolean): Flow<List<Session>> {
        return flowOf(_sessions.filter { it.isArchived == isArchived })
    }

    override fun selectSessionsByLabel(label: String?): Flow<List<Session>> {
        return flowOf(_sessions.filter { it.label == label })
    }

    override fun selectLastInsertSessionId(): Long? {
        return _sessions.lastOrNull()?.id
    }

    override suspend fun deleteSession(id: Long) {
        _sessions.find { it.id == id }?.let {
            _sessions.remove(it)
        }
    }

    override suspend fun deleteSessionAfter(timestamp: Long) {
        _sessions.filter { it.timestamp > timestamp }.forEach {
            _sessions.remove(it)
        }
    }

    override suspend fun deleteAllSessions() {
        _sessions.clear()
    }

    override suspend fun insertLabel(label: Label) {
        _labels.add(label)
    }

    override suspend fun updateLabelName(name: String, newName: String) {
        _labels.find { it.name == name }?.let {
            _labels.remove(it)
            _labels.add(it.copy(name = newName))
        }
    }

    override suspend fun updateLabelColorIndex(name: String, newColorIndex: Long) {
        _labels.find { it.name == name }?.let {
            _labels.remove(it)
            _labels.add(it.copy(colorIndex = newColorIndex))
        }
    }

    override suspend fun updateLabelOrderIndex(name: String, newOrderIndex: Long) {
        _labels.find { it.name == name }?.let {
            _labels.remove(it)
            _labels.add(it.copy(orderIndex = newOrderIndex))
        }
    }

    override suspend fun updateLabelIsArchived(name: String, newIsArchived: Boolean) {
        _labels.find { it.name == name }?.let {
            _labels.remove(it)
            _labels.add(it.copy(isArchived = newIsArchived))
        }
    }

    override suspend fun updateShouldFollowDefaultTimeProfile(
        name: String,
        newShouldFollowDefaultTimeProfile: Boolean
    ) {
        _labels.find { it.name == name }?.let {
            _labels.remove(it)
            _labels.add(it.copy(useDefaultTimeProfile = newShouldFollowDefaultTimeProfile))
        }
    }

    override suspend fun updateDefaultLabelTimerProfile(newTimerProfile: TimerProfile) {
        _labels.find { it.name == null }?.let {
            _labels.remove(it)
            _labels.add(it.copy(timerProfile = newTimerProfile))
        }
    }

    override suspend fun updateLabelTimerProfile(name: String, newTimerProfile: TimerProfile) {
        _labels.find { it.name == name }?.let {
            _labels.remove(it)
            _labels.add(it.copy(timerProfile = newTimerProfile))
        }
    }

    override fun selectDefaultLabel(): Flow<Label> {
        return flowOf(_labels.find { it.name == null }!!)
    }

    override fun selectLabelByName(name: String): Flow<Label> {
        return flowOf(_labels.find { it.name == name }!!)
    }

    override fun selectAllLabels(): Flow<List<Label>> {
        return flowOf(_labels)
    }

    override fun selectLabelsByArchived(isArchived: Boolean): Flow<List<Label>> {
        return flowOf(_labels.filter { it.isArchived == isArchived })
    }

    override suspend fun deleteLabel(name: String) {
        _labels.find { it.name == name }?.let {
            _labels.remove(it)
        }
    }

    override suspend fun deleteAllLabels() {
        _labels.clear()
    }
}