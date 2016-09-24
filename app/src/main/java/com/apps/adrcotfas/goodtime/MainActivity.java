package com.apps.adrcotfas.goodtime;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.about.AboutActivity;
import com.apps.adrcotfas.goodtime.settings.CustomNotification;
import com.apps.adrcotfas.goodtime.settings.SettingsActivity;

import java.util.Locale;
import java.util.Timer;


import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.graphics.Typeface.createFromAsset;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.ON_AFTER_RELEASE;
import static android.os.PowerManager.PARTIAL_WAKE_LOCK;
import static android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.animation.AnimationUtils.loadAnimation;
import static android.widget.Toast.LENGTH_SHORT;
import static com.apps.adrcotfas.goodtime.Preferences.FIRST_RUN;
import static com.apps.adrcotfas.goodtime.Preferences.SESSION_DURATION;
import static com.apps.adrcotfas.goodtime.Preferences.TOTAL_SESSION_COUNT;
import static java.lang.String.format;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int GOODTIME_NOTIFICATION_ID = 1;
    private static final int MAXIMUM_MILLISECONDS_BETWEEN_BACK_PRESSES = 2000;

    private static final String TAG = "MainActivity";

    private PowerManager.WakeLock mWakeLock;
    private long mBackPressed;
    private int mRemainingTime;
    private int mCurrentSessionStreak;
    private Timer mTimer;
    private TimerState mTimerState;
    private FloatingActionButton mStartButton;
    private Button mPauseButton;
    private Button mStopButton;
    private TextView mTimeLabel;
    private View mHorizontalSeparator;
    private NotificationManager mNotificationManager;
    private Preferences mPref;
    private SharedPreferences mPrivatePref;
    private AlertDialog mAlertDialog;
    private int mPreviousRingerMode;
    private boolean mPreviousWifiMode;

    @Override
    protected void onResume() {
        super.onResume();
        if (mPrivatePref.getBoolean(FIRST_RUN, true)) {
            Intent introIntent = new Intent(this, ProductTourActivity.class);
            startActivity(introIntent);
            mPrivatePref.edit().putBoolean(FIRST_RUN, false).apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPref = setUpPreferences();
        installCustomRingtones();
        saveCurrentStateOfSoundAndWifi();
        setUpUi();
        setUpState(savedInstanceState);
        setUpAndroidNougatSettings();
    }

    private Preferences setUpPreferences() {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPref.registerOnSharedPreferenceChangeListener(this);
        mPrivatePref = getSharedPreferences("preferences_private", Context.MODE_PRIVATE);
        mPrivatePref.registerOnSharedPreferenceChangeListener(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        return new Preferences(mPref);
    }

    private void setUpAndroidNougatSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                mPref.setDisableSoundAndVibration(false);
            }
        }
    }

    private void installCustomRingtones() {
        if (!mPref.getRingtonesCopied()) {
            CustomNotification.installToStorage(this);
        }
    }

    private void saveCurrentStateOfSoundAndWifi() {
        AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mPreviousRingerMode = aManager.getRingerMode();

        WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
        mPreviousWifiMode = wifiManager.isWifiEnabled();
    }

    private void setUpUi() {
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setUpToolbar(toolbar);

        setUpSessionCounter();

        mStartButton = (FloatingActionButton) findViewById(R.id.startButton);
        setUpStartButton();

        mPauseButton = (Button) findViewById(R.id.pauseButton);
        setUpPauseButton();

        mStopButton = (Button) findViewById(R.id.stopButton);
        setUpStopButton();

        mHorizontalSeparator = findViewById(R.id.horizontalSeparator);

        mTimeLabel = (TextView) findViewById(R.id.textView);
        setUpTimerLabel();
    }

    private void setUpToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
    }

    private void setUpSessionCounter() {
        Button sessionCounterButton = (Button) findViewById(R.id.totalSessionsButton);
        if (sessionCounterButton != null) {
            sessionCounterButton.setText(String.valueOf(mPrivatePref.getInt(TOTAL_SESSION_COUNT, 0)));
            sessionCounterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSessionCounterDialog();
                }
            });
        }
    }

    private void setUpStartButton() {
        final RelativeLayout buttons = (RelativeLayout) findViewById(R.id.buttons);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartButton.startAnimation(loadAnimation(getApplicationContext(), R.anim.implode));
                if (buttons != null) {
                    buttons.startAnimation(loadAnimation(getApplicationContext(), R.anim.fade));
                }

                startSession(300);
                enablePauseButton();
                mStartButton.setEnabled(false);
                mStartButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStartButton.setEnabled(true);
                    }
                }, 300);
            }
        });
    }

    private void setUpPauseButton() {
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTimer();
            }
        });
    }

    private void setUpStopButton() {
        final RelativeLayout buttons = (RelativeLayout) findViewById(R.id.buttons);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPauseButton.clearAnimation();
                if (buttons != null) {
                    buttons.startAnimation(loadAnimation(getApplicationContext(), R.anim.fade_reverse));
                }

                mStartButton.startAnimation(loadAnimation(getApplicationContext(), R.anim.implode_reverse));

                loadInitialState();
            }
        });
    }

    private void setUpTimerLabel() {
        if (mTimeLabel != null) {
            mTimeLabel.setTypeface(createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf"));
        }
    }

    private void setUpState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            loadFromSaveState(savedInstanceState);
        } else {
            loadInitialState();
        }
    }

    private void loadFromSaveState(Bundle savedInstanceState) {
        mTimerState = (TimerState) savedInstanceState.getSerializable("timerState");
        mRemainingTime = savedInstanceState.getInt("remainingTime");
        mCurrentSessionStreak = savedInstanceState.getInt("currentSessionStreak");
        mPreviousRingerMode = savedInstanceState.getInt("ringerMode");
        mPreviousWifiMode = savedInstanceState.getBoolean("wifiMode");

        switch (mTimerState) {
            case ACTIVE_WORK:
                enablePauseButton();
                startTimer(0);
                break;
            case ACTIVE_BREAK:
                disablePauseButton();
                startTimer(0);
                break;
            case PAUSED_WORK:
                mTimerState = TimerState.ACTIVE_WORK;
                loadRunningTimerUiState();
                pauseTimer();
                break;
            case INACTIVE:
                loadInitialState();
                break;
            case FINISHED_BREAK:
                mTimerState = TimerState.ACTIVE_BREAK;
                loadRunningTimerUiState();
                showContinueDialog();
                break;
            case FINISHED_WORK:
                mTimerState = TimerState.ACTIVE_WORK;
                loadRunningTimerUiState();
                showContinueDialog();
                break;
        }
    }

    private void disablePauseButton() {
        mPauseButton.setEnabled(false);
        mPauseButton.setTextColor(getResources().getColor(R.color.gray));
    }

    @Override
    protected void onDestroy() {
        removeTimer();
        removeNotifications();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        releaseWakelock();
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "A preference has changed");

        if (key.equals(TOTAL_SESSION_COUNT)) {
            Button button = (Button) findViewById(R.id.totalSessionsButton);
            if (button != null) {
                button.setText(String.valueOf(mPrivatePref.getInt(TOTAL_SESSION_COUNT, 0)));
            }
        } else if (key.equals(SESSION_DURATION)) {
            if (mTimerState == TimerState.INACTIVE) {
                updateTimerLabel(mPref.getSessionDuration() * 60);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Saving instance state");

        super.onSaveInstanceState(outState);
        outState.putSerializable("timerState", mTimerState);
        outState.putInt("remainingTime", mRemainingTime);
        outState.putInt("currentSessionStreak", mCurrentSessionStreak);
        outState.putInt("ringerMode", mPreviousRingerMode);
        outState.putBoolean("wifiMode", mPreviousWifiMode);
    }

    @Override
    public void onBackPressed() {
        if (mTimerState != TimerState.INACTIVE) {
            /// move app to background
            moveTaskToBack(true);
        } else {
            if (mBackPressed + MAXIMUM_MILLISECONDS_BETWEEN_BACK_PRESSES > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                try {
                    Toast.makeText(getBaseContext(), R.string.toast_back_press, LENGTH_SHORT)
                         .show();
                } catch (Throwable th) {
                    // ignoring this exception
                }
            }
            mBackPressed = System.currentTimeMillis();
        }
    }

    private void loadInitialState() {
        Log.d(TAG, "Loading initial state");

        mTimerState = TimerState.INACTIVE;
        mRemainingTime = mPref.getSessionDuration() * 60;
        updateTimerLabel(mRemainingTime);
        mTimeLabel.setTextColor(getResources().getColor(R.color.lightGray));

        mStartButton.setVisibility(VISIBLE);
        mPauseButton.setVisibility(INVISIBLE);
        mPauseButton.setText(getString(R.string.pause));
        mStopButton.setVisibility(INVISIBLE);
        mHorizontalSeparator.setVisibility(INVISIBLE);
        removeTimer();

        removeNotifications();
        releaseWakelock();
        shutScreenOffIfPreferred();
        restoreSoundAndWifiIfPreferred();
    }

    private void removeTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    private void shutScreenOffIfPreferred() {
        if (mPref.getKeepScreenOn()) {
            getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
        }
    }

    private void releaseWakelock() {
        if (mWakeLock != null) {
            try {
                mWakeLock.release();
            } catch (Throwable th) {
                // ignoring this exception, probably wakeLock was already released
            }
        }
    }

    private void restoreSoundAndWifiIfPreferred() {
        if (mPref.getDisableSoundAndVibration()) {
            Log.d(TAG, "Restoring sound mode");
            AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            aManager.setRingerMode(mPreviousRingerMode);
        }
        if (mPref.getDisableWifi()) {
            Log.d(TAG, "Restoring Wifi mode");
            WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
            wifiManager.setWifiEnabled(mPreviousWifiMode);
        }
    }

    private void loadRunningTimerUiState() {
        updateTimerLabel(mRemainingTime);

        mStartButton.setVisibility(INVISIBLE);
        mPauseButton.setVisibility(VISIBLE);
        mStopButton.setVisibility(VISIBLE);
        mHorizontalSeparator.setVisibility(VISIBLE);
    }

    private void startTimer(long delay) {
        Log.i(TAG, "Timer has been started");

        mTimeLabel.setTextColor(Color.WHITE);
        loadRunningTimerUiState();

        switch (mTimerState) {
            case ACTIVE_WORK:
                disableSoundAndWifiIfPreferred();
                showNotification(getString(R.string.notification_session), true);
                break;
            case ACTIVE_BREAK:
                showNotification(getString(R.string.notification_break), true);
        }

        keepScreenOnIfPreferred();
        acquirePartialWakelock();

        mTimer = new Timer();
        mTimer.schedule(new UpdateTask(new Handler(), MainActivity.this), delay, 1000);
    }

    private void disableSoundAndWifiIfPreferred() {
        if (mPref.getDisableSoundAndVibration()) {
            AudioManager aManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            aManager.setRingerMode(RINGER_MODE_SILENT);
        }

        if (mPref.getDisableWifi()) {
            WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
            wifiManager.setWifiEnabled(false);
        }
    }

    private void keepScreenOnIfPreferred() {
        if (mPref.getKeepScreenOn()) {
            getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        }
    }

    private void acquirePartialWakelock() {
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                PARTIAL_WAKE_LOCK | ON_AFTER_RELEASE | ACQUIRE_CAUSES_WAKEUP,
                "starting partial wake lock"
        );
        mWakeLock.acquire();
    }

    private void pauseTimer() {
        Log.i(TAG, "Timer has been paused");

        mTimeLabel.setTextColor(getResources().getColor(R.color.lightGray));
        long timeOfButtonPress = System.currentTimeMillis();
        releaseWakelock();
        switch (mTimerState) {
            case ACTIVE_WORK:
                mTimerState = TimerState.PAUSED_WORK;
                mPauseButton.setText(getString(R.string.resume));
                mPauseButton.startAnimation(loadAnimation(getApplicationContext(), R.anim.blink));
                removeTimer();
                showNotification(getString(R.string.notification_pause), false);
                break;
            case PAUSED_WORK:
                mTimerState = TimerState.ACTIVE_WORK;
                mPauseButton.setText(getString(R.string.pause));
                mPauseButton.clearAnimation();
                startTimer(System.currentTimeMillis() - timeOfButtonPress > 1000 ? 0 : 1000 - (System.currentTimeMillis() - timeOfButtonPress));
                break;
        }
    }

    private void onCountdownFinished() {
        Log.i(TAG, "Countdown has finished");

        acquireScreenWakelock();
        releaseWakelock();
        shutScreenOffIfPreferred();
        restoreSoundAndWifiIfPreferred();

        removeTimer();

        increaseTotalSessions();

        notifyViaVibration();
        notifyViaSound();

        bringApplicationToFront();
        if (mPref.getContinuousMode()) {
            goOnContinuousMode();
        } else {
            showContinueDialog();
        }
    }

    private void increaseTotalSessions() {
        if (mTimerState == TimerState.ACTIVE_WORK) {
            ++mCurrentSessionStreak;
            int totalSessions = mPrivatePref.getInt(TOTAL_SESSION_COUNT, 0);
            mPrivatePref.edit()
                        .putInt(TOTAL_SESSION_COUNT, ++totalSessions)
                        .apply();
        }
    }

    private void notifyViaVibration() {
        if (mPref.getNotificationVibrate()) {
            final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {0, 300, 700, 300};
            vibrator.vibrate(pattern, -1);
        }
    }

    private void notifyViaSound() {
        String notificationSound = mPref.getNotificationSound();
        if (!notificationSound.equals("")) {
            Uri uri = Uri.parse(notificationSound);
            Ringtone r = RingtoneManager.getRingtone(this, uri);
            r.play();
        }
    }

    private void acquireScreenWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock screenWakeLock = powerManager.newWakeLock(
                SCREEN_BRIGHT_WAKE_LOCK | ON_AFTER_RELEASE | ACQUIRE_CAUSES_WAKEUP,
                "wake screen lock"
        );

        screenWakeLock.acquire();
        screenWakeLock.release();
    }

    private void showContinueDialog() {
        wakeScreen();

        switch (mTimerState) {
            case ACTIVE_WORK:
            case FINISHED_WORK:
                loadInitialState();

                mTimerState = TimerState.FINISHED_WORK;

                mAlertDialog = buildStartBreakDialog();
                mAlertDialog.setCanceledOnTouchOutside(false);
                mAlertDialog.show();

                showNotification("Session complete. Continue?", false);
                break;
            case ACTIVE_BREAK:
            case FINISHED_BREAK:
                loadInitialState();

                enablePauseButton();
                mTimerState = TimerState.FINISHED_BREAK;

                if (mCurrentSessionStreak >= mPref.getSessionsBeforeLongBreak()) {
                    mCurrentSessionStreak = 0;
                }

                mAlertDialog = buildStartSessionDialog();
                mAlertDialog.setCanceledOnTouchOutside(false);
                mAlertDialog.show();

                showNotification("Break complete. Resume work?", false);
                break;
            default:
                mTimerState = TimerState.INACTIVE;
                break;
        }
    }

    private AlertDialog buildStartSessionDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_break_message))
                .setPositiveButton(getString(R.string.dialog_break_session), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSession(0);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_session_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeNotifications();
                    }
                })
                .create();
    }

    private AlertDialog buildStartBreakDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_session_message))
                .setPositiveButton(
                        getString(R.string.dialog_session_break),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which
                            ) {
                                startBreak();
                            }
                        }
                )
                .setNegativeButton(
                        getString(R.string.dialog_session_skip),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which
                            ) {
                                startSession(0);
                            }
                        }
                )
                .setNeutralButton(getString(R.string.dialog_session_close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeNotifications();
                    }
                })
                .create();
    }

    private void removeNotifications() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    private void startSession(int delay) {
        mRemainingTime = mPref.getSessionDuration() * 60;
        mTimerState = TimerState.ACTIVE_WORK;
        startTimer(delay);
    }

    private void startBreak() {
        disablePauseButton();
        mRemainingTime = (mCurrentSessionStreak >= mPref.getSessionsBeforeLongBreak())
                         ? mPref.getLongBreakDuration() * 60
                         : mPref.getBreakDuration() * 60;
        mTimerState = TimerState.ACTIVE_BREAK;
        startTimer(0);
    }

    private void wakeScreen() {
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                SCREEN_BRIGHT_WAKE_LOCK | FULL_WAKE_LOCK,
                "waking screen up"
        );
        wakeLock.acquire();
        wakeLock.release();
    }

    private void goOnContinuousMode() {
        switch (mTimerState) {
            case ACTIVE_WORK:
            case FINISHED_WORK:
                loadInitialState();
                mTimerState = TimerState.FINISHED_WORK;
                startBreak();
                break;
            case ACTIVE_BREAK:
            case FINISHED_BREAK:
                loadInitialState();
                enablePauseButton();
                mTimerState = TimerState.FINISHED_BREAK;
                if (mCurrentSessionStreak >= mPref.getSessionsBeforeLongBreak()) {
                    mCurrentSessionStreak = 0;
                }

                startSession(0);
                break;
            default:
                mTimerState = TimerState.INACTIVE;
                break;
        }
    }

    private void enablePauseButton() {
        mPauseButton.setEnabled(true);
        mPauseButton.setTextColor(getResources().getColor(R.color.yellow));
    }

    public void runTimer() {
        Log.d(TAG, "Updating timer");

        if (mTimerState != TimerState.INACTIVE) {
            if (mRemainingTime == 0) {
                onCountdownFinished();
            } else {
                --mRemainingTime;
            }

            updateTimerLabel(mRemainingTime);
        }
    }

    private void updateTimerLabel(
            final int remainingTime
    ) {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;

        String currentTick = (minutes > 0 ? minutes : "") +
                "." +
                format(Locale.US, "%02d", seconds);

        SpannableString currentFormattedTick = new SpannableString(currentTick);
        currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0, currentTick.indexOf("."), 0);
        mTimeLabel.setText(currentFormattedTick);
    }

    private void showNotification(CharSequence contentText, boolean ongoing) {
        Notification notification = new Notification.Builder(
                getApplicationContext())
                .setSmallIcon(R.drawable.ic_status_goodtime)
                .setAutoCancel(false)
                .setContentTitle("Goodtime")
                .setContentText(contentText)
                .setOngoing(ongoing)
                .setShowWhen(false)
                .setContentIntent(
                        getActivity(
                                getApplicationContext(),
                                0,
                                new Intent(getApplicationContext(), MainActivity.class),
                                FLAG_UPDATE_CURRENT
                        )
                )
                .build();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(GOODTIME_NOTIFICATION_ID, notification);
    }

    private void bringApplicationToFront() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = getActivity(this, 0, notificationIntent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private void showSessionCounterDialog() {
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_reset_title))
                .setMessage(getString(R.string.dialog_reset_message))
                .setPositiveButton(getString(R.string.dialog_reset_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCurrentSessionStreak = 0;
                        mPrivatePref.edit()
                                    .putInt(TOTAL_SESSION_COUNT, 0)
                                    .apply();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_reset_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        mAlertDialog.show();
    }
}
