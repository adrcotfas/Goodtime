package com.apps.adrcotfas.goodtime;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.graphics.Color.WHITE;
import static android.media.AudioAttributes.USAGE_ALARM;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.apps.adrcotfas.goodtime.TimerState.PAUSED;

public class Notifications {

    public final static String ACTION_PAUSE = "com.apps.adrcotfas.goodtime.PAUSE";
    public final static String ACTION_STOP = "com.apps.adrcotfas.goodtime.STOP";
    public final static String ACTION_PAUSE_UI = "com.apps.adrcotfas.goodtime.PAUSE_UI";
    public final static String ACTION_STOP_UI = "com.apps.adrcotfas.goodtime.STOP_UI";

    public static Notification createCompletionNotification(
            Context context,
            SessionType sessionType,
            String notificationSound,
            boolean vibrate
    ) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (!notificationSound.equals("")) {
            if (SDK_INT >= LOLLIPOP) {
                builder.setSound(Uri.parse(notificationSound), USAGE_ALARM);
            } else {
                builder.setSound(Uri.parse(notificationSound));
            }
        }
        if(vibrate) {
            builder.setVibrate(new long[]{0, 300, 700, 300});
        }
        builder.setSmallIcon(R.drawable.ic_status_goodtime)
               .setLights(WHITE, 250, 750)
               .setContentTitle(context.getString(R.string.dialog_session_message))
               .setContentText(buildCompletedNotificationText(context, sessionType))
               .setContentIntent(
                        getActivity(
                                context,
                                0,
                                new Intent(context.getApplicationContext(), TimerActivity.class),
                                FLAG_ONE_SHOT
                        ))
               .setAutoCancel(true);

        return builder.build();
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

    public static Notification createForegroundNotification(
            Context context,
            SessionType sessionType,
            TimerState timerState
    ) {

        Intent pauseIntent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_PAUSE);
        PendingIntent actionPauseIntent = PendingIntent.getService(context, 0,
                pauseIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        boolean isTimerActive = timerState.equals(TimerState.ACTIVE);
        NotificationCompat.Action pauseAction = new NotificationCompat.Action.Builder(
                isTimerActive ? R.drawable.ic_notification_pause
                              : R.drawable.ic_notification_resume,
                context.getString(isTimerActive ? R.string.pause
                                                : R.string.resume), actionPauseIntent).build();

        Intent stopIntent = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_STOP);
        PendingIntent actionStopIntent = PendingIntent.getService(context, 0,
                stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_notification_stop, context.getString(R.string.stop), actionStopIntent).build();

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(buildForegroundNotificationText(context, sessionType, timerState))
                .setOngoing(isNotificationOngoing(timerState))
                .setShowWhen(false)
                .setContentIntent(
                        getActivity(
                                context,
                                0,
                                new Intent(context.getApplicationContext(), TimerActivity.class),
                                FLAG_UPDATE_CURRENT
                        ))
                .addAction(pauseAction)
                .addAction(stopAction)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();
    }

    private static CharSequence buildForegroundNotificationText(
            Context context,
            SessionType sessionType,
            TimerState timerState
    ) {
        switch (sessionType) {
            case BREAK:
            case LONG_BREAK:
                return context.getString(R.string.notification_break);
            case WORK:
            default:
                if (timerState == PAUSED) {
                    return context.getString(R.string.notification_pause);
                } else {
                    return context.getString(R.string.notification_session);
                }
        }
    }

    private static boolean isNotificationOngoing(TimerState timerState) {
        switch (timerState) {
            case INACTIVE:
            case PAUSED:
                return false;
            case ACTIVE:
            default:
                return true;
        }
    }

    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            if (ACTION_PAUSE.equals(intent.getAction())) {
                Intent pauseIntent = new Intent(ACTION_PAUSE_UI);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pauseIntent);
            } else if(ACTION_STOP.equals(intent.getAction())) {
                Intent stopIntent = new Intent(ACTION_STOP_UI);
                LocalBroadcastManager.getInstance(this).sendBroadcast(stopIntent);
            }
        }
    }
}
