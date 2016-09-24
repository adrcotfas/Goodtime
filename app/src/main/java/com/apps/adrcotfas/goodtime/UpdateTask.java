package com.apps.adrcotfas.goodtime;

import android.os.Handler;

import java.util.TimerTask;

public class UpdateTask extends TimerTask {
    final Handler handler;
    final MainActivity ref;

    public UpdateTask(Handler handler, MainActivity ref) {
        super();
        this.handler = handler;
        this.ref = ref;
    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ref.runTimer();
            }
        });
    }
}
