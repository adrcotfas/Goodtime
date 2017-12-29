package com.apps.adrcotfas.goodtimeplus.BL;
import android.app.Application;

import com.apps.adrcotfas.goodtimeplus.Util.Constants;

public class GoodtimeApplication extends Application{

    private static volatile GoodtimeApplication INSTANCE;
    private static CurrentSession mCurrentSession;
    private static RxBus mBus;

    public static GoodtimeApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        mCurrentSession = new CurrentSession(Constants.SESSION_TIME);
        mBus = new RxBus();
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSession;
    }

    public RxBus getBus() {
        return mBus;
    }
}