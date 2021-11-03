package com.apps.adrcotfas.goodtime.settings.reminders

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.annotation.TargetApi
import android.os.Build
import android.app.NotificationChannel
import com.apps.adrcotfas.goodtime.R
import android.app.NotificationManager
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import android.content.pm.PackageManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.util.Log
import androidx.core.app.NotificationCompat
import com.apps.adrcotfas.goodtime.main.TimerActivity
import com.apps.adrcotfas.goodtime.util.StringUtils.formatDateAndTime
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.*
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class ReminderHelper@Inject constructor(
    @ApplicationContext val context: Context,
    val preferenceHelper: PreferenceHelper
) : OnSharedPreferenceChangeListener {

    private var pendingIntents : Array<PendingIntent?> = arrayOfNulls(7)
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(ALARM_SERVICE) as AlarmManager
    }

    init {
        preferenceHelper.preferences.registerOnSharedPreferenceChangeListener(this)
        initChannelIfNeeded()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(GOODTIME_REMINDER_CHANNEL_ID)
            if (channel == null) {
                Log.d(TAG, "initChannel")
                val c = NotificationChannel(
                    GOODTIME_REMINDER_CHANNEL_ID,
                    context.getString(R.string.reminder_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                c.setShowBadge(true)
                notificationManager.createNotificationChannel(c)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        // some weird crash in the pre-report required this
        if (key == null) {
            return
        }
        if (key.contains(PreferenceHelper.REMINDER_DAYS)) {
            Log.d(TAG, "onSharedPreferenceChanged: $key")
            val idx = key.last().toInt() - '0'.toInt()
            val reminderDay = DayOfWeek.of(idx + 1)
            if (preferenceHelper.isReminderEnabledFor(reminderDay)) {
                toggleBootReceiver(true)
                scheduleNotification(reminderDay)
            } else {
                cancelNotification(reminderDay)
                toggleBootReceiver(false)
            }
        } else if (key == PreferenceHelper.REMINDER_TIME) {
            Log.d(TAG, "onSharedPreferenceChanged: REMINDER_TIME")
            cancelNotifications()
            scheduleNotifications()
        }
    }

    private fun toggleBootReceiver(enabled: Boolean) {
        Log.d(TAG, "toggleBootReceiver ${if (enabled) "ENABLED" else "DISABLED"}")
        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun getReminderPendingIntent(index: Int): PendingIntent {
        if (pendingIntents[index] == null) {
            val intent = Intent(context, ReminderReceiver::class.java)
            intent.action = REMINDER_ACTION
            pendingIntents[index] = PendingIntent.getBroadcast(
                context,
                REMINDER_REQUEST_CODE + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        return pendingIntents[index]!!
    }

    private fun cancelNotifications() {
        Log.d(TAG, "cancelNotifications")
        for (day in DayOfWeek.values()) {
            cancelNotification(day)
        }
    }

    private fun cancelNotification(day: DayOfWeek) {
        Log.d(TAG, "cancelNotification for $day")
        val reminderPendingIntent = getReminderPendingIntent(day.ordinal)
        alarmManager.cancel(reminderPendingIntent)
    }

    fun scheduleNotifications() {
        if (preferenceHelper.isReminderEnabled()) {
            for (i in preferenceHelper.getReminderDays().withIndex()) {
                if (i.value) {
                    val reminderDay = DayOfWeek.of(i.index + 1)
                    scheduleNotification(reminderDay)
                }
            }
        }
    }

    private fun scheduleNotification(reminderDay: DayOfWeek) {
        val now = LocalDateTime.now()
        Log.d(TAG, "now: ${now.toLocalTime()}")

        val time = LocalTime.ofSecondOfDay(preferenceHelper.getReminderTime().toLong())
        Log.d(TAG, "time of reminder: $time")

        var reminderTime = now
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .with(TemporalAdjusters.nextOrSame(reminderDay))

        if (reminderTime.isBefore(now)) {
            Log.d(TAG, "reminderTime is before now; schedule for next week")
            reminderTime = reminderTime.plusWeeks(1)
        }

        Log.d(TAG, "reminderTime: $reminderTime")

        val reminderMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.d(TAG, "scheduleNotification at: " + formatDateAndTime(reminderMillis))
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            reminderMillis,
            AlarmManager.INTERVAL_DAY * 7,
            getReminderPendingIntent(reminderDay.ordinal)
        )
    }

    companion object {
        private const val TAG = "ReminderHelper"
        private const val GOODTIME_REMINDER_CHANNEL_ID = "goodtime_reminder_notification"
        const val REMINDER_ACTION = "goodtime.reminder_action"
        const val REMINDER_REQUEST_CODE = 11
        private const val REMINDER_NOTIFICATION_ID = 99

        fun notifyReminder(context: Context) {
            val openMainIntent = Intent(context, TimerActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openMainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val builder = NotificationCompat.Builder(context, GOODTIME_REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setContentTitle(context.getString(R.string.reminder_title))
                .setContentText(context.getString(R.string.reminder_text))
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(REMINDER_NOTIFICATION_ID, builder.build())
        }
        fun removeNotification(context: Context) {
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(REMINDER_NOTIFICATION_ID)
        }
    }
}