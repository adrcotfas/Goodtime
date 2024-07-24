package com.apps.adrcotfas.goodtime.di

import com.apps.adrcotfas.goodtime.labels.archived.ArchivedLabelsViewModel
import com.apps.adrcotfas.goodtime.labels.main.LabelsViewModel
import com.apps.adrcotfas.goodtime.main.MainViewModel
import com.apps.adrcotfas.goodtime.settings.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val viewModelModule: Module = module {
    singleOf(::MainViewModel)
    singleOf(::LabelsViewModel)
    singleOf(::ArchivedLabelsViewModel)
    singleOf(::SettingsViewModel)
}
