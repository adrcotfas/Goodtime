package com.apps.adrcotfas.goodtime.di

import com.apps.adrcotfas.goodtime.bl.BreakBudgetHandler
import com.apps.adrcotfas.goodtime.bl.EventListener
import com.apps.adrcotfas.goodtime.bl.FinishedSessionsHandler
import com.apps.adrcotfas.goodtime.bl.StreakAndLongBreakHandler
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimerManager
import com.apps.adrcotfas.goodtime.data.local.LocalDataRepository
import com.apps.adrcotfas.goodtime.data.settings.SettingsRepository
import org.koin.dsl.module

val timerManagerModule = module {
    single<TimerManager> {
        TimerManager(
            get<LocalDataRepository>(),
            get<SettingsRepository>(),
            get<List<EventListener>>(),
            get<TimeProvider>(),
            get<FinishedSessionsHandler>(),
            get<StreakAndLongBreakHandler>(),
            get<BreakBudgetHandler>(),
            getWith(TimerManager::class.simpleName)
        )
    }
}