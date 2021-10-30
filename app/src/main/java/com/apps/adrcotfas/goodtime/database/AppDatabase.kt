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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import com.apps.adrcotfas.goodtime.bl.GoodtimeApplication
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@Database(
    entities = [Session::class, Label::class, Profile::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionModel(): SessionDao
    abstract fun labelDao(): LabelDao
    abstract fun profileDao(): ProfileDao

    companion object {
        private const val TAG = "GoodtimeDatabase"
        private val dbToInstanceId = ConcurrentHashMap<Int, String>()
        private val threadToInstanceId = ConcurrentHashMap<Long, String>()

        const val DATABASE_NAME = "goodtime-db"
        private val LOCK = Any()
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null || !INSTANCE!!.isOpen) {
                synchronized(LOCK) {
                    if (INSTANCE == null || !INSTANCE!!.isOpen) {
                        INSTANCE = recreateInstance(context)
                    }
                }
            }
            return INSTANCE!!
        }

        fun closeInstance() {
            if (INSTANCE!!.isOpen) {
                INSTANCE!!.openHelper.close()
            }
        }

        fun recreateInstance(context: Context): AppDatabase {
            // keep track of which thread belongs to which local database
            val instanceId = UUID.randomUUID().toString()

            // custom thread with an exception handler strategy
            val executor = Executors.newCachedThreadPool { runnable: Runnable? ->
                val defaultThreadFactory =
                    Executors.defaultThreadFactory()
                val thread = defaultThreadFactory.newThread(runnable)
                thread.uncaughtExceptionHandler = resetDatabaseOnUnhandledException
                threadToInstanceId[thread.id] = instanceId
                thread
            } as ThreadPoolExecutor

            val db = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .setJournalMode(JournalMode.TRUNCATE)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .setQueryExecutor(executor)
                .build()
            dbToInstanceId[db.hashCode()] = instanceId
            return db
        }

        private var resetDatabaseOnUnhandledException =
            Thread.UncaughtExceptionHandler { thread, throwable ->
                val message = "uncaught exception in a LocalDatabase thread, resetting the database"
                Log.e(TAG, message, throwable)
                synchronized(LOCK) {
                    // there is no active local database to clean up
                    if (INSTANCE == null) return@UncaughtExceptionHandler
                    val instanceIdOfThread: String? = threadToInstanceId[thread.id]
                    val instanceIdOfActiveLocalDb: String? = dbToInstanceId[INSTANCE.hashCode()]
                    if (instanceIdOfThread == null || instanceIdOfThread != instanceIdOfActiveLocalDb) {
                        // the active local database instance is not the one
                        // that caused this thread to fail, so leave it as is
                        return@UncaughtExceptionHandler
                    }
                    INSTANCE!!.tryResetDatabase()
                }
            }
    }

    private fun tryResetDatabase() {
        try {
            // try closing existing connections
            try {
                if (this.openHelper.writableDatabase.isOpen) {
                    this.openHelper.writableDatabase.close()
                }
                if (this.openHelper.readableDatabase.isOpen) {
                    this.openHelper.readableDatabase.close()
                }
                if (this.isOpen) {
                    this.close()
                }
                if (this == INSTANCE) INSTANCE = null
            } catch (ex: Exception) {
                Log.e(TAG, "Could not close LocalDatabase", ex)
            }

            // try deleting database file
            val f: File = GoodtimeApplication.context.getDatabasePath(DATABASE_NAME)
            if (f.exists()) {
                val deleteSucceeded = SQLiteDatabase.deleteDatabase(f)
                if (!deleteSucceeded) {
                    Log.e(TAG, "Could not delete LocalDatabase")
                }
            }

            val tmp: AppDatabase = recreateInstance(GoodtimeApplication.context)
            tmp.query("SELECT * from Session", null)
            tmp.close()

            this.openHelper.readableDatabase
            this.openHelper.writableDatabase
            this.query("SELECT * from Session", null)
        } catch (ex: Exception) {
            Log.e(TAG, "Could not reset LocalDatabase", ex)
        }
    }
}