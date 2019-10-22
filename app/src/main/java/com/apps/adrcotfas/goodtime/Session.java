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
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;
import static androidx.room.ForeignKey.SET_DEFAULT;

@Entity(
        foreignKeys =
        @ForeignKey(
                entity = Label.class,
                parentColumns = {"title", "archived"},
                childColumns = {"label", "archived"},
                onUpdate = CASCADE,
                onDelete = SET_DEFAULT))
public class Session {

    public Session(long id, long timestamp, int duration, @Nullable String label) {
        this.id = id;
        this.timestamp = timestamp;
        this.duration = duration;
        this.label = label;
        this.archived = false;
    }

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long timestamp;

    public int duration;

    @Nullable
    public String label;

    @NonNull
    public Boolean archived;
}
