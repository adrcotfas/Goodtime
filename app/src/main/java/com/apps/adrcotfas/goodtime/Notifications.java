package com.apps.adrcotfas.goodtime;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;

import java.util.Calendar;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.graphics.Color.WHITE;
import static android.media.AudioAttributes.USAGE_ALARM;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;
import static com.apps.adrcotfas.goodtime.TimerState.PAUSED;

public final class Notifications {

    final static String ACTION_PAUSE_UI = "com.apps.adrcotfas.goodtime.PAUSE_UI";
    final static String ACTION_STOP_UI = "com.apps.adrcotfas.goodtime.STOP_UI";
    final static String ACTION_START_BREAK_UI = "com.apps.adrcotfas.goodtime.START_BREAK_UI";
    final static String ACTION_SKIP_BREAK_UI = "com.apps.adrcotfas.goodtime.SKIP_BREAK_UI";
    final static String ACTION_START_WORK_UI = "com.apps.adrcotfas.goodtime.START_WORK_UI";
    private final static String ACTION_PAUSE = "com.apps.adrcotfas.goodtime.PAUSE";
    private final static String ACTION_STOP = "com.apps.adrcotfas.goodtime.STOP";
    private final static String ACTION_START_BREAK = "com.apps.adrcotfas.goodtime.START_BREAK";
    private final static String ACTION_SKIP_BREAK = "com.apps.adrcotfas.goodtime.SKIP_BREAK";
    private final static String ACTION_START_WORK = "com.apps.adrcotfas.goodtime.START_WORK";

