package com.apps.adrcotfas.goodtime.Util;

import android.content.Context;
import android.content.Intent;

public class IntentWithAction extends Intent {

    public IntentWithAction(Context context, Class<?> cls, String action) {
        super(context, cls);
        this.setAction(action);
    }
}
