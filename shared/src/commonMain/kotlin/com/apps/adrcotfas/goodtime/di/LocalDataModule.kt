package com.apps.adrcotfas.goodtime.di

import app.cash.sqldelight.db.SqlDriver
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import org.koin.dsl.module

internal fun getDbPath(producePath: () -> String): String = producePath()
internal fun getTmpPath(producePath: () -> String): String = producePath()

val localDataModule = module {
    single<SqlDriver> { get<DatabaseDriverFactory>().create() }
}
