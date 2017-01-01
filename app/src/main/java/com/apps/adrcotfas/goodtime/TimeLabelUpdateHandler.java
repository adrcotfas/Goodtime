package com.apps.adrcotfas.goodtime;

import android.os.Handler;
import android.os.Message;

class TimeLabelUpdateHandler extends Handler {

    private final static int UPDATE_RATE_MS = 1000;
    private final TimerActivity activity;

    TimeLabelUpdateHandler(TimerActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {
        if (TimerActivity.MSG_UPDATE_TIME == message.what) {
            if (activity != null){
                activity.updateTimeLabel();
            }
            sendEmptyMessageDelayed(TimerActivity.MSG_UPDATE_TIME, UPDATE_RATE_MS);
        }
    }
}
