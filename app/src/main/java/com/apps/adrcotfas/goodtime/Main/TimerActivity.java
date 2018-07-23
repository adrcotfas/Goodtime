package com.apps.adrcotfas.goodtime.Main;

import androidx.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.databinding.DataBindingUtil;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apps.adrcotfas.goodtime.BL.CurrentSession;
import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.BL.NotificationHelper;
import com.apps.adrcotfas.goodtime.BL.PreferenceHelper;
import com.apps.adrcotfas.goodtime.BL.SessionType;
import com.apps.adrcotfas.goodtime.BL.TimerState;
import com.apps.adrcotfas.goodtime.Settings.SettingsActivity;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.BL.TimerService;
import com.apps.adrcotfas.goodtime.Util.IntentWithAction;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.ENABLE_SCREEN_ON;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.THEME;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.WORK_DURATION;

public class TimerActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = TimerActivity.class.getSimpleName();

    private final CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
    private AlertDialog mDialog;
    private FullscreenHelper mFullscreenHelper;

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
    TextView mTimeLabel;

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

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(null);
        
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
                //TODO: observe SessionType to show an icon
            }
        });

        mCurrentSession.getTimerState().observe(TimerActivity.this, new Observer<TimerState>() {
            @Override
            public void onChanged(@Nullable TimerState timerState) {
                if (timerState == TimerState.INACTIVE) {
                    //TODO: animate timer
                    mStopButton.setEnabled(false);
                    mSkipButton.setEnabled(false);
                    mAddSecondsButton.setEnabled(false);
                } else {
                    //TODO: stop animating timer
                    mStopButton.setEnabled(true);
                    mSkipButton.setEnabled(true);
                    mAddSecondsButton.setEnabled(true);
                }
            }
        });
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
        mTimeLabel.setText(Long.toString(TimeUnit.MILLISECONDS.toSeconds(millis)));
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
                    .setNegativeButton("Skip break", new DialogInterface.OnClickListener() {
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
}
