package com.apps.adrcotfas.goodtime.data.local

import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.apps.adrcotfas.goodtime.LocalLabel

object DatabaseExt {
    operator fun Database.Companion.invoke(driver: SqlDriver): Database {
        return Database(
            driver = driver,
            localLabelAdapter = LocalLabel.Adapter(
                IntColumnAdapter,
                IntColumnAdapter,
                IntColumnAdapter,
                IntColumnAdapter,
                IntColumnAdapter
            )
        )
    }
}