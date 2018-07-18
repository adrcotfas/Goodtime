package com.apps.adrcotfas.goodtime.BL;
import android.app.Application;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.concurrent.TimeUnit;
import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.*;

/**
 * Maintains a global state of the app and the {@link CurrentSession}
 */
public class GoodtimeApplication extends Application {

    private static volatile GoodtimeApplication INSTANCE;
    private static CurrentSessionManager mCurrentSessionManager;
    public static SharedPreferences mPreferences;

    public static GoodtimeApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        mCurrentSessionManager = new CurrentSessionManager(this, new CurrentSession(TimeUnit.MINUTES.toMillis(
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getInt(WORK_DURATION, 25))));

        mPreferences = getSharedPreferences(getPackageName() + "_private_preferences", MODE_PRIVATE);
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSessionManager.getCurrentSession();
    }

    public CurrentSessionManager getCurrentSessionManager() {
        return mCurrentSessionManager;
    }

    public static SharedPreferences getSharedPreferences() {
        return mPreferences;
    }
}