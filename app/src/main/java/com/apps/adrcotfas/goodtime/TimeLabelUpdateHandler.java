package com.apps.adrcotfas.goodtime;

import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

class TimeLabelUpdateHandler extends Handler {

    private final static int UPDATE_RATE_MS = 1000;
    private final WeakReference<TimerActivity> activity;

    TimeLabelUpdateHandler(TimerActivity activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message message) {
        if (TimerActivity.MSG_UPDATE_TIME == message.what) {
            activity.get().updateTimerLabel();
            sendEmptyMessageDelayed(TimerActivity.MSG_UPDATE_TIME, UPDATE_RATE_MS);
        }
    }
}
