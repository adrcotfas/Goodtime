/*
 * Copyright 2016-2020 Adrian Cotfas
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

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.Main.TimerActivity;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper;
import com.apps.adrcotfas.goodtime.Util.Constants;

import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleService;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static android.media.AudioManager.RINGER_MODE_SILENT;
import static com.apps.adrcotfas.goodtime.BL.NotificationHelper.GOODTIME_NOTIFICATION_ID;
import static com.apps.adrcotfas.goodtime.Util.Constants.SESSION_TYPE;
import static com.apps.adrcotfas.goodtime.Util.StringUtils.formatTime;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Class representing the foreground service which triggers the countdown timer and handles events.
 */
@AndroidEntryPoint
public class TimerService extends LifecycleService {

    private static final String TAG = TimerService.class.getSimpleName();

    private NotificationHelper notificationHelper;

    @Inject
    RingtoneAndVibrationPlayer ringtoneAndVibrationPlayer;
    
    @Inject PreferenceHelper preferenceHelper;
    
    @Inject CurrentSessionManager currentSessionManager;

    private int previousRingerMode;
    private boolean previousWifiMode;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate " + this.hashCode());
        notificationHelper = new NotificationHelper(getApplicationContext());
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

        if (intent == null) {
            return result;
        }

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
    @Subscribe
    public void onEvent(Object o) {
        if (o instanceof Constants.OneMinuteLeft) {
            onOneMinuteLeft();
        } else if (o instanceof Constants.FinishWorkEvent) {
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
            notificationHelper.clearNotification();
            ringtoneAndVibrationPlayer.stop();
        }
    }

    private void onStartEvent(SessionType sessionType) {

        EventBus.getDefault().post(new Constants.StartSessionEvent());
        if (sessionType != SessionType.WORK && preferenceHelper.isLongBreakEnabled()
                && preferenceHelper.itsTimeForLongBreak()) {
            sessionType = SessionType.LONG_BREAK;
        }

        Log.d(TAG, "onStartEvent: " + sessionType.toString());

        currentSessionManager.startTimer(sessionType);

        if (sessionType == SessionType.WORK) {
            if (preferenceHelper.isWiFiDisabled()) {
                toggleWifi(false);
            }
            if (preferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(false);
            }
            if (preferenceHelper.isDndModeActive()) {
                toggleDndMode(false);
            }
        }

        if (!preferenceHelper.isAutoStartWork() && !preferenceHelper.isAutoStartBreak()) {
            ringtoneAndVibrationPlayer.stop();
        }

        notificationHelper.clearNotification();
        startForeground(GOODTIME_NOTIFICATION_ID, notificationHelper.getInProgressBuilder(
                currentSessionManager.getCurrentSession()).build());
    }

    private void onToggleEvent() {
        currentSessionManager.toggleTimer();
        startForeground(GOODTIME_NOTIFICATION_ID, notificationHelper.getInProgressBuilder(
                currentSessionManager.getCurrentSession()).build());
    }

    private void onStopEvent() {
        Log.d(TAG, "onStopEvent");

        if (preferenceHelper.isWiFiDisabled()) {
            toggleWifi(true);
        }
        if (preferenceHelper.isSoundAndVibrationDisabled()) {
            toggleSound(true);
        }
        if (preferenceHelper.isDndModeActive()) {
            toggleDndMode(true);
        }

        SessionType sessionType = currentSessionManager.getCurrentSession().getSessionType().getValue();
        Log.d(TAG, "onStopEvent, sessionType: " + sessionType);
        if (sessionType == SessionType.LONG_BREAK) {
            preferenceHelper.resetCurrentStreak();
        }

        stopForeground(true);
        stopSelf();

        finalizeSession(sessionType, currentSessionManager.getElapsedMinutesAtStop());
    }

    private void onOneMinuteLeft() {
        acquireScreenLock();
        bringActivityToFront();
        ringtoneAndVibrationPlayer.play(SessionType.WORK, false);
    }

    private void onFinishEvent(SessionType sessionType) {
        Log.d(TAG, TimerService.this.hashCode() + " onFinishEvent " + sessionType.toString());

        acquireScreenLock();
        bringActivityToFront();

        if (sessionType == SessionType.WORK) {
            if (preferenceHelper.isWiFiDisabled()) {
                toggleWifi(true);
            }
            if (preferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(true);
            }
            if (preferenceHelper.isDndModeActive()) {
                toggleDndMode(true);
            }
        }

        ringtoneAndVibrationPlayer.play(sessionType, preferenceHelper.isRingtoneInsistent());
        stopForeground(true);

        updateLongBreakStreak(sessionType);

        // store what was done to the database
        finalizeSession(sessionType, currentSessionManager.getElapsedMinutesAtFinished());

        if (preferenceHelper.isAutoStartBreak() && sessionType == SessionType.WORK) {
            onStartEvent(SessionType.BREAK);
        } else if (preferenceHelper.isAutoStartWork() && sessionType != SessionType.WORK) {
            onStartEvent(SessionType.WORK);
        } else {
            notificationHelper.notifyFinished(sessionType);
        }
    }

