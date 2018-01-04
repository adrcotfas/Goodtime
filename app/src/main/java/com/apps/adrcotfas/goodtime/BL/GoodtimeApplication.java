package com.apps.adrcotfas.goodtime.BL;
import android.app.Application;

import com.apps.adrcotfas.goodtime.Util.Constants;

/**
 * Maintains a global state of the app and stores the event bus ({@link EventBus})
 * and the {@link CurrentSession}
 */
public class GoodtimeApplication extends Application{

    private static volatile GoodtimeApplication INSTANCE;
    private static CurrentSession mCurrentSession;
    private static EventBus mBus;

    public static GoodtimeApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        mCurrentSession = new CurrentSession(Constants.WORK_TIME);
        mBus = new EventBus();
    }

    public CurrentSession getCurrentSession() {
        return mCurrentSession;
    }

    public EventBus getBus() {
        return mBus;
    }
}