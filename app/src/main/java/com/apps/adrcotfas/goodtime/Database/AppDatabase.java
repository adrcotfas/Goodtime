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

package com.apps.adrcotfas.goodtime.Database;

import android.content.Context;

import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Session;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Session.class, LabelAndColor.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static AppDatabase INSTANCE;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE labels_new (title TEXT NOT NULL, colorId INTEGER NOT NULL, 'order' INTEGER NOT NULL DEFAULT 0, archived INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(title, archived))");
            database.execSQL(
                    "INSERT INTO labels_new (title, colorId) SELECT label, color FROM LabelAndColor");
            database.execSQL("DROP TABLE LabelAndColor");
            database.execSQL("ALTER TABLE labels_new RENAME TO LabelAndColor");

            database.execSQL(
                    "CREATE TABLE sessions_new (id INTEGER NOT NULL, timestamp INTEGER NOT NULL, duration INTEGER NOT NULL, label TEXT, archived INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id), FOREIGN KEY(label, archived) REFERENCES LabelAndColor(title, archived) ON UPDATE CASCADE ON DELETE SET DEFAULT)");
            database.execSQL(
                    "INSERT INTO sessions_new (timestamp, duration, label) SELECT endTime, totalTime, label FROM Session");
            database.execSQL("DROP TABLE Session");
            database.execSQL("ALTER TABLE sessions_new RENAME TO Session");
        }
    };

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null || !INSTANCE.isOpen()) {
            synchronized (LOCK) {
                if (INSTANCE == null || !INSTANCE.isOpen()) {
                    recreateInstance(context);
                }
            }
        }
        return INSTANCE;
    }

    public static void closeInstance() {
        if (INSTANCE.isOpen()) {
            INSTANCE.getOpenHelper().close();
        }
    }

    public static void recreateInstance(Context context) {
        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "goodtime-db")
                .setJournalMode(JournalMode.TRUNCATE)
                .addMigrations(MIGRATION_1_2)
                .build();
    }

    public abstract SessionDao sessionModel();

    public abstract LabelAndColorDao labelAndColor();
}
