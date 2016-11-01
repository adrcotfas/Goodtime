package com.apps.adrcotfas.goodtime;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.Optional;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.graphics.Color.WHITE;
import static android.media.AudioAttributes.USAGE_ALARM;

public class Notifications {

    public static Notification createFinishedNotification(
            Context context,
            TimerState timerState,
            String notificationSound
    ) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (!notificationSound.equals("")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setSound(Uri.parse(notificationSound), USAGE_ALARM);
            } else {
                builder.setSound(Uri.parse(notificationSound));
            }
        }
        builder.setSmallIcon(R.drawable.ic_status_goodtime)
                .setVibrate(new long[]{0, 300, 700, 300})
                .setLights(WHITE, 250, 750)
                .setContentTitle(context.getString(R.string.dialog_session_message))
                .setContentText(buildNotificationText(context, timerState))
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

    public static Notification createForegroundNotification(
            Context context,
            TimerState timerState
    ) {
        return new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setAutoCancel(false)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(buildNotificationText(context, timerState))
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

    private static CharSequence buildNotificationText(
            Context context,
            TimerState timerState
    ) {
        CharSequence contextText = context.getString(R.string.notification_session);
        switch (timerState) {
            case ACTIVE_WORK:
                break;
            case ACTIVE_BREAK:
                contextText = context.getString(R.string.notification_break);
                break;
            case PAUSED_WORK:
                contextText = context.getString(R.string.notification_pause);
                break;
            case FINISHED_WORK:
                contextText = context.getString(R.string.notification_work_complete);
                break;
            case FINISHED_BREAK:
                contextText = context.getString(R.string.notification_break_complete);
                break;
        }
        return contextText;
    }

    private static boolean isNotificationOngoing(TimerState timerState) {
        switch (timerState) {
            case PAUSED_WORK:
            case FINISHED_WORK:
            case FINISHED_BREAK:
                return false;
            case ACTIVE_WORK:
            case ACTIVE_BREAK:
            default:
                return true;
        }
    }
}
