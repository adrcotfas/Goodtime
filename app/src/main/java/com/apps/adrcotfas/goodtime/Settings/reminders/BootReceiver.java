package com.apps.adrcotfas.goodtime.Settings.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;


public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        if (intent.getAction() == null) return;

        try {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                Log.d(TAG, "onBootComplete");
                GoodtimeApplication.getInstance().getReminderHelper().scheduleNotification();
            }
        }
        catch (RuntimeException e) {
            Log.wtf(TAG, "Could not process intent");
        }
    }
}
