package com.apps.adrcotfas.goodtime;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.about.AboutMainActivity;
import com.apps.adrcotfas.goodtime.about.DonationsActivity;
import com.apps.adrcotfas.goodtime.about.ProductTourActivity;
import com.apps.adrcotfas.goodtime.settings.SettingsActivity;
import com.apps.adrcotfas.goodtime.util.DeviceInfo;
import com.google.android.gms.appinvite.AppInviteInvitation;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.graphics.Typeface.createFromAsset;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.ON_AFTER_RELEASE;
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
import static com.apps.adrcotfas.goodtime.Preferences.ENABLE_SESSIONS_COUNTER;
import static com.apps.adrcotfas.goodtime.SessionType.BREAK;
import static com.apps.adrcotfas.goodtime.SessionType.LONG_BREAK;
import static com.apps.adrcotfas.goodtime.SessionType.WORK;
import static com.apps.adrcotfas.goodtime.TimerState.INACTIVE;
import static com.apps.adrcotfas.goodtime.TimerState.PAUSED;
import static java.lang.String.format;

public class TimerActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        NavigationView.OnNavigationItemSelectedListener {

    public static final int NOTIFICATION_TAG = 2;
    protected final static int MSG_UPDATE_TIME = 0;
    private static final int MAXIMUM_MILLISECONDS_BETWEEN_KEY_PRESSES = 2000;
    private static final int MAXIMUM_MILLISECONDS_NOTIFICATION_TIME = 2000;
    private static final String TAG = "TimerActivity";
    private static final int REQUEST_INVITE = 0;
    private final Handler mUpdateTimeHandler = new TimeLabelUpdateHandler(this);
    private long mBackPressedAt;
    private long mTimeLabelPressedAt;
    private TextView mStartLabel;
    private TextView mStopLabel;
    private TextView mTimeLabel;
    private TextView mSessionCounterButton;
    private Preferences mPref;
    private SharedPreferences mPrivatePref;
    private AlertDialog mAlertDialog;
    private TimerService mTimerService;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mIsBoundToTimerService = false;
    private boolean mIsUiVisible;
    private ServiceConnection mTimerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) iBinder;
            mTimerService = binder.getService();
            mIsBoundToTimerService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIsBoundToTimerService = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPref = setupPreferences();
        migrateOldPreferences();
        setupUi();
        loadInitialState();
        setupAndroidNougatSettings();
        setupBroadcastReceiver();
    }

    private void migrateOldPreferences() {
        SharedPreferences oldPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPref.migrateFromOldPreferences(oldPref);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        startService(intent);
        bindService(intent, mTimerServiceConnection, Context.BIND_AUTO_CREATE);

        switchOrientation(mPref.getRotateTimeLabel());
    }

    @Override
    protected void onResume() {
        super.onResume();

        mIsUiVisible = true;
        if (mIsBoundToTimerService && mTimerService.getTimerState() != INACTIVE) {
            mTimerService.sendToBackground();
            mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
        }
        removeCompletionNotification();

        if (mPrivatePref.getBoolean(FIRST_RUN, true)) {
            Intent introIntent = new Intent(this, ProductTourActivity.class);
            startActivity(introIntent);
            mPrivatePref.edit().putBoolean(FIRST_RUN, false).apply();
        }
        setFullscreenMode(mPref.getFullscreenMode());
    }

    @Override
    protected void onStop() {
        mIsUiVisible = false;
        if (mIsBoundToTimerService && mTimerService.getTimerState() != INACTIVE) {
            mTimerService.bringToForeground();
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mIsBoundToTimerService) {
            stopService(new Intent(this, TimerService.class));
            unbindService(mTimerServiceConnection);
            mIsBoundToTimerService = false;
        }

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void setupBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case TimerService.ACTION_FINISHED_UI:
                        onCountdownFinished();
                        break;
                    case Notifications.ACTION_PAUSE_UI:
                        onTimeLabelClick();
                        mTimerService.bringToForeground();
                        break;
                    case Notifications.ACTION_STOP_UI:
                        onStopLabelClick();
                        break;
                    case Notifications.ACTION_SKIP_BREAK_UI:
                        if (mAlertDialog != null) {
                            mAlertDialog.dismiss();
                        }
                        startTimer(WORK);
                        removeCompletionNotification();
                        mTimerService.bringToForeground();
                        break;
                    case Notifications.ACTION_START_BREAK_UI:
                        if (mAlertDialog != null) {
                            mAlertDialog.dismiss();
                        }
                        startBreak();
                        removeCompletionNotification();
                        mTimerService.bringToForeground();
                        break;
                    case Notifications.ACTION_START_WORK_UI:
                        if (mAlertDialog != null) {
                            mAlertDialog.dismiss();
                        }
                        startTimer(WORK);
                        removeCompletionNotification();
                        mTimerService.bringToForeground();
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                (mBroadcastReceiver), new IntentFilter(TimerService.ACTION_FINISHED_UI)
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                (mBroadcastReceiver), new IntentFilter(Notifications.ACTION_PAUSE_UI)
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                (mBroadcastReceiver), new IntentFilter(Notifications.ACTION_STOP_UI)
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                (mBroadcastReceiver), new IntentFilter(Notifications.ACTION_SKIP_BREAK_UI)
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                (mBroadcastReceiver), new IntentFilter(Notifications.ACTION_START_BREAK_UI)
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                (mBroadcastReceiver), new IntentFilter(Notifications.ACTION_START_WORK_UI)
        );
    }

    private Preferences setupPreferences() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
        mPrivatePref = getSharedPreferences("preferences_private", Context.MODE_PRIVATE);
        mPrivatePref.registerOnSharedPreferenceChangeListener(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        return new Preferences(preferences);
    }

    private void setupAndroidNougatSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                mPref.disableSoundAndVibration();
            }
        }
    }

    private void setupUi() {
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        setupDrawer(toolbar);

        mStartLabel = (TextView) findViewById(R.id.startLabel);

        mStopLabel = (TextView) findViewById(R.id.stopLabel);
        setupStopLabel();

        mTimeLabel = (TextView) findViewById(R.id.textView);
        setupTimeLabel();
        setupPauseButton();
        setupLongPress();
    }

    private void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        } catch (Throwable th) {
            // ignoring this exception
        }

        if (mSessionCounterButton != null ) {
            toolbar.removeView(mSessionCounterButton);
        }

        if (mPref.getEnableSessionCounter()) {
            mSessionCounterButton = new TextView(this);
            mSessionCounterButton.setBackgroundColor(getResources().getColor(R.color.transparent));

            final float scale = getResources().getDisplayMetrics().density;
            final int minWidth = (int) (55 * scale + 0.5f);
            final int padding = (int) (12 * scale + 0.5f);
            mSessionCounterButton.setMinimumWidth(minWidth);
            mSessionCounterButton.setPadding(padding, 0, padding, 0);
            mSessionCounterButton.setGravity(Gravity.CENTER);
            mSessionCounterButton.setTypeface(mSessionCounterButton.getTypeface(), Typeface.BOLD);

            mSessionCounterButton.setText(String.valueOf(mPrivatePref.getInt(TOTAL_SESSION_COUNT, 0)));
            mSessionCounterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSessionCounterDialog();
                }
            });
            // TODO: remove this check and find a better solution
            if (toolbar != null) {
                toolbar.addView(mSessionCounterButton, new Toolbar.LayoutParams(GravityCompat.END));
            }
        }
    }

    private void setupDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupPauseButton() {
        mTimeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTimeLabelClick();
            }
        });
    }

    private void setupLongPress() {
        mTimeLabel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onTimeLabelLongClick();
                return true;
            }
        });
    }

    private void setupStopLabel() {
        mStopLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStopLabelClick();
            }
        });
    }

    private void setupTimeLabel() {
        if (mTimeLabel != null) {
            mTimeLabel.setTypeface(createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf"));
            updateTimeLabel();
        }
    }

    private void removeCompletionNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_TAG);
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "A preference has changed");

        switch (key) {
            case TOTAL_SESSION_COUNT:
                if (mSessionCounterButton != null) {
                    mSessionCounterButton.setText(String.valueOf(mPrivatePref.getInt(TOTAL_SESSION_COUNT, 0)));
                }
                break;
            case SESSION_DURATION:
                if (mIsBoundToTimerService && mTimerService.getTimerState() == INACTIVE) {
                    updateTimeLabel();
                }
                break;
            case ENABLE_SESSIONS_COUNTER:
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setupToolbar(toolbar);
                setupDrawer(toolbar);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (mTimerService.getTimerState() != INACTIVE) {
            moveTaskToBack(true);
        } else {
            if (mBackPressedAt + MAXIMUM_MILLISECONDS_BETWEEN_KEY_PRESSES > System.currentTimeMillis()) {
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

        mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);

        if (mIsBoundToTimerService) {
            updateTimeLabel();
            shutScreenOffIfPreferred();
        }

        setVisibility(mStartLabel, VISIBLE);
    }

    private void shutScreenOffIfPreferred() {
        if (mPref.getKeepScreenOn()) {
            getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
        }
    }

    private void startTimer(SessionType sessionType) {
        Log.i(TAG, "Timer has been started");

        mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
        setVisibility(mStartLabel, INVISIBLE);

        keepScreenOnIfPreferred();

        mTimerService.startSession(sessionType);
    }

    private void keepScreenOnIfPreferred() {
        if (mPref.getKeepScreenOn()) {
            getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onTimeLabelClick() {
        switch (mTimerService.getTimerState()) {
            case ACTIVE:
                setVisibility(mStopLabel, VISIBLE);

                if (mTimerService.getSessionType() == WORK) {
                    Log.i(TAG, "Timer has been paused");
                    mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
                    mTimerService.pauseSession();
                    mTimeLabel.startAnimation(loadAnimation(getApplicationContext(), R.anim.blink));
                } else if (mTimeLabelPressedAt + MAXIMUM_MILLISECONDS_BETWEEN_KEY_PRESSES
                        <= System.currentTimeMillis()) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setVisibility(mStopLabel, INVISIBLE);
                        }
                    }, MAXIMUM_MILLISECONDS_BETWEEN_KEY_PRESSES);
                }

                mTimeLabelPressedAt = System.currentTimeMillis();
                break;
            case PAUSED:
                Log.i(TAG, "Timer has been resumed");
                if (mIsUiVisible) {
                    mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                }
                mTimerService.unPauseSession();
                mTimeLabel.clearAnimation();
                setVisibility(mStopLabel, INVISIBLE);
                break;
            case INACTIVE:
                startTimer(WORK);
                setVisibility(mStartLabel, INVISIBLE);
                break;
        }
    }

    private void onTimeLabelLongClick() {
        switch (mTimerService.getTimerState()) {
            case ACTIVE:
            case PAUSED:
                if (mTimerService.getSessionType() == WORK) {
                    showSkipWorkDialog();
                } else {
                    showSkipBreakDialog();
                }
                break;
            case INACTIVE:
                break;
        }
    }

    private void showSkipWorkDialog() {
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_session_break) + "?")
                .setPositiveButton(getString(R.string.dialog_reset_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onStopLabelClick();
                        increaseTotalSessions();
                        startBreak();
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

    private void showSkipBreakDialog() {
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_session_skip) + "?")
                .setPositiveButton(getString(R.string.dialog_reset_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onStopLabelClick();
                        startTimer(WORK);
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

    private void onStopLabelClick() {
        mTimeLabel.clearAnimation();

        setVisibility(mStopLabel, INVISIBLE);
        mTimerService.stopSession();
        loadInitialState();
    }

    private void setVisibility(TextView textview, int visibility) {
        if (textview != null) {
            if (visibility == VISIBLE && textview.getVisibility() == INVISIBLE) {
                textview.setVisibility(VISIBLE);
                textview.startAnimation(loadAnimation(getApplicationContext(), R.anim.fade));
            } else if (visibility == INVISIBLE && textview.getVisibility() == VISIBLE) {
                textview.setVisibility(INVISIBLE);
                textview.startAnimation(loadAnimation(getApplicationContext(), R.anim.fade_reverse));
            }
        }
    }

    private void onCountdownFinished() {
        Log.i(TAG, "Countdown has finished");

        acquireScreenWakelock();
        shutScreenOffIfPreferred();

        mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
        increaseTotalSessions();
        loadInitialState();

        if (mPref.getContinuousMode()) {
            goOnContinuousMode();
        } else {
            showContinueDialog();
        }
    }

    private void increaseTotalSessions() {
        if (mTimerService.getTimerState() == INACTIVE && mTimerService.getSessionType() == WORK) {
            mTimerService.increaseCurrentSessionStreak();
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

        switch (mTimerService.getSessionType()) {
            case WORK:
                mAlertDialog = buildStartBreakDialog();
                mAlertDialog.setCanceledOnTouchOutside(false);
                mAlertDialog.show();
                break;
            case BREAK:
            case LONG_BREAK:
                if (mTimerService.getCurrentSessionStreak() >= mPref.getSessionsBeforeLongBreak()) {
                    mTimerService.resetCurrentSessionStreak();
                }
                mAlertDialog = buildStartSessionDialog();
                mAlertDialog.setCanceledOnTouchOutside(false);
                mAlertDialog.show();
        }
    }

    private AlertDialog buildStartSessionDialog() {
        return new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_break_message))
                .setPositiveButton(getString(R.string.dialog_break_session), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeCompletionNotification();
                        startTimer(WORK);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_session_close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeCompletionNotification();
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
                                removeCompletionNotification();
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
                                removeCompletionNotification();
                                startTimer(WORK);
                            }
                        }
                )
                .setNeutralButton(getString(R.string.dialog_session_close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeCompletionNotification();
                        mTimerService.sendToBackground();
                    }
                })
                .create();
    }

    private void startBreak() {
        startTimer(mTimerService.getCurrentSessionStreak() >= mPref.getSessionsBeforeLongBreak()
                ? LONG_BREAK
                : BREAK
        );
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
        switch (mTimerService.getSessionType()) {
            case WORK:
                startBreak();
                break;
            case BREAK:
            case LONG_BREAK:
                if (mTimerService.getCurrentSessionStreak() >= mPref.getSessionsBeforeLongBreak()) {
                    mTimerService.resetCurrentSessionStreak();
                }
                startTimer(WORK);
        }
        if (!mIsUiVisible) {
            mTimerService.bringToForeground();
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeCompletionNotification();
            }
        }, MAXIMUM_MILLISECONDS_NOTIFICATION_TIME);
    }

    protected void updateTimeLabel() {
        int remainingTime;
        if (mIsBoundToTimerService) {
            if (mTimerService.isTimerRunning()) {
                remainingTime = mTimerService.getRemainingTime();
            } else if (mTimerService.getTimerState().equals(PAUSED)) {
                remainingTime = mTimerService.getRemainingTimePaused();
            } else {
                remainingTime = (int) TimeUnit.MINUTES.toSeconds(mPref.getSessionDuration());
            }
        } else {
            remainingTime = (int) TimeUnit.MINUTES.toSeconds(mPref.getSessionDuration());
        }

        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;

        Log.i(TAG, "Updating time label: " + minutes + ":" + seconds);
        String currentTick = (minutes > 0 ? minutes : "") +
                "." +
                format(Locale.US, "%02d", seconds);

        SpannableString currentFormattedTick = new SpannableString(currentTick);
        currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0, currentTick.indexOf("."), 0);
        mTimeLabel.setText(currentFormattedTick);
    }

    private void showSessionCounterDialog() {
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_reset_title))
                .setMessage(getString(R.string.dialog_reset_message))
                .setPositiveButton(getString(R.string.dialog_reset_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTimerService.resetCurrentSessionStreak();
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

    private void setFullscreenMode(boolean fullscreen) {
        if (fullscreen) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    private void onInviteClicked() {

        final String appPackageName = getPackageName();
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;
        if (isIntentSafe) {
            startActivityForResult(intent, REQUEST_INVITE);
        } else {
            Toast.makeText(getBaseContext(), R.string.toast_google_services_missing, LENGTH_SHORT)
                    .show();
        }
    }

    private void switchOrientation(boolean rotate) {
        if (rotate) {
            mTimeLabel.setRotation(90);
        } else {
            mTimeLabel.setRotation(0);
        }
    }

    private void openPlayStorePage() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void openFeedback() {
        Intent email = new Intent(Intent.ACTION_SENDTO);
        email.setData(new Uri.Builder().scheme("mailto").build());
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"adrcotfas@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "[Goodtime] Feedback");
        email.putExtra(Intent.EXTRA_TEXT, "\nMy device info: \n" + DeviceInfo.getDeviceInfo()
                + "\nApp version: " + BuildConfig.VERSION_NAME
                + "\nFeedback:" + "\n");
        try {
            startActivity(Intent.createChooser(email, "Send feedback"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.about_no_email, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutMainActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.action_rate:
                openPlayStorePage();
                break;
            case R.id.action_invite:
                onInviteClicked();
                break;
            case R.id.about_pro:
                Intent donateIntent = new Intent(this, DonationsActivity.class);
                startActivity(donateIntent);
                break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_feedback:
                openFeedback();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
