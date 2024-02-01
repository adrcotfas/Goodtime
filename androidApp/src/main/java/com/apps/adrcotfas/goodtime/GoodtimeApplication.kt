package com.apps.adrcotfas.goodtime

import android.app.Application
import android.content.Context
import com.apps.adrcotfas.goodtime.di.insertKoin
import com.apps.adrcotfas.goodtime.domain.NotificationArchManager
import com.apps.adrcotfas.goodtime.domain.TimerServiceHandler
import com.apps.adrcotfas.goodtime.domain.TimerServiceHandlerImpl
import com.apps.adrcotfas.goodtime.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class GoodtimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        insertKoin(
            module {
                single<Context> { this@GoodtimeApplication }
                viewModel { MainViewModel(get()) }
                single<NotificationArchManager> {
                    NotificationArchManager(
                        get(),
                        MainActivity::class.java
                    )
                }
                single<TimerServiceHandler> { TimerServiceHandlerImpl(get()) }
            }
        )
    }
}