package com.apps.adrcotfas.goodtime;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;

import static com.apps.adrcotfas.goodtime.MainActivity.NOTIFICATION_TAG;
import static com.apps.adrcotfas.goodtime.Notifications.createFinishedNotification;
import static com.apps.adrcotfas.goodtime.Notifications.createForegroundNotification;
import static com.apps.adrcotfas.goodtime.Preferences.PREFERENCES_NAME;
import static com.apps.adrcotfas.goodtime.TimerState.INACTIVE;

public class TimerService extends Service {

    private static final int GOODTIME_NOTIFICATION_ID = 1;
    private static final String TAG = "TimerService";
    public final static String ACTION_TIMERSERVICE = "com.apps.adrcotfas.goodtime.TIMERSERVICE";
    public final static String REMAINING_TIME = "com.apps.adrcotfas.goodtime.REMAINING_TIME";
    private int mRemainingTime;
    private int mCurrentSessionStreak;
    private Timer mTimer;
    private TimerState mTimerState;
    private TimerState mTimerBroughtToForegroundState;
    private final IBinder mBinder = new TimerBinder();
    private LocalBroadcastManager mBroadcastManager;
    private int mPreviousRingerMode;
    private boolean mPreviousWifiMode;
    private boolean mIsOnForeground;
    private Preferences mPref;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
        mPref = new Preferences(preferences);

        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public void scheduleTimer(long delay){
        saveCurrentStateOfSound();
        saveCurrentStateOfWifi();

        createAndStartTimer(delay);
    }

    private void saveCurrentStateOfSound() {
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mPreviousRingerMode = aManager.getRingerMode();
    }

    private void saveCurrentStateOfWifi() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        mPreviousWifiMode = wifiManager.isWifiEnabled();
    }

    private void createAndStartTimer(long delay) {
        Log.d(TAG, "Starting new timer");

        mTimer = new Timer();
        mTimer.schedule(
                new UpdateTask(new Handler(), this),
                delay,
                1000
        );
    }

    public void countdown() {
        if (mTimerState != INACTIVE) {
            if (mRemainingTime > 0) {
                mRemainingTime--;
            }

            if (mRemainingTime == 0) {
                onCountdownFinished();
            }

            sendUpdateIntent();

            if (mIsOnForeground && mTimerBroughtToForegroundState != mTimerState) {
                bringToForegroundAndUpdateNotification();
            }
        }
    }

    private void onCountdownFinished() {
        Log.d(TAG, "Countdown finished");

        restoreSoundIfPreferred();
        restoreWifiIfPreferred();
        sendFinishedNotification();
    }

    private void restoreSoundIfPreferred() {
        if (mPref.getDisableSoundAndVibration()) {
            Log.d(TAG, "Restoring sound mode");
            AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            aManager.setRingerMode(mPreviousRingerMode);
        }
    }

    private void restoreWifiIfPreferred() {
        if (mPref.getDisableWifi()) {
            Log.d(TAG, "Restoring Wifi mode");
            WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
            wifiManager.setWifiEnabled(mPreviousWifiMode);
        }
    }

    private void sendFinishedNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(
                NOTIFICATION_TAG,
                createFinishedNotification(this, mTimerState, mPref.getNotificationSound())
        );
    }


    private void sendUpdateIntent() {
        Intent remainingTimeIntent = new Intent(ACTION_TIMERSERVICE);
        remainingTimeIntent.putExtra(REMAINING_TIME, mRemainingTime);
        mBroadcastManager.sendBroadcast(remainingTimeIntent);
    }


    public void removeTimer() {
        if (mIsOnForeground) {
            bringToForegroundAndUpdateNotification();
        }

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

    protected void setTimerState(TimerState mTimerState) {
        this.mTimerState = mTimerState;
    }

    protected void setCurrentSessionStreak(int mCurrentSessionStreak) {
        this.mCurrentSessionStreak = mCurrentSessionStreak;
    }

    protected void setRemainingTime(int mRemainingTime) {
        this.mRemainingTime = mRemainingTime;
    }

    protected void bringToForegroundAndUpdateNotification() {
        mIsOnForeground = true;
        mTimerBroughtToForegroundState = mTimerState;
        startForeground(
                GOODTIME_NOTIFICATION_ID,
                createForegroundNotification(this, mTimerState)
        );
    }

    protected void sendToBackground() {
        mIsOnForeground = false;
        stopForeground(true);
    }

}
