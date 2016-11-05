package com.apps.adrcotfas.goodtime;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.graphics.Color.WHITE;
import static android.media.AudioAttributes.USAGE_ALARM;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.apps.adrcotfas.goodtime.TimerState.PAUSED;

public class Notifications {

    public static Notification createCompletionNotification(
            Context context,
            SessionType sessionType,
            String notificationSound
    ) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (!notificationSound.equals("")) {
            if (SDK_INT >= LOLLIPOP) {
                builder.setSound(Uri.parse(notificationSound), USAGE_ALARM);
            } else {
                builder.setSound(Uri.parse(notificationSound));
            }
        }
        builder.setSmallIcon(R.drawable.ic_status_goodtime)
               .setVibrate(new long[]{0, 300, 700, 300})
               .setLights(WHITE, 250, 750)
               .setContentTitle(context.getString(R.string.dialog_session_message))
               .setContentText(buildCompletedNotificationText(context, sessionType))
               .setContentIntent(
                        getActivity(
                                context,
                                0,
                                new Intent(context.getApplicationContext(), MainActivity.class),
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
        return new Notification.Builder(context)
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
                                new Intent(context.getApplicationContext(), MainActivity.class),
                                FLAG_UPDATE_CURRENT
                        ))
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
}
