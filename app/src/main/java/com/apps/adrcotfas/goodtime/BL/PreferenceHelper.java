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

package com.apps.adrcotfas.goodtime.BL;

import android.os.SystemClock;

import com.apps.adrcotfas.goodtime.BuildConfig;
import com.apps.adrcotfas.goodtime.Label;

import java.util.concurrent.TimeUnit;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class PreferenceHelper {

    private static final String PRO = "pref_blana";

    private final static int PREFERENCES_VERSION = 1;
    private final static String PREFERENCES_VERSION_INTERNAL  = "pref_version";

    private final static String FIRST_RUN = "pref_first_run";
    public final static String PROFILE                     = "pref_profile";
    public final static String WORK_DURATION               = "pref_work_duration";
    public final static String BREAK_DURATION              = "pref_break_duration";
    public final static String ENABLE_LONG_BREAK           = "pref_enable_long_break";
    public final static String LONG_BREAK_DURATION         = "pref_long_break_duration";
    public final static String SESSIONS_BEFORE_LONG_BREAK  = "pref_sessions_before_long_break";
    public final static String ENABLE_RINGTONE             = "pref_enable_ringtone";
    public final static String INSISTENT_RINGTONE          = "pref_ringtone_insistent";
    public final static String RINGTONE_WORK_FINISHED      = "pref_ringtone";
    public final static String RINGTONE_BREAK_FINISHED     = "pref_ringtone_break";
    public final static String RINGTONE_BREAK_FINISHED_DUMMY = "pref_ringtone_break_dummy";
    private final static String ENABLE_VIBRATE              = "pref_vibrate";
    private final static String ENABLE_FULLSCREEN           = "pref_fullscreen";
    public final static String DISABLE_SOUND_AND_VIBRATION = "pref_disable_sound_and_vibration";
    private final static String DISABLE_WIFI                = "pref_disable_wifi";
    public final static String ENABLE_SCREEN_ON            = "pref_keep_screen_on";
    public final static String ENABLE_SCREENSAVER_MODE     = "pref_screen_saver";
    public final static String AUTO_START_BREAK            = "pref_auto_start_break";
    public final static String AUTO_START_WORK             = "pref_auto_start_work";
    public final static String AMOLED                      = "pref_amoled";

    public final static String DISABLE_BATTERY_OPTIMIZATION = "pref_disable_battery_optimization";

    private final static String WORK_STREAK                 = "pref_WORK_STREAK";
    private final static String LAST_WORK_FINISHED_AT       = "pref_last_work_finished_at";

    private static final String CURRENT_SESSION_LABEL      = "pref_current_session_label";
    private static final String CURRENT_SESSION_COLOR      = "pref_current_session_color";
    private static final String INTRO_SNACKBAR_STEP        = "pref_intro_snackbar_step";

    public static final String TIMER_STYLE                = "pref_timer_style";
    public static final String TIMER_STYLE_DUMMY          = "pref_timer_style_dummy";

    private static final String SESSIONS_COUNTER          = "pref_sessions_counter";
    private static final String ADD_60_SECONDS_COUNTER    = "pref_add_60_seconds_times";

    static void migratePreferences() {
        if (getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getInt(PREFERENCES_VERSION_INTERNAL, 0) == 0) {
            getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit().clear().apply();
            getDefaultSharedPreferences(GoodtimeApplication.getInstance()).edit().putInt(PREFERENCES_VERSION_INTERNAL, PREFERENCES_VERSION).apply();
        }
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

    static boolean isLongBreakEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_LONG_BREAK, false);
    }

    private static int getSessionsBeforeLongBreak() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getInt(SESSIONS_BEFORE_LONG_BREAK, 4);
    }

    static boolean isRingtoneEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_RINGTONE, true);
    }

    static boolean isRingtoneInsistent() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(INSISTENT_RINGTONE, false);
    }

    static String getNotificationSoundWorkFinished() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString(RINGTONE_WORK_FINISHED, "");
    }

    static String getNotificationSoundBreakFinished() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString(RINGTONE_BREAK_FINISHED, "");
    }

    static boolean isVibrationEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_VIBRATE, true);
    }

    public static boolean isFullscreenEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_FULLSCREEN, false);
    }

    static boolean isSoundAndVibrationDisabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(DISABLE_SOUND_AND_VIBRATION, false);
    }

    static boolean isWiFiDisabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(DISABLE_WIFI, false);
    }

    public static boolean isScreenOnEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_SCREEN_ON, false);
    }

    public static boolean isScreensaverEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_SCREENSAVER_MODE, false);
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
    static void incrementCurrentStreak() {

        // Add an extra 10 minutes to a work and break sessions duration
        // If the user did not complete another session in this time frame, just increment from 0.
        final long maxDifference = TimeUnit.MINUTES.toMillis(PreferenceHelper.getSessionDuration(SessionType.WORK)
                + PreferenceHelper.getSessionDuration(SessionType.BREAK)
                + 10);

        final long currentMillis = SystemClock.elapsedRealtime();
        final boolean increment = lastWorkFinishedAt() == 0
                || currentMillis - lastWorkFinishedAt() < maxDifference;

        GoodtimeApplication.getPrivatePreferences().edit()
                .putInt(WORK_STREAK, increment ? getCurrentStreak() + 1 : 1).apply();

        GoodtimeApplication.getPrivatePreferences().edit()
                .putLong(LAST_WORK_FINISHED_AT, increment ? currentMillis: 0).apply();
    }

    static int getCurrentStreak() {
        return GoodtimeApplication.getPrivatePreferences().getInt(WORK_STREAK, 0);
    }

    static long lastWorkFinishedAt() {
        return GoodtimeApplication.getPrivatePreferences().getLong(LAST_WORK_FINISHED_AT, 0);
    }

    static void resetCurrentStreak() {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putInt(WORK_STREAK, 0).apply();
        GoodtimeApplication.getPrivatePreferences().edit()
                .putLong(LAST_WORK_FINISHED_AT, 0).apply();
    }

    static boolean itsTimeForLongBreak() {
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

    public static String getTimerStyle() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance()).getString(TIMER_STYLE, "0");
    }

    public static boolean isSessionsCounterEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance()).getBoolean(SESSIONS_COUNTER, true);
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

    public static void setPro() {
        GoodtimeApplication.getPrivatePreferences().edit()
                .putBoolean(PRO, true).apply();
    }

    public static boolean isPro() {
        if (BuildConfig.F_DROID) {
            return true;
        } else {
            return GoodtimeApplication.getPrivatePreferences().getBoolean(PRO, false);
        }
    }
}
