/*
 * Copyright 2016-2021 Adrian Cotfas
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

package com.apps.adrcotfas.goodtime.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE labels_new (title TEXT NOT NULL, colorId INTEGER NOT NULL, 'order' INTEGER NOT NULL DEFAULT 0, archived INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(title, archived))"
        )
        db.execSQL(
            "INSERT INTO labels_new (title, colorId) SELECT label, color FROM LabelAndColor"
        )
        db.execSQL("DROP TABLE LabelAndColor")
        db.execSQL("ALTER TABLE labels_new RENAME TO Label")
        db.execSQL(
            "CREATE TABLE sessions_new (id INTEGER NOT NULL, timestamp INTEGER NOT NULL, duration INTEGER NOT NULL, label TEXT, archived INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id), FOREIGN KEY(label, archived) REFERENCES Label(title, archived) ON UPDATE CASCADE ON DELETE SET DEFAULT)"
        )
        db.execSQL(
            "INSERT INTO sessions_new (timestamp, duration, label) SELECT endTime, totalTime, label FROM Session"
        )
        db.execSQL("DROP TABLE Session")
        db.execSQL("ALTER TABLE sessions_new RENAME TO Session")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE Profile (name TEXT NOT NULL, durationWork INTEGER NOT NULL" +
                    ", durationBreak INTEGER NOT NULL" +
                    ", enableLongBreak INTEGER NOT NULL" +
                    ", durationLongBreak INTEGER NOT NULL" +
                    ", sessionsBeforeLongBreak INTEGER NOT NULL" +
                    ", PRIMARY KEY(name))"
        )
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE sessions_new (id INTEGER NOT NULL, timestamp INTEGER NOT NULL, duration INTEGER NOT NULL, label TEXT, archived INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id), FOREIGN KEY(label, archived) REFERENCES Label(title, archived) ON UPDATE CASCADE ON DELETE SET DEFAULT)"
        )
        db.execSQL(
            "INSERT INTO sessions_new (id, timestamp, duration, label, archived) SELECT id, timestamp, duration, label, archived FROM Session"
        )
        db.execSQL("DROP TABLE Session")
        db.execSQL("ALTER TABLE sessions_new RENAME TO Session")
    }
}

val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // do nothing here; it seems to be needed by the switch to kapt room compiler
    }
}

val MIGRATION_5_6: Migration = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE SessionTmp (id INTEGER NOT NULL, timestamp INTEGER NOT NULL, duration INTEGER NOT NULL, label TEXT, archived INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(id), FOREIGN KEY(label, archived) REFERENCES Label(title, archived) ON UPDATE CASCADE ON DELETE SET DEFAULT)")
        db.execSQL(
            "INSERT INTO SessionTmp (timestamp, duration, label) SELECT timestamp, duration, label FROM Session")
        db.execSQL("DROP TABLE Session")
        db.execSQL("ALTER TABLE SessionTmp RENAME TO Session")

        db.execSQL(
            "CREATE TABLE LabelTmp (title TEXT NOT NULL DEFAULT '', colorId INTEGER NOT NULL DEFAULT 0, 'order' INTEGER NOT NULL DEFAULT 0, archived INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(title, archived))")
        db.execSQL(
            "INSERT INTO LabelTmp (title, colorId, 'order', archived) SELECT title, colorId, 'order', archived FROM Label")
        db.execSQL("DROP TABLE Label")
        db.execSQL("ALTER TABLE LabelTmp RENAME TO Label")

    }
}