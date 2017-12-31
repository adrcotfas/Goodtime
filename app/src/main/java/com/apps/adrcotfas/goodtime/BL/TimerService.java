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

                mAppTimer.toggle();
                startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                        mAppNotificationManager.createNotification(getApplicationContext(), mCurrentSession));
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

    private void setupEvents() {
        GoodtimeApplication.getInstance().getBus().getEvents().subscribe(new Consumer<Object>() {

            @Override
            public void accept(Object o) throws Exception {
                if (o instanceof Constants.FinishWorkEvent) {
                    onFinishWorkEvent();
                }
            }
        });
    }

    private void onFinishWorkEvent() {
        //TODO: notify finish work
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
