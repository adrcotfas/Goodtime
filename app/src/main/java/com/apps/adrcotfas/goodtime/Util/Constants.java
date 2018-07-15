package com.apps.adrcotfas.goodtime.Util;

public class Constants {

    public static int DEFAULT_WORK_DURATION_POMODORO     = 25;
    public static int DEFAULT_BREAK_DURATION_POMODORO    = 5;
    public static int DEFAULT_LONG_BREAK_DURATION        = 15;
    public static int DEFAULT_SESSIONS_BEFORE_LONG_BREAK = 4;
    public static int DEFAULT_WORK_DURATION_5217         = 52;
    public static int DEFAULT_BREAK_DURATION_5217        = 17;

    public interface ACTION {
        String START_WORK  = "goodtime.action.startWork";
        String START_BREAK = "goodtime.action.startBreak";
        String SKIP_BREAK  = "goodtime.action.skipBreak";
        String SKIP_WORK   = "goodtime.action.skipWork";
        String TOGGLE      = "goodtime.action.toggle";
        String STOP        = "goodtime.action.stop";
        String FINISHED    = "goodtime.action.finished";
    }

    public static class FinishWorkEvent {}
    public static class FinishBreakEvent {}
    public static class UpdateTimerProgressEvent {}
    public static class ClearNotificationEvent {}
    public static class ClearFinishDialogEvent {}
}
