package com.apps.adrcotfas.goodtime.Database;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Session {

    @PrimaryKey
    @NonNull
    public Date endTime;

    @NonNull
    public long totalTime;

    public String label;
}
