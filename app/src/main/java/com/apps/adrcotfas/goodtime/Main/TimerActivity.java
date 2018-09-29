package com.apps.adrcotfas.goodtime.Main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.About.AboutActivity;
import com.apps.adrcotfas.goodtime.BL.CurrentSession;
import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.BL.NotificationHelper;
import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.BL.SessionType;
import com.apps.adrcotfas.goodtime.BL.TimerService;
import com.apps.adrcotfas.goodtime.BL.TimerState;
import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Settings.SettingsActivity;
import com.apps.adrcotfas.goodtime.Statistics.StatisticsActivity;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.Util.IntentWithAction;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
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
        extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = TimerActivity.class.getSimpleName();

    private final CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
    private AlertDialog mDialog;
    private FullscreenHelper mFullscreenHelper;
    private long mBackPressedAt;

    public void onStartButtonClick(View view) {
        start(SessionType.WORK);
    }
    public void onStopButtonClick(View view) {
        stop();
    }
    public void onSkipButtonClick(View view) {
        skip();
    }
    public void onAdd60SecondsButtonClick(View view) {
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

    ImageButton mStopButton;
    ImageButton mSkipButton;
    ImageButton mAddSecondsButton;
    ImageButton mStatusButton;
    TextView mTimeLabel;
    PopupMenu mLabelPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        ThemeHelper.setTheme(this);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mStopButton       = binding.stop;
        mSkipButton       = binding.skip;
        mTimeLabel        = binding.timeLabel;
        mAddSecondsButton = binding.add60Seconds;
        mStatusButton     = binding.status;

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(null);

        setupDrawer(binding.toolbar);
        setupLabelPopupMenu();
        setupEvents();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupEvents() {
        mCurrentSession.getDuration().observe(TimerActivity.this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long millis) {
                updateTime(millis);
            }
        });
        mCurrentSession.getSessionType().observe(TimerActivity.this, new Observer<SessionType>() {
            @Override
            public void onChanged(@Nullable SessionType sessionType) {
                if (sessionType == SessionType.WORK) {
                    mStatusButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_status_goodtime));
                } else {
                    mStatusButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_break));
                }
            }
        });

        mCurrentSession.getTimerState().observe(TimerActivity.this, new Observer<TimerState>() {
            @Override
            public void onChanged(@Nullable TimerState timerState) {
                if (timerState == TimerState.INACTIVE) {
                    mTimeLabel.clearAnimation();
                    disableButtons();
                } else if (timerState == TimerState.PAUSED) {
                    mTimeLabel.startAnimation(loadAnimation(getApplicationContext(), R.anim.blink));
                    enableButtons();
                } else {
                    mTimeLabel.clearAnimation();
                    enableButtons();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START);
        }

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

    private void setupDrawer(Toolbar toolbar) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, 0, 0);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void enableButtons() {
        mStopButton.setEnabled(true);
        mSkipButton.setEnabled(true);
        mAddSecondsButton.setEnabled(true);

        mAddSecondsButton.animate().alpha(1f).setDuration(100);
        mSkipButton.animate().alpha(1f).setDuration(100);
        mStopButton.animate().alpha(1f).setDuration(100);
    }

    private void disableButtons() {
        mStopButton.setEnabled(false);
        mSkipButton.setEnabled(false);
        mAddSecondsButton.setEnabled(false);

        mSkipButton.animate().alpha(0.5f).setDuration(100);
        mStopButton.animate().alpha(0.5f).setDuration(100);
        mAddSecondsButton.animate().alpha(0.5f).setDuration(100);
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
                if (mDialog != null) {
                    mDialog.cancel();
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

    private void setupLabelPopupMenu() {
        mLabelPopup = new PopupMenu(TimerActivity.this, mStatusButton);
        AppDatabase.getDatabase(getApplicationContext()).labelAndColor()
                .getLabels().observe(this, labels -> {
            mLabelPopup.getMenu().clear();
            mLabelPopup.getMenuInflater().inflate(R.menu.menu_main_select_label, mLabelPopup.getMenu());
            for (LabelAndColor label : labels) {
                AsyncTask.execute(() -> {
                    MenuItem item = mLabelPopup.getMenu().add(label.label);
                    SpannableString s = new SpannableString(item.getTitle());
                    final int color = AppDatabase.getDatabase(getApplicationContext()).labelAndColor().getColor(item.getTitle().toString());
                    s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
                    item.setTitle(s);
                });
            }
        });
    }

    public void showLabelPopup(View view) {
        mLabelPopup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_labels) {
                AsyncTask.execute(() -> {
                    //TODO: replace with dialog or fragment to add and edit labels
                    AppDatabase.getDatabase(getApplicationContext()).labelAndColor()
                            .addLabel(new LabelAndColor("art", getResources().getColor(R.color.pref_blue)));
                    AppDatabase.getDatabase(getApplicationContext()).labelAndColor()
                            .addLabel(new LabelAndColor("engineering", getResources().getColor(R.color.pref_red)));
                });
            } else {
                GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(item.getTitle().toString());
            }
            return true;
        });
        mLabelPopup.show();
    }

    //TODO: extract strings
    public void showFinishDialog(SessionType sessionType) {

        Log.i(TAG, "Showing the finish dialog.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (sessionType == SessionType.WORK) {
            builder.setTitle("Session complete")
                    .setPositiveButton("Start break", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            start(SessionType.BREAK);
                        }
                    })
                    .setNegativeButton("Add 60 seconds", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            add60Seconds();
                        }
                    })
                    .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EventBus.getDefault().post(new Constants.ClearNotificationEvent());
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // do nothing
                        }
                    });
        } else {
            builder.setTitle("Break complete")
                    .setPositiveButton("Begin Session", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            start(SessionType.WORK);
                        }
                    })
                    .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EventBus.getDefault().post(new Constants.ClearNotificationEvent());
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // do nothing
                        }
                    });
        }

        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.action_invite:
                break;
            case R.id.about_upgrade:
                break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_feedback:
                //TODO: move to proper place
                Intent statisticsIntent = new Intent(this, StatisticsActivity.class);
                startActivity(statisticsIntent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(Gravity.START);
        return true;
    }
}
