package com.apps.adrcotfas.goodtime.BL;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static android.media.AudioManager.RINGER_MODE_SILENT;
import static com.apps.adrcotfas.goodtime.BL.NotificationHelper.GOODTIME_NOTIFICATION_ID;
import static com.apps.adrcotfas.goodtime.Util.Constants.SESSION_TYPE;

/**
 * Class representing the foreground service which triggers the countdown timer and handles events.
 */
public class TimerService extends Service {

    private static final String TAG = TimerService.class.getSimpleName();

    private NotificationHelper mNotificationHelper;
    private RingtoneAndVibrationPlayer mRingtoneAndVibrationPlayer;

    private int mPreviousRingerMode;
    private boolean mPreviousWifiMode;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate " + this.hashCode());
        mNotificationHelper = new NotificationHelper(getApplicationContext());
        mRingtoneAndVibrationPlayer = new RingtoneAndVibrationPlayer(getApplicationContext());
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy " + this.hashCode());
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public synchronized int onStartCommand(final Intent intent, int flags, int startId) {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        int result = START_STICKY;
        Log.d(TAG, "onStartCommand " + this.hashCode() + " " + intent.getAction());
        switch (intent.getAction()) {
            case Constants.ACTION.STOP:
                onStopEvent();
                break;
            case Constants.ACTION.TOGGLE:
                onToggleEvent();
                result = START_NOT_STICKY;
                break;
            case Constants.ACTION.START:
                SessionType sessionType = SessionType.valueOf(intent.getStringExtra(SESSION_TYPE));
                onStartEvent(sessionType);
                break;
            case Constants.ACTION.ADD_SECONDS:
                onAdd60Seconds();
                break;
            case Constants.ACTION.SKIP:
                onSkipEvent();
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Called when an event is posted to the EventBus
     * @param o holds the type of the Event
     */
    public void onEvent(Object o) {
        if (o instanceof Constants.FinishWorkEvent) {
            Log.d(TAG, "onEvent " + o.getClass().getSimpleName());
            onFinishEvent(SessionType.WORK);
        } else if (o instanceof Constants.FinishBreakEvent) {
            Log.d(TAG, "onEvent " + o.getClass().getSimpleName());
            onFinishEvent(SessionType.BREAK);
        } else if (o instanceof  Constants.FinishLongBreakEvent) {
            onFinishEvent(SessionType.LONG_BREAK);
        } else if (o instanceof Constants.UpdateTimerProgressEvent) {
            updateNotificationProgress();
        } else if (o instanceof Constants.ClearNotificationEvent) {
            Log.d(TAG, "onEvent " + o.getClass().getSimpleName());
            mNotificationHelper.clearNotification();
            mRingtoneAndVibrationPlayer.stop();
        }
    }

    private void onStartEvent(SessionType sessionType) {

        EventBus.getDefault().post(new Constants.ClearFinishDialogEvent());
        if (sessionType != SessionType.WORK && PreferenceHelper.isLongBreakEnabled()
                && PreferenceHelper.itsTimeForLongBreak()) {
            sessionType = SessionType.LONG_BREAK;
        }

        Log.d(TAG, "onStartEvent: " + sessionType.toString());

        getSessionManager().startTimer(sessionType);

        if (sessionType == SessionType.WORK) {
            if (PreferenceHelper.isWiFiDisabled()) {
                toggleWifi(false);
            }
            if (PreferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(false);
            }
        }

        mNotificationHelper.clearNotification();
        startForeground(GOODTIME_NOTIFICATION_ID, mNotificationHelper.getInProgressBuilder(
                getSessionManager().getCurrentSession()).build());
    }

    //TODO: an improvement would be to stop the service when the timer is paused
    private void onToggleEvent() {
        getSessionManager().toggleTimer();
        startForeground(GOODTIME_NOTIFICATION_ID, mNotificationHelper.getInProgressBuilder(
                getSessionManager().getCurrentSession()).build());
    }

    private void onStopEvent() {
        getSessionManager().stopTimer();
        Log.d(TAG, "onStopEvent");

        if (PreferenceHelper.isWiFiDisabled()) {
            toggleWifi(true);
        }
        if (PreferenceHelper.isSoundAndVibrationDisabled()) {
            toggleSound(true);
        }

        if (getSessionManager().getCurrentSession().getSessionType().getValue() == SessionType.LONG_BREAK) {
            PreferenceHelper.resetCurrentStreak();
        }

        PreferenceHelper.toggleAdded60SecondsState(false);

        stopForeground(true);
        stopSelf();
        // TODO: store what was done of the session to ROOM
    }

    private void onFinishEvent(SessionType sessionType) {
        Log.d(TAG, TimerService.this.hashCode() + " onFinishEvent " + sessionType.toString());

        acquireScreenLock();
        if (sessionType == SessionType.WORK) {
            if (PreferenceHelper.isWiFiDisabled()) {
                toggleWifi(true);
            }
            if (PreferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(true);
            }
        }

        // TODO: store session statistics

        mRingtoneAndVibrationPlayer.play(sessionType);
        stopForeground(true);

        updateLongBreakStreak(sessionType);
        PreferenceHelper.toggleAdded60SecondsState(false);

        if (PreferenceHelper.isContinuousModeEnabled()) {
            onStartEvent(sessionType == SessionType.WORK ? SessionType.BREAK : SessionType.WORK);
        } else {
            mNotificationHelper.notifyFinished(sessionType);
        }
    }

    private void onAdd60Seconds() {
        Log.d(TAG, TimerService.this.hashCode() + " onAdd60Seconds ");
        if (getSessionManager().getCurrentSession().getTimerState().getValue() == TimerState.INACTIVE) {
            startForeground(GOODTIME_NOTIFICATION_ID, mNotificationHelper.getInProgressBuilder(
                    getSessionManager().getCurrentSession()).build());
            PreferenceHelper.toggleAdded60SecondsState(true);
        }
        getSessionManager().add60Seconds();
    }

    private void onSkipEvent() {
        final SessionType sessionType = getSessionManager().getCurrentSession().getSessionType().getValue();
        Log.d(TAG, TimerService.this.hashCode() + " onSkipEvent " + sessionType.toString());

        getSessionManager().stopTimer();
        // TODO: store what was done of the session to ROOM
        stopForeground(true);
        updateLongBreakStreak(sessionType);
        onStartEvent(sessionType == SessionType.WORK ? SessionType.BREAK : SessionType.WORK);
    }

    private void updateLongBreakStreak(SessionType sessionType) {

        final boolean isIn60AddedSecondsState = PreferenceHelper.isInAdded60SecondsState();
        if (PreferenceHelper.isLongBreakEnabled()) {
            if (sessionType == SessionType.LONG_BREAK) {
                PreferenceHelper.resetCurrentStreak();
            } else if (sessionType == SessionType.WORK && !isIn60AddedSecondsState){
                PreferenceHelper.incrementCurrentStreak();
            }
            Log.d(TAG, "PreferenceHelper.isInAdded60SecondsState: " + isIn60AddedSecondsState);
            Log.d(TAG, "PreferenceHelper.getCurrentStreak: " + PreferenceHelper.getCurrentStreak());
            Log.d(TAG, "PreferenceHelper.lastWorkFinishedAt: " + PreferenceHelper.lastWorkFinishedAt());
        }
    }

    CurrentSessionManager getSessionManager() {
        return GoodtimeApplication.getInstance().getCurrentSessionManager();
    }

    private void acquireScreenLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                AlarmReceiver.class.getName());
        wakeLock.acquire(TimeUnit.SECONDS.toMillis(1));
    }

    private void updateNotificationProgress() {
        mNotificationHelper.updateNotificationProgress(
                getSessionManager().getCurrentSession());
    }

    private void toggleSound(boolean restore) {
        final AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (restore) {
            aManager.setRingerMode(mPreviousRingerMode);
        } else {
            mPreviousRingerMode = aManager.getRingerMode();
            aManager.setRingerMode(RINGER_MODE_SILENT);
        }
    }

    private void toggleWifi(boolean restore) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        if (restore) {
            wifiManager.setWifiEnabled(mPreviousWifiMode);
        } else {
            mPreviousWifiMode = wifiManager.isWifiEnabled();
            wifiManager.setWifiEnabled(false);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
