package com.apps.adrcotfas.goodtime.Util;

import java.util.concurrent.TimeUnit;

public class Constants {
    public interface ACTION {
        String TOGGLE_TIMER = "com.apps.adrcotfas.goodtimeplus.action.toggleTimer";
        String STOP_TIMER  = "com.apps.adrcotfas.goodtimeplus.action.stop";
    }

    public static long SESSION_TIME = TimeUnit.SECONDS.toMillis(10);

    public static int NOTIFICATION_ID = 42;

    public static class FinishEvent {}
}
