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
    public final long id;

    public Session(long id, long endTime, int totalTime, @Nullable String label) {
        this.id = id;
        this.endTime = endTime;
        this.totalTime = totalTime;
        this.label = label;
    }

    public final long endTime;

    public final int totalTime;

    @Nullable
    public final String label;
}