    private void onAdd60Seconds() {
        Log.d(TAG, TimerService.this.hashCode() + " onAdd60Seconds ");
        preferenceHelper.increment60SecondsCounter();
        if (currentSessionManager.getCurrentSession().getTimerState().getValue() == TimerState.INACTIVE) {
            startForeground(GOODTIME_NOTIFICATION_ID, notificationHelper.getInProgressBuilder(
                    currentSessionManager.getCurrentSession()).build());
        }
        currentSessionManager.add60Seconds();
    }

    private void onSkipEvent() {
        final SessionType sessionType = currentSessionManager.getCurrentSession().getSessionType().getValue();
        Log.d(TAG, TimerService.this.hashCode() + " onSkipEvent " + sessionType.toString());

        if (sessionType == SessionType.WORK) {
            if (preferenceHelper.isWiFiDisabled()) {
                toggleWifi(true);
            }
            if (preferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(true);
            }
            if (preferenceHelper.isDndModeActive()) {
                toggleDndMode(true);
            }
        }

        currentSessionManager.stopTimer();
        stopForeground(true);
        updateLongBreakStreak(sessionType);

        finalizeSession(sessionType, currentSessionManager.getElapsedMinutesAtStop());

        onStartEvent(sessionType == SessionType.WORK ? SessionType.BREAK : SessionType.WORK);
    }

    private void updateLongBreakStreak(SessionType sessionType) {
        if (preferenceHelper.isLongBreakEnabled()) {
            if (sessionType == SessionType.LONG_BREAK) {
                preferenceHelper.resetCurrentStreak();
            } else if (sessionType == SessionType.WORK){
                preferenceHelper.incrementCurrentStreak();
            }
            Log.d(TAG, "preferenceHelper.getCurrentStreak: " + preferenceHelper.getCurrentStreak());
            Log.d(TAG, "preferenceHelper.lastWorkFinishedAt: " + preferenceHelper.lastWorkFinishedAt());
        }
    }

    private void acquireScreenLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                AlarmReceiver.class.getName());
        wakeLock.acquire(5000);
    }

    private void updateNotificationProgress() {
        notificationHelper.updateNotificationProgress(
                currentSessionManager.getCurrentSession());
    }

    private void toggleSound(boolean restore) {
        if (isNotificationPolicyAccessGranted()) {
            toggleSoundInternal(restore);
        } else {
            // should not happen
            Log.w(TAG, "Trying to toggle sound but permission was not granted.");
        }
    }

    private void toggleSoundInternal(boolean restore) {
        Thread t = new Thread(() -> {
            final AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (restore) {
                if (previousRingerMode == RINGER_MODE_SILENT) {
                    return;
                }
                aManager.setRingerMode(previousRingerMode);
            } else {
                previousRingerMode = aManager.getRingerMode();
                aManager.setRingerMode(RINGER_MODE_SILENT);
            }
        });
        t.start();
    }

    private void toggleDndMode(boolean restore) {
        if (isNotificationPolicyAccessGranted()) {
            togglePriorityMode(restore);
        } else {
            // should not happen
            Log.w(TAG, "Trying to toggle DnD mode but permission was not granted.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void togglePriorityMode(boolean restore) {
        Thread t = new Thread(() -> {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                if (restore) {
                    manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                } else {
                    manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
                }
            }
        });
        t.start();
    }

    private void toggleWifi(boolean restore) {
        Runnable r = () -> {
            WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
            if (restore) {
                wifiManager.setWifiEnabled(previousWifiMode);
            } else {
                previousWifiMode = wifiManager.isWifiEnabled();
                wifiManager.setWifiEnabled(false);
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    //TODO: clean-up this mess
    private void finalizeSession(SessionType sessionType, int minutes) {
        currentSessionManager.stopTimer();
        preferenceHelper.resetAdd60SecondsCounter();
        currentSessionManager.getCurrentSession().setDuration(
                TimeUnit.MINUTES.toMillis(preferenceHelper.getSessionDuration(SessionType.WORK)));

        if (sessionType != SessionType.WORK) {
            return;
        }

        final String label = currentSessionManager.getCurrentSession().getLabel().getValue();
        final long endTime = System.currentTimeMillis();

        Handler handler = new Handler();

        Runnable r = () -> {
            Log.d(TAG, "finalizeSession / elapsed minutes: " + minutes);
            if (minutes > 0) {
                try {
                    Session session = new Session(
                            0,
                            endTime,
                            minutes,
                            label);
                    AppDatabase.getDatabase(getApplicationContext()).sessionModel().addSession(session);
                    Log.d(TAG, "finalizeSession, saving session finished at" + formatTime(endTime));
                } catch (Exception e) {
                    // the label was deleted in the meantime so set it to null and save the unlabeled session
                    handler.post(() -> currentSessionManager.getCurrentSession().setLabel(null));
                    Session session = new Session(
                            0,
                            endTime,
                            minutes,
                            null);
                    AppDatabase.getDatabase(getApplicationContext()).sessionModel().addSession(session);
                }
            }
        };
        Thread t = new Thread(r);
        Log.d(TAG, "finalizeSession, start thread");
        t.start();
    }

    private void bringActivityToFront() {
        Intent activityIntent = new Intent(this, TimerActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra("42", 42);
        getApplication().startActivity(activityIntent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isNotificationPolicyAccessGranted() {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }
}
