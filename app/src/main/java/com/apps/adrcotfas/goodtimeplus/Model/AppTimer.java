package com.apps.adrcotfas.goodtimeplus.Model;

import android.os.CountDownTimer;
import android.util.Log;

import com.apps.adrcotfas.goodtimeplus.Util.Constants;

public class AppTimer {

    private static String TAG = CountDownTimer.class.getSimpleName();

    private CountDownTimer mTimer;
    private CurrentSession mCurrentSession;
    private static long COUNTDOWN_INTERVAL = 1000; // 1 second
    private long mRemaining;

    public AppTimer(CurrentSession currentSession) {
        this.mCurrentSession = currentSession;
        mRemaining = mCurrentSession.getDuration().getValue();
    }

    public void start() {
        mTimer = new CountDownTimer(mRemaining, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.v(TAG, "is Ticking: " + millisUntilFinished + " millis remaining.");
                mCurrentSession.setDuration(millisUntilFinished);
                mRemaining = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                Log.v(TAG, "is finished.");
                mCurrentSession.setTimerState(TimerState.FINISHED);
                mRemaining = 0;
            }
        };
        mTimer.start();
        mCurrentSession.setTimerState(TimerState.ACTIVE);
    }

    public void stop() {
        mTimer.cancel();
        mCurrentSession = new CurrentSession(Constants.SESSION_TIME);
    }


    public void toggle() {
        if (mCurrentSession.getTimerState().getValue() == TimerState.ACTIVE) {
            mCurrentSession.setTimerState(TimerState.PAUSED);
            mTimer.cancel();
        } else if (mCurrentSession.getTimerState().getValue() == TimerState.PAUSED) {
            mCurrentSession.setTimerState(TimerState.ACTIVE);
            start();
        } else {
            Log.wtf(TAG, "Trying to toggle the timer but it's not in a valid state.");
        }
    }
}
