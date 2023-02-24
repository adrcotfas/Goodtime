/*
 * Copyright 2016-2021 Adrian Cotfas
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
package com.apps.adrcotfas.goodtime.bl

import com.apps.adrcotfas.goodtime.database.AppDatabase.Companion.getDatabase
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.LifecycleService
import javax.inject.Inject
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import org.greenrobot.eventbus.EventBus
import kotlin.jvm.Synchronized
import android.content.Intent
import org.greenrobot.eventbus.Subscribe
import com.apps.adrcotfas.goodtime.util.Constants.OneMinuteLeft
import com.apps.adrcotfas.goodtime.util.Constants.FinishWorkEvent
import com.apps.adrcotfas.goodtime.util.Constants.FinishBreakEvent
import com.apps.adrcotfas.goodtime.util.Constants.FinishLongBreakEvent
import com.apps.adrcotfas.goodtime.util.Constants.UpdateTimerProgressEvent
import com.apps.adrcotfas.goodtime.util.Constants.ClearNotificationEvent
import com.apps.adrcotfas.goodtime.util.Constants.StartSessionEvent
import android.os.PowerManager
import android.media.AudioManager
import androidx.annotation.RequiresApi
import android.os.Build
import android.app.NotificationManager
import android.net.wifi.WifiManager
import com.apps.adrcotfas.goodtime.main.TimerActivity
import android.annotation.TargetApi
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.util.Constants
import com.apps.adrcotfas.goodtime.util.toFormattedTime
import com.apps.adrcotfas.goodtime.util.toLocalTime
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.Runnable
import java.util.concurrent.TimeUnit

/**
 * Class representing the foreground service which triggers the countdown timer and handles events.
 */
