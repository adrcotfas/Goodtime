package com.apps.adrcotfas.goodtime

import android.app.Application
import android.content.Context
import com.apps.adrcotfas.goodtime.bl.ALARM_MANAGER_HANDLER
import com.apps.adrcotfas.goodtime.bl.AlarmManagerHandler
import com.apps.adrcotfas.goodtime.bl.EventListener
import com.apps.adrcotfas.goodtime.bl.NotificationArchManager
import com.apps.adrcotfas.goodtime.bl.TIMER_SERVICE_HANDLER
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimerServiceStarter
import com.apps.adrcotfas.goodtime.di.getWith
import com.apps.adrcotfas.goodtime.di.insertKoin
import com.apps.adrcotfas.goodtime.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

class GoodtimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        insertKoin(
            module {
                single<Context> { this@GoodtimeApplication }
                viewModel { MainViewModel(get(), get()) }
                single<NotificationArchManager> {
                    NotificationArchManager(
                        get<Context>(),
                        MainActivity::class.java
                    )
                }
                single<EventListener>(named(EventListener.TIMER_SERVICE_HANDLER)) {
                    TimerServiceStarter(get())
                }
                single<EventListener>(named(EventListener.ALARM_MANAGER_HANDLER)) {
                    AlarmManagerHandler(
                        get<Context>(),
                        get<TimeProvider>(),
                        getWith(AlarmManagerHandler::class.simpleName)
                    )
                }
            }
        )
    }
}