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

package com.apps.adrcotfas.goodtime.Settings;

import com.apps.adrcotfas.goodtime.BL.GoodtimeApplication;
import com.apps.adrcotfas.goodtime.BL.SessionType;
import com.apps.adrcotfas.goodtime.BuildConfig;
import com.apps.adrcotfas.goodtime.Label;
import com.apps.adrcotfas.goodtime.Profile;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.Constants;

import org.joda.time.LocalTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class PreferenceHelper {

    private static final String PRO = "pref_blana";

    private final static int PREFERENCES_VERSION = 2;
    private final static String PREFERENCES_VERSION_INTERNAL  = "pref_version";

    private final static String FIRST_RUN = "pref_first_run";
    public final static String PROFILE                     = "pref_profile";
    public final static String WORK_DURATION               = "pref_work_duration";
    public final static String BREAK_DURATION              = "pref_break_duration";
    public final static String ENABLE_LONG_BREAK           = "pref_enable_long_break";
    public final static String LONG_BREAK_DURATION         = "pref_long_break_duration";
    public final static String SESSIONS_BEFORE_LONG_BREAK  = "pref_sessions_before_long_break";
    public final static String ENABLE_RINGTONE             = "pref_enable_ringtone";
    public final static String PRIORITY_ALARM             = "pref_priority_alarm";
    public final static String INSISTENT_RINGTONE          = "pref_ringtone_insistent";
    public final static String RINGTONE_WORK_FINISHED      = "pref_ringtone";
    public final static String RINGTONE_BREAK_FINISHED     = "pref_ringtone_break";
    public final static String VIBRATION_TYPE             = "pref_vibration_type";
    private final static String ENABLE_FULLSCREEN           = "pref_fullscreen";
    public final static String DISABLE_SOUND_AND_VIBRATION = "pref_disable_sound_and_vibration";
    public final static String DND_MODE                    = "pref_dnd";
    public final static String DISABLE_WIFI                = "pref_disable_wifi";
    public final static String ENABLE_SCREEN_ON            = "pref_keep_screen_on";
    public final static String ENABLE_SCREENSAVER_MODE     = "pref_screen_saver";
    public final static String ENABLE_ONE_MINUTE_BEFORE_NOTIFICATION = "pref_one_minute_left_notification";
    public final static String AUTO_START_BREAK            = "pref_auto_start_break";
    public final static String AUTO_START_WORK             = "pref_auto_start_work";
    public final static String AMOLED                      = "pref_amoled";

    public final static String DISABLE_BATTERY_OPTIMIZATION = "pref_disable_battery_optimization";
    public static final String SAVE_CUSTOM_PROFILE          = "pref_save_custom_profile";
    public static final String ENABLE_FLASHING_NOTIFICATION  = "pref_flashing_notification";

    private final static String WORK_STREAK                 = "pref_WORK_STREAK";
    private final static String LAST_WORK_FINISHED_AT       = "pref_last_work_finished_at";

    private static final String CURRENT_SESSION_LABEL      = "pref_current_session_label";
    private static final String CURRENT_SESSION_COLOR      = "pref_current_session_color";
    private static final String INTRO_SNACKBAR_STEP        = "pref_intro_snackbar_step";
    private static final String INTRO_ARCHIVE_LABEL        = "pref_intro_archive_label";

    public static final String TIMER_STYLE                = "pref_timer_style";

    private static final String SESSIONS_COUNTER          = "pref_sessions_counter";
    private static final String SHOW_CURRENT_LABEL        = "pref_show_label";
    private static final String ADD_60_SECONDS_COUNTER    = "pref_add_60_seconds_times";

    private static final String UNSAVED_PROFILE_ACTIVE = "pref_custom_pref_active";

    public final static String ENABLE_REMINDER = "pref_enable_reminder";
    public final static String REMINDER_TIME_VALUE = "pref_reminder_time_value";
    public final static String REMINDER_WEEKDAYS_VALUE = "pref_reminder_weekdays_value";

    public static void migratePreferences() {
        int version = getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getInt(PREFERENCES_VERSION_INTERNAL, 0);
        if (version == 0) {
            getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit().clear().apply();
        } else if (version == 1) {
            final String OLD_MINUTES_ONLY = "2";
            if (PreferenceHelper.getTimerStyle().equals(OLD_MINUTES_ONLY)) {
                PreferenceHelper.setTimerStyle(0);
            }
        }
        getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit().putInt(PREFERENCES_VERSION_INTERNAL, PREFERENCES_VERSION).apply();
    }

    public static long getSessionDuration(SessionType sessionType) {
        final long duration;
        switch (sessionType) {
            case WORK:
                duration = getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                        .getInt(WORK_DURATION, 25);
                break;
            case BREAK:
                duration = getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                        .getInt(BREAK_DURATION, 5);
                break;
            case LONG_BREAK:
                duration = getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                        .getInt(LONG_BREAK_DURATION, 15);
                break;
            default:
                duration = 42;
                break;
        }
        return duration;
    }

    public static boolean isLongBreakEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_LONG_BREAK, false);
    }

    private static int getSessionsBeforeLongBreak() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getInt(SESSIONS_BEFORE_LONG_BREAK, 4);
    }

    public static boolean isRingtoneEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_RINGTONE, true);
    }

    public static boolean isPriorityAlarm() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(PRIORITY_ALARM, false);
    }

    public static boolean isRingtoneInsistent() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(INSISTENT_RINGTONE, false);
    }

    public static boolean isFlashingNotificationEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_FLASHING_NOTIFICATION, false);
    }

    public static String getNotificationSoundWorkFinished() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString(RINGTONE_WORK_FINISHED, "");
    }

    public static String getNotificationSoundBreakFinished() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString(RINGTONE_BREAK_FINISHED, "");
    }

    public static int getVibrationType() {
        return Integer.parseInt(getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                    .getString(VIBRATION_TYPE, "2" /*STRONG*/));
    }

    public static boolean isFullscreenEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_FULLSCREEN, false);
    }

    public static boolean isSoundAndVibrationDisabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(DISABLE_SOUND_AND_VIBRATION, false);
    }

    public static boolean isDndModeActive() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(DND_MODE, false);
    }

    public static boolean isWiFiDisabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(DISABLE_WIFI, false);
    }

    public static boolean isScreenOnEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_SCREEN_ON, true);
    }

    public static boolean isScreensaverEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_SCREENSAVER_MODE, false);
    }

    public static boolean oneMinuteBeforeNotificationEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_ONE_MINUTE_BEFORE_NOTIFICATION, false);
    }

    public static boolean isAutoStartBreak() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(AUTO_START_BREAK, false);
    }

    public static boolean isAutoStartWork() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(AUTO_START_WORK, false);
    }

    public static boolean isAmoledTheme() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(AMOLED, true);
    }

    /**
     * Increments the current completed work session streak but only if it's completed
     * in a reasonable time frame comparing with the last completed work session,
     * else it considers this session the first completed one in the streak.
     */
    public static void incrementCurrentStreak() {

        // Add an extra 10 minutes to a work and break sessions duration
        // If the user did not complete another session in this time frame, just increment from 0.
        final long maxDifference = TimeUnit.MINUTES.toMillis(PreferenceHelper.getSessionDuration(SessionType.WORK)
                + PreferenceHelper.getSessionDuration(SessionType.BREAK)
                + 20);

        final long currentMillis = System.currentTimeMillis();
        final long lastWorkFinishedAt = lastWorkFinishedAt();
        final boolean increment = (lastWorkFinishedAt == 0)
                || ((currentMillis - lastWorkFinishedAt) < maxDifference);

        final int currentStreak = getCurrentStreak();
        GoodtimeApplication.getPrivatePreferences().edit()
                .putInt(WORK_STREAK, increment ? currentStreak + 1 : 1).apply();

        GoodtimeApplication.getPrivatePreferences().edit()
                .putLong(LAST_WORK_FINISHED_AT, increment ? currentMillis: 0).apply();
    }

    public static int getCurrentStreak() {
        return GoodtimeApplication.getPrivatePreferences().getInt(WORK_STREAK, 0);
    }

    public static long lastWorkFinishedAt() {
        return GoodtimeApplication.getPrivatePreferences().getLong(LAST_WORK_FINISHED_AT, 0);
    }

    public static void resetCurrentStreak() {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putInt(WORK_STREAK, 0).apply();
        GoodtimeApplication.getPrivatePreferences().edit()
                .putLong(LAST_WORK_FINISHED_AT, 0).apply();
    }

    public static boolean itsTimeForLongBreak() {
        return getCurrentStreak() >= getSessionsBeforeLongBreak();
    }

    public static Label getCurrentSessionLabel() {
        return new Label(
                getDefaultSharedPreferences(GoodtimeApplication.getInstance()).getString(CURRENT_SESSION_LABEL, null),
                getDefaultSharedPreferences(GoodtimeApplication.getInstance()).getInt(CURRENT_SESSION_COLOR, 0));
    }

    public static void setCurrentSessionLabel(Label label) {
        getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit()
                .putString(CURRENT_SESSION_LABEL, label.title).apply();
        getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit()
                .putInt(CURRENT_SESSION_COLOR, label.colorId).apply();
    }

    public static boolean isFirstRun() {
        return GoodtimeApplication.getPrivatePreferences().getBoolean(FIRST_RUN, true);
    }

    public static void consumeFirstRun() {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putBoolean(FIRST_RUN, false).apply();
    }

    public static void setProfile25_5() {
        setUnsavedProfileActive(false);
        getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit()
                .putString(PROFILE, Constants.PROFILE_NAME_DEFAULT)
                .putInt(WORK_DURATION, Constants.DEFAULT_WORK_DURATION_DEFAULT)
                .putInt(BREAK_DURATION, Constants.DEFAULT_BREAK_DURATION_DEFAULT)
                .putBoolean(ENABLE_LONG_BREAK, false)
                .putInt(LONG_BREAK_DURATION, Constants.DEFAULT_LONG_BREAK_DURATION)
                .putInt(SESSIONS_BEFORE_LONG_BREAK, Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK)
                .apply();
    }

    public static void setProfile52_17() {
        setUnsavedProfileActive(false);
        getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit()
                .putString(PROFILE, Constants.PROFILE_NAME_52_17)
                .putInt(WORK_DURATION, Constants.DEFAULT_WORK_DURATION_5217)
                .putInt(BREAK_DURATION, Constants.DEFAULT_BREAK_DURATION_5217)
                .putBoolean(ENABLE_LONG_BREAK, false)
                .putInt(LONG_BREAK_DURATION, Constants.DEFAULT_LONG_BREAK_DURATION)
                .putInt(SESSIONS_BEFORE_LONG_BREAK, Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK)
                .apply();
    }

    public static void setProfile(Profile profile) {
        setUnsavedProfileActive(false);
        getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit()
                .putString(PROFILE, profile.name)
                .putInt(WORK_DURATION, profile.durationWork)
                .putInt(BREAK_DURATION, profile.durationBreak)
                .putBoolean(ENABLE_LONG_BREAK, profile.enableLongBreak)
                .putInt(LONG_BREAK_DURATION, profile.durationLongBreak)
                .putInt(SESSIONS_BEFORE_LONG_BREAK, profile.sessionsBeforeLongBreak)
                .apply();
    }

    public static String getProfile() {
            return getDefaultSharedPreferences(GoodtimeApplication.getInstance()).getString(PROFILE,
                    GoodtimeApplication.getInstance().getResources().getString(R.string.pref_profile_default));
    }

    public static void setTimerStyle(int value) {
        getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit().putString(TIMER_STYLE, String.valueOf(value)).apply();
    }

    public static String getTimerStyle() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance()).getString(TIMER_STYLE, "0");
    }

    public static boolean isSessionsCounterEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance()).getBoolean(SESSIONS_COUNTER, true);
    }

    public static boolean showCurrentLabel() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance()).getBoolean(SHOW_CURRENT_LABEL, false);
    }

    public static int getLastIntroStep() {
        return GoodtimeApplication.getPrivatePreferences().getInt(INTRO_SNACKBAR_STEP, 0);
    }

    public static void setLastIntroStep(int step) {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putInt(INTRO_SNACKBAR_STEP, step).apply();
    }

    /**
     * @return the number of times the current session timer was increased with "Add 60 seconds"
     */
    public static int getAdd60SecondsCounter() {
        return GoodtimeApplication.getPrivatePreferences().getInt(ADD_60_SECONDS_COUNTER, 0);
    }

    public static void resetAdd60SecondsCounter() {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putInt(ADD_60_SECONDS_COUNTER, 0).apply();
    }

    public static void increment60SecondsCounter() {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putInt(ADD_60_SECONDS_COUNTER, getAdd60SecondsCounter() + 1).apply();
    }

    public static void setPro(boolean value) {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putBoolean(PRO, value).apply();
    }

    public static boolean isPro() {
        if (BuildConfig.F_DROID) {
            return true;
        } else {
            return GoodtimeApplication.getPrivatePreferences().getBoolean(PRO, false);
        }
    }

    public static boolean getArchivedLabelHintWasShown() {
        return GoodtimeApplication.getPrivatePreferences()
                .getBoolean(INTRO_ARCHIVE_LABEL, false);
    }

    public static void setArchivedLabelHintWasShown(boolean state) {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putBoolean(INTRO_ARCHIVE_LABEL, state).apply();
    }

    public static boolean isUnsavedProfileActive() {
        return GoodtimeApplication.getPrivatePreferences()
                .getBoolean(UNSAVED_PROFILE_ACTIVE, false);
    }

    public static void setUnsavedProfileActive(boolean state) {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putBoolean(UNSAVED_PROFILE_ACTIVE, state).apply();
    }

    public static boolean isReminderEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_REMINDER, false);
    }

    public static long getTimeOfReminder() {
        long defaultTime = new LocalTime(9, 0).toDateTimeToday().getMillis();
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getLong(REMINDER_TIME_VALUE, defaultTime);
    }

    public static void setTimeOfReminder(long time) {
        getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit()
                .putLong(REMINDER_TIME_VALUE, time).apply();
    }

    public static void setWeekdaysOfReminder(List<Integer> selectedWeekdaysAsNumbers) {
        String weekdays = new JSONArray(selectedWeekdaysAsNumbers).toString();
        getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit()
                .putString(REMINDER_WEEKDAYS_VALUE, weekdays).apply();
    }

    public static List<Integer> getWeekdaysOfReminder() {
        String weekdaysString = getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString(REMINDER_WEEKDAYS_VALUE, "[]");
        List<Integer> selectedWeekdays = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(weekdaysString);
            for (int index = 0; index< jsonArray.length();index++){
                selectedWeekdays.add(jsonArray.getInt(index));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return selectedWeekdays;
    }
}
