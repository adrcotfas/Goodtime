package com.apps.adrcotfas.goodtime.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface SessionDao {

    @Query("select * from Session where id = :id")
    LiveData<Session> getSessionById(long id);

    @Insert(onConflict = REPLACE)
    long addSession(Session borrowModel);

    @Delete
    void deleteSession(Session session);
}
