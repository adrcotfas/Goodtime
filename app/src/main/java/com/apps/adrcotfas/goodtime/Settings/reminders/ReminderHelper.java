package com.apps.adrcotfas.goodtime.Settings.reminders;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.apps.adrcotfas.goodtime.Main.TimerActivity;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper;
import com.apps.adrcotfas.goodtime.Util.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;


import static android.app.AlarmManager.RTC_WAKEUP;

public class ReminderHelper extends ContextWrapper implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "ReminderHelper";

    private static final String GOODTIME_REMINDER_NOTIFICATION = "goodtime_reminder_notification";
    public final static String REMINDER_ACTION = "goodtime.reminder_action";
    public final static int REMINDER_REQUEST_CODE = 11;
    public static final int REMINDER_NOTIFICATION_ID = 99;

    private static AlarmManager alarmManager;
    private static PendingIntent pendingIntent;

    public ReminderHelper(Context context) {
        super(context);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initChannel();
        }
        if (PreferenceHelper.isReminderEnabled()) {
            enableBootReceiver();
            scheduleNotification();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void initChannel() {
        Log.d(TAG, "initChannel");
        NotificationChannel c = new NotificationChannel(
                GOODTIME_REMINDER_NOTIFICATION,
                getString(R.string.reminder_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        c.setShowBadge(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(c);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferenceHelper.ENABLE_REMINDER)) {
            Log.d(TAG, "onSharedPreferenceChanged");
            if (PreferenceHelper.isReminderEnabled()) {
                enableBootReceiver();
                scheduleNotification();
            } else {
                disableBootReceiver();
                unscheduledNotification();
            }
        }
    }

    public void enableBootReceiver() {
        Log.d(TAG, "enableBootReceiver");
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableBootReceiver() {
        Log.d(TAG, "disableBootReceiver");
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private AlarmManager getAlarmManager() {
        if (alarmManager  == null) {
            alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        }
        return alarmManager;
    }

    private PendingIntent getReminderPendingIntent() {
        if (pendingIntent == null) {
            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.setAction(REMINDER_ACTION);
            pendingIntent = PendingIntent.getBroadcast(
                    this,
                    REMINDER_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        return pendingIntent;
    }

    private void unscheduledNotification() {
        Log.d(TAG, "unscheduledNotification");
        getAlarmManager().cancel(getReminderPendingIntent());
    }

    public void scheduleNotification() {
        if (PreferenceHelper.isReminderEnabled()) {

            long calendarMillis = PreferenceHelper.getTimeOfReminder();
            Log.d(TAG, "time of reminder: " + StringUtils.formatDateAndTime(calendarMillis));

            final DateTime now = new DateTime();
            Log.d(TAG, "now: %s" + StringUtils.formatDateAndTime(now.getMillis()));
            if (now.isAfter(calendarMillis)) {
                calendarMillis  = new LocalTime(calendarMillis).toDateTimeToday().plusDays(1).getMillis();
            }

            Log.d(TAG, "scheduleNotification at: " + StringUtils.formatDateAndTime(calendarMillis));

            getAlarmManager().setInexactRepeating(
                    RTC_WAKEUP,
                    calendarMillis,
                    AlarmManager.INTERVAL_DAY,
                    getReminderPendingIntent());
        }
    }

    public static void notifyReminder(Context context) {
        Intent openMainIntent = new Intent(context, TimerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openMainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, GOODTIME_REMINDER_NOTIFICATION)
                        .setSmallIcon(R.drawable.ic_status_goodtime)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(pendingIntent)
                        .setShowWhen(false)
                        .setOnlyAlertOnce(true)
                        .setContentTitle(context.getString(R.string.reminder_title))
                        .setContentText(context.getString(R.string.reminder_text));
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(REMINDER_NOTIFICATION_ID, builder.build());
    }

    public static void removeNotification(Context context) {
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(REMINDER_NOTIFICATION_ID);
    }
}
