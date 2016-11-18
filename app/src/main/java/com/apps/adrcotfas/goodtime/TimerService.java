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
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;

import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.ON_AFTER_RELEASE;
import static android.os.PowerManager.PARTIAL_WAKE_LOCK;
import static com.apps.adrcotfas.goodtime.MainActivity.NOTIFICATION_TAG;
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
    private SessionType mCurrentSession;
    private PowerManager.WakeLock mWakeLock;

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
        releaseWakelock();
    }

    public void startSession(long delay, SessionType sessionType) {
        mRemainingTime = calculateSessionDurationFor(sessionType);
        Log.i(TAG, "Starting new timer for " + sessionType + ", duration " + mRemainingTime);

        acquirePartialWakelock();

        if (mPref.getDisableSoundAndVibration() && sessionType == WORK) {
            saveCurrentStateOfSound();
            disableSound();
        }
        if (mPref.getDisableWifi() && sessionType == WORK) {
            saveCurrentStateOfWifi();
            disableWifi();
        }

        mTimerState = ACTIVE;
        mCurrentSession = sessionType;

        createAndStartTimer(delay);
    }

    private int calculateSessionDurationFor(
            SessionType sessionType
    ) {
        switch (sessionType) {
            case WORK:
                return mPref.getSessionDuration() * 60;
            case BREAK:
                return mPref.getBreakDuration() * 60;
            case LONG_BREAK:
                return mPref.getLongBreakDuration() * 60;
            default:
                throw new IllegalStateException("This cannot happen");
        }
    }

    private void acquirePartialWakelock() {
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                PARTIAL_WAKE_LOCK | ON_AFTER_RELEASE | ACQUIRE_CAUSES_WAKEUP,
                "starting partial wake lock"
        );
        mWakeLock.acquire();
    }

    public void unpauseTimer(long delay) {
        mTimerState = ACTIVE;
        createAndStartTimer(delay);
    }

    public void stopSession() {
        Log.d(TAG, "Session stopped");

        sendToBackground();

        releaseWakelock();
        removeTimer();

        if (mPref.getDisableSoundAndVibration()) {
            restoreSound();
        }
        if (mPref.getDisableWifi()) {
            restoreWifi();
        }

        mTimerState = INACTIVE;
    }

    private void createAndStartTimer(long delay) {
        Log.d(TAG, "Starting new timer");

        sendUpdateIntent();

        mTimer = new Timer();
        mTimer.schedule(
                new UpdateTask(new Handler(), this),
                delay,
                1000
        );
    }

    public void countdown() {
        if (mTimerState != INACTIVE && mTimerState != PAUSED) {
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
        releaseWakelock();
        removeTimer();

        if (mPref.getDisableSoundAndVibration()) {
            restoreSound();
        }
        if (mPref.getDisableWifi()) {
            restoreWifi();
        }

        sendFinishedNotification();
        mTimerState = INACTIVE;
        sendToBackground();
    }


    private void releaseWakelock() {
        if (mWakeLock != null) {
            try {
                mWakeLock.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
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

    private void disableSound() {
        Log.d(TAG, "Disabling sound");
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        aManager.setRingerMode(RINGER_MODE_SILENT);
    }

    private void disableWifi() {
        Log.d(TAG, "Disabling Wifi");
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }

    private void restoreSound() {
        Log.d(TAG, "Restoring sound mode");
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        aManager.setRingerMode(mPreviousRingerMode);
    }

    private void restoreWifi() {
        Log.d(TAG, "Restoring Wifi mode");
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(mPreviousWifiMode);
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

    private void sendUpdateIntent() {
        Intent remainingTimeIntent = new Intent(ACTION_TIMERSERVICE);
        remainingTimeIntent.putExtra(REMAINING_TIME, mRemainingTime);
        mBroadcastManager.sendBroadcast(remainingTimeIntent);
    }

    public void pauseTimer() {
        mTimerState = PAUSED;
        removeTimer();
        releaseWakelock();
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

    public SessionType getSessionType() {
        return mCurrentSession;
    }

    public int getRemainingTime() {
        return mRemainingTime;
    }

    public int getCurrentSessionStreak() {
        return mCurrentSessionStreak;
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
