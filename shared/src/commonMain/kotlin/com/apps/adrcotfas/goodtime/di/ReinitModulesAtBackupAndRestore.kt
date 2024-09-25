package com.apps.adrcotfas.goodtime.di

import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

fun reinitModulesAtBackupAndRestore()  {
    unloadKoinModules(localDataModule)
    loadKoinModules(localDataModule)
}