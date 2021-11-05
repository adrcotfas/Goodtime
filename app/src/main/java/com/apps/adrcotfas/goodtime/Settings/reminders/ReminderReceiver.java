package com.apps.adrcotfas.goodtime.Settings.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        ReminderHelper.notifyReminder(context);
        ReminderHelper.scheduleNotification(context);
    }
}
