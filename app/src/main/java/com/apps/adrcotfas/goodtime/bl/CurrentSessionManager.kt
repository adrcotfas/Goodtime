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
package com.apps.adrcotfas.goodtime.bl

import android.content.ContextWrapper
import javax.inject.Inject
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.bl.CurrentSessionManager.AppCountDownTimer
import android.content.IntentFilter
import com.apps.adrcotfas.goodtime.util.Constants.ACTION
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import com.apps.adrcotfas.goodtime.util.Constants
import org.greenrobot.eventbus.EventBus
import com.apps.adrcotfas.goodtime.util.Constants.UpdateTimerProgressEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * This class manages and modifies the mutable members of [CurrentSession]
 * The duration is updated using an [AppCountDownTimer]. Events coming from other layers will
 * trigger an update of the [CurrentSession]'s [TimerState] and [SessionType].
 */
class CurrentSessionManager @Inject constructor(@ApplicationContext val context: Context, val preferenceHelper: PreferenceHelper) :
    ContextWrapper(context) {

    var currentSession = CurrentSession(TimeUnit.MINUTES.toMillis(preferenceHelper.getSessionDuration(SessionType.WORK)), preferenceHelper.currentSessionLabel.title)

    private lateinit var timer: AppCountDownTimer
    private var remaining : Long = 0 // [ms]

    private val alarmReceiver: AlarmReceiver = AlarmReceiver(object : AlarmReceiver.OnAlarmReceivedListener{
        override fun onAlarmReceived() {
            currentSession.setTimerState(TimerState.INACTIVE)
        }
    })

    private var sessionDuration: Long = 0

    fun startTimer(sessionType: SessionType) {
        Log.v(TAG, "startTimer: $sessionType")
        sessionDuration =
            TimeUnit.MINUTES.toMillis(preferenceHelper.getSessionDuration(sessionType))
        currentSession.setTimerState(TimerState.ACTIVE)
        currentSession.setSessionType(sessionType)
        currentSession.setDuration(sessionDuration)
        scheduleAlarm(
            sessionType, sessionDuration, preferenceHelper.oneMinuteBeforeNotificationEnabled()
                    && sessionDuration > TimeUnit.MINUTES.toMillis(1)
        )
        timer = AppCountDownTimer(sessionDuration)
        timer.start()
    }

    fun toggleTimer() {
        when (currentSession.timerState.value) {
            TimerState.PAUSED -> {
                Log.v(TAG, "toggleTimer PAUSED")
                scheduleAlarm(
                    currentSession.sessionType.value,
                    remaining,
                    preferenceHelper.oneMinuteBeforeNotificationEnabled()
                            && remaining > TimeUnit.MINUTES.toMillis(1)
                )
                timer.start()
                currentSession.setTimerState(TimerState.ACTIVE)
            }
            TimerState.ACTIVE -> {
                Log.v(TAG, "toggleTimer UNPAUSED")
                cancelAlarm()
                timer.cancel()
                timer = AppCountDownTimer(remaining)
                currentSession.setTimerState(TimerState.PAUSED)
            }
            else -> Log.wtf(TAG, "The timer is in an invalid state.")
        }
    }

    fun stopTimer() {
        cancelAlarm()
        if (this::timer.isInitialized) {
            timer.cancel()
        }
        currentSession.setTimerState(TimerState.INACTIVE)
        currentSession.setSessionType(SessionType.INVALID)
    }

    /**
     * This is used to get the minutes that should be stored to the statistics
     * To be called when the session is finished without user interaction
     * @return the minutes elapsed
     */
    val elapsedMinutesAtFinished: Int
        get() {
            val sessionMinutes = TimeUnit.MILLISECONDS.toMinutes(sessionDuration)
                .toInt()
            val extraMinutes = preferenceHelper.add60SecondsCounter
            return sessionMinutes + extraMinutes
        }

    /**
     * This is used to get the minutes that should be stored to the statistics
     * To be called when the user manually stops an ongoing session (or skips)
     * @return the minutes elapsed
     */
    val elapsedMinutesAtStop: Int
        get() {
            val sessionMinutes = TimeUnit.MILLISECONDS.toMinutes(sessionDuration)
                .toInt()
            val extraMinutes = preferenceHelper.add60SecondsCounter
            val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remaining + 30000)
                .toInt()
            return sessionMinutes - remainingMinutes + extraMinutes
        }

    private fun scheduleAlarm(
        sessionType: SessionType?,
        duration: Long,
        remindOneMinuteLeft: Boolean
    ) {
        this.registerReceiver(alarmReceiver, IntentFilter(ACTION.FINISHED))
        val triggerAtMillis = duration + SystemClock.elapsedRealtime()
        Log.v(TAG, "scheduleAlarm " + sessionType.toString())
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis, getAlarmPendingIntent(sessionType)
        )
        if (remindOneMinuteLeft && sessionType == SessionType.WORK) {
            Log.v(TAG, "scheduled one minute left")
            val triggerAtMillisOneMinuteLeft =
                duration - TimeUnit.MINUTES.toMillis(1) + SystemClock.elapsedRealtime()
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMillisOneMinuteLeft, oneMinuteLeftAlarmPendingIntent
            )
        }
    }

    private fun cancelAlarm() {
        val intent = getAlarmPendingIntent(currentSession.sessionType.value)
        alarmManager.cancel(intent)
        val intentOneMinuteLeft = oneMinuteLeftAlarmPendingIntent
        alarmManager.cancel(intentOneMinuteLeft)
        unregisterAlarmReceiver()
    }

    private fun unregisterAlarmReceiver() {
        Log.v(TAG, "unregisterAlarmReceiver")
        try {
            unregisterReceiver(alarmReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "AlarmReceiver is already unregistered.")
        }
    }

    private val alarmManager: AlarmManager
        get() = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager

    private fun getAlarmPendingIntent(sessionType: SessionType?): PendingIntent {
        val intent = Intent(ACTION.FINISHED)
        intent.putExtra(Constants.SESSION_TYPE, sessionType.toString())
        return PendingIntent.getBroadcast(
            applicationContext, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val oneMinuteLeftAlarmPendingIntent: PendingIntent
        get() {
            val intent = Intent(ACTION.FINISHED)
            intent.putExtra(Constants.ONE_MINUTE_LEFT, true)
            return PendingIntent.getBroadcast(
                applicationContext, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

    fun add60Seconds() {
        Log.v(TAG, "add60Seconds")
        val extra: Long = 60000 //TimeUnit.SECONDS.toMillis(60);
        cancelAlarm()
        timer.cancel()
        remaining = min(remaining + extra, TimeUnit.MINUTES.toMillis(240))
        timer = AppCountDownTimer(remaining)
        if (currentSession.timerState.value != TimerState.PAUSED) {
            scheduleAlarm(
                currentSession.sessionType.value,
                remaining,
                preferenceHelper.oneMinuteBeforeNotificationEnabled()
                        && remaining > TimeUnit.MINUTES.toMillis(1)
            )
            timer.start()
            currentSession.setTimerState(TimerState.ACTIVE)
        } else {
            currentSession.setDuration(remaining)
        }
    }

    private inner class AppCountDownTimer
    /**
     * @param millisInFuture    The number of millis in the future from the call
     * to [.start] until the countdown is done and [.onFinish]
     * is called.
     */(millisInFuture: Long) : CountDownTimer(millisInFuture, 1000) {

        private val TAG = AppCountDownTimer::class.java.simpleName

        /**
         * This is useful only when the screen is turned on. It seems that onTick is not called for every tick if the
         * phone is locked and the app runs in the background.
         * I found this the hard way when using the session duration(which is set here) in saving to statistics.
         */
        override fun onTick(millisUntilFinished: Long) {
            Log.v(TAG, "is Ticking: $millisUntilFinished millis remaining.")
            currentSession.setDuration(millisUntilFinished)
            remaining = millisUntilFinished
            EventBus.getDefault().post(UpdateTimerProgressEvent())
        }

        override fun onFinish() {
            Log.v(TAG, "is finished.")
            remaining = 0
        }
    }

    companion object {
        private val TAG = CurrentSessionManager::class.java.simpleName
    }

}