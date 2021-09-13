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
package com.apps.adrcotfas.goodtime.main


import com.apps.adrcotfas.goodtime.settings.reminders.ReminderHelper.Companion.removeNotification
import com.apps.adrcotfas.goodtime.util.BatteryUtils.Companion.isIgnoringBatteryOptimizations
import dagger.hilt.android.AndroidEntryPoint
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog.OnLabelSelectedListener
import javax.inject.Inject
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.bl.CurrentSessionManager
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.apps.adrcotfas.goodtime.statistics.SessionViewModel
import com.apps.adrcotfas.goodtime.bl.SessionType
import com.apps.adrcotfas.goodtime.bl.TimerState
import android.content.Intent
import com.apps.adrcotfas.goodtime.bl.TimerService
import android.os.Build
import android.os.Bundle
import org.greenrobot.eventbus.EventBus
import androidx.databinding.DataBindingUtil
import com.apps.adrcotfas.goodtime.R
import com.kobakei.ratethisapp.RateThisApp
import androidx.lifecycle.ViewModelProvider
import android.view.animation.Animation
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.BaseTransientBottomBar
import android.annotation.SuppressLint
import com.apps.adrcotfas.goodtime.settings.SettingsActivity
import com.apps.adrcotfas.goodtime.bl.NotificationHelper
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.content.DialogInterface
import com.apps.adrcotfas.goodtime.bl.CurrentSession
import android.widget.Toast
import android.widget.FrameLayout
import org.greenrobot.eventbus.Subscribe
import com.apps.adrcotfas.goodtime.util.Constants.FinishWorkEvent
import com.apps.adrcotfas.goodtime.util.Constants.FinishBreakEvent
import com.apps.adrcotfas.goodtime.util.Constants.FinishLongBreakEvent
import com.apps.adrcotfas.goodtime.util.Constants.StartSessionEvent
import com.apps.adrcotfas.goodtime.util.Constants.OneMinuteLeft
import com.apps.adrcotfas.goodtime.statistics.main.SelectLabelDialog
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.apps.adrcotfas.goodtime.BuildConfig
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.database.Session
import com.apps.adrcotfas.goodtime.databinding.ActivityMainBinding
import com.apps.adrcotfas.goodtime.ui.ActivityWithBilling
import com.apps.adrcotfas.goodtime.util.*
import com.apps.adrcotfas.goodtime.util.Constants.ClearNotificationEvent
import org.joda.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TimerActivity : ActivityWithBilling(), OnSharedPreferenceChangeListener,
    OnLabelSelectedListener,
    FinishedSessionDialog.Listener {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var currentSessionManager: CurrentSessionManager

    private var mDialogSessionFinished: FinishedSessionDialog? = null
    private var mFullscreenHelper: FullscreenHelper? = null
    private var mBackPressedAt: Long = 0
    private var mBlackCover: View? = null
    private var mWhiteCover: View? = null
    private var mLabelButton: MenuItem? = null
    private var mBoundsView: View? = null
    private var mTimeLabel: TextView? = null
    private lateinit var mToolbar: Toolbar
    private var mTutorialDot: ImageView? = null
    private var mLabelChip: Chip? = null
    private var mSessionViewModel: SessionViewModel? = null
    private var mViewModel: TimerActivityViewModel? = null
    private var mSessionsCounterText: TextView? = null
    private var mCurrentSessionType = SessionType.INVALID
    fun onStartButtonClick(view: View) {
        start(SessionType.WORK)
    }

    fun onStopButtonClick() {
        stop()
    }

    fun onSkipButtonClick() {
        skip()
    }

    fun onAdd60SecondsButtonClick() {
        add60Seconds()
    }

    private fun skip() {
        if (currentSession.timerState.value !== TimerState.INACTIVE) {
            val skipIntent: Intent = IntentWithAction(
                this@TimerActivity, TimerService::class.java,
                Constants.ACTION.SKIP
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(skipIntent)
            } else {
                startService(skipIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        if (preferenceHelper.isFirstRun()) {
            // show app intro
            val i = Intent(this@TimerActivity, MainIntroActivity::class.java)
            startActivity(i)
            preferenceHelper.consumeFirstRun()
        }
        ThemeHelper.setTheme(this, preferenceHelper.isAmoledTheme())
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBlackCover = binding.blackCover
        mWhiteCover = binding.whiteCover
        mToolbar = binding.bar
        mTimeLabel = binding.timeLabel
        mTutorialDot = binding.tutorialDot
        mBoundsView = binding.main
        mLabelChip = binding.labelView
        mLabelChip!!.setOnClickListener { showEditLabelDialog() }
        setupTimeLabelEvents()
        setSupportActionBar(mToolbar)
        supportActionBar?.title = null

        // dismiss it at orientation change
        val selectLabelDialog =
            supportFragmentManager.findFragmentByTag(DIALOG_SELECT_LABEL_TAG) as DialogFragment?
        selectLabelDialog?.dismiss()
        if (!BuildConfig.F_DROID) {
            // Monitor launch times and interval from installation
            RateThisApp.onCreate(this)
            // If the condition is satisfied, "Rate this app" dialog will be shown
            RateThisApp.showRateDialogIfNeeded(this)
        }
        mViewModel = ViewModelProvider(this).get(TimerActivityViewModel::class.java)
    }

    override fun showSnackBar(@StringRes resourceId: Int) {
        if (this::binding.isInitialized) {
            Snackbar.make(binding.root, getString(resourceId), Snackbar.LENGTH_LONG).setAnchorView(mToolbar).show()
        }
    }

    /**
     * Shows the tutorial snackbars
     */
    private fun showTutorialSnackbars() {
        val messageSize = 4
        val i = preferenceHelper.lastIntroStep
        if (i < messageSize) {
            val messages = listOf(
                getString(R.string.tutorial_tap),
                getString(R.string.tutorial_swipe_left),
                getString(R.string.tutorial_swipe_up),
                getString(R.string.tutorial_swipe_down)
            )
            val animations = listOf(
                AnimationUtils.loadAnimation(applicationContext, R.anim.tutorial_tap),
                AnimationUtils.loadAnimation(applicationContext, R.anim.tutorial_swipe_right),
                AnimationUtils.loadAnimation(applicationContext, R.anim.tutorial_swipe_up),
                AnimationUtils.loadAnimation(applicationContext, R.anim.tutorial_swipe_down)
            )
            mTutorialDot!!.visibility = View.VISIBLE
            mTutorialDot!!.animate().translationX(0f).translationY(0f)
            mTutorialDot!!.clearAnimation()
            mTutorialDot!!.animation = animations[preferenceHelper.lastIntroStep]
            val s = Snackbar.make(
                mToolbar,
                messages[preferenceHelper.lastIntroStep],
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("OK") {
                    val nextStep = i + 1
                    preferenceHelper.lastIntroStep = nextStep
                    showTutorialSnackbars()
                }
                .setAnchorView(mToolbar)
            s.behavior = object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View): Boolean {
                    return false
                }
            }
            s.show()
        } else {
            mTutorialDot!!.animate().translationX(0f).translationY(0f)
            mTutorialDot!!.clearAnimation()
            mTutorialDot!!.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTimeLabelEvents() {
        mTimeLabel!!.setOnTouchListener(object : OnSwipeTouchListener(this@TimerActivity) {
            public override fun onSwipeRight(view: View) {
                onSkipSession()
            }

            public override fun onSwipeLeft(view: View) {
                onSkipSession()
            }

            public override fun onSwipeBottom(view: View) {
                onStopSession()
                if (preferenceHelper.isScreensaverEnabled()) {
                    recreate()
                }
            }

            public override fun onSwipeTop(view: View) {
                if (currentSession.timerState.value !== TimerState.INACTIVE) {
                    onAdd60SecondsButtonClick()
                }
            }

            public override fun onClick(view: View) {
                onStartButtonClick(view)
            }

            public override fun onLongClick(view: View) {
                val settingsIntent = Intent(this@TimerActivity, SettingsActivity::class.java)
                startActivity(settingsIntent)
            }

            public override fun onPress(view: View) {
                mTimeLabel!!.startAnimation(
                    AnimationUtils.loadAnimation(
                        applicationContext, R.anim.scale_reversed
                    )
                )
            }

            public override fun onRelease(view: View) {
                mTimeLabel!!.startAnimation(
                    AnimationUtils.loadAnimation(
                        applicationContext, R.anim.scale
                    )
                )
                if (currentSession.timerState.value === TimerState.PAUSED) {
                    val handler = Handler()
                    handler.postDelayed({
                        mTimeLabel!!.startAnimation(
                            AnimationUtils.loadAnimation(applicationContext, R.anim.blink)
                        )
                    }, 300)
                }
            }
        })
    }

    private fun onSkipSession() {
        if (currentSession.timerState.value !== TimerState.INACTIVE) {
            onSkipButtonClick()
        }
    }

    private fun onStopSession() {
        if (currentSession.timerState.value !== TimerState.INACTIVE) {
            onStopButtonClick()
        }
    }

    override fun onAttachedToWindow() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    override fun onPause() {
        super.onPause()
        mViewModel!!.isActive = false
    }

    override fun onResume() {
        super.onResume()
        removeNotification(applicationContext)
        mViewModel!!.isActive = true
        if (mViewModel!!.showFinishDialog) {
            showFinishedSessionUI()
        }

        // initialize notification channels on the first run
        if (preferenceHelper.isFirstRun()) {
            NotificationHelper(this)
        }

        // this is to refresh the current status icon color
        invalidateOptionsMenu()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.registerOnSharedPreferenceChangeListener(this)
        toggleKeepScreenOn(preferenceHelper.isScreenOnEnabled())
        toggleFullscreenMode()
        showTutorialSnackbars()
        setTimeLabelColor()
        mBlackCover!!.animate().alpha(0f).duration = 500

        // then only reason we're doing this here is because a FinishSessionEvent
        // comes together with a "bring activity on top"
        if (preferenceHelper.isFlashingNotificationEnabled() && mViewModel!!.enableFlashingNotification) {
            mWhiteCover!!.visibility = View.VISIBLE
            if (preferenceHelper.isAutoStartBreak() && (mCurrentSessionType === SessionType.BREAK || mCurrentSessionType === SessionType.LONG_BREAK)
                || preferenceHelper.isAutoStartWork() && mCurrentSessionType === SessionType.WORK
            ) {
                startFlashingNotificationShort()
            } else {
                startFlashingNotification()
            }
        }
    }

    override fun onDestroy() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.unregisterOnSharedPreferenceChangeListener(this)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_main, menu)
        val batteryButton = menu.findItem(R.id.action_battery_optimization)
        batteryButton.isVisible = !isIgnoringBatteryOptimizations(this)
        mLabelButton = menu.findItem(R.id.action_current_label).also {
            it.icon.setColorFilter(
                ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_ALL_LABELS),
                PorterDuff.Mode.SRC_ATOP
            )
        }
        //TODO: move this to onResume
        setupEvents()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val bottomNavigationDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavigationDrawerFragment.show(
                    supportFragmentManager,
                    bottomNavigationDrawerFragment.tag
                )
            }
            R.id.action_battery_optimization -> showBatteryOptimizationDialog()
            R.id.action_current_label -> showEditLabelDialog()
            R.id.action_sessions_counter -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.action_reset_counter_title)
                    .setMessage(R.string.action_reset_counter)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        mSessionViewModel!!.deleteSessionsFinishedToday()
                        preferenceHelper.resetCurrentStreak()
                    }
                    .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
                    .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showBatteryOptimizationDialog() {
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:" + this.packageName)
        startActivity(intent)
    }

    private fun setupEvents() {
        currentSession.duration.observe(
            this,
            { millis: Long -> updateTimeLabel(millis) })
        currentSession.sessionType.observe(this, { sessionType: SessionType ->
            mCurrentSessionType = sessionType
            setupLabelView()
            setTimeLabelColor()
        })
        currentSession.timerState.observe(this, { timerState: TimerState ->
            when {
                timerState === TimerState.INACTIVE -> {
                    setupLabelView()
                    setTimeLabelColor()
                    val handler = Handler()
                    handler.postDelayed({ mTimeLabel!!.clearAnimation() }, 300)
                }
                timerState === TimerState.PAUSED -> {
                    val handler = Handler()
                    handler.postDelayed({
                        mTimeLabel!!.startAnimation(
                            AnimationUtils.loadAnimation(applicationContext, R.anim.blink)
                        )
                    }, 300)
                }
                else -> {
                    val handler = Handler()
                    handler.postDelayed({ mTimeLabel!!.clearAnimation() }, 300)
                }
            }
        })
    }

    private val currentSession: CurrentSession
        get() = currentSessionManager.currentSession

    @SuppressLint("WrongConstant")
    override fun onBackPressed() {
        if (currentSession.timerState.value !== TimerState.INACTIVE) {
            moveTaskToBack(true)
        } else {
            if (mBackPressedAt + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
            } else {
                try {
                    Toast.makeText(
                        baseContext,
                        R.string.action_press_back_button,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } catch (th: Throwable) {
                    // ignoring this exception
                }
            }
            mBackPressedAt = System.currentTimeMillis()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val alertMenuItem = menu.findItem(R.id.action_sessions_counter)
        alertMenuItem.isVisible = false
        val sessionsCounterEnabled = preferenceHelper.isSessionsCounterEnabled
        if (sessionsCounterEnabled) {
            val mSessionsCounter = alertMenuItem.actionView as FrameLayout
            mSessionsCounterText = mSessionsCounter.findViewById(R.id.view_alert_count_textview)
            mSessionsCounter.setOnClickListener { onOptionsItemSelected(alertMenuItem) }
            mSessionViewModel = ViewModelProvider(this).get(SessionViewModel::class.java)
            mSessionViewModel!!.allSessionsByEndTime.observe(this, { sessions: List<Session> ->
                val today = LocalDate()
                var statsToday = 0
                for (s in sessions) {
                    val crt = LocalDate(Date(s.timestamp))
                    if (crt.isEqual(today)) {
                        statsToday++
                    }
                    if (crt.isBefore(today)) {
                        break
                    }
                }
                if (mSessionsCounterText != null) {
                    mSessionsCounterText!!.text = statsToday.toString()
                }
                alertMenuItem.isVisible = true
            })
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Called when an event is posted to the EventBus
     * @param o holds the type of the Event
     */
    @Subscribe
    fun onEventMainThread(o: Any?) {
        if (o is FinishWorkEvent) {
            if (preferenceHelper.isAutoStartBreak()) {
                if (preferenceHelper.isFlashingNotificationEnabled()) {
                    mViewModel!!.enableFlashingNotification = true
                }
            } else {
                mViewModel!!.dialogPendingType = SessionType.WORK
                showFinishedSessionUI()
            }
        } else if (o is FinishBreakEvent || o is FinishLongBreakEvent) {
            if (preferenceHelper.isAutoStartWork()) {
                if (preferenceHelper.isFlashingNotificationEnabled()) {
                    mViewModel!!.enableFlashingNotification = true
                }
            } else {
                mViewModel!!.dialogPendingType = SessionType.BREAK
                showFinishedSessionUI()
            }
        } else if (o is StartSessionEvent) {
            if (mDialogSessionFinished != null) {
                mDialogSessionFinished!!.dismissAllowingStateLoss()
            }
            mViewModel!!.showFinishDialog = false
            if (!preferenceHelper.isAutoStartBreak() && !preferenceHelper.isAutoStartWork()) {
                stopFlashingNotification()
            }
        } else if (o is OneMinuteLeft) {
            if (preferenceHelper.isFlashingNotificationEnabled()) {
                startFlashingNotificationShort()
            }
        }
    }

    private fun updateTimeLabel(millis: Long) {
        var seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        seconds -= minutes * 60
        val currentFormattedTick: String
        val isMinutesStyle =
            preferenceHelper.timerStyle == resources.getString(R.string.pref_timer_style_minutes_value)
        currentFormattedTick = if (isMinutesStyle) {
            TimeUnit.SECONDS.toMinutes(minutes * 60 + seconds + 59).toString()
        } else {
            ((if (minutes > 9) minutes else "0$minutes")
                .toString() + ":"
                    + if (seconds > 9) seconds else "0$seconds")
        }
        mTimeLabel!!.text = currentFormattedTick
        Log.v(TAG, "drawing the time label.")
        if (preferenceHelper.isScreensaverEnabled() && seconds == 1L && currentSession.timerState.value !== TimerState.PAUSED) {
            teleportTimeView()
        }
    }

    private fun start(sessionType: SessionType) {
        var startIntent = Intent()
        when (currentSession.timerState.value) {
            TimerState.INACTIVE -> startIntent = IntentWithAction(
                this@TimerActivity, TimerService::class.java,
                Constants.ACTION.START, sessionType
            )
            TimerState.ACTIVE, TimerState.PAUSED -> startIntent = IntentWithAction(
                this@TimerActivity,
                TimerService::class.java,
                Constants.ACTION.TOGGLE
            )
            else -> Log.wtf(TAG, "Invalid timer state.")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent)
        } else {
            startService(startIntent)
        }
    }

    private fun stop() {
        val stopIntent: Intent =
            IntentWithAction(this@TimerActivity, TimerService::class.java, Constants.ACTION.STOP)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent)
        } else {
            startService(stopIntent)
        }
        mWhiteCover!!.visibility = View.GONE
        mWhiteCover!!.clearAnimation()
    }

    private fun add60Seconds() {
        val stopIntent: Intent = IntentWithAction(
            this@TimerActivity,
            TimerService::class.java,
            Constants.ACTION.ADD_SECONDS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(stopIntent)
        } else {
            startService(stopIntent)
        }
    }

    private fun showEditLabelDialog() {
        val fragmentManager = supportFragmentManager
        SelectLabelDialog.newInstance(this, preferenceHelper.currentSessionLabel.title, false)
            .show(fragmentManager, DIALOG_SELECT_LABEL_TAG)
    }

    private fun showFinishedSessionUI() {
        if (mViewModel!!.isActive) {
            mViewModel!!.showFinishDialog = false
            mViewModel!!.enableFlashingNotification = true
            Log.i(TAG, "Showing the finish dialog.")
            mDialogSessionFinished = FinishedSessionDialog.newInstance(this)
                .also { it.show(supportFragmentManager, TAG) }
        } else {
            mViewModel!!.showFinishDialog = true
            mViewModel!!.enableFlashingNotification = false
        }
    }

    private fun toggleFullscreenMode() {
        if (preferenceHelper.isFullscreenEnabled()) {
            if (mFullscreenHelper == null) {
                mFullscreenHelper = FullscreenHelper(findViewById(R.id.main), supportActionBar)
            } else {
                mFullscreenHelper!!.hide()
            }
        } else {
            if (mFullscreenHelper != null) {
                mFullscreenHelper!!.disable()
                mFullscreenHelper = null
            }
        }
    }

    private fun delayToggleFullscreenMode() {
        val handler = Handler()
        handler.postDelayed({ toggleFullscreenMode() }, 300)
    }

    private fun toggleKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PreferenceHelper.WORK_DURATION -> if (currentSession.timerState.value
                === TimerState.INACTIVE
            ) {
                currentSession.setDuration(
                    TimeUnit.MINUTES.toMillis(
                        preferenceHelper.getSessionDuration(SessionType.WORK)
                    )
                )
            }
            PreferenceHelper.ENABLE_SCREEN_ON -> toggleKeepScreenOn(preferenceHelper.isScreenOnEnabled())
            PreferenceHelper.AMOLED -> {
                recreate()
                if (!preferenceHelper.isScreensaverEnabled()) {
                    recreate()
                }
            }
            PreferenceHelper.ENABLE_SCREENSAVER_MODE -> if (!preferenceHelper.isScreensaverEnabled()) {
                recreate()
            }
            else -> {
            }
        }
    }

    private fun setupLabelView() {
        val label = preferenceHelper.currentSessionLabel
        if (isInvalidLabel(label)) {
            mLabelChip!!.visibility = View.GONE
            mLabelButton!!.isVisible = true
            val color = ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_ALL_LABELS)
            mLabelButton!!.icon.setColorFilter(
                color, PorterDuff.Mode.SRC_ATOP
            )
        } else {
            val color = ThemeHelper.getColor(this, label.colorId)
            if (preferenceHelper.showCurrentLabel()) {
                mLabelButton!!.isVisible = false
                mLabelChip!!.visibility = View.VISIBLE
                mLabelChip!!.text = label.title
                mLabelChip!!.chipBackgroundColor = ColorStateList.valueOf(color)
            } else {
                mLabelChip!!.visibility = View.GONE
                mLabelButton!!.isVisible = true
                mLabelButton!!.icon.setColorFilter(
                    color, PorterDuff.Mode.SRC_ATOP
                )
            }
        }
    }

    private fun isInvalidLabel(label: Label): Boolean {
        return label.title == "" || label.title == getString(R.string.label_unlabeled)
    }

    private fun setTimeLabelColor() {
        val label = preferenceHelper.currentSessionLabel
        if (mTimeLabel != null) {
            if (mCurrentSessionType === SessionType.BREAK || mCurrentSessionType === SessionType.LONG_BREAK) {
                mTimeLabel!!.setTextColor(ThemeHelper.getColor(this, ThemeHelper.COLOR_INDEX_BREAK))
                return
            }
            if (!isInvalidLabel(label)) {
                mTimeLabel!!.setTextColor(ThemeHelper.getColor(this, label.colorId))
            } else {
                mTimeLabel!!.setTextColor(
                    ThemeHelper.getColor(
                        this,
                        ThemeHelper.COLOR_INDEX_UNLABELED
                    )
                )
            }
        }
    }

    override fun onLabelSelected(label: Label) {
        currentSession.setLabel(label.title)
        preferenceHelper.currentSessionLabel = label
        setupLabelView()
        setTimeLabelColor()
    }

    private fun teleportTimeView() {
        val margin = ThemeHelper.dpToPx(this, 48f)
        val maxX = mBoundsView!!.width - mTimeLabel!!.width - margin
        val maxY = mBoundsView!!.height - mTimeLabel!!.height - margin
        val boundX = maxX - margin
        val boundY = maxY - margin
        if (boundX > 0 && boundY > 0) {
            val r = Random()
            val newX = r.nextInt(boundX) + margin
            val newY = r.nextInt(boundY) + margin
            mTimeLabel!!.animate().x(newX.toFloat()).y(newY.toFloat()).duration = 100
        }
    }

    override fun onFinishedSessionDialogPositiveButtonClick(sessionType: SessionType) {
        if (sessionType === SessionType.WORK) {
            start(SessionType.BREAK)
        } else {
            start(SessionType.WORK)
        }
        delayToggleFullscreenMode()
        stopFlashingNotification()
    }

    override fun onFinishedSessionDialogNeutralButtonClick(sessionType: SessionType) {
        EventBus.getDefault().post(ClearNotificationEvent())
        delayToggleFullscreenMode()
        stopFlashingNotification()
    }

    private fun startFlashingNotification() {
        mWhiteCover!!.visibility = View.VISIBLE
        mWhiteCover!!.startAnimation(
            AnimationUtils.loadAnimation(
                applicationContext, R.anim.blink_screen
            )
        )
    }

    private fun startFlashingNotificationShort() {
        mWhiteCover!!.visibility = View.VISIBLE
        val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.blink_screen_3_times)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                stopFlashingNotification()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        mWhiteCover!!.startAnimation(anim)
    }

    private fun stopFlashingNotification() {
        mWhiteCover!!.visibility = View.GONE
        mWhiteCover!!.clearAnimation()
        mViewModel!!.enableFlashingNotification = false
    }

    companion object {
        private val TAG = TimerActivity::class.java.simpleName
        private const val DIALOG_SELECT_LABEL_TAG = "dialogSelectLabel"
    }
}