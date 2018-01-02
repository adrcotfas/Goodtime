package com.apps.adrcotfas.goodtime.BL;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.apps.adrcotfas.goodtime.Main.TimerActivity;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;

public class AppNotificationManager {

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    public AppNotificationManager(Context context) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(notificationChannel);
            mBuilder = new NotificationCompat.Builder(context, notificationChannel.getId());
        } else {
            mBuilder = new NotificationCompat.Builder(context);
        }
    }

    //TODO: https://developer.android.com/guide/topics/ui/notifiers/notifications.html#Updating
    // https://developer.android.com/reference/android/app/Notification.Builder.html#setChronometerCountDown(boolean)
    public Notification createNotification(Context context, CurrentSession currentSession) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, new Intent(context, TimerActivity.class), 0);

        Intent intent = new Intent(context, TimerService.class);
        intent.setAction(Constants.ACTION.STOP_TIMER);
        PendingIntent stopIntent = PendingIntent.getService(context,
                0, intent, 0);

        //TODO: create functions to create actions for stop, pause and resume
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_launcher_foreground,
                "STOP",
                stopIntent).build();

        if (mBuilder.mActions.isEmpty()) {
            mBuilder.addAction(stopAction);
            mBuilder.addAction(stopAction);
        } else {
            mBuilder.mActions.set(0, stopAction);
            mBuilder.mActions.set(0, stopAction);
        }

        return mBuilder
                .setContentTitle("Work session in progress")
                .setContentText("x minutes remaining")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1))
                .setOngoing(true)
                .setShowWhen(false).build();
    }

    public void notifyFinished(Context context) {

        //TODO: notify finished and stop service
        //needs separate notificationID because of continuous mode
        // need to find a way to cancel the notification sound in cont mode
    }
}
