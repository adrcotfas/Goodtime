package com.apps.adrcotfas.goodtime;

import android.os.Handler;

import java.util.TimerTask;

public class UpdateTask extends TimerTask {

    private final Handler handler;
    private final TimerService ref;

    public UpdateTask(Handler handler, TimerService ref) {
        super();
        this.handler = handler;
        this.ref = ref;
    }

    @Override
    public void run() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ref.countdown();
            }
        });
    }
}
