package com.apps.adrcotfas.goodtime.BL;

import android.os.CountDownTimer;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;

import java.util.concurrent.TimeUnit;

public class CurrentSessionManager {

    private static String TAG = CountDownTimer.class.getSimpleName();

    private CountDownTimer mTimer;
    private CurrentSession mCurrentSession;
    private long mRemaining;

    public CurrentSessionManager(CurrentSession currentSession) {
        this.mCurrentSession = currentSession;
        mRemaining = mCurrentSession.getDuration().getValue();
        mTimer = new AppCountDownTimer(mRemaining);
    }

    public void startTimer() {

        mTimer = new AppCountDownTimer(mRemaining);
        mTimer.start();
        mCurrentSession.setTimerState(TimerState.ACTIVE);
    }

    public void stopTimer() {
        mTimer.cancel();
        mCurrentSession.setTimerState(TimerState.INACTIVE);
        mCurrentSession.setDuration(Constants.SESSION_TIME);
        mCurrentSession.setSessionType(SessionType.WORK);
    }

    public void toggleTimer() {

        switch(mCurrentSession.getTimerState().getValue()) {
            case INACTIVE:
            case PAUSED:
                mCurrentSession.setTimerState(TimerState.ACTIVE);
                startTimer();
                break;
            case ACTIVE:
                mCurrentSession.setTimerState(TimerState.PAUSED);
                mTimer.cancel();
                break;
        }
    }

    private class AppCountDownTimer extends CountDownTimer {

        private long mMinutesUntilFinished;

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         */
        public AppCountDownTimer(long millisInFuture) {
            super(millisInFuture, 1000);
            mMinutesUntilFinished = TimeUnit.MILLISECONDS.toMinutes(millisInFuture);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.v(TAG, "is Ticking: " + millisUntilFinished + " millis remaining.");
            mCurrentSession.setDuration(millisUntilFinished);
            mRemaining = millisUntilFinished;
            if (mMinutesUntilFinished > TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)) {
                mMinutesUntilFinished = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                GoodtimeApplication.getInstance().getBus().send(new Constants.UpdateTimerProgressEvent());
            }
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
