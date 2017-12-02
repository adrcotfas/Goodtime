package com.apps.adrcotfas.goodtimeplus.Model;

import android.os.CountDownTimer;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class AppTimer extends CountDownTimer {

    private static String TAG = CountDownTimer.class.getSimpleName();

    private CurrentSession mCurrentSession;
    private static long countDownInterval = 1000;

    public AppTimer(CurrentSession currentSession) {
        super(currentSession.getDuration().getValue(), countDownInterval);
        this.mCurrentSession = currentSession;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
        mCurrentSession.setDuration(seconds);
        Log.v(TAG, "is Ticking: " + seconds + " remaining.");
    }

    @Override
    public void onFinish() {
        //TODO: notify observers
    }
}
