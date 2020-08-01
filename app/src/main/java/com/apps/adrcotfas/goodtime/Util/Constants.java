/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.Util;

public class Constants {

    public static final String PROFILE_NAME_DEFAULT = "25/5";
    public static final String PROFILE_NAME_52_17 = "52/17";
    public static final int DEFAULT_WORK_DURATION_DEFAULT = 25;
    public static final int DEFAULT_BREAK_DURATION_DEFAULT = 5;
    public static final int DEFAULT_LONG_BREAK_DURATION        = 15;
    public static final int DEFAULT_SESSIONS_BEFORE_LONG_BREAK = 4;
    public static final int DEFAULT_WORK_DURATION_5217         = 52;
    public static final int DEFAULT_BREAK_DURATION_5217        = 17;

    public static final String sku = "upgraded_version";

    public interface ACTION {
        String START       = "goodtime.action.start";
        String SKIP        = "goodtime.action.skip";
        String TOGGLE      = "goodtime.action.toggle";
        String STOP        = "goodtime.action.stop";
        String FINISHED    = "goodtime.action.finished";
        String ADD_SECONDS = "goodtime.action.addseconds";
    }

    public final static String SESSION_TYPE = "goodtime.session.type";
    public final static String ONE_MINUTE_LEFT = "goodtime.one.minute.left";

    public static class FinishWorkEvent {}
    public static class FinishBreakEvent {}
    public static class FinishLongBreakEvent {}
    public static class UpdateTimerProgressEvent {}
    public static class ClearNotificationEvent {}
    public static class StartSessionEvent {}
    public static class OneMinuteLeft {}
}
