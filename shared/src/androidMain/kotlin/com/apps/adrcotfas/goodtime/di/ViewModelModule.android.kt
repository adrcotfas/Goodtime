package com.apps.adrcotfas.goodtime.di

import com.apps.adrcotfas.goodtime.labels.LabelsViewModel
import com.apps.adrcotfas.goodtime.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

actual val viewModelModule: Module = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::LabelsViewModel)
}