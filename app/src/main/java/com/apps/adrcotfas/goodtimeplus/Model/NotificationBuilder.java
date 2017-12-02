package com.apps.adrcotfas.goodtimeplus.Model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.apps.adrcotfas.goodtimeplus.R;

public class NotificationBuilder {

    public static Notification createNotification(Context context, CurrentSession currentSession) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(context, notificationChannel.getId());
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        return builder
                .setContentTitle("Goodtime")
                .setContentText("Ceva fin")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }
}
