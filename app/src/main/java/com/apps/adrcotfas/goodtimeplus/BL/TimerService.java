package com.apps.adrcotfas.goodtimeplus.BL;

import android.app.Notification;
import android.app.Service;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apps.adrcotfas.goodtimeplus.Util.Constants;

public class TimerService extends Service {

    private static final String TAG = TimerService.class.getSimpleName();

    private CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
    private AppTimer mAppTimer = new AppTimer(mCurrentSession);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case Constants.ACTION.TOGGLE_TIMER:

                mAppTimer.toggle();
                Notification notification = AppNotificationManager.createNotification(getApplicationContext(), mCurrentSession);
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                        notification);
                break;

            case Constants.ACTION.STOP_TIMER:
                mAppTimer.stop();
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
