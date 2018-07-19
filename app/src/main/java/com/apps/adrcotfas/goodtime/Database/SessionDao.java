package com.apps.adrcotfas.goodtime.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface SessionDao {

    @Query("select * from Session where id = :id")
    LiveData<Session> getSessionById(long id);

    @Insert(onConflict = REPLACE)
    long addSession(Session borrowModel);

    @Delete
    void deleteSession(Session session);
}
