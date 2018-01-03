package com.apps.adrcotfas.goodtime.BL;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.apps.adrcotfas.goodtime.Util.Constants;

import io.reactivex.functions.Consumer;

public class TimerService extends Service {

    private static final String TAG = TimerService.class.getSimpleName();

    private CurrentSession mCurrentSession;
    private AppTimer mAppTimer;
    private AppNotificationManager mAppNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppNotificationManager = new AppNotificationManager(getApplicationContext());
        mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
        mAppTimer = new AppTimer(mCurrentSession);

        setupEvents();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        switch (intent.getAction()) {
            case Constants.ACTION.TOGGLE_TIMER:
                onToggleEvent();
                break;

            case Constants.ACTION.STOP_TIMER:
                onStopEvent();
                break;

            default:
                break;
        }
        return START_STICKY;
    }

    private void setupEvents() {
        GoodtimeApplication.getInstance().getBus().getEvents().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o instanceof Constants.FinishEvent) {
                    onFinishEvent();
                }
            }
        });
    }

    private void onToggleEvent() {
        mAppTimer.toggle();
        startForeground(Constants.NOTIFICATION_ID,
                mAppNotificationManager.createNotification(getApplicationContext(), mCurrentSession));
    }

    private void onStopEvent() {
        mAppTimer.stop();
        stopForeground(true);
        stopSelf();

        // TODO: store what was done of the session to ROOM
    }

    private void onFinishEvent() {
        mAppNotificationManager.notifyFinished(mCurrentSession);

        // TODO: update notification
        // TODO: trigger dialog box
        // TODO: store session to ROOM

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