    static Notification createCompletionNotification(
            Context context,
            SessionType sessionType,
            String notificationSound,
            boolean vibrate,
            boolean addButtons
    ) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_status_goodtime)
                .setPriority(PRIORITY_HIGH)
                .setVisibility(VISIBILITY_PUBLIC)
                .setLights(WHITE, 250, 750)
                .setContentTitle(buildCompletedNotificationTitle(context, sessionType))
                .setContentText(buildCompletedNotificationText(context, sessionType))
                .setContentIntent(
                        getActivity(
                                context,
                                0,
                                new Intent(context.getApplicationContext(), TimerActivity.class),
                                FLAG_ONE_SHOT
                        ))
                .setAutoCancel(true);

        if(addButtons) {
            NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
            if (isWorkingSession(sessionType)) {
                NotificationCompat.Action startBreakAction = createStartBreakAction(context);
                NotificationCompat.Action skipBreakAction = createSkipBreakAction(context);
                builder.addAction(startBreakAction)
                        .addAction(skipBreakAction);
                extender.addAction(startBreakAction)
                        .addAction(skipBreakAction);
            } else {
                NotificationCompat.Action startWorkAction = createStartWorkAction(context);
                builder.addAction(startWorkAction);
                extender.addAction(startWorkAction);
            }
            builder.extend(extender);
        }
        if (vibrate) {
            builder.setVibrate(new long[]{0, 300, 700, 300});
        }
        if (!notificationSound.equals("")) {
            if (SDK_INT >= LOLLIPOP) {
                builder.setSound(Uri.parse(notificationSound), USAGE_ALARM);
            } else {
                builder.setSound(Uri.parse(notificationSound), AudioManager.STREAM_ALARM);
            }
        }
        return builder.build();
    }

    private static CharSequence buildCompletedNotificationTitle(Context context, SessionType sessionType) {
        switch (sessionType) {
            case BREAK:
            case LONG_BREAK:
                return context.getString(R.string.dialog_break_message);
            case WORK:
            default:
                return context.getString(R.string.dialog_session_message);
        }
    }

    private static CharSequence buildCompletedNotificationText(Context context, SessionType sessionType) {
        switch (sessionType) {
            case BREAK:
            case LONG_BREAK:
                return context.getString(R.string.notification_break_complete);
            case WORK:
            default:
                return context.getString(R.string.notification_work_complete);
        }
    }

    static Notification createForegroundNotification(
            Context context,
            SessionType sessionType,
            TimerState timerState,
            int remainingTime
    ) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_status_goodtime)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(buildForegroundNotificationText(context, sessionType, timerState, remainingTime))
                .setVisibility(VISIBILITY_PUBLIC)
                .setOngoing(isTimerActive(timerState))
                .setShowWhen(false)
                .setContentIntent(
                        getActivity(
                                context,
                                0,
                                new Intent(context.getApplicationContext(), TimerActivity.class),
                                FLAG_UPDATE_CURRENT
                        ));

        builder.addAction(createStopAction(context));
        if (isWorkingSession(sessionType)) {
            builder.addAction(
                    isTimerActive(timerState) ? createPauseAction(context)
                                              : createResumeAction(context));
        }

        return builder.build();
    }

    private static CharSequence buildForegroundNotificationText(
            Context context,
            SessionType sessionType,
            TimerState timerState,
            int remainingTime
    ) {
        switch (sessionType) {
            case BREAK:
            case LONG_BREAK:
                return context.getString(R.string.notification_break)
                        + buildNotificationCountdownTime(context, remainingTime);
            case WORK:
            default:
                if (timerState == PAUSED) {
                    return context.getString(R.string.notification_pause);
                } else {
                    return context.getString(R.string.notification_session)
                            + buildNotificationCountdownTime(context, remainingTime);
                }
        }
    }

    private static CharSequence buildNotificationCountdownTime(Context context, int remainingTime) {

        boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(context);

        String inFormat = is24HourFormat ? " k:mm" : " h:mm aa";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, remainingTime);

        return DateFormat.format(inFormat, calendar) + ".";
    }

    private static boolean isTimerActive(TimerState timerState) {
        switch (timerState) {
            case INACTIVE:
            case PAUSED:
                return false;
            case ACTIVE:
            default:
                return true;
        }
    }

    private static boolean isWorkingSession(SessionType sessionType) {
        return sessionType.equals(SessionType.WORK);
    }

    private static NotificationCompat.Action createStartBreakAction(Context context) {
        Intent startBreakIntent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_START_BREAK);
        PendingIntent startBreakPendingIntent = PendingIntent.getService(
                context, 0, startBreakIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                context.getString(R.string.dialog_session_break),
                startBreakPendingIntent).build();
    }

    private static NotificationCompat.Action createSkipBreakAction(Context context) {
        Intent skipBreakIntent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_SKIP_BREAK);
        PendingIntent skipBreakPendingIntent = PendingIntent.getService(
                context, 0, skipBreakIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_skip,
                context.getString(R.string.dialog_session_skip),
                skipBreakPendingIntent).build();
    }

    private static NotificationCompat.Action createStartWorkAction(Context context) {
        Intent startWorkIntent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_START_WORK);
        PendingIntent startWorkPendingIntent = PendingIntent.getService(
                context, 0, startWorkIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                context.getString(R.string.dialog_break_session),
                startWorkPendingIntent).build();
    }

    private static NotificationCompat.Action createPauseAction(Context context) {
        Intent pauseIntent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(
                context, 0, pauseIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_pause,
                context.getString(R.string.pause),
                pausePendingIntent).build();
    }

    private static NotificationCompat.Action createResumeAction(Context context) {
        Intent pauseIntent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(
                context, 0, pauseIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_resume,
                context.getString(R.string.resume),
                pausePendingIntent).build();
    }

    private static NotificationCompat.Action createStopAction(Context context) {
        Intent stopIntent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                context, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_stop, context.getString(R.string.stop),
                stopPendingIntent).build();
    }

    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            switch (intent.getAction()) {
                case ACTION_PAUSE:
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            new Intent(ACTION_PAUSE_UI));
                    break;
                case ACTION_STOP:
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            new Intent(ACTION_STOP_UI));
                    break;
                case ACTION_SKIP_BREAK:
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            new Intent(ACTION_SKIP_BREAK_UI));
                    break;
                case ACTION_START_BREAK:
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            new Intent(ACTION_START_BREAK_UI));
                    break;
                case ACTION_START_WORK:
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            new Intent(ACTION_START_WORK_UI));
                    break;
            }
        }
    }
}
