package com.apps.adrcotfas.goodtime.BL;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;

import de.greenrobot.event.EventBus;

import static android.media.AudioManager.RINGER_MODE_SILENT;
import static com.apps.adrcotfas.goodtime.BL.NotificationHelper.GOODTIME_NOTIFICATION_ID;

/**
 * Class representing the foreground service which triggers the countdown timer and handles events.
 */
public class TimerService extends Service {

    private static final String TAG = TimerService.class.getSimpleName();

    private CurrentSessionManager mSessionManager;
    private NotificationHelper mNotificationHelper;
    private RingtoneAndVibrationPlayer mRingtoneAndVibrationPlayer;

    private int mPreviousRingerMode;
    private boolean mPreviousWifiMode;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v(TAG, "onCreate");
        mNotificationHelper = new NotificationHelper(getApplicationContext());
        mRingtoneAndVibrationPlayer = new RingtoneAndVibrationPlayer(getApplicationContext());
        mSessionManager = GoodtimeApplication.getInstance().getCurrentSessionManager();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
        mSessionManager.unregisterAlarmReceiver();
        super.onDestroy();
    }

    @Override
    public synchronized int onStartCommand(final Intent intent, int flags, int startId) {

        switch (intent.getAction()) {
            case Constants.ACTION.START_WORK:
            case Constants.ACTION.SKIP_BREAK:
                onStartEvent(SessionType.WORK);
                break;
            case Constants.ACTION.STOP:
                onStopEvent();
                break;
            case Constants.ACTION.TOGGLE:
                onToggleEvent();
                break;
            case Constants.ACTION.START_BREAK:
                onStartEvent(SessionType.BREAK);
                break;
            case Constants.ACTION.SKIP_WORK:
                onSkipWorkEvent();
                break;
            default:
                break;
        }
        return START_STICKY;
    }

    /**
     * Called when an event is posted to the EventBus
     * @param o holds the type of the Event
     */
    public void onEvent(Object o) {
        if (o instanceof Constants.FinishWorkEvent) {
            onFinishEvent(SessionType.WORK);
        } else if (o instanceof Constants.FinishBreakEvent) {
            onFinishEvent(SessionType.BREAK);
        } else if (o instanceof Constants.UpdateTimerProgressEvent) {
            updateNotificationProgress();
        } else if (o instanceof Constants.ClearNotificationEvent) {
            mNotificationHelper.clearNotification();
            mRingtoneAndVibrationPlayer.stop();
        }
    }

    private void onStartEvent(SessionType sessionType) {
        EventBus.getDefault().post(new Constants.ClearFinishDialogEvent());
        mSessionManager.startTimer(sessionType);

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
                mSessionManager.getCurrentSession()).build());
    }

    private void onToggleEvent() {
        mSessionManager.toggleTimer();
        startForeground(GOODTIME_NOTIFICATION_ID, mNotificationHelper.getInProgressBuilder(
                mSessionManager.getCurrentSession()).build());
    }

    private void onStopEvent() {
        mSessionManager.stopTimer();
        Log.v(TAG, "onStopEvent");

        if (PreferenceHelper.isWiFiDisabled()) {
            toggleWifi(true);
        }
        if (PreferenceHelper.isSoundAndVibrationDisabled()) {
            toggleSound(true);
        }

        stopForeground(true);
        stopSelf();
        // TODO: store what was done of the session to ROOM
    }

    private void onFinishEvent(SessionType sessionType) {
        Log.v(TAG, "onFinishEvent");

        if (sessionType == SessionType.WORK) {
            if (PreferenceHelper.isWiFiDisabled()) {
                toggleWifi(true);
            }
            if (PreferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(true);
            }
        }

        // TODO: store session to ROOM
        // TODO: in continuous mode, do not stop the service
        stopForeground(true);
        mNotificationHelper.notifyFinished(sessionType);
        mRingtoneAndVibrationPlayer.play(sessionType);
    }

    private void onSkipWorkEvent() {
        // TODO: store what was done of the session to ROOM
        onStartEvent(SessionType.BREAK);
    }

    private void updateNotificationProgress() {
        mNotificationHelper.updateNotificationProgress(
                mSessionManager.getCurrentSession());
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
