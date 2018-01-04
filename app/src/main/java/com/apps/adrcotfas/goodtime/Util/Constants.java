package com.apps.adrcotfas.goodtime.Util;

import com.apps.adrcotfas.goodtime.BL.SessionType;

import java.util.concurrent.TimeUnit;

public class Constants {

    public interface ACTION {
        String START_WORK  = "goodtime.action.startWork";
        String START_BREAK = "goodtime.action.startBreak";
        String SKIP_BREAK  = "goodtime.action.skipBreak";
        String SKIP_WORK   = "goodtime.action.skipWork";
        String TOGGLE      = "goodtime.action.toggle";
        String STOP        = "goodtime.action.stop";
    }

    public static long WORK_TIME = TimeUnit.SECONDS.toMillis(10);
    public static final long BREAK_TIME = TimeUnit.SECONDS.toMillis(5);

    public static int NOTIFICATION_ID = 42;

    public static class FinishWorkEvent {}
    public static class FinishBreakEvent {}
    public static class UpdateTimerProgressEvent {}
    public static class ClearNotificationEvent {}
    public static class ClearFinishDialogEvent {}
}
