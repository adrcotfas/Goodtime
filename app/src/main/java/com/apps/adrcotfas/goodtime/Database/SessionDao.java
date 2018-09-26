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

    @Query("select * from Session where label = :label ORDER BY endTime DESC")
    LiveData<List<Session>> getSessions(String label);

    @Insert(onConflict = REPLACE)
    void addSession(Session session);

    @Query("update Session SET endTime = :endTime, totalTime = :totalTime, label = :label WHERE id = :id")
    void editSession(long id, long endTime, long totalTime, String label);

    @Query("delete from Session where id = :id")
    void deleteSession(long id);

    @Query("delete from Session")
    void deleteAllSessions();
}
