package com.apps.adrcotfas.goodtime.settings.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.apps.adrcotfas.goodtime.bl.notifications.NotificationArchManager
import com.apps.adrcotfas.goodtime.di.injectLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val notificationManager : NotificationArchManager by inject()
    private val logger by injectLogger(TAG)

    override fun onReceive(context: Context, intent: Intent) {
        logger.d("onReceive")
        notificationManager.notifyReminder()
    }

    companion object {
        private const val TAG = "ReminderReceiver"
    }
}