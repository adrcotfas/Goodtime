package com.apps.adrcotfas.goodtime.bl.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.bl.DomainTimerData
import com.apps.adrcotfas.goodtime.bl.TimerService
import com.apps.adrcotfas.goodtime.bl.TimerState
import com.apps.adrcotfas.goodtime.bl.TimerType
import com.apps.adrcotfas.goodtime.shared.R as SharedR

//TODO: count-up should have a stop button action
//TODO: add break icon to break notifications
class NotificationArchManager(private val context: Context, private val activityClass: Class<*>) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createMainNotificationChannel()
        createReminderChannel()
    }

    fun buildInProgressNotification(data: DomainTimerData): Notification {
        val isCountDown = data.getTimerProfile().isCountdown
        val baseTime = if (isCountDown) data.endTime else SystemClock.elapsedRealtime()
        val running = data.state != TimerState.PAUSED
        val timerType = data.type

        val stateText = if (timerType == TimerType.WORK) {
            if (running) {
                //TODO: extract strings
                "Work session in progress"
            } else {
                "Work session paused"
            }
        } else {
            "Break in progress"
        }

        val builder = NotificationCompat.Builder(context, MAIN_CHANNEL_ID).apply {
            setSmallIcon(SharedR.drawable.ic_status_goodtime)
            setCategory(NotificationCompat.CATEGORY_PROGRESS)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(createOpenActivityIntent(activityClass))
            setOngoing(true)
            setShowWhen(false)
            setAutoCancel(false)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
            setCustomContentView(
                buildChronometer(
                    base = baseTime,
                    running = running,
                    stateText = stateText,
                    isCountDown = isCountDown
                )
            )
        }
        if (isCountDown) {
            if (timerType == TimerType.WORK) {
                if (running) {
                    val pauseAction = createNotificationAction(
                        title = "Pause",
                        action = TimerService.Companion.Action.Toggle
                    )
                    builder.addAction(pauseAction)
                    val addOneMinuteAction = createNotificationAction(
                        title = "+1 min",
                        action = TimerService.Companion.Action.AddOneMinute
                    )
                    builder.addAction(addOneMinuteAction)
                } else {
                    val resumeAction = createNotificationAction(
                        title = "Resume",
                        action = TimerService.Companion.Action.Toggle
                    )
                    builder.addAction(resumeAction)
                    val stopAction = createNotificationAction(
                        title = "Stop",
                        action = TimerService.Companion.Action.DoReset
                    )
                    builder.addAction(stopAction)
                }
            } else {
                val stopAction = createNotificationAction(
                    title = "Stop",
                    action = TimerService.Companion.Action.DoReset
                )
                builder.addAction(stopAction)
                val addOneMinuteAction = createNotificationAction(
                    title = "+1 min",
                    action = TimerService.Companion.Action.AddOneMinute
                )
                builder.addAction(addOneMinuteAction)
            }
            val nextActionTitle = if (timerType == TimerType.WORK) {
                "Start break"
            } else {
                "Start work"
            }
            val nextAction = createNotificationAction(
                title = nextActionTitle,
                action = TimerService.Companion.Action.Skip
            )
            if (data.label.profile.isBreakEnabled) {
                builder.addAction(nextAction)
            }
        } else {
            val stopAction = createNotificationAction(
                title = "Stop",
                action = TimerService.Companion.Action.DoReset
            )
            builder.addAction(stopAction)
        }
        return builder.build()
    }

    fun notifyFinished(data: DomainTimerData, withActions: Boolean) {
        val timerType = data.type
        val labelName = data.getLabelName()

        val mainStateText = if (timerType == TimerType.WORK) {
            "Work session finished"
        } else {
            "Break finished"
        }
        val labelText = if (data.isDefaultLabel()) "" else "$labelName: "
        val stateText = "$labelText$mainStateText"

        val builder = NotificationCompat.Builder(context, MAIN_CHANNEL_ID).apply {
            setSmallIcon(SharedR.drawable.ic_status_goodtime)
            setCategory(NotificationCompat.CATEGORY_PROGRESS)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(createOpenActivityIntent(activityClass))
            setOngoing(false)
            setShowWhen(false)
            setAutoCancel(true)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
            setContentTitle(stateText)
        }
        val extender = NotificationCompat.WearableExtender()
        if (withActions) {
            builder.setContentText("Continue?")
            val nextActionTitle =
                if (timerType == TimerType.WORK && data.label.profile.isBreakEnabled) {
                    "Start break"
                } else {
                    "Start work"
                }
            val nextAction = createNotificationAction(
                title = nextActionTitle,
                action = TimerService.Companion.Action.Next
            )
            extender.addAction(nextAction)
            builder.addAction(nextAction)
        }
        builder.extend(extender)
        notificationManager.notify(FINISHED_NOTIFICATION_ID, builder.build())
    }

    fun clearFinishedNotification() {
        notificationManager.cancel(FINISHED_NOTIFICATION_ID)
    }

    fun notifyReminder() {
        val pendingIntent = createOpenActivityIntent(activityClass)
        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(SharedR.drawable.ic_status_goodtime)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setShowWhen(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentTitle("Productivity reminder")
            .setContentText("It's time to be productive!")
        notificationManager.notify(REMINDER_NOTIFICATION_ID, builder.build())
    }

    private fun createMainNotificationChannel() {
        val name = "Goodtime Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(MAIN_CHANNEL_ID, name, importance).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
            setBypassDnd(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createReminderChannel() {
        val name = "Goodtime Reminder Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(REMINDER_CHANNEL_ID, name, importance).apply {
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildChronometer(
        base: Long,
        running: Boolean,
        stateText: CharSequence,
        isCountDown: Boolean = true,
    ): RemoteViews {
        val content =
            RemoteViews(context.packageName, R.layout.chronometer_notif_content)
        content.setChronometerCountDown(R.id.chronometer, isCountDown)
        content.setChronometer(R.id.chronometer, base, null, running)
        content.setTextViewText(R.id.state, stateText)
        return content
    }

    private fun createOpenActivityIntent(
        activityClass: Class<*>
    ): PendingIntent {
        val intent = Intent(context, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationAction(
        icon: IconCompat? = null,
        title: String,
        action: TimerService.Companion.Action
    ): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            icon,
            title,
            PendingIntent.getService(
                context,
                0,
                TimerService.createIntentWithAction(context, action),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        ).build()
    }

    fun toggleDndMode(enabled: Boolean) {
        if (enabled) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        } else {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    fun isNotificationPolicyAccessGranted(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    companion object {
        const val MAIN_CHANNEL_ID = "goodtime.notification"
        const val IN_PROGRESS_NOTIFICATION_ID = 42
        const val FINISHED_NOTIFICATION_ID = 43
        const val REMINDER_CHANNEL_ID = "goodtime_reminder_notification"
        const val REMINDER_NOTIFICATION_ID = 99
    }
}
