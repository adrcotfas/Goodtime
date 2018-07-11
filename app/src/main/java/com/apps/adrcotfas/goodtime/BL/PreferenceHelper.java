package com.apps.adrcotfas.goodtime.BL;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

public class PreferenceHelper {

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
    public final static String PRO_VERSION                 = "pref_testing_pro_version";

    public static long getSessionDuration(SessionType sessionType) {
        if (sessionType == SessionType.WORK) {
            return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                    .getInt(WORK_DURATION, 25);
        } else {
            return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                    .getInt(BREAK_DURATION, 5);
        }
    }

    public static boolean isLongBreakEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(ENABLE_LONG_BREAK, true);
    }

    public static long getLongBreakDuration() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getInt(LONG_BREAK_DURATION, 15);
    }

    public static long getSessionsBeforeLongBreak() {
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

    public static boolean isProVersion() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean(PRO_VERSION, false);
    }
}
