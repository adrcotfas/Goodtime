package com.apps.adrcotfas.goodtime;

import android.app.Notification;
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

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static com.apps.adrcotfas.goodtime.Preferences.PREFERENCES_NAME;

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

        mTimer = new Timer();
        mTimer.schedule(new UpdateTask(new Handler(), TimerService.this), delay , 1000);
    }

    public void runTimer() {
        if (mTimerState != TimerState.INACTIVE) {
            if (mRemainingTime != 0) {
                --mRemainingTime;
            }
            Intent remainingTimeIntent = new Intent(ACTION_TIMERSERVICE);
            remainingTimeIntent.putExtra(REMAINING_TIME, getRemainingTime());
            mBroadcastManager.sendBroadcast(remainingTimeIntent);

            if (mIsOnForeground && mTimerBroughtToForegroundState != mTimerState) {
                bringToForegroundAndUpdateNotification();
            }
        }
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

    public int getPreviousRingerMode() {
        return mPreviousRingerMode;
    }

    public boolean getPreviousWifiMode() {
        return mPreviousWifiMode;
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

    protected void setPreviousRingerMode(int previousRingerMode) {
        this.mPreviousRingerMode = previousRingerMode;
    }

    protected void setPreviousWifiMode(boolean previousWifiMode) {
        this.mPreviousWifiMode = previousWifiMode;
    }

    protected void restoreSound() {
        Log.d(TAG, "Restoring sound mode");
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        aManager.setRingerMode(mPreviousRingerMode);
    }

    protected void restoreWifi() {
        Log.d(TAG, "Restoring Wifi mode");
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(mPreviousWifiMode);
    }

    protected void disableSound() {
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        aManager.setRingerMode(RINGER_MODE_SILENT);
    }

    protected void disableWifi() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }

    protected void bringToForegroundAndUpdateNotification() {
        mIsOnForeground = true;
        mTimerBroughtToForegroundState = mTimerState;
        startForeground(GOODTIME_NOTIFICATION_ID, createNotification());
    }

    protected void sendToBackground() {
        mIsOnForeground = false;
        stopForeground(true);
    }

    private void saveCurrentStateOfSound() {
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mPreviousRingerMode = aManager.getRingerMode();
    }
    private void saveCurrentStateOfWifi() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        mPreviousWifiMode = wifiManager.isWifiEnabled();
    }

    private Notification createNotification() {

        CharSequence contextText = getString(R.string.notification_session);
        boolean ongoing = true;
        switch (mTimerState) {
            case ACTIVE_WORK:
                break;
            case ACTIVE_BREAK:
                contextText = getString(R.string.notification_break);
                break;
            case PAUSED_WORK:
                contextText = getString(R.string.notification_pause);
                ongoing = false;
                break;
            case FINISHED_WORK:
                contextText = getString(R.string.notification_work_complete);
                ongoing = false;
                break;
            case FINISHED_BREAK:
                contextText = getString(R.string.notification_break_complete);
                ongoing = false;
                break;
            }

        return new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setAutoCancel(false)
                .setContentTitle("Goodtime")
                .setContentText(contextText)
                .setOngoing(ongoing)
                .setShowWhen(false)
                .setContentIntent(
                        getActivity(
                                this,
                                0,
                                new Intent(getApplicationContext(), MainActivity.class),
                                FLAG_UPDATE_CURRENT))
                .build();
    }
}
