package com.apps.adrcotfas.goodtimeplus.Model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.apps.adrcotfas.goodtimeplus.Util.Constants;
import com.apps.adrcotfas.goodtimeplus.R;

public class TimerService extends Service{

    private static final String TAG = TimerService.class.getSimpleName();

    CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case Constants.ACTION.START_TIMER:

                new AppTimer(mCurrentSession).start();

                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(notificationChannel);
                    builder = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
                } else {
                    builder = new NotificationCompat.Builder(getApplicationContext());
                }

                Notification notification = builder
                        .setContentTitle("Goodtime")
                        .setContentText("Ceva fin")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .build();

                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                        notification);
            break;

            case Constants.ACTION.STOP_TIMER:
                Log.i(TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();
            break;

            default:
            break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
