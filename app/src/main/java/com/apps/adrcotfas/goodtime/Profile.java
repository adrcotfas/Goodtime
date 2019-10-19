package com.apps.adrcotfas.goodtime;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Profile {

    @Ignore
    public Profile(String name
            , int durationWork
            , int durationBreak) {
        this.name = name;
        this.durationWork = durationWork;
        this.durationBreak = durationBreak;
        this.enableLongBreak = false;
        this.durationLongBreak = 15;
        this.sessionsBeforeLongBreak = 4;
    }

    public Profile(String name
            , int durationWork
            , int durationBreak
            , int durationLongBreak
            , int sessionsBeforeLongBreak) {
        this.name = name;
        this.durationWork = durationWork;
        this.durationBreak = durationBreak;
        this.enableLongBreak = true;
        this.durationLongBreak = durationLongBreak;
        this.sessionsBeforeLongBreak = sessionsBeforeLongBreak;
    }

    @PrimaryKey
    @NonNull
    public String name;

    public int durationWork;
    public int durationBreak;

    public boolean enableLongBreak;
    public int durationLongBreak;
    public int sessionsBeforeLongBreak;
}
