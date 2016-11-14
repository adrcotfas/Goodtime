package com.apps.adrcotfas.goodtime;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.concurrent.TimeUnit;

import static com.apps.adrcotfas.goodtime.TimerActivity.NOTIFICATION_TAG;
import static com.apps.adrcotfas.goodtime.Notifications.createCompletionNotification;
import static com.apps.adrcotfas.goodtime.Notifications.createForegroundNotification;
import static com.apps.adrcotfas.goodtime.Preferences.PREFERENCES_NAME;
import static com.apps.adrcotfas.goodtime.SessionType.WORK;
import static com.apps.adrcotfas.goodtime.TimerState.ACTIVE;
import static com.apps.adrcotfas.goodtime.TimerState.INACTIVE;
import static com.apps.adrcotfas.goodtime.TimerState.PAUSED;

public class TimerService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "TimerService";
    public final static String ACTION_TIMERSERVICE = "com.apps.adrcotfas.goodtime.TIMERSERVICE";
    public final static String REMAINING_TIME = "com.apps.adrcotfas.goodtime.REMAINING_TIME";

    private long mCountDownFinishedTime;
    private int mCurrentSessionStreak;
    private TimerState mTimerState;
    private TimerState mTimerBroughtToForegroundState;
    private final IBinder mBinder = new TimerBinder();
    private LocalBroadcastManager mBroadcastManager;
    private int mPreviousRingerMode;
    private boolean mPreviousWifiMode;
    private boolean mIsOnForeground;
    private boolean mIsTimerRunning;
    private Preferences mPref;
    private SessionType mCurrentSession;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
        mPref = new Preferences(preferences);
        mTimerState = INACTIVE;
        mCurrentSession = WORK;

        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startSession(SessionType sessionType) {
        mIsTimerRunning = true;
        mCountDownFinishedTime = calculateSessionDurationFor(sessionType);
        Log.i(TAG, "Starting new timer for " + sessionType + ", ending at " + mCountDownFinishedTime);

        saveCurrentStateOfSound();
        saveCurrentStateOfWifi();

        mTimerState = ACTIVE;
        mCurrentSession = sessionType;
    }

    private long calculateSessionDurationFor(SessionType sessionType) {
        long currentTime = System.currentTimeMillis();
        switch (sessionType) {
            case WORK:
                return currentTime + TimeUnit.MINUTES.toMillis(mPref.getSessionDuration());
            case BREAK:
                return currentTime + TimeUnit.MINUTES.toMillis(mPref.getBreakDuration());
            case LONG_BREAK:
                return currentTime + TimeUnit.MINUTES.toMillis(mPref.getLongBreakDuration());
            default:
                throw new IllegalStateException("This cannot happen");
        }
    }

    private void saveCurrentStateOfSound() {
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mPreviousRingerMode = aManager.getRingerMode();
    }

    private void saveCurrentStateOfWifi() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        mPreviousWifiMode = wifiManager.isWifiEnabled();
    }

    public void unpauseTimer(long delay) {
        mTimerState = ACTIVE;
    }

    public void stopSession() {
        mIsTimerRunning = false;
        Log.d(TAG, "Session stopped");

        sendToBackground();

        restoreSoundIfPreferred();
        restoreWifiIfPreferred();

        mTimerState = INACTIVE;
    }

    private void onCountdownFinished() {
        Log.d(TAG, "Countdown finished");

        restoreSoundIfPreferred();
        restoreWifiIfPreferred();
        sendFinishedNotification();

        mTimerState = INACTIVE;

        sendToBackground();
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
                createCompletionNotification(
                        this,
                        mCurrentSession,
                        mPref.getNotificationSound(),
                        mPref.getNotificationVibrate()
                )
        );
    }

    public void pauseTimer() {
        mTimerState = PAUSED;
    }

    public boolean isTimerRunning() {
        return mIsTimerRunning;
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

    public SessionType getSessionType() {
        return mCurrentSession;
    }

    public int getCurrentSessionStreak() {
        return mCurrentSessionStreak;
    }

    public int getRemainingTime() {
        return (int) (TimeUnit.MILLISECONDS.toSeconds(
                mCountDownFinishedTime - System.currentTimeMillis()));
    }

    public void increaseCurrentSessionStreak() {
        mCurrentSessionStreak++;
    }

    public void resetCurrentSessionStreak() {
        mCurrentSessionStreak = 0;
    }

    protected void bringToForegroundAndUpdateNotification() {
        mIsOnForeground = true;
        mTimerBroughtToForegroundState = mTimerState;
        startForeground(
                NOTIFICATION_ID,
                createForegroundNotification(this, mCurrentSession, mTimerState)
        );
    }

    protected void sendToBackground() {
        mIsOnForeground = false;
        stopForeground(true);
    }

}
