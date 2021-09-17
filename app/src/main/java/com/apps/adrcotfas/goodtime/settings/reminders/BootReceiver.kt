package com.apps.adrcotfas.goodtime.settings.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import java.lang.RuntimeException
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderHelper: ReminderHelper
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) return
        try {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Log.d(TAG, "onBootComplete")
                reminderHelper.scheduleNotifications()
            }
        } catch (e: RuntimeException) {
            Log.wtf(TAG, "Could not process intent")
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
