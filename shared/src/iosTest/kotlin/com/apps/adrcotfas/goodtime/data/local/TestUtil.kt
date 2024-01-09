package com.apps.adrcotfas.goodtime.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration

internal actual fun testDbConnection(): SqlDriver {
    return NativeSqliteDriver(
        schema = Database.Schema,
        name = DATABASE_NAME,
        onConfiguration = { config: DatabaseConfiguration ->
            config.copy(
                extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true),
                inMemory = true
            )
        }

    )
}