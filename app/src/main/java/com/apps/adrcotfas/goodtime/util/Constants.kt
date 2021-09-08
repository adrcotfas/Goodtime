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
package com.apps.adrcotfas.goodtime.util

object Constants {
    const val PROFILE_NAME_DEFAULT = "25/5"
    const val PROFILE_NAME_52_17 = "52/17"
    const val DEFAULT_WORK_DURATION_DEFAULT = 25
    const val DEFAULT_BREAK_DURATION_DEFAULT = 5
    const val DEFAULT_LONG_BREAK_DURATION = 15
    const val DEFAULT_SESSIONS_BEFORE_LONG_BREAK = 4
    const val DEFAULT_WORK_DURATION_5217 = 52
    const val DEFAULT_BREAK_DURATION_5217 = 17
    const val SESSION_TYPE = "goodtime.session.type"
    const val ONE_MINUTE_LEFT = "goodtime.one.minute.left"
    const val sku = "upgraded_version"


    interface ACTION {
        companion object {
            const val START = "goodtime.action.start"
            const val SKIP = "goodtime.action.skip"
            const val TOGGLE = "goodtime.action.toggle"
            const val STOP = "goodtime.action.stop"
            const val FINISHED = "goodtime.action.finished"
            const val ADD_SECONDS = "goodtime.action.addseconds"
        }
    }

    class FinishWorkEvent
    class FinishBreakEvent
    class FinishLongBreakEvent
    class UpdateTimerProgressEvent
    class ClearNotificationEvent
    class StartSessionEvent
    class OneMinuteLeft
}