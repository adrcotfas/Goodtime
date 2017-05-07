package com.apps.adrcotfas.goodtime;

import android.content.SharedPreferences;
import android.util.Log;

public class Preferences {

    public static final String PREFERENCES_NAME = "com.apps.adrcotfas.goodtime.preferences";
    public static final String DISABLE_SOUND_AND_VIBRATION = "pref_disableSoundAndVibration";
    static final String FIRST_RUN = "pref_firstRun";
    static final String SESSION_DURATION = "pref_workTime";
    static final String TOTAL_SESSION_COUNT = "pref_totalSessions";
    static final String ENABLE_SESSIONS_COUNTER = "pref_counter";
    static final String FULLSCREEN_MODE = "pref_fullscreen";
    static final String SESSION_TYPE_ICON = "pref_sessionTypeIcon";
    private static final String TAG = "Preferences";
    private static final int CURRENT_SETTINGS_VERSION = 3;
    private static final String NOTIFICATION_SOUND = "pref_notificationSound";
    private static final String BREAK_DURATION = "pref_breakTime";
    private static final String LONG_BREAK_DURATION = "pref_longBreakDuration";
    private static final String SESSIONS_BEFORE_LONG_BREAK = "pref_sessionsBeforeLongBreak";
    private static final String SETTINGS_VERSION = "pref_settings_version";
    private static final String DISABLE_WIFI = "pref_disableWifi";
    private static final String KEEP_SCREEN_ON = "pref_keepScreenOn";
    private static final String CONTINUOUS_MODE = "pref_continuousMode";
    private static final String NOTIFICATION_VIBRATE = "pref_vibrate";
    private static final String LANDSCAPE_MODE = "pref_landscape";

    private final SharedPreferences mPref;

    Preferences(SharedPreferences pref) {
        mPref = pref;

        if (pref.getInt(SETTINGS_VERSION, 0) < CURRENT_SETTINGS_VERSION) {
            mPref.edit()
                 .putInt(SETTINGS_VERSION, CURRENT_SETTINGS_VERSION)
                 .putBoolean(LANDSCAPE_MODE, false)
                 .apply();
        }
    }

    int getSessionDuration() {
        return mPref.getInt(SESSION_DURATION, 25);
    }

    int getSessionsBeforeLongBreak() {
        return mPref.getInt(SESSIONS_BEFORE_LONG_BREAK, 4);
    }

    int getBreakDuration() {
        return mPref.getInt(BREAK_DURATION, 5);
    }

    int getLongBreakDuration() {
        return mPref.getInt(LONG_BREAK_DURATION, 15);
    }

    boolean getKeepScreenOn() {
        return mPref.getBoolean(KEEP_SCREEN_ON, false);
    }

    boolean getDisableSoundAndVibration() {
        return mPref.getBoolean(DISABLE_SOUND_AND_VIBRATION, false);
    }

    boolean getDisableWifi() {
        return mPref.getBoolean(DISABLE_WIFI, true);
    }

    boolean getContinuousMode() {
        return mPref.getBoolean(CONTINUOUS_MODE, false);
    }

    boolean getNotificationVibrate() {
        return mPref.getBoolean(NOTIFICATION_VIBRATE, true);
    }

    boolean getRotateTimeLabel() {
        return mPref.getBoolean(LANDSCAPE_MODE, false);
    }

    boolean getFullscreenMode() {
        return mPref.getBoolean(FULLSCREEN_MODE, false);
    }

    boolean getSessionTypeIcon() {
        return mPref.getBoolean(SESSION_TYPE_ICON, true);
    }

    boolean getEnableSessionCounter() {
        return mPref.getBoolean(ENABLE_SESSIONS_COUNTER, true);
    }

    String getNotificationSound() {
        return mPref.getString(NOTIFICATION_SOUND, "");
    }

    void disableSoundAndVibration() {
        mPref.edit().putBoolean(DISABLE_SOUND_AND_VIBRATION, false).apply();
    }

    void migrateFromOldPreferences(SharedPreferences oldPref) {
        if(oldPref.getInt(SESSION_DURATION, -1) == -1) {
            return;
        }

        Log.i(TAG, "Migrating settings");

        mPref.edit()
             .putInt(SESSION_DURATION, oldPref.getInt(SESSION_DURATION, 25))
             .putInt(SESSIONS_BEFORE_LONG_BREAK, oldPref.getInt(SESSIONS_BEFORE_LONG_BREAK, 4))
             .putInt(BREAK_DURATION, oldPref.getInt(BREAK_DURATION, 5))
             .putInt(LONG_BREAK_DURATION, oldPref.getInt(LONG_BREAK_DURATION, 15))
             .putBoolean(KEEP_SCREEN_ON, oldPref.getBoolean(KEEP_SCREEN_ON, false))
             .putBoolean(DISABLE_SOUND_AND_VIBRATION, oldPref.getBoolean(DISABLE_SOUND_AND_VIBRATION, false))
             .putBoolean(DISABLE_WIFI, oldPref.getBoolean(DISABLE_WIFI, true))
             .putBoolean(CONTINUOUS_MODE, oldPref.getBoolean(CONTINUOUS_MODE, false))
             .putBoolean(NOTIFICATION_VIBRATE, oldPref.getBoolean(NOTIFICATION_VIBRATE, true))
             .putBoolean(LANDSCAPE_MODE, oldPref.getBoolean(LANDSCAPE_MODE, false))
             .putBoolean(FULLSCREEN_MODE, oldPref.getBoolean(FULLSCREEN_MODE, false))
             .putBoolean(ENABLE_SESSIONS_COUNTER, oldPref.getBoolean(ENABLE_SESSIONS_COUNTER, true))
             .putString(NOTIFICATION_SOUND, oldPref.getString(NOTIFICATION_SOUND, ""))
             .apply();

        oldPref.edit().clear().apply();
    }
}
