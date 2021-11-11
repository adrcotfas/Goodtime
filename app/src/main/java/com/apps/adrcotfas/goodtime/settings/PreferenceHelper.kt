/*
 * Copyright 2016-2021 Adrian Cotfas
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
package com.apps.adrcotfas.goodtime.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.apps.adrcotfas.goodtime.bl.SessionType
import com.apps.adrcotfas.goodtime.BuildConfig
import com.apps.adrcotfas.goodtime.database.Label
import com.apps.adrcotfas.goodtime.database.Profile
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.util.Constants
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class PreferenceHelper(val context: Context) {

    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val preferencesPrivate: SharedPreferences =
        context.getSharedPreferences(
            context.packageName + "_private_preferences",
            Context.MODE_PRIVATE
        )

    companion object {
        private const val PRO = "pref_blana"
        private const val PREFERENCES_VERSION = 2
        private const val PREFERENCES_VERSION_INTERNAL = "pref_version"
        private const val FIRST_RUN = "pref_first_run"
        const val PROFILE = "pref_profile"
        const val WORK_DURATION = "pref_work_duration"
        const val BREAK_DURATION = "pref_break_duration"
        const val ENABLE_LONG_BREAK = "pref_enable_long_break"
        const val LONG_BREAK_DURATION = "pref_long_break_duration"
        const val SESSIONS_BEFORE_LONG_BREAK = "pref_sessions_before_long_break"
        const val ENABLE_RINGTONE = "pref_enable_ringtone"
        const val PRIORITY_ALARM = "pref_priority_alarm"
        const val INSISTENT_RINGTONE = "pref_ringtone_insistent"
        const val RINGTONE_WORK_FINISHED = "pref_notification_sound_work"
        const val RINGTONE_BREAK_FINISHED = "pref_notification_sound_break"
        const val VIBRATION_TYPE = "pref_vibration_type"
        private const val ENABLE_FULLSCREEN = "pref_fullscreen"
        const val DISABLE_SOUND_AND_VIBRATION = "pref_disable_sound_and_vibration"
        const val DND_MODE = "pref_dnd"
        const val DISABLE_WIFI = "pref_disable_wifi"
        const val ENABLE_SCREEN_ON = "pref_keep_screen_on"
        const val ENABLE_SCREENSAVER_MODE = "pref_screen_saver"
        const val ENABLE_ONE_MINUTE_BEFORE_NOTIFICATION = "pref_one_minute_left_notification"
        const val AUTO_START_BREAK = "pref_auto_start_break"
        const val AUTO_START_WORK = "pref_auto_start_work"
        const val AMOLED = "pref_amoled"
        const val DISABLE_BATTERY_OPTIMIZATION = "pref_disable_battery_optimization"
        const val SAVE_CUSTOM_PROFILE = "pref_save_custom_profile"
        const val ENABLE_FLASHING_NOTIFICATION = "pref_flashing_notification"
        private const val WORK_STREAK = "pref_WORK_STREAK"
        private const val LAST_WORK_FINISHED_AT = "pref_last_work_finished_at"
        private const val CURRENT_SESSION_LABEL = "pref_current_session_label"
        private const val CURRENT_SESSION_COLOR = "pref_current_session_color"
        private const val INTRO_SNACKBAR_STEP = "pref_intro_snackbar_step"
        private const val INTRO_ARCHIVE_LABEL = "pref_intro_archive_label"
        const val TIMER_STYLE = "pref_timer_style"
        private const val SESSIONS_COUNTER = "pref_sessions_counter"
        private const val SHOW_CURRENT_LABEL = "pref_show_label"
        private const val ADD_60_SECONDS_COUNTER = "pref_add_60_seconds_times"
        private const val UNSAVED_PROFILE_ACTIVE = "pref_custom_pref_active"
        const val REMINDER_TIME = "pref_reminder_time"
        const val REMINDER_DAYS = "pref_reminder_days"
        const val WORK_DAY_START = "pref_work_day_start"
    }

    fun migratePreferences() {
        val version =
            preferences.getInt(PREFERENCES_VERSION_INTERNAL, 0)
        if (version == 0) {
            preferences.edit().clear().apply()
        } else if (version == 1) {
            val oldMinutesOnly = "2"
            if (timerStyle == oldMinutesOnly) {
                setTimerStyle(0)
            }
        }
        preferences.edit()
            .putInt(
                PREFERENCES_VERSION_INTERNAL, PREFERENCES_VERSION
            ).apply()
    }

    fun getSessionDuration(sessionType: SessionType?): Long {
        val duration: Long = when (sessionType) {
            SessionType.WORK -> preferences.getInt(WORK_DURATION, 25).toLong()
            SessionType.BREAK -> preferences.getInt(BREAK_DURATION, 5).toLong()
            SessionType.LONG_BREAK -> preferences.getInt(LONG_BREAK_DURATION, 15).toLong()
            else -> 42
        }
        return duration
    }


    fun isLongBreakEnabled() = preferences.getBoolean(ENABLE_LONG_BREAK, false)
    private fun getSessionsBeforeLongBreak() = preferences.getInt(SESSIONS_BEFORE_LONG_BREAK, 4)
    fun isRingtoneEnabled() = preferences.getBoolean(ENABLE_RINGTONE, true)
    fun isPriorityAlarm() = preferences.getBoolean(PRIORITY_ALARM, false)
    fun isRingtoneInsistent() = preferences.getBoolean(INSISTENT_RINGTONE, false)
    fun isFlashingNotificationEnabled() =
        preferences.getBoolean(ENABLE_FLASHING_NOTIFICATION, false)

    fun getNotificationSoundWorkFinished() = preferences.getString(RINGTONE_WORK_FINISHED, "")
    fun setNotificationSoundWorkFinished(newValue: String) =
        setNotificationSoundFinished(RINGTONE_WORK_FINISHED, newValue)

    fun getNotificationSoundBreakFinished() = preferences.getString(RINGTONE_BREAK_FINISHED, "")
    fun setNotificationSoundBreakFinished(newValue: String) =
        setNotificationSoundFinished(RINGTONE_BREAK_FINISHED, newValue)

    fun getNotificationSoundFinished(key: String): String? {
        assert(key == RINGTONE_WORK_FINISHED || key == RINGTONE_BREAK_FINISHED)
        return preferences
            .getString(key, "")
    }

    fun setNotificationSoundFinished(key: String, newValue: String?) {
        assert(key == RINGTONE_WORK_FINISHED || key == RINGTONE_BREAK_FINISHED)
        preferences.edit()
            .putString(key, newValue).apply()
    }

    fun getVibrationType() = preferences.getString(VIBRATION_TYPE, "2")!!.toInt()

    fun isFullscreenEnabled() = preferences.getBoolean(ENABLE_FULLSCREEN, false)
    fun isSoundAndVibrationDisabled() = preferences.getBoolean(DISABLE_SOUND_AND_VIBRATION, false)
    fun isDndModeActive() = preferences.getBoolean(DND_MODE, false)
    fun isWiFiDisabled() = preferences.getBoolean(DISABLE_WIFI, false)
    fun isScreenOnEnabled() = preferences.getBoolean(ENABLE_SCREEN_ON, true)
    fun isScreensaverEnabled() = preferences.getBoolean(ENABLE_SCREENSAVER_MODE, false)
    fun oneMinuteBeforeNotificationEnabled() =
        preferences.getBoolean(ENABLE_ONE_MINUTE_BEFORE_NOTIFICATION, false)

    fun isAutoStartBreak() = preferences.getBoolean(AUTO_START_BREAK, false)
    fun isAutoStartWork() = preferences.getBoolean(AUTO_START_WORK, false)
    fun isAmoledTheme() = preferences.getBoolean(AMOLED, true)

    /**
     * Increments the current completed work session streak but only if it's completed
     * in a reasonable time frame comparing with the last completed work session,
     * else it considers this session the first completed one in the streak.
     */
    fun incrementCurrentStreak() {
        // Add an extra 10 minutes to a work and break sessions duration
        // If the user did not complete another session in this time frame, just increment from 0.
        val maxDifference = TimeUnit.MINUTES.toMillis(
            getSessionDuration(SessionType.WORK)
                    + getSessionDuration(SessionType.BREAK)
                    + 20
        )
        val currentMillis = System.currentTimeMillis()
        val lastWorkFinishedAt = lastWorkFinishedAt()
        val increment = (lastWorkFinishedAt == 0L
                || currentMillis - lastWorkFinishedAt < maxDifference)
        val currentStreak = getCurrentStreak()
        preferencesPrivate.edit()
            .putInt(WORK_STREAK, if (increment) currentStreak + 1 else 1).apply()
        preferencesPrivate.edit()
            .putLong(LAST_WORK_FINISHED_AT, if (increment) currentMillis else 0).apply()
    }

    fun getCurrentStreak() = preferencesPrivate.getInt(WORK_STREAK, 0)

    fun lastWorkFinishedAt() = preferencesPrivate.getLong(LAST_WORK_FINISHED_AT, 0)
    fun resetCurrentStreak() {
        preferencesPrivate.edit()
            .putInt(WORK_STREAK, 0).apply()
        preferencesPrivate.edit()
            .putLong(LAST_WORK_FINISHED_AT, 0).apply()
    }

    fun itsTimeForLongBreak() = getCurrentStreak() >= getSessionsBeforeLongBreak()

    var currentSessionLabel: Label
        get() = Label(
            preferences
                .getString(
                    CURRENT_SESSION_LABEL, ""
                )!!,
            preferences.getInt(
                CURRENT_SESSION_COLOR, 0
            )
        )
        set(label) {
            preferences.edit()
                .putString(CURRENT_SESSION_LABEL, label.title).apply()
            preferences.edit()
                .putInt(CURRENT_SESSION_COLOR, label.colorId).apply()
        }


    fun isFirstRun() = preferencesPrivate.getBoolean(FIRST_RUN, true)

    fun consumeFirstRun() = preferencesPrivate.edit().putBoolean(FIRST_RUN, false).apply()

    fun setProfile25to5() {
        setUnsavedProfileActive(false)
        preferences.edit()
            .putString(PROFILE, Constants.PROFILE_NAME_DEFAULT)
            .putInt(WORK_DURATION, Constants.DEFAULT_WORK_DURATION_DEFAULT)
            .putInt(BREAK_DURATION, Constants.DEFAULT_BREAK_DURATION_DEFAULT)
            .putBoolean(ENABLE_LONG_BREAK, false)
            .putInt(LONG_BREAK_DURATION, Constants.DEFAULT_LONG_BREAK_DURATION)
            .putInt(SESSIONS_BEFORE_LONG_BREAK, Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK)
            .apply()
    }


    fun setProfile52to17() {
        setUnsavedProfileActive(false)
        preferences.edit()
            .putString(PROFILE, Constants.PROFILE_NAME_52_17)
            .putInt(WORK_DURATION, Constants.DEFAULT_WORK_DURATION_5217)
            .putInt(BREAK_DURATION, Constants.DEFAULT_BREAK_DURATION_5217)
            .putBoolean(ENABLE_LONG_BREAK, false)
            .putInt(LONG_BREAK_DURATION, Constants.DEFAULT_LONG_BREAK_DURATION)
            .putInt(SESSIONS_BEFORE_LONG_BREAK, Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK)
            .apply()
    }

    fun setProfile(profile: Profile) {
        setUnsavedProfileActive(false)
        preferences.edit()
            .putString(PROFILE, profile.name)
            .putInt(WORK_DURATION, profile.durationWork)
            .putInt(BREAK_DURATION, profile.durationBreak)
            .putBoolean(ENABLE_LONG_BREAK, profile.enableLongBreak)
            .putInt(LONG_BREAK_DURATION, profile.durationLongBreak)
            .putInt(SESSIONS_BEFORE_LONG_BREAK, profile.sessionsBeforeLongBreak)
            .apply()
    }

    val profile: String?
        get() = preferences
            .getString(
                PROFILE,
                context.resources.getString(R.string.pref_profile_default)
            )

    fun setTimerStyle(value: Int) =
        preferences.edit().putString(TIMER_STYLE, value.toString()).apply()

    val timerStyle: String?
        get() = preferences
            .getString(
                TIMER_STYLE, "0"
            )

    val isSessionsCounterEnabled: Boolean
        get() = preferences
            .getBoolean(
                SESSIONS_COUNTER, true
            )

    fun showCurrentLabel(): Boolean {
        return preferences
            .getBoolean(
                SHOW_CURRENT_LABEL, false
            )
    }

    var lastIntroStep: Int
        get() = preferencesPrivate.getInt(INTRO_SNACKBAR_STEP, 0)
        set(step) {
            preferencesPrivate.edit()
                .putInt(INTRO_SNACKBAR_STEP, step).apply()
        }

    /**
     * @return the number of times the current session timer was increased with "Add 60 seconds"
     */
    val add60SecondsCounter: Int
        get() = preferencesPrivate.getInt(ADD_60_SECONDS_COUNTER, 0)

    fun resetAdd60SecondsCounter() {
        preferencesPrivate.edit()
            .putInt(ADD_60_SECONDS_COUNTER, 0).apply()
    }

    fun increment60SecondsCounter() {
        preferencesPrivate.edit()
            .putInt(ADD_60_SECONDS_COUNTER, add60SecondsCounter + 1).apply()
    }

    fun isPro() = if (BuildConfig.F_DROID) {
        true
    } else {
        preferencesPrivate.getBoolean(PRO, false)
    }

    fun setPro(value: Boolean) = preferencesPrivate.edit()
        .putBoolean(PRO, value).apply()

    var archivedLabelHintWasShown: Boolean
        get() = preferencesPrivate
            .getBoolean(INTRO_ARCHIVE_LABEL, false)
        set(state) {
            preferencesPrivate.edit()
                .putBoolean(INTRO_ARCHIVE_LABEL, state).apply()
        }

    fun isUnsavedProfileActive() = preferencesPrivate.getBoolean(UNSAVED_PROFILE_ACTIVE, false)

    fun setUnsavedProfileActive(state: Boolean) =
        preferencesPrivate.edit().putBoolean(UNSAVED_PROFILE_ACTIVE, state).apply()

    fun isReminderEnabled() = getReminderDays().contains(true)
    fun isReminderEnabledFor(dayOfWeek: DayOfWeek) = getReminderDays()[dayOfWeek.ordinal]
    fun getReminderDays() = getBooleanArray(REMINDER_DAYS, 7)

    fun getReminderTime() = preferences.getInt(REMINDER_TIME, LocalTime.of(9, 0).toSecondOfDay())
    fun setReminderTime(secondOfDay: Int) = preferences.edit().putInt(REMINDER_TIME, secondOfDay).apply()

    fun getStartOfDay() = preferences.getInt(WORK_DAY_START, LocalTime.of(0, 0).toSecondOfDay())
    fun setStartOfDay(secondOfDay: Int) = preferences.edit().putInt(WORK_DAY_START, secondOfDay).apply()

    fun getBooleanArray(key: String, size: Int): BooleanArray {
        val result = BooleanArray(size)
        for (i in 0 until size) {
            result[i] = preferences.getBoolean(key + "_" + i, false)
        }
        return result
    }
    fun getStartOfDayDeltaMillis(): Long = TimeUnit.SECONDS.toMillis(getStartOfDay().toLong())
}