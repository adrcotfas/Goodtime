package com.apps.adrcotfas.goodtime.settings.reminders

import com.apps.adrcotfas.goodtime.settings.reminders.ReminderHelper.Companion.notifyReminder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        notifyReminder(context)
    }

    companion object {
        private const val TAG = "ReminderReceiver"
    }
}