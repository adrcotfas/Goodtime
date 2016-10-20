package com.apps.adrcotfas.goodtime;

import android.app.Service;
import android.content.Intent;
import java.util.Timer;

import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class TimerService extends Service {

    private static final String TAG = "TimerService";
    private int mRemainingTime;
    private int mCurrentSessionStreak;

    private Timer mTimer;
    private TimerState mTimerState;

    public final static String TIMERSERVICE_ACTION = "TimerService";
    public final static String REMAINING_TIME = "remainingTime";
    public final static String SESSION_FINISHED = "sessionFinished";
    private final IBinder mBinder = new TimerBinder();
    private final Handler mHandler = new Handler();
    private LocalBroadcastManager mBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.removeCallbacks(sendUpdatesToUI);
        mHandler.postDelayed(sendUpdatesToUI, 1000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(sendUpdatesToUI);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this, 1000);
        }
    };

    public void scheduleTimer(long delay){

        mTimer = new Timer();
        mTimer.schedule(new UpdateTask(new Handler(), TimerService.this), delay , 1000);
    }

    public void runTimer() {
        Log.d(TAG, "Updating timer");

        if (mTimerState != TimerState.INACTIVE) {
            Intent remainingTimeIntent = new Intent(TIMERSERVICE_ACTION);
            remainingTimeIntent.putExtra(REMAINING_TIME, getRemainingTime());

            mBroadcastManager.sendBroadcast(remainingTimeIntent);

            if (mRemainingTime == 0) {
                Intent countdownFinishedIntent = new Intent(TIMERSERVICE_ACTION);
                countdownFinishedIntent.putExtra(SESSION_FINISHED, true);
                mBroadcastManager.sendBroadcast(countdownFinishedIntent);
            } else {
                --mRemainingTime;
            }
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

    @Nullable
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
