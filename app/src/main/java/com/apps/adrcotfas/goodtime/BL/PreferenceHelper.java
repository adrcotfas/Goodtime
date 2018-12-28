package com.apps.adrcotfas.goodtime.BL;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class PreferenceHelper {

    private final static String FIRST_RUN = "pref_first_run";

    public final static String PROFILE                     = "pref_profile";
    public final static String WORK_DURATION               = "pref_work_duration";
    public final static String BREAK_DURATION              = "pref_break_duration";
    public final static String ENABLE_LONG_BREAK           = "pref_enable_long_break";
    public final static String LONG_BREAK_DURATION         = "pref_long_break_duration";
    public final static String SESSIONS_BEFORE_LONG_BREAK  = "pref_sessions_before_long_break";
    public final static String ENABLE_RINGTONE             = "pref_enable_ringtone";
    public final static String INSISTENT_RINGTONE          = "pref_ringtone_insistent";
    public final static String RINGTONE_WORK               = "pref_ringtone";
    public final static String RINGTONE_BREAK              = "pref_ringtone_break";
    public final static String ENABLE_VIBRATE              = "pref_vibrate";
    public final static String ENABLE_FULLSCREEN           = "pref_fullscreen";
    public final static String DISABLE_SOUND_AND_VIBRATION = "pref_disable_sound_and_vibration";
    public final static String DISABLE_WIFI                = "pref_disable_wifi";
    public final static String ENABLE_SCREEN_ON            = "pref_keep_screen_on";
    public final static String ENABLE_CONTINUOUS_MODE      = "pref_continuous_mode";
    public final static String THEME                       = "pref_theme";

    public final static String DISABLE_BATTERY_OPTIMIZATION = "pref_disable_battery_optimization";

    public final static String WORK_STREAK                 = "pref_WORK_STREAK";
    public final static String LAST_WORK_FINISHED_AT       = "pref_last_work_finished_at";
    private static final String ADDED_60_SECONDS_STATE     = "pref_added_60_seconds_State";

    private static final String CURRENT_SESSION_LABEL      = "pref_current_session_label";
    private static final String INTRO_SNACKBAR_STEP        = "pref_intro_snackbar_step";

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

    public static int getSessionsBeforeLongBreak() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getInt(SESSIONS_BEFORE_LONG_BREAK, 4);
    }

    public static boolean isRingtoneEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_RINGTONE, true);
    }

    public static boolean isRingtoneInsistent() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(INSISTENT_RINGTONE, false);
    }

    public static String getNotificationSound() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString(RINGTONE_WORK, "");
    }

    public static String getNotificationSoundBreak() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString(RINGTONE_BREAK, "");
    }

    public static boolean isVibrationEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_VIBRATE, true);
    }

    public static boolean isFullscreenEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_FULLSCREEN, false);
    }

    public static boolean isSoundAndVibrationDisabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(DISABLE_SOUND_AND_VIBRATION, false);
    }

    public static boolean isWiFiDisabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(DISABLE_WIFI, false);
    }

    public static boolean isScreenOnEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_SCREEN_ON, false);
    }

    public static boolean isContinuousModeEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_CONTINUOUS_MODE, false);
    }

    public static int getTheme() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getInt(THEME, 0);
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
                + 10);

        final long currentMillis = SystemClock.elapsedRealtime();
        final boolean increment = lastWorkFinishedAt() == 0
                || currentMillis - lastWorkFinishedAt() < maxDifference;

        GoodtimeApplication.getSharedPreferences().edit()
                .putInt(WORK_STREAK, increment ? getCurrentStreak() + 1 : 1).apply();

        GoodtimeApplication.getSharedPreferences().edit()
                .putLong(LAST_WORK_FINISHED_AT, increment ? currentMillis: 0).apply();
    }

    public static int getCurrentStreak() {
        return GoodtimeApplication.getSharedPreferences().getInt(WORK_STREAK, 0);
    }

    public static long lastWorkFinishedAt() {
        return GoodtimeApplication.getSharedPreferences().getLong(LAST_WORK_FINISHED_AT, 0);
    }

    public static void resetCurrentStreak() {
        GoodtimeApplication.getSharedPreferences().edit()
                .putInt(WORK_STREAK, 0).apply();
        GoodtimeApplication.getSharedPreferences().edit()
                .putLong(LAST_WORK_FINISHED_AT, 0).apply();
    }

    public static boolean itsTimeForLongBreak() {
        return getCurrentStreak() >= getSessionsBeforeLongBreak();
    }

    /**
     * Persists the state of a session prolonged by the "Add 60 seconds" action.
     * It should be set to true when a session is started from an inactive state with the
     * "add 60 seconds" action.
     * It should be set to false when a session is stopped by a "stop" event and when a session is finished.
     * @param enable specifies the new state.
     */
    public static void toggleAdded60SecondsState(boolean enable) {
        GoodtimeApplication.getSharedPreferences().edit()
                .putBoolean(ADDED_60_SECONDS_STATE, enable).apply();
    }

    /**
     * This is used to identify when not to increase the current finished session streak.
     * @return the state of the "added 60 seconds" state.
     */
    public static boolean isInAdded60SecondsState() {
        return GoodtimeApplication.getSharedPreferences().getBoolean(ADDED_60_SECONDS_STATE, false);
    }

    public static String getCurrentSessionLabel() {
        return GoodtimeApplication.getSharedPreferences().getString(CURRENT_SESSION_LABEL, null);
    }

    public static void setCurrentSessionLabel(String label) {
        GoodtimeApplication.getSharedPreferences().edit()
                .putString(CURRENT_SESSION_LABEL, label).apply();
    }

    public static boolean isFirstRun() {
        return GoodtimeApplication.getSharedPreferences().getBoolean(FIRST_RUN, true);
    }

    public static void consumeFirstRun() {
        GoodtimeApplication.getSharedPreferences().edit()
                .putBoolean(FIRST_RUN, true).apply();
    }

    public static int getLastIntroStep() {
        return GoodtimeApplication.getSharedPreferences().getInt(INTRO_SNACKBAR_STEP, 0);
    }

    public static void setLastIntroStep(int step) {
        GoodtimeApplication.getSharedPreferences().edit()
                .putInt(INTRO_SNACKBAR_STEP, step).apply();
    }
}
