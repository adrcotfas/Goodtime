package com.apps.adrcotfas.goodtime

import android.app.Application
import android.content.Context
import com.apps.adrcotfas.goodtime.bl.ALARM_MANAGER_HANDLER
import com.apps.adrcotfas.goodtime.bl.AlarmManagerHandler
import com.apps.adrcotfas.goodtime.bl.EventListener
import com.apps.adrcotfas.goodtime.bl.notifications.NotificationArchManager
import com.apps.adrcotfas.goodtime.bl.SOUND_AND_VIBRATION_PLAYER
import com.apps.adrcotfas.goodtime.bl.notifications.SoundVibrationAndTorchPlayer
import com.apps.adrcotfas.goodtime.bl.notifications.SoundPlayer
import com.apps.adrcotfas.goodtime.bl.TIMER_SERVICE_HANDLER
import com.apps.adrcotfas.goodtime.bl.TimeProvider
import com.apps.adrcotfas.goodtime.bl.TimerServiceStarter
import com.apps.adrcotfas.goodtime.bl.notifications.TorchManager
import com.apps.adrcotfas.goodtime.bl.notifications.VibrationPlayer
import com.apps.adrcotfas.goodtime.di.getWith
import com.apps.adrcotfas.goodtime.di.insertKoin
import com.apps.adrcotfas.goodtime.settings.SoundsViewModel
import com.apps.adrcotfas.goodtime.settings.reminders.ReminderHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

class GoodtimeApplication : Application() {
    private val applicationScope = MainScope()

    override fun onCreate() {
        super.onCreate()
        insertKoin(
            module {
                single<Context> { this@GoodtimeApplication }
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
                single<ReminderHelper> {
                    ReminderHelper(
                        get(),
                        get(),
                        getWith(ReminderHelper::class.simpleName)
                    )
                }
                viewModel<SoundsViewModel> {
                    SoundsViewModel(
                        settingsRepository = get(),
                        logger = getWith(SoundsViewModel::class.simpleName)
                    )
                }
                single {
                    SoundPlayer(
                        context = get(),
                        settingsRepo = get(),
                        logger = getWith(SoundPlayer::class.simpleName)
                    )
                }
                single {
                    VibrationPlayer(
                        context = get(),
                        settingsRepo = get()
                    )
                }
                single {
                    TorchManager(
                        context = get(),
                        settingsRepo = get(),
                        logger = getWith(TorchManager::class.simpleName)
                    )
                }
                single<EventListener>(named(EventListener.SOUND_AND_VIBRATION_PLAYER)) {
                    SoundVibrationAndTorchPlayer(
                        soundPlayer = get(),
                        vibrationPlayer = get(),
                        torchManager = get()
                    )
                }
            }
        )
        val reminderHelper = get<ReminderHelper>()
        applicationScope.launch {
            reminderHelper.init()
        }
    }
}