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

import android.content.ContextWrapper
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.annotation.TargetApi
import android.os.Build
import android.app.NotificationChannel
import com.apps.adrcotfas.goodtime.R
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import com.apps.adrcotfas.goodtime.main.TimerActivity
import com.apps.adrcotfas.goodtime.util.IntentWithAction
import com.apps.adrcotfas.goodtime.util.Constants
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Class responsible with creating and updating notifications for the foreground [TimerService]
 * and triggering notifications for events like finishing a session or updating the remaining time.
 * The notifications are customized according to [PreferenceHelper].
 */
class NotificationHelper @Inject constructor(context: Context) : ContextWrapper(context) {
    private val manager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private val builder: NotificationCompat.Builder

    fun notifyFinished(sessionType: SessionType) {
        Log.v(TAG, "notifyFinished")
        clearNotification()
        manager.notify(GOODTIME_NOTIFICATION_ID, getFinishedSessionBuilder(sessionType).build())
    }

    fun updateNotificationProgress(currentSession: CurrentSession) {
        Log.v(TAG, "updateNotificationProgress")
        manager.notify(
            GOODTIME_NOTIFICATION_ID,
            getInProgressBuilder(currentSession)
                .setOnlyAlertOnce(true)
                .setContentText(buildProgressText(currentSession.duration.value))
                .build()
        )
    }

    fun clearNotification() {
        manager.cancelAll()
    }

    private fun buildProgressText(duration: Long?): CharSequence {
        var secondsLeft = TimeUnit.MILLISECONDS.toSeconds(duration!!)
        val minutesLeft = secondsLeft / 60
        secondsLeft %= 60
        return (if (minutesLeft > 9) minutesLeft else "0$minutesLeft").toString() + ":" +
                if (secondsLeft > 9) secondsLeft else "0$secondsLeft"
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initChannels() {
        val channelInProgress = NotificationChannel(
            GOODTIME_NOTIFICATION, getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        channelInProgress.setBypassDnd(true)
        channelInProgress.setShowBadge(true)
        channelInProgress.setSound(null, null)
        manager.createNotificationChannel(channelInProgress)
    }

    /**
     * Get a "in progress" notification
     *
     * @param currentSession the current session
     * @return the builder
     */
    @SuppressLint("RestrictedApi")
    fun getInProgressBuilder(currentSession: CurrentSession): NotificationCompat.Builder {
        Log.v(TAG, "createNotification")
        builder.mActions.clear()
        if (currentSession.sessionType.value == SessionType.WORK) {
            builder.setSmallIcon(R.drawable.ic_status_goodtime)
            if (currentSession.timerState.value == TimerState.PAUSED) {
                builder.addAction(buildStopAction(this))
                    .addAction(buildResumeAction(this))
                    .setContentTitle(getString(R.string.action_paused_title))
                    .setContentText(getString(R.string.action_continue))
            } else {
                builder.addAction(buildStopAction(this))
                    .addAction(buildPauseAction(this))
                    .setContentTitle(getString(R.string.action_progress_work))
                    .setContentText(buildProgressText(currentSession.duration.value))
            }
        } else {
            builder.setSmallIcon(R.drawable.ic_break)
            builder.addAction(buildStopAction(this))
                .setContentTitle(getString(R.string.action_progress_break))
                .setContentText(buildProgressText(currentSession.duration.value))
        }
        return builder
    }

    private fun getFinishedSessionBuilder(sessionType: SessionType): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(this, GOODTIME_NOTIFICATION)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(createActivityIntent())
            .setOngoing(false)
            .setShowWhen(false)
        val extender = NotificationCompat.WearableExtender()
        if (sessionType == SessionType.WORK) {
            builder.setContentTitle(getString(R.string.action_finished_session))
                .setContentText(getString(R.string.action_continue))
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setLights(Color.GREEN, 500, 2000)
                .addAction(buildStartBreakAction(this))
                .addAction(buildSkipBreakAction(this))
            extender.addAction(buildStartBreakAction(this))
            extender.addAction(buildSkipBreakAction(this))
        } else {
            builder.setContentTitle(getString(R.string.action_finished_break))
                .setContentText(getString(R.string.action_continue))
                .setLights(Color.RED, 500, 2000)
                .setSmallIcon(R.drawable.ic_break)
                .addAction(buildStartWorkAction(this))
            extender.addAction(buildStartWorkAction(this))
        }
        builder.extend(extender)
        return builder
    }

    private fun createActivityIntent(): PendingIntent {
        val openMainIntent = Intent(this, TimerActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            openMainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildStartWorkAction(context: Context): NotificationCompat.Action {
        val togglePendingIntent = PendingIntent.getService(
            context, START_WORK_ID,
            IntentWithAction(
                context,
                TimerService::class.java,
                Constants.ACTION.START,
                SessionType.WORK
            ), PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification_resume,
            context.getString(R.string.action_start_work),
            togglePendingIntent
        ).build()
    }

    private fun buildStartBreakAction(context: Context): NotificationCompat.Action {
        val togglePendingIntent = PendingIntent.getService(
            context, START_BREAK_ID,
            IntentWithAction(
                context,
                TimerService::class.java,
                Constants.ACTION.START,
                SessionType.BREAK
            ), PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification_resume,
            context.getString(R.string.action_start_break),
            togglePendingIntent
        ).build()
    }

    private fun buildSkipBreakAction(context: Context): NotificationCompat.Action {
        val togglePendingIntent = PendingIntent.getService(
            context, SKIP_BREAK_ID,
            IntentWithAction(
                context,
                TimerService::class.java,
                Constants.ACTION.START,
                SessionType.WORK
            ), PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification_skip,
            context.getString(R.string.action_skip_break),
            togglePendingIntent
        ).build()
    }

    companion object {
        private val TAG = NotificationHelper::class.java.simpleName
        private const val GOODTIME_NOTIFICATION = "goodtime.notification"
        const val GOODTIME_NOTIFICATION_ID = 42
        private const val START_WORK_ID = 33
        private const val SKIP_BREAK_ID = 34
        private const val START_BREAK_ID = 35
        private const val RESUME_ID = 36
        private const val PAUSE_ID = 37
        private const val STOP_ID = 38
        private fun buildStopAction(context: Context): NotificationCompat.Action {
            val stopPendingIntent = PendingIntent.getService(
                context,
                STOP_ID,
                IntentWithAction(context, TimerService::class.java, Constants.ACTION.STOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Action.Builder(
                R.drawable.ic_stop,
                context.getString(R.string.action_stop),
                stopPendingIntent
            ).build()
        }

        private fun buildResumeAction(context: Context): NotificationCompat.Action {
            val togglePendingIntent = PendingIntent.getService(
                context,
                RESUME_ID,
                IntentWithAction(context, TimerService::class.java, Constants.ACTION.TOGGLE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                context.getString(R.string.action_resume),
                togglePendingIntent
            ).build()
        }

        private fun buildPauseAction(context: Context): NotificationCompat.Action {
            val togglePendingIntent = PendingIntent.getService(
                context,
                PAUSE_ID,
                IntentWithAction(context, TimerService::class.java, Constants.ACTION.TOGGLE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Action.Builder(
                R.drawable.ic_notification_pause,
                context.getString(R.string.action_pause),
                togglePendingIntent
            ).build()
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChannels()
        }
        builder = NotificationCompat.Builder(this, GOODTIME_NOTIFICATION)
            .setSmallIcon(R.drawable.ic_status_goodtime)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createActivityIntent())
            .setOngoing(true)
            .setShowWhen(false)
    }
}