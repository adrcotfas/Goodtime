package com.apps.adrcotfas.goodtime.di

import com.apps.adrcotfas.goodtime.labels.ArchivedLabelsViewModel
import com.apps.adrcotfas.goodtime.labels.LabelsViewModel
import com.apps.adrcotfas.goodtime.main.MainViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val viewModelModule: Module = module {
    singleOf(::MainViewModel)
    singleOf(::LabelsViewModel)
    singleOf(::ArchivedLabelsViewModel)
}
