package com.apps.adrcotfas.goodtimeplus.Util;

import java.util.concurrent.TimeUnit;

public class Constants {
    public interface ACTION {
        String TOGGLE_TIMER = "com.apps.adrcotfas.goodtimeplus.action.toggleTimer";
        String STOP_TIMER  = "com.apps.adrcotfas.goodtimeplus.action.stop";
    }

    public static long SESSION_TIME = TimeUnit.MINUTES.toMillis(25);

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 42;
    }
}
