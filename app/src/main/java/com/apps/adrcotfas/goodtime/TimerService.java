package com.apps.adrcotfas.goodtime;

import android.app.Service;
import android.content.Intent;
import java.util.Timer;

import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class TimerService extends Service {

    private static final String TAG = "TimerService";
    public final static String ACTION_TIMERSERVICE = "com.apps.adrcotfas.goodtime.TIMERSERVICE";
    public final static String REMAINING_TIME = "com.apps.adrcotfas.goodtime.REMAINING_TIME";
    private int mRemainingTime;
    private int mCurrentSessionStreak;
    private Timer mTimer;
    private TimerState mTimerState;
    private final IBinder mBinder = new TimerBinder();
    private LocalBroadcastManager mBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    public void scheduleTimer(long delay){

        mTimer = new Timer();
        mTimer.schedule(new UpdateTask(new Handler(), TimerService.this), delay , 1000);
    }

    public void runTimer() {
        Log.d(TAG, "Updating timer");

        if (mTimerState != TimerState.INACTIVE) {
            if (mRemainingTime != 0) {
                --mRemainingTime;
            }
            Intent remainingTimeIntent = new Intent(ACTION_TIMERSERVICE);
            remainingTimeIntent.putExtra(REMAINING_TIME, getRemainingTime());
            mBroadcastManager.sendBroadcast(remainingTimeIntent);
        }
    }

    public void removeTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    public class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public TimerState getTimerState() {
        return mTimerState;
    }

    public int getRemainingTime() {
        return mRemainingTime;
    }

    public int getCurrentSessionStreak() {
        return mCurrentSessionStreak;
    }

    public void setTimerState(TimerState mTimerState) {
        this.mTimerState = mTimerState;
    }

    public void setCurrentSessionStreak(int mCurrentSessionStreak) {
        this.mCurrentSessionStreak = mCurrentSessionStreak;
    }

    public void setRemainingTime(int mRemainingTime) {
        this.mRemainingTime = mRemainingTime;
    }
}
