package com.apps.adrcotfas.goodtime.Util;

import android.content.Context;
import android.content.Intent;

import com.apps.adrcotfas.goodtime.BL.SessionType;

import static com.apps.adrcotfas.goodtime.Util.Constants.SESSION_TYPE;

public class IntentWithAction extends Intent {

    public IntentWithAction(Context context, Class<?> cls, String action) {
        super(context, cls);
        this.setAction(action);
    }

    public IntentWithAction(Context context, Class<?> cls, String action, SessionType sessionType) {
        super(context, cls);
        this.setAction(action);
        this.putExtra(SESSION_TYPE, sessionType.toString());
    }
}
