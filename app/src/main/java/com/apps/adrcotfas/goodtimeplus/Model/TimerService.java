package com.apps.adrcotfas.goodtimeplus.Model;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.apps.adrcotfas.goodtimeplus.Util.Constants;

public class TimerService extends Service{

    private static final String TAG = TimerService.class.getSimpleName();

    private CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
    private AppTimer mAppTimer = new AppTimer(mCurrentSession);
    private Notification mNotification;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case Constants.ACTION.START_TIMER:

                mNotification = NotificationBuilder.createNotification(getApplicationContext(), mCurrentSession);
                mAppTimer.start();
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                        mNotification);
            break;

            case Constants.ACTION.STOP_TIMER:
                mAppTimer.toggle();
//                Log.v(TAG, "Stop timer");
//                mAppTimer.cancel();
//                stopForeground(true);
//                stopSelf();
            break;

            case Constants.ACTION.TOGGLE_TIMER:
                mAppTimer.toggle();

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
