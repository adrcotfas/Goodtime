package com.apps.adrcotfas.goodtimeplus.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.apps.adrcotfas.goodtimeplus.Model.CurrentSession;
import com.apps.adrcotfas.goodtimeplus.Model.GoodtimeApplication;

public class SessionViewModel extends AndroidViewModel{

    private CurrentSession mCurrentSession;

    public SessionViewModel(@NonNull Application application) {
        super(application);
        mCurrentSession = GoodtimeApplication.getInstance().getCurrentSession();
    }

    public CurrentSession getSession() {
        return mCurrentSession;
    }
}
