package com.apps.adrcotfas.goodtimeplus.Model;
import android.app.Application;

import com.apps.adrcotfas.goodtimeplus.Util.Constants;

public class GoodtimeApplication extends Application{

    private static volatile GoodtimeApplication INSTANCE;
    private static CurrentSession mCurrentSession;

    public static GoodtimeApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        mCurrentSession = new CurrentSession(Constants.SESSION_TIME);
    }

    public void setDuration(long newDuration) {
        mCurrentSession.setDuration(newDuration);
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSession;
    }
}