@AndroidEntryPoint
class TimerService : LifecycleService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var ringtoneAndVibrationPlayer: RingtoneAndVibrationPlayer

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    @Inject
    lateinit var currentSessionManager: CurrentSessionManager

    private var previousRingerMode = 0
    private var previousWifiMode = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate " + this.hashCode())
        notificationHelper = NotificationHelper(applicationContext)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy " + this.hashCode())
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Synchronized
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        var result = START_STICKY
        if (intent == null) {
            return result
        }
        Log.d(TAG, "onStartCommand " + this.hashCode() + " " + intent.action)
        when (intent.action) {
            Constants.ACTION.STOP -> onStopEvent()
            Constants.ACTION.TOGGLE -> {
                onToggleEvent()
                result = START_NOT_STICKY
            }
            Constants.ACTION.START -> {
                val sessionType =
                    SessionType.valueOf(intent.getStringExtra(Constants.SESSION_TYPE)!!)
                onStartEvent(sessionType)
            }
            Constants.ACTION.ADD_SECONDS -> onAdd60Seconds()
            Constants.ACTION.SKIP -> onSkipEvent()
            else -> {
            }
        }
        return result
    }

    /**
     * Called when an event is posted to the EventBus
     * @param o holds the type of the Event
     */
    @Subscribe
    fun onEvent(o: Any) {
        when (o) {
            is OneMinuteLeft -> {
                onOneMinuteLeft()
            }
            is FinishWorkEvent -> {
                Log.d(TAG, "onEvent " + o.javaClass.simpleName)
                onFinishEvent(SessionType.WORK)
            }
            is FinishBreakEvent -> {
                Log.d(TAG, "onEvent " + o.javaClass.simpleName)
                onFinishEvent(SessionType.BREAK)
            }
            is FinishLongBreakEvent -> {
                onFinishEvent(SessionType.LONG_BREAK)
            }
            is UpdateTimerProgressEvent -> {
                updateNotificationProgress()
            }
            is ClearNotificationEvent -> {
                Log.d(TAG, "onEvent " + o.javaClass.simpleName)
                notificationHelper.clearNotification()
                ringtoneAndVibrationPlayer.stop()
            }
        }
    }

    private fun onStartEvent(sessionType: SessionType) {
        var sessionTypeTmp = sessionType
        EventBus.getDefault().post(StartSessionEvent())
        if (sessionTypeTmp !== SessionType.WORK && preferenceHelper.isLongBreakEnabled()
            && preferenceHelper.itsTimeForLongBreak()
        ) {
            sessionTypeTmp = SessionType.LONG_BREAK
        }
        Log.d(TAG, "onStartEvent: $sessionTypeTmp")
        currentSessionManager.startTimer(sessionTypeTmp)
        if (sessionTypeTmp === SessionType.WORK) {
            if (preferenceHelper.isWiFiDisabled()) {
                toggleWifi(false)
            }
            if (preferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(false)
            }
            if (preferenceHelper.isDndModeActive()) {
                toggleDndMode(false)
            }
        }
        if (!preferenceHelper.isAutoStartWork() && !preferenceHelper.isAutoStartBreak()) {
            ringtoneAndVibrationPlayer.stop()
        }
        notificationHelper.clearNotification()
        startForeground(
            NotificationHelper.GOODTIME_NOTIFICATION_ID, notificationHelper.getInProgressBuilder(
                currentSessionManager.currentSession
            ).build()
        )
    }

    private fun onToggleEvent() {
        currentSessionManager.toggleTimer()
        startForeground(
            NotificationHelper.GOODTIME_NOTIFICATION_ID, notificationHelper.getInProgressBuilder(
                currentSessionManager.currentSession
            ).build()
        )
    }

    private fun onStopEvent() {
        Log.d(TAG, "onStopEvent")
        if (preferenceHelper.isWiFiDisabled()) {
            toggleWifi(true)
        }
        if (preferenceHelper.isSoundAndVibrationDisabled()) {
            toggleSound(true)
        }
        if (preferenceHelper.isDndModeActive()) {
            toggleDndMode(true)
        }
        val sessionType = currentSessionManager.currentSession.sessionType.value
        Log.d(TAG, "onStopEvent, sessionType: $sessionType")
        if (sessionType === SessionType.LONG_BREAK) {
            preferenceHelper.resetCurrentStreak()
        }
        stopForeground()
        stopSelf()
        finalizeSession(sessionType, currentSessionManager.elapsedMinutesAtStop)
    }

    private fun onOneMinuteLeft() {
        acquireScreenLock()
        bringActivityToFront()
        ringtoneAndVibrationPlayer.play(SessionType.WORK, false)
    }

    private fun onFinishEvent(sessionType: SessionType) {
        Log.d(
            TAG,
            this@TimerService.hashCode().toString() + " onFinishEvent " + sessionType.toString()
        )
        acquireScreenLock()
        bringActivityToFront()
        if (sessionType === SessionType.WORK) {
            if (preferenceHelper.isWiFiDisabled()) {
                toggleWifi(true)
            }
            if (preferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(true)
            }
            if (preferenceHelper.isDndModeActive()) {
                toggleDndMode(true)
            }
        }
        ringtoneAndVibrationPlayer.play(sessionType, preferenceHelper.isRingtoneInsistent())
        stopForeground()
        updateLongBreakStreak(sessionType)

        // store what was done to the database
        finalizeSession(sessionType, currentSessionManager.elapsedMinutesAtFinished)
        if (preferenceHelper.isAutoStartBreak() && sessionType === SessionType.WORK) {
            onStartEvent(SessionType.BREAK)
        } else if (preferenceHelper.isAutoStartWork() && sessionType !== SessionType.WORK) {
            onStartEvent(SessionType.WORK)
        } else {
            notificationHelper.notifyFinished(sessionType)
        }
    }

    private fun onAdd60Seconds() {
        Log.d(TAG, this@TimerService.hashCode().toString() + " onAdd60Seconds ")
        preferenceHelper.increment60SecondsCounter()
        if (currentSessionManager.currentSession.timerState.value == TimerState.INACTIVE) {
            startForeground(
                NotificationHelper.GOODTIME_NOTIFICATION_ID,
                notificationHelper.getInProgressBuilder(
                    currentSessionManager.currentSession
                ).build()
            )
        }
        currentSessionManager.add60Seconds()
    }

    private fun onSkipEvent() {
        val sessionType = currentSessionManager.currentSession.sessionType.value
        Log.d(
            TAG,
            this@TimerService.hashCode().toString() + " onSkipEvent " + sessionType.toString()
        )
        if (sessionType === SessionType.WORK) {
            if (preferenceHelper.isWiFiDisabled()) {
                toggleWifi(true)
            }
            if (preferenceHelper.isSoundAndVibrationDisabled()) {
                toggleSound(true)
            }
            if (preferenceHelper.isDndModeActive()) {
                toggleDndMode(true)
            }
        }
        currentSessionManager.stopTimer()
        stopForeground()
        updateLongBreakStreak(sessionType)
        finalizeSession(sessionType, currentSessionManager.elapsedMinutesAtStop)
        onStartEvent(if (sessionType === SessionType.WORK) SessionType.BREAK else SessionType.WORK)
    }

    private fun updateLongBreakStreak(sessionType: SessionType?) {
        if (preferenceHelper.isLongBreakEnabled()) {
            if (sessionType === SessionType.LONG_BREAK) {
                preferenceHelper.resetCurrentStreak()
            } else if (sessionType === SessionType.WORK) {
                preferenceHelper.incrementCurrentStreak()
            }
            Log.d(
                TAG,
                "preferenceHelper.getCurrentStreak: " + preferenceHelper.getCurrentStreak()
            )
            Log.d(
                TAG,
                "preferenceHelper.lastWorkFinishedAt: " + preferenceHelper.lastWorkFinishedAt()
            )
        }
    }

    private fun acquireScreenLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            AlarmReceiver::class.java.name
        )
        wakeLock.acquire(5000)
    }

    private fun updateNotificationProgress() {
        notificationHelper.updateNotificationProgress(
            currentSessionManager.currentSession
        )
    }

    private fun toggleSound(restore: Boolean) {
        if (isNotificationPolicyAccessGranted) {
            toggleSoundInternal(restore)
        } else {
            // should not happen
            Log.w(TAG, "Trying to toggle sound but permission was not granted.")
        }
    }

    private fun toggleSoundInternal(restore: Boolean) {
        val t = Thread {
            val aManager = getSystemService(AUDIO_SERVICE) as AudioManager
            if (restore) {
                if (previousRingerMode == AudioManager.RINGER_MODE_SILENT) {
                    return@Thread
                }
                aManager.ringerMode = previousRingerMode
            } else {
                previousRingerMode = aManager.ringerMode
                aManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }
        }
        t.start()
    }

    private fun toggleDndMode(restore: Boolean) {
        if (isNotificationPolicyAccessGranted) {
            togglePriorityMode(restore)
        } else {
            // should not happen
            Log.w(TAG, "Trying to toggle DnD mode but permission was not granted.")
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun togglePriorityMode(restore: Boolean) {
        val t = Thread {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (restore) {
                manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            } else {
                manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            }
        }
        t.start()
    }

    private fun toggleWifi(restore: Boolean) {
        val r = Runnable {
            val wifiManager = this.getSystemService(WIFI_SERVICE) as WifiManager
            if (restore) {
                wifiManager.isWifiEnabled = previousWifiMode
            } else {
                previousWifiMode = wifiManager.isWifiEnabled
                wifiManager.isWifiEnabled = false
            }
        }
        val t = Thread(r)
        t.start()
    }

    private fun finalizeSession(sessionType: SessionType?, minutes: Int) {
        currentSessionManager.stopTimer()
        preferenceHelper.resetAdd60SecondsCounter()
        currentSessionManager.currentSession.setDuration(
            TimeUnit.MINUTES.toMillis(preferenceHelper.getSessionDuration(SessionType.WORK))
        )
        if (sessionType !== SessionType.WORK) {
            return
        }

        val labelVal = currentSessionManager.currentSession.label.value
        val labelValProper =
            if (labelVal == null || labelVal == "" || labelVal == "unlabeled") null else labelVal

        val endTime = System.currentTimeMillis()
        Log.d(TAG, "finalizeSession / elapsed minutes: $minutes")
        if (minutes > 0) {
            Log.d(
                TAG,
                "finalizeSession, saving session finished at" + endTime.toLocalTime()
                    .toFormattedTime()
            )
            val session = Session(0, endTime, minutes, labelValProper)
            lifecycleScope.launch {
                try {
                    getDatabase(applicationContext).sessionModel().addSession(session)
                } catch (e: Exception) {
                    // the label was deleted in the meantime so set it to null and save the unlabeled session
                    withContext(Dispatchers.Main) {
                        currentSessionManager.currentSession.setLabel("")
                    }
                    val newSession = session.apply { label = null }
                    getDatabase(applicationContext).sessionModel().addSession(newSession)
                }
            }
        }
    }

    private fun stopForeground() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            stopForeground(STOP_FOREGROUND_REMOVE)
        else
            stopForeground(true)
    }

    private fun bringActivityToFront() {
        val activityIntent = Intent(this, TimerActivity::class.java)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activityIntent.putExtra("42", 42)
        application.startActivity(activityIntent)
    }

    @get:TargetApi(Build.VERSION_CODES.M)
    private val isNotificationPolicyAccessGranted: Boolean
        get() {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.isNotificationPolicyAccessGranted
        }

    companion object {
        private val TAG = TimerService::class.java.simpleName
    }
}