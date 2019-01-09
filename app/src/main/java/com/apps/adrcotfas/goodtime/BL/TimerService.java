/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.BL;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.Main.TimerActivity;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Util.Constants;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import de.greenrobot.event.EventBus;

import static android.media.AudioManager.RINGER_MODE_SILENT;
import static com.apps.adrcotfas.goodtime.BL.NotificationHelper.GOODTIME_NOTIFICATION_ID;
import static com.apps.adrcotfas.goodtime.Util.Constants.SESSION_TYPE;

/**
 * Class representing the foreground service which triggers the countdown timer and handles events.
 */
public class TimerService extends LifecycleService {

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
        super.onStartCommand(intent, flags, startId);

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

        mRingtoneAndVibrationPlayer.stop();
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

        SessionType sessionType = getSessionManager().getCurrentSession().getSessionType().getValue();
        if (sessionType == SessionType.LONG_BREAK) {
            PreferenceHelper.resetCurrentStreak();
        }

        PreferenceHelper.toggleAdded60SecondsState(false);

        stopForeground(true);
        stopSelf();

        // store what was done to the database
        if (sessionType == SessionType.WORK) {
            saveToDb();
        }

    }

    private void onFinishEvent(SessionType sessionType) {
        Log.d(TAG, TimerService.this.hashCode() + " onFinishEvent " + sessionType.toString());

        bringActivityToFront();

        acquireScreenLock();
        if (sessionType == SessionType.WORK) {
            if (PreferenceHelper.isWiFiDisabled()) {
                toggleWifi(true);
            }
            if (PreferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(true);
            }
        }

        mRingtoneAndVibrationPlayer.play(sessionType);
        stopForeground(true);

        updateLongBreakStreak(sessionType);
        PreferenceHelper.toggleAdded60SecondsState(false);

        if (PreferenceHelper.isAutoStartBreak() && sessionType == SessionType.WORK) {
            onStartEvent(SessionType.BREAK);
        } else if (PreferenceHelper.isAutoStartWork() && sessionType != SessionType.WORK) {
            onStartEvent(SessionType.WORK);
        } else {
            mNotificationHelper.notifyFinished(sessionType);
        }

        // store what was done to the database
        if (sessionType == SessionType.WORK) {
            saveToDb();
        } else {
            getSessionManager().getCurrentSession().setDuration(
                    TimeUnit.MINUTES.toMillis(PreferenceHelper.getSessionDuration(SessionType.WORK)));
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
        return GoodtimeApplication.getCurrentSessionManager();
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

    private void saveToDb() {
        //TODO: save session of at least one minute
        //TODO: refactor this mess
        if (true /*getSessionManager().getElapsedTime() >= 60*/) {

            Handler handler = new Handler();
            Runnable r = () -> {
                try {
                    Session session = new Session(
                            0,
                            System.currentTimeMillis(),
                            getSessionManager().getElapsedTime(),
                            getSessionManager().getCurrentSession().getLabel().getValue());
                    AppDatabase.getDatabase(getApplicationContext()).sessionModel().addSession(session);
                } catch (Exception e) {
                    // the label was deleted in the meantime so set it to null and save the unlabeled session
                    handler.post(() -> getSessionManager().getCurrentSession().setLabel(null));
                    Session session = new Session(
                            0,
                            System.currentTimeMillis(),
                            getSessionManager().getElapsedTime(),
                            null);
                    AppDatabase.getDatabase(getApplicationContext()).sessionModel().addSession(session);
                }
                handler.post(() -> getSessionManager().getCurrentSession().setDuration(
                        TimeUnit.MINUTES.toMillis(PreferenceHelper.getSessionDuration(SessionType.WORK))));
            };
            Thread t = new Thread(r);
            t.start();
        }
    }

    private void bringActivityToFront() {
        Intent activityIntent = new Intent(this, TimerActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra("42", 42);
        getApplication().startActivity(activityIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
}
