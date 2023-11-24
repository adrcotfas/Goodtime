package com.apps.adrcotfas.goodtime

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.apps.adrcotfas.goodtime.data.local.Database
import java.util.Properties

internal actual fun testDbConnection(): SqlDriver = JdbcSqliteDriver(
    JdbcSqliteDriver.IN_MEMORY,
    properties = Properties().apply { put("foreign_keys", "true") })
    .also { Database.Schema.create(it) }