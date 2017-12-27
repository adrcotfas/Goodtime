package com.apps.adrcotfas.goodtimeplus.Util;

import java.util.concurrent.TimeUnit;

public class Constants {
    public interface ACTION {
        String START_TIMER = "com.apps.adrcotfas.goodtimeplus.action.startTimer";
        String STOP_TIMER  = "com.apps.adrcotfas.goodtimeplus.action.stopTimer";
        String TOGGLE_TIMER = "com.apps.adrcotfas.goodtimeplus.action.toggleTimer";
    }

    public static long SESSION_TIME = TimeUnit.MINUTES.toMillis(25);

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 42;
    }
}
