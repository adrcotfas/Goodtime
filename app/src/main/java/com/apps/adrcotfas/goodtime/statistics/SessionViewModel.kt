/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.apps.adrcotfas.goodtime.statistics

import com.apps.adrcotfas.goodtime.database.SessionDao
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.adrcotfas.goodtime.database.AppDatabase
import com.apps.adrcotfas.goodtime.database.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    database: AppDatabase
) : ViewModel() {

    private val dao: SessionDao = database.sessionModel()

    val allSessions: LiveData<List<Session>>
        get() = dao.allSessions

    val allSessionsUnlabeled: LiveData<List<Session>>
        get() = dao.allSessionsUnlabeled

    val allSessionsUnarchived: LiveData<List<Session>>
        get() = dao.allSessionsUnarchived

    fun getAllSessionsUnarchived(startMillis: Long, endMillis: Long): LiveData<List<Session>>
        = dao.getAllSessionsUnarchived(startMillis, endMillis)

    fun getSession(id: Long): LiveData<Session> {
        return dao.getSession(id)
    }

    fun addSession(session: Session) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.addSession(session)
        }
    }

    fun editSession(session: Session) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editSession(
                session.id, session.timestamp, session.duration, session.label
            )
        }
    }

    fun editLabel(id: Long?, label: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.editLabel(
                id!!, label
            )
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteSession(id)
        }
    }

    fun deleteSessionsFinishedAfter(timestamp: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteSessionsAfter(timestamp)
        }
    }

    fun getSessions(label: String): LiveData<List<Session>> {
        return dao.getSessions(label)
    }
}