package com.apps.adrcotfas.goodtime.BL;

import android.os.CountDownTimer;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;

public class AppTimer {

    private static String TAG = CountDownTimer.class.getSimpleName();

    private CountDownTimer mTimer;
    private CurrentSession mCurrentSession;
    private long mRemaining;

    public AppTimer(CurrentSession currentSession) {
        this.mCurrentSession = currentSession;
        mRemaining = mCurrentSession.getDuration().getValue();
        mTimer = new AppCountDownTimer(mRemaining);
    }

    public void start() {

        mTimer = new AppCountDownTimer(mRemaining);
        mTimer.start();
        mCurrentSession.setTimerState(TimerState.ACTIVE);
    }

    public void stop() {
        mTimer.cancel();
        mCurrentSession.setTimerState(TimerState.INACTIVE);
        mCurrentSession.setDuration(Constants.SESSION_TIME);
        mCurrentSession.setSessionType(SessionType.WORK);
    }

    public void toggle() {

        switch(mCurrentSession.getTimerState().getValue()) {
            case INACTIVE:
            case PAUSED:
                mCurrentSession.setTimerState(TimerState.ACTIVE);
                start();
                break;
            case ACTIVE:
                mCurrentSession.setTimerState(TimerState.PAUSED);
                mTimer.cancel();
                break;
        }
    }

    private class AppCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         */
        public AppCountDownTimer(long millisInFuture) {
            super(millisInFuture, 1000);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.v(TAG, "is Ticking: " + millisUntilFinished + " millis remaining.");
            mCurrentSession.setDuration(millisUntilFinished);
            mRemaining = millisUntilFinished;
        }

        @Override
        public void onFinish() {
            Log.v(TAG, "is finished.");
            GoodtimeApplication.getInstance().getBus().send(new Constants.FinishEvent());
            mCurrentSession.setTimerState(TimerState.INACTIVE);
            mRemaining = 0;
        }
    }
}
