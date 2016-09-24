package com.apps.adrcotfas.goodtime;

import android.content.SharedPreferences;
import com.apps.adrcotfas.goodtime.settings.CustomNotification;

public class Preferences {
    private final SharedPreferences mPref;

    public Preferences(SharedPreferences pref) {
        mPref = pref;
    }

    public static final String FIRST_RUN = "pref_firstRun";
    public static final String SESSION_DURATION = "pref_workTime";
    public static final String BREAK_DURATION = "pref_breakTime";
    public static final String LONG_BREAK_DURATION = "pref_longBreakDuration";
    public static final String SESSIONS_BEFORE_LONG_BREAK = "pref_sessionsBeforeLongBreak";
    public static final String DISABLE_SOUND_AND_VIBRATION = "pref_disableSoundAndVibration";
    public static final String DISABLE_WIFI = "pref_disableWifi";
    public static final String KEEP_SCREEN_ON = "pref_keepScreenOn";
    public static final String CONTINUOUS_MODE = "pref_continuousMode";
    public static final String NOTIFICATION_VIBRATE = "pref_vibrate";
    public static final String NOTIFICATION_SOUND = "pref_notificationSound";
    public static final String TOTAL_SESSION_COUNT = "pref_totalSessions";

    public boolean getRingtonesCopied() {
        return mPref.getBoolean(CustomNotification.PREF_KEY_RINGTONES_COPIED, false);
    }

    public int getSessionDuration() {
        return mPref.getInt(SESSION_DURATION, 25);
    }

    public int getSessionsBeforeLongBreak() {
        return mPref.getInt(SESSIONS_BEFORE_LONG_BREAK, 4);
    }

    public int getBreakDuration() {
        return mPref.getInt(BREAK_DURATION, 5);
    }

    public int getLongBreakDuration() {
        return mPref.getInt(LONG_BREAK_DURATION, 15);
    }

    public boolean getKeepScreenOn() {
        return mPref.getBoolean(KEEP_SCREEN_ON, false);
    }

    public boolean getDisableSoundAndVibration() {
        return mPref.getBoolean(DISABLE_SOUND_AND_VIBRATION, false);
    }

    public boolean getDisableWifi() {
        return mPref.getBoolean(DISABLE_WIFI, false);
    }

    public boolean getContinuousMode() {
        return mPref.getBoolean(CONTINUOUS_MODE, false);
    }

    public boolean getNotificationVibrate() {
        return mPref.getBoolean(NOTIFICATION_VIBRATE, true);
    }

    public String getNotificationSound() {
        return mPref.getString(NOTIFICATION_SOUND, "");
    }
}
