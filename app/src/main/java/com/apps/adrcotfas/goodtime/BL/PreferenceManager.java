package com.apps.adrcotfas.goodtime.BL;

import android.preference.Preference;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

public class PreferenceManager implements Preference.OnPreferenceChangeListener{

    public static long getWorkDuration() {
        return Long.parseLong(getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString("WORK_DURATION", "25"));
    }

    public static long getBreakDuration() {
        return Long.parseLong(getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString("BREAK_DURATION", "5"));
    }

    public static boolean isLongBreakEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean("ENABLE_LONG_BREAK", true);
    }

    public static long getLongBreakDuration() {
        return Long.parseLong(getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString("LONG_BREAK_DURATION", "15"));
    }

    public static long getSessionsBeforeLongBreak() {
        return Long.parseLong(getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString("SESSIONS_BEFORE_LONG_BREAK", "4"));
    }

    public static boolean isRingtoneEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean("ENABLE_RINGTONE", true);
    }

    public static String getRingtoneWork() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString("RINGTONE_WORK", "");
    }

    public static String getRingtoneBreak() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString("RINGTONE_BREAK", "");
    }

    public static boolean isVibrationEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean("ENABLE_VIBRATE", true);
    }

    public static boolean isFullscreenEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean("ENABLE_FULLSCREEN", false);
    }

    public static boolean isSoundAndVibrationDisabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean("DISABLE_SOUND_AND_VIBRATION", false);
    }

    public static boolean isWiFiDisabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean("DISABLE_WIFI", false);
    }

    public static boolean isScreenOnEnabled() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getBoolean("ENABLE_SCREEN_ON", false);
    }

    public static String getTheme() {
        return getDefaultSharedPreferences(GoodtimeApplication.getInstance())
                .getString("THEME", "");
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }
}
