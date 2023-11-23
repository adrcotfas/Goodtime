package com.apps.adrcotfas.goodtime.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun create(): SqlDriver {
        return AndroidSqliteDriver(Database.Schema, context, "goodtime-db")
    }
}