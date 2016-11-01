package com.apps.adrcotfas.goodtime;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.about.AboutActivity;
import com.apps.adrcotfas.goodtime.settings.CustomNotification;
import com.apps.adrcotfas.goodtime.settings.SettingsActivity;

import java.util.Locale;

import static android.app.PendingIntent.getActivity;
import static android.graphics.Typeface.createFromAsset;
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
import static com.apps.adrcotfas.goodtime.Preferences.PREFERENCES_NAME;
import static com.apps.adrcotfas.goodtime.Preferences.SESSION_DURATION;
import static com.apps.adrcotfas.goodtime.Preferences.TOTAL_SESSION_COUNT;
import static java.lang.String.format;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MAXIMUM_MILLISECONDS_BETWEEN_BACK_PRESSES = 2000;
    private static final String TAG = "MainActivity";
    public static final int NOTIFICATION_TAG = 2;

    private PowerManager.WakeLock mWakeLock;
    private long mBackPressedAt;
    private FloatingActionButton mStartButton;
    private Button mPauseButton;
    private Button mStopButton;
    private TextView mTimeLabel;
    private View mHorizontalSeparator;
    private Preferences mPref;
    private SharedPreferences mPrivatePref;
    private AlertDialog mAlertDialog;
    private OrientationListener mOrientationListener;

    private TimerService mTimerService;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mIsBoundToTimerService = false;
    private ServiceConnection mTimerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) iBinder;
            mTimerService = binder.getService();
            mIsBoundToTimerService = true;
            mTimerService.setTimerState(TimerState.INACTIVE);
            mTimerService.sendToBackground();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIsBoundToTimerService = false;
        }
    };

    private class OrientationListener extends OrientationEventListener{
        final int ROTATION_O        = 1;
        final int ROTATION_90       = 2;
        final int ROTATION_MINUS90  = 3;

        private int rotation = ROTATION_O;
        public OrientationListener(Context context) { super(context); }

        @Override public void onOrientationChanged(int orientation) {
            if( (orientation < 35 || orientation > 325)){
                if (rotation == ROTATION_90) {
                    mTimeLabel.startAnimation(loadAnimation(getApplicationContext(), R.anim.landscape_to_portrait));
                }
                else if (rotation == ROTATION_MINUS90) {
                    mTimeLabel.startAnimation(loadAnimation(getApplicationContext(), R.anim.revlandscape_to_portrait));
                }
                rotation = ROTATION_O;
            }
            else if(orientation > 55 && orientation < 125 && rotation != ROTATION_MINUS90){
                rotation = ROTATION_MINUS90;
                mTimeLabel.startAnimation(loadAnimation(getApplicationContext(), R.anim.portrait_to_revlandscape));
            }
            else if(orientation > 235 && orientation < 305 && rotation != ROTATION_90){
                rotation = ROTATION_90;
                mTimeLabel.startAnimation(loadAnimation(getApplicationContext(), R.anim.portrait_to_landscape));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPref = setUpPreferences();
        installCustomRingtones();
        setUpUi();
        loadInitialState();
        setUpAndroidNougatSettings();
        setupBroadcastReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        startService(intent);
        bindService(intent, mTimerServiceConnection, Context.BIND_AUTO_CREATE);

        if (mPref.getRotateTimeLabel()) {
            mOrientationListener.enable();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsBoundToTimerService) {
            mTimerService.sendToBackground();
        }
        if (mPrivatePref.getBoolean(FIRST_RUN, true)) {
            Intent introIntent = new Intent(this, ProductTourActivity.class);
            startActivity(introIntent);
            mPrivatePref.edit().putBoolean(FIRST_RUN, false).apply();
        }
    }

    @Override
    protected void onStop() {
        if (mIsBoundToTimerService && mTimerService.getTimerState() != TimerState.INACTIVE) {
            mTimerService.bringToForegroundAndUpdateNotification();
        }
        if (mPref.getRotateTimeLabel()) {
            mOrientationListener.disable();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mIsBoundToTimerService) {
            mTimerService.removeTimer();
            stopService(new Intent(this, TimerService.class));
            unbindService(mTimerServiceConnection);
            mIsBoundToTimerService = false;
        }

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        releaseWakelock();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void setupBroadcastReceiver(){
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mIsBoundToTimerService) {
                    int remainingTime = intent.getIntExtra(TimerService.REMAINING_TIME, 0);

                    Log.d(TAG, "Updating timer, " + remainingTime + " remaining");

                    updateTimerLabel(remainingTime);

                    if (remainingTime == 0) {
                        onCountdownFinished();
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((mBroadcastReceiver),
                new IntentFilter(TimerService.ACTION_TIMERSERVICE)
        );
    }

    private Preferences setUpPreferences() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
        mPrivatePref = getSharedPreferences("preferences_private", Context.MODE_PRIVATE);
        mPrivatePref.registerOnSharedPreferenceChangeListener(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        return new Preferences(preferences);
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

        mOrientationListener = new OrientationListener(this);
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
            updateTimerLabel(mPref.getSessionDuration() * 60);
        }
    }

    private void disablePauseButton() {
        mPauseButton.setEnabled(false);
        mPauseButton.setTextColor(getResources().getColor(R.color.gray));
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
            if (mTimerService.getTimerState() == TimerState.INACTIVE) {
                updateTimerLabel(mPref.getSessionDuration() * 60);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mTimerService.getTimerState() != TimerState.INACTIVE) {
            /// move app to background
            moveTaskToBack(true);
        } else {
            if (mBackPressedAt + MAXIMUM_MILLISECONDS_BETWEEN_BACK_PRESSES > System.currentTimeMillis()) {
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
            mBackPressedAt = System.currentTimeMillis();
        }
    }

    private void loadInitialState() {
        Log.d(TAG, "Loading initial state");

        if (mIsBoundToTimerService) {
            mTimerService.setTimerState(TimerState.INACTIVE);
            int remainingTime = mPref.getSessionDuration() * 60;
            mTimerService.setRemainingTime(remainingTime);
            updateTimerLabel(remainingTime);
            mTimerService.removeTimer();
            shutScreenOffIfPreferred();
        }
        mTimeLabel.setTextColor(getResources().getColor(R.color.lightGray));

        mStartButton.setVisibility(VISIBLE);
        mPauseButton.setVisibility(INVISIBLE);
        mPauseButton.setText(getString(R.string.pause));
        mStopButton.setVisibility(INVISIBLE);
        mHorizontalSeparator.setVisibility(INVISIBLE);

        releaseWakelock();

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

    private void startTimer(long delay) {
        Log.i(TAG, "Timer has been started");

        mTimeLabel.setTextColor(Color.WHITE);
        loadRunningTimerUiState();

        keepScreenOnIfPreferred();
        acquirePartialWakelock();

        mTimerService.scheduleTimer(delay);
    }

    private void loadRunningTimerUiState() {
        updateTimerLabel(mTimerService.getRemainingTime());

        mStartButton.setVisibility(INVISIBLE);
        mPauseButton.setVisibility(VISIBLE);
        mStopButton.setVisibility(VISIBLE);
        mHorizontalSeparator.setVisibility(VISIBLE);
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
        switch (mTimerService.getTimerState()) {
            case ACTIVE_WORK:
                mTimerService.setTimerState(TimerState.PAUSED_WORK);
                mPauseButton.setText(getString(R.string.resume));
                mPauseButton.startAnimation(loadAnimation(getApplicationContext(), R.anim.blink));
                mTimerService.removeTimer();
                break;
            case PAUSED_WORK:
                mTimerService.setTimerState(TimerState.ACTIVE_WORK);
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

        mTimerService.removeTimer();

        increaseTotalSessions();

        bringApplicationToFront();
        if (mPref.getContinuousMode()) {
            goOnContinuousMode();
        } else {
            showContinueDialog();
        }
    }

    private void increaseTotalSessions() {
        if (mTimerService.getTimerState() == TimerState.ACTIVE_WORK) {
            mTimerService.setCurrentSessionStreak(mTimerService.getCurrentSessionStreak() + 1);
            int totalSessions = mPrivatePref.getInt(TOTAL_SESSION_COUNT, 0);
            mPrivatePref.edit()
                        .putInt(TOTAL_SESSION_COUNT, ++totalSessions)
                        .apply();
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

        switch (mTimerService.getTimerState()) {
            case ACTIVE_WORK:
            case FINISHED_WORK:
                loadInitialState();

                mAlertDialog = buildStartBreakDialog();
                mAlertDialog.setCanceledOnTouchOutside(false);
                mAlertDialog.show();

                break;
            case ACTIVE_BREAK:
            case FINISHED_BREAK:
                loadInitialState();
                enablePauseButton();

                if (mTimerService.getCurrentSessionStreak() >= mPref.getSessionsBeforeLongBreak()) {
                    mTimerService.setCurrentSessionStreak(0);
                }

                mAlertDialog = buildStartSessionDialog();
                mAlertDialog.setCanceledOnTouchOutside(false);
                mAlertDialog.show();

                break;
            default:
                mTimerService.setTimerState(TimerState.INACTIVE);
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
                        mTimerService.sendToBackground();
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
                        mTimerService.sendToBackground();
                    }
                })
                .create();
    }

    private void startSession(int delay) {
        mTimerService.setRemainingTime(mPref.getSessionDuration() * 60);
        mTimerService.setTimerState(TimerState.ACTIVE_WORK);
        startTimer(delay);
    }

    private void startBreak() {
        disablePauseButton();
        mTimerService.setRemainingTime(
                mTimerService.getCurrentSessionStreak() >= mPref.getSessionsBeforeLongBreak()
                ? mPref.getLongBreakDuration() * 60
                : mPref.getBreakDuration() * 60
        );
        mTimerService.setTimerState(TimerState.ACTIVE_BREAK);
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
        switch (mTimerService.getTimerState()) {
            case ACTIVE_WORK:
            case FINISHED_WORK:
                loadInitialState();
                mTimerService.setTimerState(TimerState.FINISHED_WORK);
                startBreak();
                break;
            case ACTIVE_BREAK:
            case FINISHED_BREAK:
                loadInitialState();
                enablePauseButton();
                mTimerService.setTimerState(TimerState.FINISHED_BREAK);
                if (mTimerService.getCurrentSessionStreak() >= mPref.getSessionsBeforeLongBreak()) {
                    mTimerService.setCurrentSessionStreak(0);
                }

                startSession(0);
                break;
            default:
                mTimerService.setTimerState(TimerState.INACTIVE);
                break;
        }
    }

    private void enablePauseButton() {
        mPauseButton.setEnabled(true);
        mPauseButton.setTextColor(getResources().getColor(R.color.yellow));
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
                        mTimerService.setCurrentSessionStreak(0);
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
