package com.apps.adrcotfas.goodtime.Database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface SessionDao {

    @Query("select * from Session where :endTime == :endTime")
    LiveData<List<Session>> getSessionByEndTime(long endTime);

    @Insert(onConflict = REPLACE)
    long addSession(Session session);

    @Delete
    void deleteSession(Session session);
}
