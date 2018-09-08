package com.apps.adrcotfas.goodtime;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Session {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public Session(long id, long endTime, long totalTime, String label) {
        this.id = id;
        this.endTime = endTime;
        this.totalTime = totalTime;
        this.label = label;
    }

    @NonNull
    public long endTime;

    @NonNull
    public long totalTime;

    public String label;
}
