package com.apps.adrcotfas.goodtime.Settings.reminders

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.annotation.TargetApi
import android.os.Build
import android.app.NotificationChannel
import com.apps.adrcotfas.goodtime.R
import android.app.NotificationManager
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper
import android.content.pm.PackageManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.util.Log
import com.apps.adrcotfas.goodtime.Main.TimerActivity
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.apps.adrcotfas.goodtime.Util.StringUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import org.joda.time.DateTime
import org.joda.time.LocalTime
import javax.inject.Inject

class ReminderHelper@Inject constructor(
    @ApplicationContext val context: Context,
    val preferenceHelper: PreferenceHelper
) : OnSharedPreferenceChangeListener {

    private val pendingIntent : PendingIntent by lazy {
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.action = REMINDER_ACTION
        PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(ALARM_SERVICE) as AlarmManager
    }

    init {
        PreferenceManager.getDefaultSharedPreferences(context)
            .registerOnSharedPreferenceChangeListener(this)
        initChannelIfNeeded()
        if (preferenceHelper.isReminderEnabled()) {
            toggleBootReceiver(true)
            scheduleNotification()
        }
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == PreferenceHelper.ENABLE_REMINDER) {
            Log.d(TAG, "onSharedPreferenceChanged")
            toggleBootReceiver(preferenceHelper.isReminderEnabled())
            if (preferenceHelper.isReminderEnabled()) {
                scheduleNotification()
            } else {
                unscheduledNotification()
            }
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

    private fun unscheduledNotification() {
        Log.d(TAG, "unscheduledNotification")
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleNotification() {
        if (preferenceHelper.isReminderEnabled()) {
            var calendarMillis: Long = preferenceHelper.getTimeOfReminder()
            Log.d(TAG, "time of reminder: ${StringUtils.formatDateAndTime(calendarMillis)}")
            val now = DateTime()
            Log.d(TAG, "now: ${StringUtils.formatDateAndTime(now.millis)}")
            if (now.isAfter(calendarMillis)) {
                calendarMillis = LocalTime(calendarMillis).toDateTimeToday().plusDays(1).millis
            }
            Log.d(TAG, "scheduleNotification at: ${StringUtils.formatDateAndTime(calendarMillis)}")
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendarMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    companion object {
        private const val TAG = "ReminderHelper"
        private const val GOODTIME_REMINDER_CHANNEL_ID = "goodtime_reminder_notification"
        const val REMINDER_ACTION = "goodtime.reminder_action"
        const val REMINDER_REQUEST_CODE = 11
        private const val REMINDER_NOTIFICATION_ID = 99

        @JvmStatic
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

        @JvmStatic
        fun removeNotification(context: Context) {
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(REMINDER_NOTIFICATION_ID)
        }
    }
}