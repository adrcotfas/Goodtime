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

import com.apps.adrcotfas.goodtime.database.AppDatabase.Companion.getDatabase
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.apps.adrcotfas.goodtime.database.SessionDao
import androidx.lifecycle.LiveData
import com.apps.adrcotfas.goodtime.database.Session
import org.joda.time.LocalDate
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: SessionDao = getDatabase(application).sessionModel()
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    val allSessionsByEndTime: LiveData<List<Session>>
        get() = dao.allSessionsByEndTime

    fun getSession(id: Long): LiveData<Session> {
        return dao.getSession(id)
    }

    fun addSession(session: Session) {
        executorService.execute { dao.addSession(session) }
    }

    fun editSession(id: Long?, endTime: Long, totalTime: Long, label: String?) {
        executorService.execute {
            dao.editSession(
                id!!, endTime, totalTime, label
            )
        }
    }

    fun editLabel(id: Long?, label: String?) {
        executorService.execute {
            dao.editLabel(
                id!!, label
            )
        }
    }

    fun deleteSession(id: Long) {
        executorService.execute { dao.deleteSession(id) }
    }

    fun deleteSessionsFinishedToday() {
        executorService.execute {
            dao.deleteSessionsAfter(
                LocalDate().toDateTimeAtStartOfDay().millis
            )
        }
    }

    fun getSessions(label: String): LiveData<List<Session>> {
        return dao.getSessions(label)
    }

    val allSessionsUnlabeled: LiveData<List<Session>>
        get() = dao.allSessionsUnlabeled
    val allSessions: LiveData<List<Session>>
        get() = dao.allSessions

}