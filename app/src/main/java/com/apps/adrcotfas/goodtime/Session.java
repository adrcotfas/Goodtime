package com.apps.adrcotfas.goodtime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;
import static androidx.room.ForeignKey.SET_NULL;

@Entity(indices  = {@Index("label")},
        foreignKeys = @ForeignKey(
        entity = LabelAndColor.class,
        parentColumns = "label",
        childColumns = "label",
        onUpdate = CASCADE,
        onDelete = SET_NULL))
public class Session {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public Session(long id, long endTime, int totalTime, @Nullable String label) {
        this.id = id;
        this.endTime = endTime;
        this.totalTime = totalTime;
        this.label = label;
    }

    @NonNull
    public long endTime;

    @NonNull
    public int totalTime;

    @Nullable
    public String label;
}
