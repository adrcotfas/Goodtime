package com.apps.adrcotfas.goodtime.data.local

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}

const val DATABASE_NAME = "goodtime-db"