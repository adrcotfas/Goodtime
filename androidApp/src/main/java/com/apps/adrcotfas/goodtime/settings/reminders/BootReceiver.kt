package com.apps.adrcotfas.goodtime.settings.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.apps.adrcotfas.goodtime.di.injectLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.RuntimeException

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val reminderHelper: ReminderHelper by inject()
    private val logger by injectLogger(TAG)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) return
        try {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                logger.d("onBootComplete")
                reminderHelper.scheduleNotifications()
            }
        } catch (e: RuntimeException) {
            logger.e("Could not process intent")
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
