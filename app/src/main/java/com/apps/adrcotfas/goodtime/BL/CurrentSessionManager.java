package com.apps.adrcotfas.goodtime.BL;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Main.TimerActivity;
import com.apps.adrcotfas.goodtime.Util.Constants;

import java.util.concurrent.TimeUnit;
import de.greenrobot.event.EventBus;

import static com.apps.adrcotfas.goodtime.Util.Constants.ACTION.FINISHED;

/**
 * This class manages and modifies the mutable members of {@link CurrentSession}
 * The duration is updated using an {@link AppCountDownTimer}. Events coming from other layers will
 * trigger an update of the {@link CurrentSession}'s {@link TimerState} and {@link SessionType}.
 */
public class CurrentSessionManager extends ContextWrapper{

    private static String TAG = CurrentSessionManager.class.getSimpleName();
    public final static String SESSION_TYPE = "goodtime.session.type";

    private AppCountDownTimer mTimer;
    private CurrentSession mCurrentSession;
    private long mRemaining;
    private AlarmReceiver mAlarmReceiver;

    public CurrentSessionManager(Context context, CurrentSession currentSession) {
        super(context);
        mCurrentSession = currentSession;
        mAlarmReceiver = new AlarmReceiver();
    }

    public void startTimer(SessionType sessionType) {
        // TODO: set the duration according to the settings. Also include long break

        // TODO modify to minutes
        long duration = TimeUnit.SECONDS.toMillis(PreferenceHelper.getSessionDuration(sessionType));

        mCurrentSession.setTimerState(TimerState.ACTIVE);
        mCurrentSession.setSessionType(sessionType);
        mCurrentSession.setDuration(duration);

        scheduleAlarm(sessionType, duration);
        mTimer = new AppCountDownTimer(duration);
        mTimer.start();
    }

    public void toggleTimer() {
        switch(mCurrentSession.getTimerState().getValue()) {
            case PAUSED:
                Log.v(TAG, "toggleTimer PAUSED");
                scheduleAlarm(mCurrentSession.getSessionType().getValue(), mRemaining);
                mTimer.start();
                mCurrentSession.setTimerState(TimerState.ACTIVE);
                break;
            case ACTIVE:
                Log.v(TAG, "toggleTimer UNPAUSED");
                cancelAlarm();
                mTimer.cancel();
                mTimer = new AppCountDownTimer(mRemaining);
                mCurrentSession.setTimerState(TimerState.PAUSED);
                break;
            default:
                Log.wtf(TAG, "The timer is in an invalid state.");
                break;
        }
    }

    public void stopTimer() {
        cancelAlarm();
        mTimer.cancel();
        mCurrentSession.setTimerState(TimerState.INACTIVE);
        long workDuration = TimeUnit.MINUTES.toMillis(PreferenceHelper.getSessionDuration(SessionType.WORK));
        mCurrentSession.setDuration(workDuration);
    }

    private void scheduleAlarm(SessionType sessionType, long duration) {
        this.registerReceiver(mAlarmReceiver, new IntentFilter(FINISHED));
        final long triggerAtMillis = duration + SystemClock.elapsedRealtime();

        Log.v(TAG, "scheduleAlarm " + sessionType.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getAlarmManager().setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis, getAlarmPendingIntent(sessionType));

            //TODO: find a more elegant solution for the second alarm
            //which just brings the main activity on top in a smooth way.
            //Starting it from the service lags

            getAlarmManager().setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis, getAlarmActivityIntent());

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getAlarmManager().setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis, getAlarmPendingIntent(sessionType));

            getAlarmManager().setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis, getAlarmActivityIntent());
        } else {
            getAlarmManager().set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis, getAlarmPendingIntent(sessionType));

            getAlarmManager().set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis, getAlarmActivityIntent());
        }
    }

    private PendingIntent getAlarmActivityIntent() {
        Intent activityIntent = new Intent(this, TimerActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, 0,
                activityIntent, 0);
    }

    private void cancelAlarm() {
        getAlarmManager().cancel(getAlarmPendingIntent(mCurrentSession.getSessionType().getValue()));
        getAlarmManager().cancel(getAlarmActivityIntent());
        unregisterAlarmReceiver();
    }

    public void unregisterAlarmReceiver() {
        Log.v(TAG, "unregisterAlarmReceiver");
        try {
            this.unregisterReceiver(mAlarmReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "AlarmReceiver is already unregistered.");
        }
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getAlarmPendingIntent(SessionType sessionType) {
        Intent intent = new Intent(FINISHED);
        intent.putExtra(SESSION_TYPE, sessionType.toString());
        return PendingIntent.getBroadcast(getApplicationContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSession;
    }

    private class AppCountDownTimer extends CountDownTimer {

        private String TAG = AppCountDownTimer.class.getSimpleName();
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

            // update notification only if the screen is on
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH && powerManager.isInteractive()) ||
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH && powerManager.isScreenOn())) {
                EventBus.getDefault().post(new Constants.UpdateTimerProgressEvent());
            }
        }

        @Override
        public void onFinish() {
            Log.v(TAG, "is finished.");
            mRemaining = 0;
        }
    }
}
