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

package com.apps.adrcotfas.goodtime.Database;

import com.apps.adrcotfas.goodtime.Session;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface SessionDao {

    @Query("select * from Session where id = :id")
    LiveData<Session> getSession(long id);

    @Query("select * from Session ORDER BY endTime DESC")
    LiveData<List<Session>> getAllSessionsByEndTime();

    @Query("select * from Session ORDER BY totalTime DESC")
    LiveData<List<Session>> getAllSessionsByDuration();

    @Query("select * from Session where label is NULL ORDER BY endTime DESC")
    LiveData<List<Session>> getAllSessionsUnlabeled();

    @Query("select * from Session where label = :label ORDER BY endTime DESC")
    LiveData<List<Session>> getSessions(String label);

    @Insert(onConflict = REPLACE)
    void addSession(Session session);

    @Query("update Session SET endTime = :endTime, totalTime = :totalTime, label = :label WHERE id = :id")
    void editSession(long id, long endTime, long totalTime, String label);

    @Query("update Session SET label = :label WHERE id = :id")
    void editLabel(long id, String label);

    @Query("delete from Session where id = :id")
    void deleteSession(long id);

    @Query("delete from Session")
    void deleteAllSessions();
}
