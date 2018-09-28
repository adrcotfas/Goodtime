package com.apps.adrcotfas.goodtime.BL;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.apps.adrcotfas.goodtime.Database.AppDatabase;
import com.apps.adrcotfas.goodtime.LabelAndColor;
import com.apps.adrcotfas.goodtime.R;

import java.util.concurrent.TimeUnit;

import androidx.preference.PreferenceManager;

import static com.apps.adrcotfas.goodtime.BL.PreferenceHelper.WORK_DURATION;

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

        //TODO: move this somewhere else?
        AsyncTask.execute(() -> AppDatabase.getDatabase(getApplicationContext()).labelAndColor()
                .addLabel(new LabelAndColor("", getResources().getColor(R.color.classicAccent))));
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSessionManager.getCurrentSession();
    }

    public static CurrentSessionManager getCurrentSessionManager() {
        return mCurrentSessionManager;
    }

    public static SharedPreferences getSharedPreferences() {
        return mPreferences;
    }
}