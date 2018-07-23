package com.apps.adrcotfas.goodtime.Util;

public class Constants {

    public static int DEFAULT_WORK_DURATION_POMODORO     = 25;
    public static int DEFAULT_BREAK_DURATION_POMODORO    = 5;
    public static int DEFAULT_LONG_BREAK_DURATION        = 15;
    public static int DEFAULT_SESSIONS_BEFORE_LONG_BREAK = 4;
    public static int DEFAULT_WORK_DURATION_5217         = 52;
    public static int DEFAULT_BREAK_DURATION_5217        = 17;

    public interface ACTION {
        String START       = "goodtime.action.start";
        String SKIP        = "goodtime.action.skip";
        String TOGGLE      = "goodtime.action.toggle";
        String STOP        = "goodtime.action.stop";
        String FINISHED    = "goodtime.action.finished";
        String ADD_SECONDS = "goodtime.action.addseconds";
    }

    public final static String SESSION_TYPE = "goodtime.session.type";

    public static class FinishWorkEvent {}
    public static class FinishBreakEvent {}
    public static class FinishLongBreakEvent {}
    public static class UpdateTimerProgressEvent {}
    public static class ClearNotificationEvent {}
    public static class ClearFinishDialogEvent {}
}
