package com.apps.adrcotfas.goodtime.di

import com.apps.adrcotfas.goodtime.data.local.backup.BackupViewModel
import com.apps.adrcotfas.goodtime.labels.archived.ArchivedLabelsViewModel
import com.apps.adrcotfas.goodtime.labels.main.LabelsViewModel
import com.apps.adrcotfas.goodtime.main.MainViewModel
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val viewModelModule: Module = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    viewModelOf(::MainViewModel)
    viewModelOf(::LabelsViewModel)
    viewModelOf(::ArchivedLabelsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::BackupViewModel)
}