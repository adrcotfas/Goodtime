package com.apps.adrcotfas.goodtime.BL;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.apps.adrcotfas.goodtime.Util.Constants;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

/**
 * Class representing the foreground service which triggers the countdown timer and handles events.
 */
public class TimerService extends Service {

    private static final String TAG = TimerService.class.getSimpleName();

    private CurrentSession mCurrentSession;
    private CurrentSessionManager mCurrentSessionManager;
    private NotificationManager mAppNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppNotificationManager = new NotificationManager(getApplicationContext());
        mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
        mCurrentSessionManager = new CurrentSessionManager(mCurrentSession);

        setupEvents();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        switch (intent.getAction()) {
            case Constants.ACTION.TOGGLE_TIMER:
            case Constants.ACTION.SKIP_BREAK:
                onToggleEvent();
                break;

            case Constants.ACTION.STOP_TIMER:
                onStopEvent();
                break;
            case Constants.ACTION.START_WORK:
                // TODO: update CurrentSession to work
                onToggleEvent();
                break;
            case Constants.ACTION.START_BREAK:
                // TODO: update CurrentSession to break
                onToggleEvent();
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
                } else if (o instanceof Constants.UpdateTimerProgressEvent) {
                    updateNotificationProgress();
                }
            }
        });
    }

    private void updateNotificationProgress() {
        mAppNotificationManager.updateNotificationProgress(mCurrentSession.getDuration().getValue());
    }

    private void onToggleEvent() {
        mCurrentSessionManager.toggleTimer();
        startForeground(Constants.NOTIFICATION_ID,
                mAppNotificationManager.createNotification(getApplicationContext(), mCurrentSession));
    }

    private void onStopEvent() {
        mCurrentSessionManager.stopTimer();
        stopForeground(true);
        stopSelf();

        // TODO: store what was done of the session to ROOM
    }

    private void onFinishEvent() {
        mAppNotificationManager.notifyFinished(getApplicationContext(), mCurrentSession);

        // TODO: update notification
        // TODO: trigger dialog box
        // TODO: store session to ROOM

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
