package com.apps.adrcotfas.goodtime.BL;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.apps.adrcotfas.goodtime.Util.Constants;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Class representing the foreground service which triggers the countdown timer and handles events.
 */
public class TimerService extends Service {

    private static final String TAG = TimerService.class.getSimpleName();

    private CurrentSessionManager mSessionManager;
    private NotificationHelper mNotificationHelper;
    private Disposable mEventSubscription;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v(TAG, "onCreate");
        mNotificationHelper = new NotificationHelper(getApplicationContext());
        mSessionManager = GoodtimeApplication.getInstance().getCurrentSessionManager();

        setupEvents();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        if (mEventSubscription != null && !mEventSubscription.isDisposed()) {
            mEventSubscription.dispose();
        }
        super.onDestroy();
    }

    @Override
    public synchronized int onStartCommand(final Intent intent, int flags, int startId) {

        switch (intent.getAction()) {
            case Constants.ACTION.START_WORK:
            case Constants.ACTION.SKIP_BREAK:
                onStartEvent(SessionType.WORK);
                break;
            case Constants.ACTION.STOP:
                onStopEvent();
                break;
            case Constants.ACTION.TOGGLE:
                onToggleEvent();
                break;
            case Constants.ACTION.START_BREAK:
                onStartEvent(SessionType.BREAK);
                break;
            case Constants.ACTION.SKIP_WORK:
                onSkipWorkEvent();
                break;
            default:
                break;
        }
        return START_STICKY;
    }

    private void setupEvents() {
        mEventSubscription = GoodtimeApplication.getInstance().getBus().getEvents().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o instanceof Constants.FinishWorkEvent) {
                    onFinishEvent(SessionType.WORK);
                } else if (o instanceof Constants.FinishBreakEvent) {
                    onFinishEvent(SessionType.BREAK);
                } else if (o instanceof Constants.UpdateTimerProgressEvent) {
                    updateNotificationProgress();
                } else if (o instanceof Constants.ClearNotificationEvent) {
                    mNotificationHelper.clearNotification();
                }
            }
        });
    }

    private void onStartEvent(SessionType sessionType) {
        GoodtimeApplication.getInstance().getBus().send(new Constants.ClearFinishDialogEvent());
        mSessionManager.startTimer(sessionType);
        startForeground(Constants.NOTIFICATION_ID, mNotificationHelper.createNotification(
                getApplicationContext(), mSessionManager.getCurrentSession()));
    }

    private void onToggleEvent() {
        mSessionManager.toggleTimer();
        startForeground(Constants.NOTIFICATION_ID, mNotificationHelper.createNotification(
                getApplicationContext(), mSessionManager.getCurrentSession()));
    }

    private void onStopEvent() {
        mSessionManager.stopTimer();
        Log.v(TAG, "onStopEvent");
        stopForeground(true);
        stopSelf();
        // TODO: store what was done of the session to ROOM
    }

    private void onFinishEvent(SessionType sessionType) {
        Log.v(TAG, "onFinishEvent");
        // TODO: store session to ROOM
        // TODO: in continuous mode, do not stop the service
        stopForeground(true);
        stopSelf();
        mNotificationHelper.notifyFinished(getApplicationContext(), sessionType);
    }

    private void onSkipWorkEvent() {
        // TODO: store what was done of the session to ROOM
        onStartEvent(SessionType.BREAK);
    }

    private void updateNotificationProgress() {
        mNotificationHelper.updateNotificationProgress(
                mSessionManager.getCurrentSession().getDuration().getValue());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
