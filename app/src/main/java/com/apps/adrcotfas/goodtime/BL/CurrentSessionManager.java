package com.apps.adrcotfas.goodtime.BL;

import android.os.CountDownTimer;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;

import java.util.concurrent.TimeUnit;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import de.greenrobot.event.EventBus;

/**
 * This class manages and modifies the mutable members of {@link CurrentSession}
 * The duration is updated using an {@link AppCountDownTimer}. Events coming from other layers will
 * trigger an update of the {@link CurrentSession}'s {@link TimerState} and {@link SessionType}.
 */
public class CurrentSessionManager {

    private static String TAG = CountDownTimer.class.getSimpleName();

    private AppCountDownTimer mTimer;
    private CurrentSession mCurrentSession;
    private long mRemaining;

    public CurrentSessionManager(CurrentSession currentSession) {
        this.mCurrentSession = currentSession;
        mRemaining = mCurrentSession.getDuration().getValue();
        mTimer = new AppCountDownTimer(mRemaining);
    }

    public void startTimer(SessionType sessionType) {
        // TODO: set the duration according to the settings. Also include long break

        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(FinishSessionWorker.class)
                        .setInitialDelay(PreferenceHelper.getSessionDuration(sessionType), TimeUnit.SECONDS)
                        .addTag(FinishSessionWorker.WORK_TAG)
                        .build();
        WorkManager.getInstance().enqueue(workRequest);

        mCurrentSession.setTimerState(TimerState.ACTIVE);
        mCurrentSession.setSessionType(sessionType);

        // TODO modify to minutes
        long duration = TimeUnit.SECONDS.toMillis(PreferenceHelper.getSessionDuration(sessionType));
        mCurrentSession.setDuration(duration);
        mTimer = new AppCountDownTimer(duration);
        mTimer.start();
    }

    public void toggleTimer() {
        switch(mCurrentSession.getTimerState().getValue()) {
            case PAUSED:
                mTimer.start();
                OneTimeWorkRequest workRequest =
                        new OneTimeWorkRequest.Builder(FinishSessionWorker.class)
                                .setInitialDelay(mRemaining, TimeUnit.MILLISECONDS)
                                .build();
                WorkManager.getInstance().enqueue(workRequest);

                mCurrentSession.setTimerState(TimerState.ACTIVE);
                break;
            case ACTIVE:
                mTimer.cancel();
                WorkManager.getInstance().cancelAllWork();

                mTimer = new AppCountDownTimer(mRemaining);
                mCurrentSession.setTimerState(TimerState.PAUSED);
                break;
            default:
                Log.wtf(TAG, "The timer is in an invalid state.");
                break;
        }
    }

    public void stopTimer() {
        mTimer.cancel();
        mCurrentSession.setTimerState(TimerState.INACTIVE);
        long workDuration = TimeUnit.MINUTES.toMillis(PreferenceHelper.getSessionDuration(SessionType.WORK));
        mCurrentSession.setDuration(workDuration);
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSession;
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
                EventBus.getDefault().post(new Constants.UpdateTimerProgressEvent());
            }
        }

        @Override
        public void onFinish() {
            Log.v(TAG, "is finished.");
            long workDuration = TimeUnit.MINUTES.toMillis(PreferenceHelper.getSessionDuration(SessionType.WORK));
            mCurrentSession.setDuration(workDuration);
            mCurrentSession.setTimerState(TimerState.INACTIVE);
            mRemaining = 0;
        }
    }
}
