package com.apps.adrcotfas.goodtime;

import android.app.Service;
import android.content.Intent;
import java.util.Timer;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import static android.media.AudioManager.RINGER_MODE_SILENT;

public class TimerService extends Service {

    private static final String TAG = "TimerService";
    public final static String ACTION_TIMERSERVICE = "com.apps.adrcotfas.goodtime.TIMERSERVICE";
    public final static String REMAINING_TIME = "com.apps.adrcotfas.goodtime.REMAINING_TIME";
    private int mRemainingTime;
    private int mCurrentSessionStreak;
    private Timer mTimer;
    private TimerState mTimerState;
    private final IBinder mBinder = new TimerBinder();
    private LocalBroadcastManager mBroadcastManager;
    private int mPreviousRingerMode;
    private boolean mPreviousWifiMode;

    @Override
    public void onCreate() {
        super.onCreate();
        saveCurrentStateOfSound();
        saveCurrentStateOfWifi();
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    public void scheduleTimer(long delay){

        mTimer = new Timer();
        mTimer.schedule(new UpdateTask(new Handler(), TimerService.this), delay , 1000);
    }

    public void runTimer() {
        Log.d(TAG, "Updating timer");

        if (mTimerState != TimerState.INACTIVE) {
            if (mRemainingTime != 0) {
                --mRemainingTime;
            }
            Intent remainingTimeIntent = new Intent(ACTION_TIMERSERVICE);
            remainingTimeIntent.putExtra(REMAINING_TIME, getRemainingTime());
            mBroadcastManager.sendBroadcast(remainingTimeIntent);
        }
    }

    public void removeTimer() {
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

    public int getPreviousRingerMode() {
        return mPreviousRingerMode;
    }

    public boolean getPreviousWifiMode() {
        return mPreviousWifiMode;
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

    protected void setPreviousRingerMode(int previousRingerMode) {
        this.mPreviousRingerMode = previousRingerMode;
    }

    protected void setPreviousWifiMode(boolean previousWifiMode) {
        this.mPreviousWifiMode = previousWifiMode;
    }

    protected void restoreSound() {
        Log.d(TAG, "Restoring sound mode");
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        aManager.setRingerMode(mPreviousRingerMode);
    }

    protected void restoreWifi() {
        Log.d(TAG, "Restoring Wifi mode");
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(mPreviousWifiMode);
    }

    protected void disableSound() {
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        aManager.setRingerMode(RINGER_MODE_SILENT);
    }

    protected void disableWifi() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }

    private void saveCurrentStateOfSound() {
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mPreviousRingerMode = aManager.getRingerMode();
    }
    private void saveCurrentStateOfWifi() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        mPreviousWifiMode = wifiManager.isWifiEnabled();
    }
}
