package com.apps.adrcotfas.goodtime;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.graphics.Color.WHITE;
import static android.media.AudioAttributes.USAGE_ALARM;
import static com.apps.adrcotfas.goodtime.MainActivity.NOTIFICATION_TAG;
import static com.apps.adrcotfas.goodtime.Preferences.PREFERENCES_NAME;

public class TimerService extends Service {

    private static final int GOODTIME_NOTIFICATION_ID = 1;
    private static final String TAG = "TimerService";
    public final static String ACTION_TIMERSERVICE = "com.apps.adrcotfas.goodtime.TIMERSERVICE";
    public final static String REMAINING_TIME = "com.apps.adrcotfas.goodtime.REMAINING_TIME";
    private int mRemainingTime;
    private int mCurrentSessionStreak;
    private Timer mTimer;
    private TimerState mTimerState;
    private TimerState mTimerBroughtToForegroundState;
    private final IBinder mBinder = new TimerBinder();
    private LocalBroadcastManager mBroadcastManager;
    private int mPreviousRingerMode;
    private boolean mPreviousWifiMode;
    private boolean mIsOnForeground;
    private Preferences mPref;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
        mPref = new Preferences(preferences);

        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public void scheduleTimer(long delay){
        saveCurrentStateOfSound();
        saveCurrentStateOfWifi();

        mTimer = new Timer();
        mTimer.schedule(new UpdateTask(new Handler(), TimerService.this), delay , 1000);
    }

    private void saveCurrentStateOfSound() {
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mPreviousRingerMode = aManager.getRingerMode();
    }

    private void saveCurrentStateOfWifi() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        mPreviousWifiMode = wifiManager.isWifiEnabled();
    }


    public void runTimer() {
        if (mTimerState != TimerState.INACTIVE) {
            if (mRemainingTime > 0) {
                mRemainingTime--;
            }

            if (mRemainingTime == 0) {
                onCountdownFinished();
            }

            sendUpdateIntent();

            if (mIsOnForeground && mTimerBroughtToForegroundState != mTimerState) {
                bringToForegroundAndUpdateNotification();
            }
        }
    }

    private void onCountdownFinished() {
        Log.d(TAG, "Countdown finished, restoring sound and WiFi and sending a notification");

        restoreSoundIfPreferred();
        restoreWifiIfPreferred();
        sendFinishedNotification();
    }

    private void restoreSoundIfPreferred() {
        if (mPref.getDisableSoundAndVibration()) {
            Log.d(TAG, "Restoring sound mode");
            AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            aManager.setRingerMode(mPreviousRingerMode);
        }
    }

    private void restoreWifiIfPreferred() {
        if (mPref.getDisableWifi()) {
            Log.d(TAG, "Restoring Wifi mode");
            WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
            wifiManager.setWifiEnabled(mPreviousWifiMode);
        }
    }

    private void sendFinishedNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        String notificationSound = mPref.getNotificationSound();
        if (!notificationSound.equals("")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setSound(Uri.parse(notificationSound), USAGE_ALARM);
            } else {
                mBuilder.setSound(Uri.parse(notificationSound));
            }
        }
        mBuilder.setSmallIcon(R.drawable.ic_status_goodtime)
                .setVibrate(new long[]{0, 300, 700, 300})
                .setLights(WHITE, 250, 750)
                .setContentTitle(getString(R.string.dialog_session_message))
                .setContentText(buildNotificationText())
                .setContentIntent(
                        getActivity(
                                this,
                                0,
                                new Intent(getApplicationContext(), MainActivity.class),
                                FLAG_ONE_SHOT
                        ))
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_TAG, mBuilder.build());
    }

    private CharSequence buildNotificationText() {
        CharSequence contextText = getString(R.string.notification_session);
        switch (mTimerState) {
            case ACTIVE_WORK:
                break;
            case ACTIVE_BREAK:
                contextText = getString(R.string.notification_break);
                break;
            case PAUSED_WORK:
                contextText = getString(R.string.notification_pause);
                break;
            case FINISHED_WORK:
                contextText = getString(R.string.notification_work_complete);
                break;
            case FINISHED_BREAK:
                contextText = getString(R.string.notification_break_complete);
                break;
        }
        return contextText;
    }

    private void sendUpdateIntent() {
        Intent remainingTimeIntent = new Intent(ACTION_TIMERSERVICE);
        remainingTimeIntent.putExtra(REMAINING_TIME, getRemainingTime());
        mBroadcastManager.sendBroadcast(remainingTimeIntent);
    }


    public void removeTimer() {
        if (mIsOnForeground) {
            bringToForegroundAndUpdateNotification();
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    public class TimerBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public TimerState getTimerState() {
        return mTimerState;
    }

    public int getRemainingTime() {
        return mRemainingTime;
    }

    public int getCurrentSessionStreak() {
        return mCurrentSessionStreak;
    }

    protected void setTimerState(TimerState mTimerState) {
        this.mTimerState = mTimerState;
    }

    protected void setCurrentSessionStreak(int mCurrentSessionStreak) {
        this.mCurrentSessionStreak = mCurrentSessionStreak;
    }

    protected void setRemainingTime(int mRemainingTime) {
        this.mRemainingTime = mRemainingTime;
    }

    protected void bringToForegroundAndUpdateNotification() {
        mIsOnForeground = true;
        mTimerBroughtToForegroundState = mTimerState;
        startForeground(GOODTIME_NOTIFICATION_ID, createForegroundNotification());
    }

    protected void sendToBackground() {
        mIsOnForeground = false;
        stopForeground(true);
    }

    private Notification createForegroundNotification() {
        return new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setAutoCancel(false)
                .setContentTitle("Goodtime")
                .setContentText(buildNotificationText())
                .setOngoing(isNotificationOngoing())
                .setShowWhen(false)
                .setContentIntent(
                        getActivity(
                                this,
                                0,
                                new Intent(getApplicationContext(), MainActivity.class),
                                FLAG_UPDATE_CURRENT
                        ))
                .build();
    }

    private boolean isNotificationOngoing() {
        switch (mTimerState) {
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
