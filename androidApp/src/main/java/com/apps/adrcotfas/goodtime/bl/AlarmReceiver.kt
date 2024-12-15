package com.apps.adrcotfas.goodtime.bl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val timerManager: TimerManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.ON_AFTER_RELEASE
                    or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            "Goodtime:AlarmReceiver"
        ).apply {
            acquire(10.seconds.inWholeMilliseconds)
        }
        timerManager.finish()
    }
}