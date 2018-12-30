package com.apps.adrcotfas.goodtime.Main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.BL.CurrentSession;
import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.BL.NotificationHelper;
import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.BL.SessionType;
import com.apps.adrcotfas.goodtime.BL.TimerService;
import com.apps.adrcotfas.goodtime.BL.TimerState;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Settings.SettingsActivity;
import com.apps.adrcotfas.goodtime.Statistics.Main.SelectLabelDialog;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.Util.IntentWithAction;
import com.apps.adrcotfas.goodtime.Util.OnSwipeTouchListener;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import de.greenrobot.event.EventBus;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
import static android.view.animation.AnimationUtils.loadAnimation;
import static android.widget.Toast.LENGTH_SHORT;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.ENABLE_SCREEN_ON;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.THEME;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.WORK_DURATION;
import static java.lang.String.format;

public class TimerActivity
        extends
        AppCompatActivity
        implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        SelectLabelDialog.OnLabelSelectedListener {

    private static final String TAG = TimerActivity.class.getSimpleName();

    private final CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
    private AlertDialog mDialogSessionFinished;
    private FullscreenHelper mFullscreenHelper;
    private long mBackPressedAt;
    private LabelsViewModel mLabelsViewModel;

    public void onStartButtonClick() {
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

    MenuItem mStatusButton;
    TextView mTimeLabel;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        if (PreferenceHelper.isFirstRun()) {
            // show app intro
            PreferenceHelper.consumeFirstRun();
        }

        ThemeHelper.setTheme(this);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mToolbar = binding.bar;
        mTimeLabel = binding.timeLabel;
        mLabelsViewModel = ViewModelProviders.of(this).get(LabelsViewModel.class);
        showTutorialSnackbars();

        setupTimeLabelEvents();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);

        setupEvents();
    }

    /**
     * Shows the tutorial snackbars
     */
    private void showTutorialSnackbars() {

        final int MESSAGE_SIZE = 4;
        int i = PreferenceHelper.getLastIntroStep();

        if (i < MESSAGE_SIZE) {

            List<String> messages = new ArrayList<>();
            //TODO: extract strings
            messages.add("Tap the timer to start");
            messages.add("Swipe left or right to skip the current session");
            messages.add("Swipe up to add one more minute");
            messages.add("Swipe down on the timer to stop");

            Snackbar s = Snackbar.make(mToolbar, messages.get(PreferenceHelper.getLastIntroStep()), Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", view -> {
                        int nextStep = i + 1;
                        PreferenceHelper.setLastIntroStep(nextStep);
                        showTutorialSnackbars();
                    })
                    .setAnchorView(mToolbar)
                    .setActionTextColor(getResources().getColor(R.color.dayNightTeal));
            s.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.dayNightGray));
            TextView tv = s.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            if (tv != null) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
            s.show();
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
            }

            @Override
            public void onSwipeTop(View view) {
                if (mCurrentSession.getTimerState().getValue() != TimerState.INACTIVE) {
                    onAdd60SecondsButtonClick();
                }
            }

            @Override
            public void onClick(View view) {
                onStartButtonClick();
            }

            @Override
            public boolean onLongClick(View view) {
                showEditLabelDialog();
                return true;
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
    protected void onResume() {
        super.onResume();

        // initialize notification channels on the first run
        new NotificationHelper(this);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);
        toggleKeepScreenOn(PreferenceHelper.isScreenOnEnabled());
        toggleFullscreenMode();
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
        mStatusButton = menu.findItem(R.id.action_state);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            BottomNavigationDrawerFragment bottomNavigationDrawerFragment = new BottomNavigationDrawerFragment();
            bottomNavigationDrawerFragment.show(getSupportFragmentManager(), bottomNavigationDrawerFragment.getTag());
        }
        else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupEvents() {
        mCurrentSession.getDuration().observe(TimerActivity.this, millis -> updateTime(millis));
        mCurrentSession.getSessionType().observe(TimerActivity.this, sessionType -> {
            if (mStatusButton != null) {
                if (sessionType == SessionType.WORK) {
                    mStatusButton.setIcon(getResources().getDrawable(R.drawable.ic_status_goodtime));
                } else {
                    mStatusButton.setIcon(getResources().getDrawable(R.drawable.ic_break));
                }
            }
        });

        mCurrentSession.getTimerState().observe(TimerActivity.this, timerState -> {
            if (timerState == TimerState.INACTIVE) {
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
                    Toast.makeText(getBaseContext(), "Press the back button again to exit", LENGTH_SHORT)
                            .show();
                } catch (Throwable th) {
                    // ignoring this exception
                }
            }
            mBackPressedAt = System.currentTimeMillis();
        }
    }

    /**
     * Called when an event is posted to the EventBus
     * @param o holds the type of the Event
     */
    public void onEventMainThread(Object o) {
        if (!PreferenceHelper.isContinuousModeEnabled()) {
            if (o instanceof Constants.FinishWorkEvent) {
                showFinishDialog(SessionType.WORK);
            } else if (o instanceof Constants.FinishBreakEvent
                    || o instanceof Constants.FinishLongBreakEvent) {
                showFinishDialog(SessionType.BREAK);
            } else if (o instanceof Constants.ClearFinishDialogEvent) {
                if (mDialogSessionFinished != null) {
                    mDialogSessionFinished.cancel();
                }
            }
        }
    }

    public void updateTime(Long millis) {

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        seconds -= (minutes * 60);

        String currentTick = (minutes > 0 ? minutes : " ") + "." +
                format(Locale.US, "%02d", seconds);

        SpannableString currentFormattedTick = new SpannableString(currentTick);
        currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0,
                currentTick.indexOf("."), 0);

        mTimeLabel.setText(currentFormattedTick);
        Log.v(TAG, "drawing the time label.");
    }

    public void start(SessionType sessionType) {
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

    public void stop() {
        Intent stopIntent = new IntentWithAction(TimerActivity.this, TimerService.class, Constants.ACTION.STOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent);
        } else {
            startService(stopIntent);
        }
    }

    private void add60Seconds() {
        Intent stopIntent = new IntentWithAction(TimerActivity.this, TimerService.class, Constants.ACTION.ADD_SECONDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent);
        } else {
            startService(stopIntent);
        }
    }

    public void showEditLabelDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SelectLabelDialog.newInstance(this, PreferenceHelper.getCurrentSessionLabel(), false)
                .show(fragmentManager, "");
    }

    //TODO: extract strings
    public void showFinishDialog(SessionType sessionType) {

        Log.i(TAG, "Showing the finish dialog.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (sessionType == SessionType.WORK) {
            builder.setTitle("Session complete")
                    .setPositiveButton("Start break", (dialog, which) -> start(SessionType.BREAK))
                    .setNegativeButton("Add 60 seconds", (dialog, which) -> add60Seconds())
                    .setNeutralButton("Close", (dialog, which) -> EventBus.getDefault().post(new Constants.ClearNotificationEvent()))
                    .setOnCancelListener(dialog -> {
                        // do nothing
                    });
        } else {
            builder.setTitle("Break complete")
                    .setPositiveButton("Begin Session", (dialog, which) -> start(SessionType.WORK))
                    .setNeutralButton("Close", (dialog, which) -> EventBus.getDefault().post(new Constants.ClearNotificationEvent()))
                    .setOnCancelListener(dialog -> {
                        // do nothing
                    });
        }

        mDialogSessionFinished = builder.create();
        mDialogSessionFinished.setCanceledOnTouchOutside(false);
        mDialogSessionFinished.show();
    }

    private void toggleFullscreenMode() {
        if (PreferenceHelper.isFullscreenEnabled()) {
            if (mFullscreenHelper == null) {
                mFullscreenHelper = new FullscreenHelper(findViewById(R.id.main), getSupportActionBar());
            }
        } else {
            if (mFullscreenHelper != null) {
                mFullscreenHelper.disable();
                mFullscreenHelper = null;
            }
        }
    }

    public void toggleKeepScreenOn(boolean enabled) {
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
                    updateTime(TimeUnit.MINUTES.toMillis(PreferenceHelper.getSessionDuration(SessionType.WORK)));
                }
                break;
            case ENABLE_SCREEN_ON:
                toggleKeepScreenOn(PreferenceHelper.isScreenOnEnabled());
                break;
            case THEME:
                recreate();
                break;
            default:
                break;
        }
    }

    @Override
    public void onLabelSelected(LabelAndColor labelAndColor) {
        if (labelAndColor != null) {
            GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(labelAndColor.label);
            PreferenceHelper.setCurrentSessionLabel(labelAndColor.label);
        } else {
            GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(null);
        }
    }
}
