package com.apps.adrcotfas.goodtime;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LabelAndColor {

    public LabelAndColor(String label, int color) {
        this.label = label;
        this.color = color;
    }

    @PrimaryKey
    @NonNull
    public String label;

    public int color;
}
