package com.apps.adrcotfas.goodtime;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.about.AboutActivity;
import com.apps.adrcotfas.goodtime.settings.CustomNotification;
import com.apps.adrcotfas.goodtime.settings.SettingsActivity;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import im.delight.apprater.AppRater;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int GOODTIME_NOTIFICATION_ID = 1;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    PowerManager.WakeLock mWakeLock;
    private long mBackPressed;
    private int mSessionTime;
    private int mBreakTime;
    private int mRemainingTime;
    private int mLongBreakTime;
    private int mCompletedSessions;
    private int mSessionsBeforeLongBreak;
    private Timer mTimer;
    private TimerState mTimerState;
    private FloatingActionButton mStartButton;
    private Button mPauseButton;
    private Button mStopButton;
    private TextView mTimeLabel;
    private View mHorizontalSeparator;
    private NotificationManager mNotificationManager;
    private SharedPreferences mPref;
    private SharedPreferences mPrivatePref;
    private AlertDialog mAlertDialog;
    private int mRingerMode;
    private boolean mWifiMode;
    private boolean mDisableSoundAndVibration;
    private boolean mDisableWifi;
    private boolean mKeepScreenOn;
    private boolean mContinuousMode;

    @Override
    protected void onResume() {
        super.onResume();
        if (mPrivatePref.getBoolean("pref_firstRun", true)) {
            Intent introIntent = new Intent(this, ProductTourActivity.class);
            startActivity(introIntent);
            mPrivatePref.edit().putBoolean("pref_firstRun", false).apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);


        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPref.registerOnSharedPreferenceChangeListener(this);

        resetPreferencesIfNeeded();
        installCustomRingtones();

        mPrivatePref = getSharedPreferences("preferences_private", Context.MODE_PRIVATE);
        mPrivatePref.registerOnSharedPreferenceChangeListener(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        Button sessionCounterButton = (Button) findViewById(R.id.totalSessionsButton);
        if (sessionCounterButton != null) {
            sessionCounterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSessionCounterDialog();
                }
            });
        }
        if (sessionCounterButton != null) {
            sessionCounterButton.setText(String.valueOf(mPrivatePref.getInt("pref_totalSessions", 0)));
        }

        Typeface robotoThin = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
        final RelativeLayout buttons = (RelativeLayout) findViewById(R.id.buttons);
        mTimeLabel = (TextView) findViewById(R.id.textView);
        if (mTimeLabel != null) {
            mTimeLabel.setTypeface(robotoThin);
        }
        mStartButton = (FloatingActionButton) findViewById(R.id.startButton);
        mPauseButton = (Button) findViewById(R.id.pauseButton);
        mStopButton = (Button) findViewById(R.id.stopButton);
        mHorizontalSeparator = findViewById(R.id.horizontalSeparator);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartButton.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.implode));
                if (buttons != null) {
                    buttons.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade));
                }
                mRemainingTime = mSessionTime * 60;
                mTimerState = TimerState.ACTIVE_WORK;
                mPauseButton.setEnabled(true);
                mPauseButton.setTextColor(getResources().getColor(R.color.yellow));
                startTimer(300);
                mStartButton.setEnabled(false); // avoid double-click
                mStartButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStartButton.setEnabled(true);
                    }
                }, 300);
            }
        });
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTimer();
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation fadeReversed = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_reverse);
                if (buttons != null) {
                    buttons.startAnimation(fadeReversed);
                }
                mPauseButton.clearAnimation();
                Animation implodeReversed = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.implode_reverse);
                mStartButton.startAnimation(implodeReversed);
                loadInitialState();
            }
        });

        mSessionTime = mPref.getInt("pref_workTime", 25);
        mBreakTime = mPref.getInt("pref_breakTime", 5);
        mLongBreakTime = mPref.getInt("pref_longBreakDuration", 15);
        mSessionsBeforeLongBreak = mPref.getInt("pref_sessionsBeforeLongBreak", 4);
        mDisableSoundAndVibration = mPref.getBoolean("pref_disableSoundAndVibration", false);
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mRingerMode = aManager.getRingerMode();

        mDisableWifi = mPref.getBoolean("pref_disableWifi", false);
        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        mWifiMode = wifiManager.isWifiEnabled();
        mKeepScreenOn = mPref.getBoolean("pref_keepScreenOn", false);
        mContinuousMode = mPref.getBoolean("pref_continuousMode", false);

        setupAppRater();
        if (savedInstanceState != null) {
            mTimerState = (TimerState) savedInstanceState.getSerializable("timerState");
            mRemainingTime = savedInstanceState.getInt("remainingTime");
            mSessionsBeforeLongBreak = savedInstanceState.getInt("sessionsBeforeLongBreak");
            mRingerMode = savedInstanceState.getInt("ringerMode");
            mWifiMode = savedInstanceState.getBoolean("wifiMode");
            mDisableSoundAndVibration = savedInstanceState.getBoolean("disableSoundAndVibration");
            mDisableWifi = savedInstanceState.getBoolean("disableWifi");
            mKeepScreenOn = savedInstanceState.getBoolean("keepScreenOn");
            mContinuousMode = savedInstanceState.getBoolean("continuousMode");

            switch (mTimerState) {
                case ACTIVE_WORK:
                    mPauseButton.setEnabled(true);
                    mPauseButton.setTextColor(getResources().getColor(R.color.yellow));
                    startTimer(0);
                    break;
                case ACTIVE_BREAK:
                    mPauseButton.setEnabled(false);
                    mPauseButton.setTextColor(getResources().getColor(R.color.gray));
                    startTimer(0);
                    break;
                case PAUSED_WORK:
                    mTimerState = TimerState.ACTIVE_WORK;
                    loadRunningTimerUIState();
                    pauseTimer();
                    break;
                case INACTIVE:
                    loadInitialState();
                    break;
                case FINISHED_BREAK:
                    mTimerState = TimerState.ACTIVE_BREAK;
                    loadRunningTimerUIState();
                    showDialog();
                    break;
                case FINISHED_WORK:
                    mTimerState = TimerState.ACTIVE_WORK;
                    loadRunningTimerUIState();
                    showDialog();
                    break;
            }
        } else {
            loadInitialState();
        }
    }

    // Copies custom ringtones to the device storage
    private void installCustomRingtones() {
        // Add the custom alarm tones to the phone's storage, if they weren't copied yet.
        // Works on a separate thread.
        if (!mPref.getBoolean(CustomNotification.PREF_KEY_RINGTONES_COPIED, false))
            CustomNotification.installToStorage(this);
    }

    // This function is needed to avoid crashes when updating to a newer version
    // which contains different types of Preferences
    private void resetPreferencesIfNeeded() {
        String string = "invalid";
        try {
            string = mPref.getString("pref_workTime", "invalid");
        } catch (Throwable throwable) {

        }
        if (!string.equals("invalid")) {
            mPref.edit().clear().commit();
        }

    }

    @Override
    protected void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        if (mWakeLock != null) {
            try {
                mWakeLock.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        mSessionTime = mPref.getInt("pref_workTime", 25);
        mBreakTime = mPref.getInt("pref_breakTime", 5);
        mLongBreakTime = mPref.getInt("pref_longBreakDuration", 15);
        mSessionsBeforeLongBreak = mPref.getInt("pref_sessionsBeforeLongBreak", 4);
        Button button = (Button) findViewById(R.id.totalSessionsButton);
        if (button != null) {
            button.setText(String.valueOf(mPrivatePref.getInt("pref_totalSessions", 0)));
        }
        mDisableSoundAndVibration = mPref.getBoolean("pref_disableSoundAndVibration", false);
        mDisableWifi = mPref.getBoolean("pref_disableWifi", false);
        mKeepScreenOn = mPref.getBoolean("pref_keepScreenOn", false);
        mContinuousMode = mPref.getBoolean("pref_continuousMode", false);

        if (mTimerState != null) {
            switch (mTimerState) {
                case INACTIVE:
                    String currentTick = String.format(Locale.US, "%d.00", mSessionTime);
                    SpannableString currentFormattedTick = new SpannableString(currentTick);
                    currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0, currentTick.indexOf("."), 0);
                    if (mTimeLabel != null)
                        mTimeLabel.setText(currentFormattedTick);
                    break;
                case ACTIVE_WORK:
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("timerState", mTimerState);
        outState.putInt("remainingTime", mRemainingTime);
        outState.putInt("sessionsBeforeLongBreak", mSessionsBeforeLongBreak);
        outState.putBoolean("disableSoundAndVibration", mDisableSoundAndVibration);
        outState.putInt("ringerMode", mRingerMode);
        outState.putBoolean("disableWifi", mDisableWifi);
        outState.putBoolean("wifiMode", mWifiMode);
        outState.putBoolean("keepScreenOn", mKeepScreenOn);
        outState.putBoolean("continuousMode", mContinuousMode);
    }

    @Override
    public void onBackPressed() {
        if (mTimerState != TimerState.INACTIVE) {
            /// move app to background
            moveTaskToBack(true);
        }
        else {
            Toast exitToast = Toast.makeText(getBaseContext(), "Press the back button again to exit", Toast.LENGTH_SHORT);
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                exitToast.cancel();
                super.onBackPressed();
                return;
            } else {
                try {
                    exitToast.show();
                } catch (Throwable th) {
                    // ignoring this exception
                }
            }
            mBackPressed = System.currentTimeMillis();
        }
    }

    public void loadInitialState() {
        mTimerState = TimerState.INACTIVE;
        mRemainingTime = mSessionTime * 60;
        String currentTick = String.format(Locale.US, "%d.%02d",
                mRemainingTime / 60,
                mRemainingTime % 60);
        SpannableString currentFormattedTick = new SpannableString(currentTick);
        currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0, currentTick.indexOf("."), 0);
        mTimeLabel.setText(currentFormattedTick);
        mTimeLabel.setTextColor(getResources().getColor(R.color.lightGray));

        mStartButton.setVisibility(View.VISIBLE);
        mPauseButton.setVisibility(View.INVISIBLE);
        mPauseButton.setText(getString(R.string.pause));
        mStopButton.setVisibility(View.INVISIBLE);
        mHorizontalSeparator.setVisibility(View.INVISIBLE);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();

        if (mWakeLock != null) {
            try {
                mWakeLock.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
        }
        if (mKeepScreenOn) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mDisableSoundAndVibration) {
            AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            aManager.setRingerMode(mRingerMode);
        }
        if (mDisableWifi) {
            WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
            wifiManager.setWifiEnabled(mWifiMode);
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void loadRunningTimerUIState() {
        String currentTick;
        SpannableString currentFormattedTick;
        if (mRemainingTime >= 60) {
            currentTick = String.format(Locale.US, "%d.%02d",
                    mRemainingTime / 60,
                    mRemainingTime % 60);
            currentFormattedTick = new SpannableString(currentTick);
            currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0, currentTick.indexOf("."), 0);
        } else {
            currentTick = String.format(Locale.US, " .%02d",
                    mRemainingTime % 60);
            currentFormattedTick = new SpannableString(currentTick);
            currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0, currentTick.indexOf("."), 0);
        }

        mTimeLabel.setText(currentFormattedTick);
        mStartButton.setVisibility(View.INVISIBLE);
        mPauseButton.setVisibility(View.VISIBLE);
        mStopButton.setVisibility(View.VISIBLE);
        mHorizontalSeparator.setVisibility(View.VISIBLE);
    }

    public void startTimer(long delay) {

        mTimeLabel.setTextColor(Color.WHITE);
        switch (mTimerState) {
            case ACTIVE_WORK:
                if (mDisableSoundAndVibration) {
                    AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }

                if (mDisableWifi) {
                    WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);
                }
                createNotification("Work session in progress.", true);
                break;
            case ACTIVE_BREAK:
                createNotification("Break session in progress.", true);
        }
        loadRunningTimerUIState();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (mKeepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "starting partial wake lock");
        mWakeLock.acquire();

        mTimer = new Timer();
        mTimer.schedule(new UpdateTask(new Handler(), MainActivity.this), delay, 1000);
    }

    public void pauseTimer() {

        mTimeLabel.setTextColor(getResources().getColor(R.color.lightGray));
        long timeOfButtonPress = System.currentTimeMillis();
        if (mWakeLock != null) {
            try {
                mWakeLock.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
        }
        switch (mTimerState) {
            case ACTIVE_WORK:
                mTimerState = TimerState.PAUSED_WORK;
                mPauseButton.setText(getString(R.string.resume));
                mPauseButton.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink));
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                }
                createNotification("Work session is paused. Resume?", false);
                break;
            case PAUSED_WORK:
                mTimerState = TimerState.ACTIVE_WORK;
                mPauseButton.setText(getString(R.string.pause));
                mPauseButton.clearAnimation();
                startTimer(System.currentTimeMillis() - timeOfButtonPress > 1000 ? 0 : 1000 - (System.currentTimeMillis() - timeOfButtonPress));
                break;
        }
    }

    public void onCountdownFinished() {

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock screenWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "wake screen lock");

        screenWakeLock.acquire();
        screenWakeLock.release();

        if (mWakeLock != null)
            mWakeLock.release();

        if (mKeepScreenOn) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mDisableSoundAndVibration) {
            AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            aManager.setRingerMode(mRingerMode);
        }
        if (mDisableWifi) {
            WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
            wifiManager.setWifiEnabled(mWifiMode);
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        if (mTimerState == TimerState.ACTIVE_WORK) {
            ++mCompletedSessions;
            int totalSessions = mPrivatePref.getInt("pref_totalSessions", 0);
            mPrivatePref.edit().putInt("pref_totalSessions", ++totalSessions).apply();
        }
        if (mPref.getBoolean("pref_vibrate", true)) {
            final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 300, 700, 300};
            vibrator.vibrate(pattern, -1);
        }

        String notificationSound = mPref.getString("pref_notificationSound", "");
        if (!notificationSound.equals("")) {
            Uri uri = Uri.parse(notificationSound);
            Ringtone r = RingtoneManager.getRingtone(this, uri);
            r.play();
        }
        bringApplicationToFront();
        if (mContinuousMode) {
            goOnContinuousMode();
        }
        else
            showDialog();
    }

    public void showDialog() {
        wakeScreen();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        switch (mTimerState) {
            case ACTIVE_WORK:
            case FINISHED_WORK:
                loadInitialState();
                mTimerState = TimerState.FINISHED_WORK;
                alertDialogBuilder.setTitle("Session complete");
                alertDialogBuilder.setPositiveButton("Start break", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPauseButton.setEnabled(false);
                        mPauseButton.setTextColor(getResources().getColor(R.color.gray));
                        mRemainingTime = (mCompletedSessions >= mSessionsBeforeLongBreak) ? mLongBreakTime * 60 : mBreakTime * 60;
                        mTimerState = TimerState.ACTIVE_BREAK;
                        startTimer(0);
                    }
                });
                alertDialogBuilder.setNegativeButton("Skip break", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRemainingTime = mSessionTime * 60;
                        mTimerState = TimerState.ACTIVE_WORK;
                        startTimer(0);
                    }
                });
                alertDialogBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mNotificationManager != null)
                            mNotificationManager.cancelAll();
                    }
                });
                mAlertDialog = alertDialogBuilder.create();
                mAlertDialog.setCanceledOnTouchOutside(false);
                mAlertDialog.show();
                createNotification("Session complete. Continue?", false);
                break;
            case ACTIVE_BREAK:
            case FINISHED_BREAK:
                loadInitialState();
                mPauseButton.setEnabled(true);
                mPauseButton.setTextColor(getResources().getColor(R.color.yellow));
                mTimerState = TimerState.FINISHED_BREAK;
                if (mCompletedSessions >= mSessionsBeforeLongBreak)
                    mCompletedSessions = 0;
                alertDialogBuilder.setTitle("Break complete");
                alertDialogBuilder.setPositiveButton("Begin session", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRemainingTime = mSessionTime * 60;
                        mTimerState = TimerState.ACTIVE_WORK;
                        startTimer(0);
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mNotificationManager != null)
                            mNotificationManager.cancelAll();
                    }
                });
                mAlertDialog = alertDialogBuilder.create();
                mAlertDialog.setCanceledOnTouchOutside(false);
                mAlertDialog.show();
                createNotification("Break complete. Resume work?", false);
                break;
            default:
                mTimerState = TimerState.INACTIVE;
                break;
        }
    }

    private void wakeScreen() {
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK), "TAG");
        wakeLock.acquire();
        wakeLock.release();
    }

    private void goOnContinuousMode() {
        switch (mTimerState) {
            case ACTIVE_WORK:
            case FINISHED_WORK:
                loadInitialState();
                mTimerState = TimerState.FINISHED_WORK;
                mPauseButton.setEnabled(false);
                mPauseButton.setTextColor(getResources().getColor(R.color.gray));
                mRemainingTime = (mCompletedSessions >= mSessionsBeforeLongBreak) ? mLongBreakTime * 60 : mBreakTime * 60;
                mTimerState = TimerState.ACTIVE_BREAK;
                startTimer(0);
            break;
            case ACTIVE_BREAK:
            case FINISHED_BREAK:
                loadInitialState();
                mPauseButton.setEnabled(true);
                mPauseButton.setTextColor(getResources().getColor(R.color.yellow));
                mTimerState = TimerState.FINISHED_BREAK;
                if (mCompletedSessions >= mSessionsBeforeLongBreak)
                    mCompletedSessions = 0;

                    mRemainingTime = mSessionTime * 60;
                    mTimerState = TimerState.ACTIVE_WORK;
                    startTimer(0);
                break;
            default:
                mTimerState = TimerState.INACTIVE;
                break;
        }
    }

    public void runTimer() {
        if (mTimerState != TimerState.INACTIVE) {
            if (mRemainingTime == 0) {
                onCountdownFinished();
            } else
                --mRemainingTime;

            String currentTick;
            SpannableString currentFormattedTick;
            if (mRemainingTime >= 60) {
                currentTick = String.format(Locale.US, "%d." + (mRemainingTime % 60 < 10 ? "0%d" : "%d"),
                        mRemainingTime / 60,
                        mRemainingTime % 60);
                currentFormattedTick = new SpannableString(currentTick);
                currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0, currentTick.indexOf("."), 0);
            } else {
                currentTick = String.format(Locale.US, " ." + (mRemainingTime % 60 < 10 ? "0%d" : "%d"),
                        mRemainingTime % 60);
                currentFormattedTick = new SpannableString(currentTick);
                currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0, currentTick.indexOf("."), 0);
            }

            mTimeLabel.setText(currentFormattedTick);
        }
    }

    private void createNotification(CharSequence contentText, boolean ongoing) {

        Notification.Builder notificationBuilder = new Notification.Builder(
                getApplicationContext())
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setAutoCancel(false)
                .setContentTitle("Goodtime")
                .setContentText(contentText)
                .setOngoing(ongoing)
                .setShowWhen(false);
        notificationBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(GOODTIME_NOTIFICATION_ID, notificationBuilder.build());
    }

    private void bringApplicationToFront() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    void showSessionCounterDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Reset sessions counter?");
        alertDialogBuilder.setMessage("The completed sessions counter will be reset.");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCompletedSessions = 0;
                mPrivatePref.edit().putInt("pref_totalSessions", 0).apply();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.show();
    }

    void setupAppRater() {
        AppRater appRater = new AppRater(this);
        appRater.setPhrases("Rate this app", "If you found this app useful please rate it on Google Play. Thanks for your support!", "Rate now", "Later", "No, thanks");
        appRater.show();
    }

    private enum TimerState {INACTIVE, ACTIVE_WORK, PAUSED_WORK, ACTIVE_BREAK, FINISHED_WORK, FINISHED_BREAK}

    private class UpdateTask extends TimerTask {
        final Handler handler;
        final MainActivity ref;

        public UpdateTask(Handler handler, MainActivity ref) {
            super();
            this.handler = handler;
            this.ref = ref;
        }

        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ref.runTimer();
                }
            });
        }
    }
}
