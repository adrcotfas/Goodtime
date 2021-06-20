/*
 * Copyright 2016-2020 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.Main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.BL.CurrentSession;
import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.BL.NotificationHelper;
import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper;
import com.apps.adrcotfas.goodtime.BL.SessionType;
import com.apps.adrcotfas.goodtime.BL.TimerService;
import com.apps.adrcotfas.goodtime.BL.TimerState;
import com.apps.adrcotfas.goodtime.BuildConfig;
import com.apps.adrcotfas.goodtime.Label;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Session;
import com.apps.adrcotfas.goodtime.Settings.SettingsActivity;
import com.apps.adrcotfas.goodtime.Settings.reminders.ReminderHelper;
import com.apps.adrcotfas.goodtime.Statistics.Main.SelectLabelDialog;
import com.apps.adrcotfas.goodtime.Statistics.SessionViewModel;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.Util.IntentWithAction;
import com.apps.adrcotfas.goodtime.Util.OnSwipeTouchListener;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.common.BaseActivity;
import com.apps.adrcotfas.goodtime.databinding.ActivityMainBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.kobakei.ratethisapp.RateThisApp;

import org.greenrobot.eventbus.Subscribe;
import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import org.greenrobot.eventbus.EventBus;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
import static android.view.animation.AnimationUtils.loadAnimation;
import static android.widget.Toast.LENGTH_SHORT;
import static com.apps.adrcotfas.goodtime.Settings.PreferenceHelper.AMOLED;
import static com.apps.adrcotfas.goodtime.Settings.PreferenceHelper.ENABLE_SCREENSAVER_MODE;
import static com.apps.adrcotfas.goodtime.Settings.PreferenceHelper.ENABLE_SCREEN_ON;
import static com.apps.adrcotfas.goodtime.Settings.PreferenceHelper.WORK_DURATION;
import static com.apps.adrcotfas.goodtime.Util.BatteryUtils.isIgnoringBatteryOptimizations;

public class TimerActivity
        extends
        BaseActivity
        implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        SelectLabelDialog.OnLabelSelectedListener, FinishedSessionDialog.Listener {

    private static final String TAG = TimerActivity.class.getSimpleName();

    private final CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
    private FinishedSessionDialog mDialogSessionFinished;
    private FullscreenHelper mFullscreenHelper;
    private long mBackPressedAt;

    private View mBlackCover;
    private View mWhiteCover;

    private MenuItem mLabelButton;
    private View mBoundsView;
    private TextView mTimeLabel;
    private Toolbar mToolbar;
    private ImageView mTutorialDot;
    private Chip mLabelChip;

    private SessionViewModel mSessionViewModel;
    private TimerActivityViewModel mViewModel;

    private TextView mSessionsCounterText;

    private SessionType mCurrentSessionType = SessionType.INVALID;

    private static final String DIALOG_SELECT_LABEL_TAG = "dialogSelectLabel";

    public void onStartButtonClick(View view) {
        start(SessionType.WORK);
    }
    public void onStopButtonClick() {
        stop();
    }
    public void onSkipButtonClick() {
        skip();
    }
    public void onAdd60SecondsButtonClick() {
        add60Seconds();
    }

    private void skip() {
        if (mCurrentSession.getTimerState().getValue() != TimerState.INACTIVE) {
            Intent skipIntent = new IntentWithAction(TimerActivity.this, TimerService.class,
                    Constants.ACTION.SKIP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(skipIntent);
            } else {
                startService(skipIntent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        if (PreferenceHelper.isFirstRun()) {
            // show app intro
            Intent i = new Intent(TimerActivity.this, MainIntroActivity.class);
            startActivity(i);

            PreferenceHelper.consumeFirstRun();
        }

        ThemeHelper.setTheme(this);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mBlackCover = binding.blackCover;
        mWhiteCover = binding.whiteCover;
        mToolbar = binding.bar;
        mTimeLabel = binding.timeLabel;
        mTutorialDot = binding.tutorialDot;
        mBoundsView = binding.main;
        mLabelChip = binding.labelView;
        mLabelChip.setOnClickListener(v -> showEditLabelDialog());

        setupTimeLabelEvents();

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
        }

        // dismiss it at orientation change
        DialogFragment selectLabelDialog = ((DialogFragment)getSupportFragmentManager().findFragmentByTag(DIALOG_SELECT_LABEL_TAG));
        if (selectLabelDialog != null) {
            selectLabelDialog.dismiss();
        }

        if (!BuildConfig.F_DROID) {
            // Monitor launch times and interval from installation
            RateThisApp.onCreate(this);
            // If the condition is satisfied, "Rate this app" dialog will be shown
            RateThisApp.showRateDialogIfNeeded(this);
        }

        mViewModel = new ViewModelProvider(this).get(TimerActivityViewModel.class);
    }

    /**
     * Shows the tutorial snackbars
     */
    private void showTutorialSnackbars() {
        final int MESSAGE_SIZE = 4;
        int i = PreferenceHelper.getLastIntroStep();

        if (i < MESSAGE_SIZE) {

            final List<String> messages = Arrays.asList(
                    getString(R.string.tutorial_tap),
                    getString(R.string.tutorial_swipe_left),
                    getString(R.string.tutorial_swipe_up),
                    getString(R.string.tutorial_swipe_down));

            final List<Animation> animations = Arrays.asList(
                    loadAnimation(getApplicationContext(), R.anim.tutorial_tap),
                    loadAnimation(getApplicationContext(), R.anim.tutorial_swipe_right),
                    loadAnimation(getApplicationContext(), R.anim.tutorial_swipe_up),
                    loadAnimation(getApplicationContext(), R.anim.tutorial_swipe_down));

            mTutorialDot.setVisibility(View.VISIBLE);
            mTutorialDot.animate().translationX(0).translationY(0);
            mTutorialDot.clearAnimation();
            mTutorialDot.setAnimation(animations.get(PreferenceHelper.getLastIntroStep()));

            Snackbar s = Snackbar.make(mToolbar, messages.get(PreferenceHelper.getLastIntroStep()), Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", view -> {
                        int nextStep = i + 1;
                        PreferenceHelper.setLastIntroStep(nextStep);
                        showTutorialSnackbars();
                    })
                    .setAnchorView(mToolbar)
                    .setActionTextColor(getResources().getColor(R.color.teal200));

            s.setBehavior(new BaseTransientBottomBar.Behavior() {
                @Override
                public boolean canSwipeDismissView(View child) {
                    return false;
                }
            });

            TextView tv = s.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            if (tv != null) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
            s.show();
        } else {
            mTutorialDot.animate().translationX(0).translationY(0);
            mTutorialDot.clearAnimation();
            mTutorialDot.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTimeLabelEvents() {
        mTimeLabel.setOnTouchListener(new OnSwipeTouchListener(TimerActivity.this)
        {
            @Override
            public void onSwipeRight(View view) {
                onSkipSession();
            }

            @Override
            public void onSwipeLeft(View view) {
                onSkipSession();
            }

            @Override
            public void onSwipeBottom(View view) {
                onStopSession();
                if (PreferenceHelper.isScreensaverEnabled()) {
                    recreate();
                }
            }

            @Override
            public void onSwipeTop(View view) {
                if (mCurrentSession.getTimerState().getValue() != TimerState.INACTIVE) {
                    onAdd60SecondsButtonClick();
                }
            }

            @Override
            public void onClick(View view) {
                onStartButtonClick(view);
            }

            @Override
            public void onLongClick(View view) {
                Intent settingsIntent = new Intent(TimerActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }

            @Override
            public void onPress(View view) {
                mTimeLabel.startAnimation(loadAnimation(getApplicationContext(), R.anim.scale_reversed));
            }

            @Override
            public void onRelease(View view) {
                mTimeLabel.startAnimation(loadAnimation(getApplicationContext(), R.anim.scale));
                if (mCurrentSession.getTimerState().getValue() == TimerState.PAUSED) {
                    final Handler handler = new Handler();
                    handler.postDelayed(() -> mTimeLabel.startAnimation(
                            loadAnimation(getApplicationContext(), R.anim.blink)), 300);
                }
            }
        });
    }

    private void onSkipSession() {
        if (mCurrentSession.getTimerState().getValue() != TimerState.INACTIVE) {
            onSkipButtonClick();
        }
    }

    private void onStopSession() {
        if (mCurrentSession.getTimerState().getValue() != TimerState.INACTIVE) {
            onStopButtonClick();
        }
    }

    @Override
    public void onAttachedToWindow() {
        getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED
                | FLAG_TURN_SCREEN_ON);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mViewModel.isActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReminderHelper.removeNotification(getApplicationContext());

        mViewModel.isActive = true;
        if (mViewModel.showFinishDialog) {
            showFinishedSessionUI();
        }

        // initialize notification channels on the first run
        if (PreferenceHelper.isFirstRun()) {
            new NotificationHelper(this);
        }

        // this is to refresh the current status icon color
        invalidateOptionsMenu();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);
        toggleKeepScreenOn(PreferenceHelper.isScreenOnEnabled());
        toggleFullscreenMode();

        showTutorialSnackbars();
        setTimeLabelColor();

        mBlackCover.animate().alpha(0.f).setDuration(500);

        // then only reason we're doing this here is because a FinishSessionEvent
        // comes together with a "bring activity on top"
        if (PreferenceHelper.isFlashingNotificationEnabled() && mViewModel.enableFlashingNotification) {
            mWhiteCover.setVisibility(View.VISIBLE);
            if ((PreferenceHelper.isAutoStartBreak() && (mCurrentSessionType == SessionType.BREAK || mCurrentSessionType == SessionType.LONG_BREAK))
                    || (PreferenceHelper.isAutoStartWork() && mCurrentSessionType == SessionType.WORK)) {
                startFlashingNotificationShort();
            } else {
                startFlashingNotification();
            }
        }
    }

    @Override
    protected void onDestroy() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        MenuItem batteryButton = menu.findItem(R.id.action_battery_optimization);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations(this)) {
            batteryButton.setVisible(true);
        } else {
            batteryButton.setVisible(false);
        }

        mLabelButton = menu.findItem(R.id.action_current_label);
        mLabelButton.getIcon().setColorFilter(
                ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_ALL_LABELS), PorterDuff.Mode.SRC_ATOP);
        setupEvents();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                BottomNavigationDrawerFragment bottomNavigationDrawerFragment = new BottomNavigationDrawerFragment();
                bottomNavigationDrawerFragment.show(getSupportFragmentManager(), bottomNavigationDrawerFragment.getTag());
                break;
            case R.id.action_battery_optimization:
                showBatteryOptimizationDialog();
                break;
            case R.id.action_current_label:
                showEditLabelDialog();
                break;
            case R.id.action_sessions_counter:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.action_reset_counter_title)
                        .setMessage(R.string.action_reset_counter)
                        .setPositiveButton(android.R.string.ok, (dialog, which)
                                -> {
                            mSessionViewModel.deleteSessionsFinishedToday();
                            PreferenceHelper.resetCurrentStreak();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBatteryOptimizationDialog() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + this.getPackageName()));
        startActivity(intent);
    }

    private void setupEvents() {
        mCurrentSession.getDuration().observe(TimerActivity.this, this::updateTimeLabel);
        mCurrentSession.getSessionType().observe(TimerActivity.this, sessionType -> {
            mCurrentSessionType = sessionType;
            setupLabelView();
            setTimeLabelColor();
        });

        mCurrentSession.getTimerState().observe(TimerActivity.this, timerState -> {
            if (timerState == TimerState.INACTIVE) {
                setupLabelView();
                setTimeLabelColor();
                final Handler handler = new Handler();
                handler.postDelayed(() -> mTimeLabel.clearAnimation(), 300);
            } else if (timerState == TimerState.PAUSED) {
                final Handler handler = new Handler();
                handler.postDelayed(() -> mTimeLabel.startAnimation(
                        loadAnimation(getApplicationContext(), R.anim.blink)), 300);
            } else {
                final Handler handler = new Handler();
                handler.postDelayed(() -> mTimeLabel.clearAnimation(), 300);
            }
        });
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onBackPressed() {
        if (mCurrentSession.getTimerState().getValue() != TimerState.INACTIVE) {
            moveTaskToBack(true);
        } else {
            if (mBackPressedAt + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                try {
                    Toast.makeText(getBaseContext(), R.string.action_press_back_button, LENGTH_SHORT)
                            .show();
                } catch (Throwable th) {
                    // ignoring this exception
                }
            }
            mBackPressedAt = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.action_sessions_counter);
        alertMenuItem.setVisible(false);
        boolean sessionsCounterEnabled = PreferenceHelper.isSessionsCounterEnabled();
        if (sessionsCounterEnabled) {
            FrameLayout mSessionsCounter = (FrameLayout) alertMenuItem.getActionView();
            mSessionsCounterText = mSessionsCounter.findViewById(R.id.view_alert_count_textview);
            mSessionsCounter.setOnClickListener(v -> onOptionsItemSelected(alertMenuItem));

            mSessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
            mSessionViewModel.getAllSessionsByEndTime().observe(this, sessions -> {
                final LocalDate today = new LocalDate();
                int statsToday = 0;
                for (Session s : sessions) {
                    final LocalDate crt = new LocalDate(new Date(s.timestamp));
                    if (crt.isEqual(today)) {
                        statsToday++;
                    }
                    if (crt.isBefore(today)) {
                        break;
                    }
                }
                if (mSessionsCounterText != null) {
                    mSessionsCounterText.setText(Integer.toString(statsToday));
                }
                alertMenuItem.setVisible(true);
            });
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Called when an event is posted to the EventBus
     * @param o holds the type of the Event
     */
    @Subscribe
    public void onEventMainThread(Object o) {
        if (o instanceof Constants.FinishWorkEvent) {
            if (PreferenceHelper.isAutoStartBreak()) {
                if (PreferenceHelper.isFlashingNotificationEnabled()) {
                    mViewModel.enableFlashingNotification = true;
                }
            } else {
                mViewModel.dialogPendingType = SessionType.WORK;
                showFinishedSessionUI();
            }
        } else if (o instanceof Constants.FinishBreakEvent || o instanceof Constants.FinishLongBreakEvent) {
            if (PreferenceHelper.isAutoStartWork()) {
                if (PreferenceHelper.isFlashingNotificationEnabled()) {
                    mViewModel.enableFlashingNotification = true;
                }
            } else {
                mViewModel.dialogPendingType = SessionType.BREAK;
                showFinishedSessionUI();
            }
        } else if (o instanceof Constants.StartSessionEvent) {
            if (mDialogSessionFinished != null) {
                mDialogSessionFinished.dismissAllowingStateLoss();
            }
            mViewModel.showFinishDialog = false;
            if (!PreferenceHelper.isAutoStartBreak() && !PreferenceHelper.isAutoStartWork()) {
                stopFlashingNotification();
            }
        } else if (o instanceof Constants.OneMinuteLeft) {
            if (PreferenceHelper.isFlashingNotificationEnabled()) {
                startFlashingNotificationShort();
            }
        }
    }

    private void updateTimeLabel(Long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        seconds -= (minutes * 60);

        String currentFormattedTick;

        boolean isMinutesStyle = PreferenceHelper.getTimerStyle().equals(getResources().getString(R.string.pref_timer_style_minutes_value));
        if (isMinutesStyle) {
            currentFormattedTick = String.valueOf(TimeUnit.SECONDS.toMinutes((minutes * 60) + seconds + 59));
        } else {
            currentFormattedTick =
                    ((minutes > 9) ? minutes  : "0" + minutes)
                            + ":"
                            + ((seconds > 9) ? seconds : "0" + seconds);
        }

        mTimeLabel.setText(currentFormattedTick);

        Log.v(TAG, "drawing the time label.");

        if (PreferenceHelper.isScreensaverEnabled() && mCurrentSession.getTimerState().getValue() != TimerState.PAUSED) {
            teleportTimeView();
        }

    }

    private void start(SessionType sessionType) {
        Intent startIntent = new Intent();
        switch (mCurrentSession.getTimerState().getValue()) {
            case INACTIVE:
                startIntent = new IntentWithAction(TimerActivity.this, TimerService.class,
                        Constants.ACTION.START, sessionType);
                break;
            case ACTIVE:
            case PAUSED:
                startIntent = new IntentWithAction(TimerActivity.this, TimerService.class, Constants.ACTION.TOGGLE);
                break;
            default:
                Log.wtf(TAG, "Invalid timer state.");
                break;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
        } else {
            startService(startIntent);
        }
    }

    private void stop() {
        Intent stopIntent = new IntentWithAction(TimerActivity.this, TimerService.class, Constants.ACTION.STOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent);
        } else {
            startService(stopIntent);
        }
        mWhiteCover.setVisibility(View.GONE);
        mWhiteCover.clearAnimation();
    }

    private void add60Seconds() {
        Intent stopIntent = new IntentWithAction(TimerActivity.this, TimerService.class, Constants.ACTION.ADD_SECONDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent);
        } else {
            startService(stopIntent);
        }
    }

    private void showEditLabelDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SelectLabelDialog.newInstance(this, PreferenceHelper.getCurrentSessionLabel().title, false)
                .show(fragmentManager, DIALOG_SELECT_LABEL_TAG);
    }

    private void showFinishedSessionUI() {
        if (mViewModel.isActive) {
            mViewModel.showFinishDialog = false;
            mViewModel.enableFlashingNotification = true;
            Log.i(TAG, "Showing the finish dialog.");
            mDialogSessionFinished = FinishedSessionDialog.newInstance(this);
            mDialogSessionFinished.show(getSupportFragmentManager(), TAG);
        } else {
            mViewModel.showFinishDialog = true;
            mViewModel.enableFlashingNotification = false;
        }
    }

    private void toggleFullscreenMode() {
        if (PreferenceHelper.isFullscreenEnabled()) {
            if (mFullscreenHelper == null) {
                mFullscreenHelper = new FullscreenHelper(findViewById(R.id.main), getSupportActionBar());
            } else {
                mFullscreenHelper.hide();
            }
        } else {
            if (mFullscreenHelper != null) {
                mFullscreenHelper.disable();
                mFullscreenHelper = null;
            }
        }
    }

    private void delayToggleFullscreenMode() {
        final Handler handler = new Handler();
        handler.postDelayed(this::toggleFullscreenMode, 300);
    }

    private void toggleKeepScreenOn(boolean enabled) {
        if (enabled) {
            getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case WORK_DURATION:
                if (GoodtimeApplication.getInstance().getCurrentSession().getTimerState().getValue()
                        == TimerState.INACTIVE) {
                    mCurrentSession.setDuration(TimeUnit.MINUTES.toMillis(PreferenceHelper.getSessionDuration(SessionType.WORK)));
                }
                break;
            case ENABLE_SCREEN_ON:
                toggleKeepScreenOn(PreferenceHelper.isScreenOnEnabled());
                break;
            case AMOLED:
                recreate();
            case ENABLE_SCREENSAVER_MODE:
                if (!PreferenceHelper.isScreensaverEnabled()) {
                    recreate();
                }
                break;
            default:
                break;
        }
    }

    private void setupLabelView() {
        Label label = PreferenceHelper.getCurrentSessionLabel();

        if (label.title == null ||
                label.title.equals(getString(R.string.label_unlabeled))) {
            mLabelChip.setVisibility(View.GONE);
            mLabelButton.setVisible(true);
            int color = ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_ALL_LABELS);
            mLabelButton.getIcon().setColorFilter(
                    color, PorterDuff.Mode.SRC_ATOP);
        } else {
            final int color = ThemeHelper.getColor(this, label.colorId);
            if (PreferenceHelper.showCurrentLabel()) {
                mLabelButton.setVisible(false);
                mLabelChip.setVisibility(View.VISIBLE);
                mLabelChip.setText(label.title);
                mLabelChip.setChipBackgroundColor(ColorStateList.valueOf(color));
            } else {
                mLabelChip.setVisibility(View.GONE);
                mLabelButton.setVisible(true);
                mLabelButton.getIcon().setColorFilter(
                        color, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    private void setTimeLabelColor() {
        Label label = PreferenceHelper.getCurrentSessionLabel();
        if (mTimeLabel != null) {
            if (mCurrentSessionType == SessionType.BREAK || mCurrentSessionType == SessionType.LONG_BREAK) {
                mTimeLabel.setTextColor(ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_BREAK));
                return;
            }
            if (label.title != null) {
                mTimeLabel.setTextColor(ThemeHelper.getColor(this, label.colorId));
            } else {
                mTimeLabel.setTextColor(ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_UNLABELED));
            }
        }
    }

    @Override
    public void onLabelSelected(Label label) {
        if (label != null) {
            GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(label.title);
            PreferenceHelper.setCurrentSessionLabel(label);
        } else {
            GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(null);
        }
        setupLabelView();
        setTimeLabelColor();
    }

    private Direction direction = Direction.random();

    private void teleportTimeView() {

//region Highly unoptimised code to visually track the TopLeft of mTimeLabel
//        View dot = new View(mBoundsView.getContext());
//        dot.setBackgroundColor(Color.WHITE);
//        dot.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
//        dot.setX(mTimeLabel.getX());
//        dot.setY(mTimeLabel.getY());
//        ((ConstraintLayout) mBoundsView.getParent()).addView(dot);
//endregion

        int margin = ThemeHelper.dpToPx(this, 48);

        // Bounds in which the TopLeft corner/pixel of mTimeLabel can move
        Rect bounds = new Rect(
                ((int) mBoundsView.getX()) + margin,
                ((int) mBoundsView.getY()) + margin,
                mBoundsView.getWidth() - mTimeLabel.getWidth() - margin,
                mBoundsView.getHeight() - mTimeLabel.getHeight() - margin
        );

        Log.d(TAG, "teleportTimeView: Bounds ==> " + bounds.toShortString());

        if (bounds.width() > 0 && bounds.height() > 0) {

            float x = mTimeLabel.getX();
            float y = mTimeLabel.getY();

            float newX = x;
            float newY = y;

            if (direction.getHorizontal() == Horizontal.LEFT) {
                if (x == bounds.left) {
                    direction = new Direction(Horizontal.RIGHT, direction.getVertical());
                    newX = x + 1;
                } else {
                    newX = x - 1;
                }
            } else if (direction.getHorizontal() == Horizontal.RIGHT) {
                if (x == bounds.right) {
                    direction = new Direction(Horizontal.LEFT, direction.getVertical());
                    newX = x - 1;
                } else {
                    newX = x + 1;
                }
            }
            if (direction.getVertical() == Vertical.UP) {
                if (y == bounds.top) {
                    direction = new Direction(direction.getHorizontal(), Vertical.DOWN);
                    newY = y + 1;
                } else {
                    newY = y - 1;
                }
            } else if (direction.getVertical() == Vertical.DOWN) {
                if (y == bounds.bottom) {
                    direction = new Direction(direction.getHorizontal(), Vertical.UP);
                    newY = y - 1;
                } else {
                    newY = y + 1;
                }
            }
            mTimeLabel.setX(newX);
            mTimeLabel.setY(newY);
        }
    }

    @Override
    public void onFinishedSessionDialogPositiveButtonClick(SessionType sessionType) {
        if (sessionType == SessionType.WORK) {
            start(SessionType.BREAK);
            delayToggleFullscreenMode();
        } else {
            start(SessionType.WORK);
            delayToggleFullscreenMode();
        }
        stopFlashingNotification();
    }

    @Override
    public void onFinishedSessionDialogNeutralButtonClick(SessionType sessionType) {
        if (sessionType == SessionType.WORK) {
            EventBus.getDefault().post(new Constants.ClearNotificationEvent());
            delayToggleFullscreenMode();
        } else {
            EventBus.getDefault().post(new Constants.ClearNotificationEvent());
            delayToggleFullscreenMode();
        }
        stopFlashingNotification();
    }

    private void startFlashingNotification() {
        mWhiteCover.setVisibility(View.VISIBLE);
        mWhiteCover.startAnimation(loadAnimation(getApplicationContext(), R.anim.blink_screen));
    }

    private void startFlashingNotificationShort() {
        mWhiteCover.setVisibility(View.VISIBLE);
        final Animation anim = loadAnimation(getApplicationContext(), R.anim.blink_screen_3_times);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) { stopFlashingNotification(); }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        mWhiteCover.startAnimation(anim);
    }

    private void stopFlashingNotification() {
        mWhiteCover.setVisibility(View.GONE);
        mWhiteCover.clearAnimation();
        mViewModel.enableFlashingNotification = false;
    }
}
