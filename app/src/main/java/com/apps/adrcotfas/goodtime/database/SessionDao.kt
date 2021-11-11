/*
 * Copyright 2016-2021 Adrian Cotfas
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
package com.apps.adrcotfas.goodtime.database

import androidx.room.Dao
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apps.adrcotfas.goodtime.util.startOfTodayMillis
import com.apps.adrcotfas.goodtime.util.startOfTomorrowMillis

@Dao
interface SessionDao {
    @Query("select * from Session where id = :id")
    fun getSession(id: Long): LiveData<Session>

    @get:Query("select * from Session ORDER BY timestamp DESC")
    val allSessions: LiveData<List<Session>>

    @get:Query("select * from Session where (archived is 0 OR archived is NULL) ORDER BY timestamp DESC")
    val allSessionsUnarchived: LiveData<List<Session>>

    @Query("select * from Session where (archived is 0 OR archived is NULL) and timestamp >= :intervalStart and timestamp < :intervalEnd ORDER BY timestamp DESC")
    fun getAllSessionsUnarchived(intervalStart : Long, intervalEnd : Long): LiveData<List<Session>>

    @get:Query("select * from Session where label is NULL ORDER BY timestamp DESC")
    val allSessionsUnlabeled: LiveData<List<Session>>

    @Query("select * from Session where label = :label ORDER BY timestamp DESC")
    fun getSessions(label: String): LiveData<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSession(session: Session)

    @Query("update Session SET timestamp = :timestamp, duration = :duration, label = :label WHERE id = :id")
    suspend fun editSession(id: Long, timestamp: Long, duration: Int, label: String?)

    @Query("update Session SET label = :label WHERE id = :id")
    suspend fun editLabel(id: Long, label: String?)

    @Query("delete from Session where id = :id")
    suspend fun deleteSession(id: Long)

    /**
     * Deletes sessions finished at a later timestamp than the one provided as input.
     * Typically used to delete today's finished sessions
     * @param timestamp Sessions finished later than this timestamp will be deleted
     */
    @Query("delete from Session where timestamp >= :timestamp")
    suspend fun deleteSessionsAfter(timestamp: Long)

    @Query("delete from Session")
    suspend fun deleteAllSessions()
}