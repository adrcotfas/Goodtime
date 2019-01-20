/*
 * Copyright 2016-2019 Adrian Cotfas
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
import android.graphics.PorterDuff;
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

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
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
import com.apps.adrcotfas.goodtime.Upgrade.UpgradeActivity;
import com.apps.adrcotfas.goodtime.Util.Constants;
import com.apps.adrcotfas.goodtime.Util.IntentWithAction;
import com.apps.adrcotfas.goodtime.Util.OnSwipeTouchListener;
import com.apps.adrcotfas.goodtime.Util.ThemeHelper;
import com.apps.adrcotfas.goodtime.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import de.greenrobot.event.EventBus;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
import static android.view.animation.AnimationUtils.loadAnimation;
import static android.widget.Toast.LENGTH_SHORT;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.ENABLE_SCREENSAVER_MODE;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.ENABLE_SCREEN_ON;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.AMOLED;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.WORK_DURATION;
import static java.lang.String.format;

public class TimerActivity
        extends
        AppCompatActivity
        implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        SelectLabelDialog.OnLabelSelectedListener,
        BillingProcessor.IBillingHandler {

    private static final String TAG = TimerActivity.class.getSimpleName();

    private final CurrentSession mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
    private AlertDialog mDialogSessionFinished;
    private FullscreenHelper mFullscreenHelper;
    private BillingProcessor mBillingProcessor;
    private long mBackPressedAt;

    private MenuItem mStatusButton;
    private View mBoundsView;
    private TextView mTimeLabel;
    private Toolbar mToolbar;

    private static final String DIALOG_SELECT_LABEL_TAG = "dialogSelectLabel";

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

        mToolbar = binding.bar;
        mTimeLabel = binding.timeLabel;

        mBoundsView = binding.main;

        setupTimeLabelEvents();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);

        mBillingProcessor = BillingProcessor.newBillingProcessor(this, getString(R.string.licence_key), getString(R.string.merchant_id),  this);
        mBillingProcessor.initialize();

        // dismiss it at orientation change
        DialogFragment selectLabelDialog = ((DialogFragment)getSupportFragmentManager().findFragmentByTag(DIALOG_SELECT_LABEL_TAG));
        if (selectLabelDialog != null) {
            selectLabelDialog.dismiss();
        }
    }

    /**
     * Shows the tutorial snackbars
     */
    private void showTutorialSnackbars() {

        final int MESSAGE_SIZE = 5;
        int i = PreferenceHelper.getLastIntroStep();

        if (i < MESSAGE_SIZE) {

            List<String> messages = new ArrayList<>();

            messages.add(getString(R.string.tutorial_tap));
            messages.add(getString(R.string.tutorial_swipe_left));
            messages.add(getString(R.string.tutorial_swipe_up));
            messages.add(getString(R.string.tutorial_swipe_down));
            messages.add(getString(R.string.tutorial_long_click));

            Snackbar s = Snackbar.make(mToolbar, messages.get(PreferenceHelper.getLastIntroStep()), Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", view -> {
                        int nextStep = i + 1;
                        PreferenceHelper.setLastIntroStep(nextStep);
                        showTutorialSnackbars();
                    })
                    .setAnchorView(mToolbar)
                    // TODO: extract style to xml
                    .setActionTextColor(getResources().getColor(R.color.teal200));
            s.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.gray1000));
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
                onStartButtonClick();
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
    protected void onResume() {
        super.onResume();

        // initialize notification channels on the first run
        new NotificationHelper(this);

        // this is to refresh the current status icon color
        invalidateOptionsMenu();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);
        toggleKeepScreenOn(PreferenceHelper.isScreenOnEnabled());
        toggleFullscreenMode();

        if (mBillingProcessor != null) {
            mBillingProcessor.loadOwnedPurchasesFromGoogle();
        }
        showTutorialSnackbars();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mBillingProcessor != null) {
            mBillingProcessor.loadOwnedPurchasesFromGoogle();
        }
    }

    @Override
    protected void onDestroy() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);

        if (mBillingProcessor != null)
            mBillingProcessor.release();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        mStatusButton = menu.findItem(R.id.action_state);
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
            case R.id.action_current_label:
                if (PreferenceHelper.isPro()) {
                    showEditLabelDialog();
                } else {
                    UpgradeActivity.launchUpgradeActivity(this);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupEvents() {
        mCurrentSession.getDuration().observe(TimerActivity.this, this::updateTime);
        mCurrentSession.getSessionType().observe(TimerActivity.this, sessionType -> {
            if (mStatusButton != null) {
                if (sessionType == SessionType.WORK) {
                    mStatusButton.setIcon(getResources().getDrawable(R.drawable.ic_status_goodtime));
                } else {
                    mStatusButton.setIcon(getResources().getDrawable(R.drawable.ic_break));
                }
                setStatusIconColor();
            }
        });

        mCurrentSession.getTimerState().observe(TimerActivity.this, timerState -> {
            if (timerState == TimerState.INACTIVE) {
                if (mStatusButton != null) {
                    mStatusButton.setVisible(false);
                }
                final Handler handler = new Handler();
                handler.postDelayed(() -> mTimeLabel.clearAnimation(), 300);
            } else if (timerState == TimerState.PAUSED) {
                if (mStatusButton != null) {
                    mStatusButton.setVisible(true);
                }
                final Handler handler = new Handler();
                handler.postDelayed(() -> mTimeLabel.startAnimation(
                        loadAnimation(getApplicationContext(), R.anim.blink)), 300);
            } else {
                if (mStatusButton != null) {
                    mStatusButton.setVisible(true);
                }
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

    /**
     * Called when an event is posted to the EventBus
     * @param o holds the type of the Event
     */
    public void onEventMainThread(Object o) {
        if (!PreferenceHelper.isAutoStartBreak() && o instanceof Constants.FinishWorkEvent) {
            showFinishDialog(SessionType.WORK);
        } else if (!PreferenceHelper.isAutoStartWork() && (o instanceof Constants.FinishBreakEvent
                || o instanceof Constants.FinishLongBreakEvent)) {
            showFinishDialog(SessionType.BREAK);
        } else if (o instanceof Constants.ClearFinishDialogEvent) {
            if (mDialogSessionFinished != null) {
                mDialogSessionFinished.dismiss();
            }
        }
    }

    private void updateTime(Long millis) {

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        seconds -= (minutes * 60);

        String currentTick = (minutes > 0 ? minutes : " ") + "\u2009" +
                format(Locale.US, "%02d", seconds);

        SpannableString currentFormattedTick = new SpannableString(currentTick);
        currentFormattedTick.setSpan(new RelativeSizeSpan(2f), 0,
                currentTick.indexOf("\u2009"), 0);

        mTimeLabel.setText(currentFormattedTick);
        Log.v(TAG, "drawing the time label.");

        if (PreferenceHelper.isScreensaverEnabled() && seconds == 1 && mCurrentSession.getTimerState().getValue() != TimerState.PAUSED) {
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
        SelectLabelDialog.newInstance(this, PreferenceHelper.getCurrentSessionLabel().label, false)
                .show(fragmentManager, DIALOG_SELECT_LABEL_TAG);
    }

    private void showFinishDialog(SessionType sessionType) {

        Log.i(TAG, "Showing the finish dialog.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (sessionType == SessionType.WORK) {
            builder.setTitle(R.string.action_finished_session)
                    .setPositiveButton(R.string.action_start_break, (dialog, which) -> start(SessionType.BREAK))
                    .setNeutralButton(R.string.dialog_close, (dialog, which) -> EventBus.getDefault().post(new Constants.ClearNotificationEvent()))
                    .setOnCancelListener(dialog -> {
                        // do nothing
                    });
        } else {
            builder.setTitle(R.string.action_finished_break)
                    .setPositiveButton(R.string.action_start_work, (dialog, which) -> start(SessionType.WORK))
                    .setNeutralButton(android.R.string.cancel, (dialog, which) -> EventBus.getDefault().post(new Constants.ClearNotificationEvent()))
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

    private void setStatusIconColor() {
        LabelAndColor labelAndColor = PreferenceHelper.getCurrentSessionLabel();

        if (mStatusButton != null) {
            if (labelAndColor.label != null) {
                mStatusButton.getIcon().setColorFilter(
                        ThemeHelper.getColor(this, labelAndColor.color), PorterDuff.Mode.SRC_ATOP);
            } else {
                mStatusButton.getIcon().setColorFilter(
                        ThemeHelper.getColor(this, -1), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    public void onLabelSelected(LabelAndColor labelAndColor) {
        if (labelAndColor != null) {
            GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(labelAndColor.label);
            PreferenceHelper.setCurrentSessionLabel(labelAndColor);
        } else {
            GoodtimeApplication.getCurrentSessionManager().getCurrentSession().setLabel(null);
        }
        setStatusIconColor();
    }

    private void teleportTimeView() {

        int margin = ThemeHelper.dpToPx(this, 48);
        int maxX = mBoundsView.getWidth() - mTimeLabel.getWidth() - margin;
        int maxY = mBoundsView.getHeight() - mTimeLabel.getHeight() - margin;

        int boundX = maxX - margin;
        int boundY = maxY - margin;

        if (boundX > 0 && boundY > 0) {
            Random r = new Random();
            int newX = r.nextInt(boundX) + margin;
            int newY = r.nextInt(boundY) + margin;
            mTimeLabel.animate().x(newX).y(newY).setDuration(100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        // do nothing here
    }

    @Override
    public void onPurchaseHistoryRestored() {
        boolean found = false;
        for(String sku : mBillingProcessor.listOwnedProducts()) {
            if (sku.equals(UpgradeActivity.sku)) {
                found = true;
            }
        }
        if (found) {
            PreferenceHelper.setPro();
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        // do nothing here
    }

    @Override
    public void onBillingInitialized() {
        mBillingProcessor.loadOwnedPurchasesFromGoogle();
    }
}
